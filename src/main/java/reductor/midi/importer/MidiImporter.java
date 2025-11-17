package reductor.midi.importer;

import reductor.app.Application;
import reductor.core.KeySignature;
import reductor.core.Tempo;
import reductor.core.TimeSignature;
import reductor.core.Ranged;
import reductor.core.builders.NoteBuilder;
import reductor.core.builders.PieceBuilder;
import reductor.midi.MidiUtil;
import reductor.midi.parser.MidiContainer;
import reductor.midi.parser.events.Event;
import reductor.midi.validator.EventType;
import reductor.midi.parser.events.NoteOffEvent;
import reductor.midi.parser.events.NoteOnEvent;
import reductor.core.*;

import javax.sound.midi.*;
import java.util.*;


public class MidiImporter {


    private MidiImporter(){}

    public static Piece toPiece(MidiContainer mc) {

        int len = Math.toIntExact(mc.getSequenceLengthInTicks());

        List<Note> notes = toNotes(mc.getNoteOnEvents(), mc.getNoteOffEvents());
        List<TimeSignature> timeSigs = assignRanges(mc.getTimeSignatureEvents(),
                len, TimeSignature.class);
        List<KeySignature> keySigs = assignRanges(mc.getKeySignatureEvents(),
                len, KeySignature.class);
        List<Tempo> tempos = assignRanges(mc.getSetTempoEvents(),
                len, Tempo.class);

        return PieceBuilder.builder(mc.getResolution())
                .note(notes)
                .timeSignature(timeSigs)
                .keySignature(keySigs)
                .tempo(tempos)
                .build();
    }

    // See `docs/note-pairing-and-midi.md`
    public static List<Note> toNotes(List<NoteOnEvent> noteOnEvents, List<NoteOffEvent> noteOffEvents) {

        // Don't want to alter the original lists (by sorting them)
        // LinkedLists work fine for us here (need easy removals, but don't care about random access)
        List<NoteOnEvent> noteOnEventsCopy = new ArrayList<>(noteOnEvents);
        noteOnEventsCopy.sort(Comparator.comparingLong(NoteOnEvent::getTick));
        LinkedList<NoteOnEvent> ons = new LinkedList<>(noteOnEvents);

        List<NoteOffEvent> noteOffEventsCopy = new ArrayList<>(noteOffEvents);
        noteOffEventsCopy.sort(Comparator.comparingLong(NoteOffEvent::getTick));
        LinkedList<NoteOffEvent> offs = new LinkedList<>(noteOffEvents);

        List<Note> outNotes = new ArrayList<>();
        NoteOnEvent on;
        NoteOffEvent off;
        Iterator<NoteOnEvent> onsIterator = ons.iterator();
        while(onsIterator.hasNext()) {

            on = onsIterator.next();

            Iterator<NoteOffEvent> offsIterator = offs.iterator();
            while(offsIterator.hasNext()) {

                off = offsIterator.next();

                // Criterion 1: matching pitches
                if (on.getPitch() == off.getPitch()) {

                    // Criterion 2: Don't create a range of [0,0]. Quantization
                    //     should have created only exclusive ranges.
                    //     Inclusive ones are a big problem for this program.
                    if (off.getTick() != on.getTick()) {

                        int onInt = Math.toIntExact(on.getTick());
                        int offInt = Math.toIntExact(off.getTick());

                        // Found a match/pair --> construct the `Note` object
                        Note note = NoteBuilder.builder()
                                .pitch(new Pitch(on.getPitch()))
                                .start(onInt)
                                .stop(offInt)
                                .instrument(on.getTrackName())
                                .build();

                        outNotes.add(note);
                    }

                    // Overlapping pairs (on @ 0, off @ 0) removed here as well
                    onsIterator.remove();
                    offsIterator.remove();
                    break;
                }

            } // end offs while()

        } // end ons while()

        // For testing / debugging purposes
        List<NoteOnEvent> unpairedOns = new ArrayList<>(ons);
        List<NoteOffEvent> unpairedOffs = new ArrayList<>(offs);
        // end

        if (!ons.isEmpty()) {
            throw new RuntimeException(
                    "unpaired note on", new Throwable(unpairedOns.toString())
            );
        }

        if (!offs.isEmpty()) {
            throw new RuntimeException(
                    "unpaired note off", new Throwable(unpairedOffs.toString())
            );
        }

        return outNotes;
    }

    /**
     * Returns a {@link KeySignature} given a valid MIDI key signature event:
     *
     * @param event A {@link javax.sound.midi.MidiEvent}
     *
     * @return A {@link KeySignature} corresponding to the MIDI data.
     */
    public static KeySignature toKeySignature(MidiEvent event, Range range) {

        byte[] data;

        if (event.getMessage() instanceof MetaMessage mm
                &&  mm.getType() == EventType.KEY_SIGNATURE.code()) {
            data = mm.getData();
        } else {
            throw new RuntimeException(
                    "toKeySignature was given an event that is not a key signature event"
            );
        }

        int accidentalCount = data[0];
        int mode = data[1];

        if (accidentalCount < -7 || accidentalCount > 7) {
            throw new IllegalArgumentException(
                    "bytes representing accidental counts must have a value of between -7 and 7"
            );
        }

        if (mode == 0  ||  mode == 1) {
            return new KeySignature(accidentalCount, mode, range);
        } else {
            throw new IllegalArgumentException(
                    "bytes representing modes must have a value of either 0 (major) or 1 (minor)"
            );
        }

    }

    /**
     * Returns a {@link Tempo} given a valid MIDI set tempo event:
     *
     * @param event A {@link javax.sound.midi.MidiEvent}
     *
     * @return A {@link Tempo} corresponding to the MIDI data.
     */
    public static Tempo toTempo(MidiEvent event, Range range) {

        byte[] data;
        if (event.getMessage() instanceof MetaMessage mm) {
            if (mm.getType() == EventType.SET_TEMPO.code()) {
                data = mm.getData();
            } else {
                throw new RuntimeException("toTempo was given an event that is not a set tempo event");
            }
        } else {
            throw new RuntimeException("toTempo was given an event that is not a set tempo event");
        }

        return new Tempo(MidiUtil.convertMicrosecondsToBPM(data), range);
    }

    public static TimeSignature toTimeSignature(MidiEvent event, Range range) {

        byte[] data;
        if (event.getMessage() instanceof MetaMessage mm
                &&  mm.getType() == EventType.TIME_SIGNATURE.code()) {
            data = mm.getData();
        } else {
            throw new RuntimeException("toTimeSignature was given an event that is not time signature event");
        }

        int upperNumeral = data[0] & 0xFF;
        int lowerNumeralExponent = data[1] & 0xFF;
        int lowerNumeral = (int) Math.pow(2, lowerNumeralExponent);

        int clockTicksPerTick = data[2] & 0xFF; // don't delete
        int thirtySecondNotesPerBeat = data[3] & 0xFF; // don't delete

        return new TimeSignature(
                upperNumeral,
                lowerNumeral,
                range
        );

    }

    public static <E extends Event<?>, C extends Ranged> List<C> assignRanges(
            List<E> midiEvents,
            int sequenceLengthInTicks,
            Class<C> classToConvertTo
    ) {

        if (midiEvents.isEmpty()) { return new ArrayList<>(); }
        List<E> eventsCopy = new ArrayList<>(midiEvents);
        eventsCopy.sort(Comparator.comparingLong(Event::getTick));

        List<C> out = new ArrayList<>();

        int size = eventsCopy.size();
        for (int i = 0; i < size; i++) {

            Event<?> event = eventsCopy.get(i);

            int tick = Math.toIntExact(event.getTick());
            int nextTick;

            nextTick = Math.toIntExact(i == size - 1
                    ? sequenceLengthInTicks + 1
                    : eventsCopy.get(i + 1).getTick());

            // This checks for the "same" midi meta event being sent at the same time, just on different tracks
            if (tick == nextTick) { continue; }

            // Meta events don't get "off" messages, it's just whenever the next event occurs.
            // So really this "should" be tick, nextTick, like [0,480], but that range.high() value won't even be
            // used in the reverse conversion process - the only thing that matters is the internal representation
            // and what Piece needs, which is that standard range.high() is nextTick - 1.
            Range range = new Range(tick, nextTick - 1);

            C instance = switch (classToConvertTo.getSimpleName()) {
                case "TimeSignature" -> classToConvertTo.cast(toTimeSignature(event.getMidiEvent(), range));
                case "KeySignature" -> classToConvertTo.cast(toKeySignature(event.getMidiEvent(), range));
                case "Tempo" -> classToConvertTo.cast(toTempo(event.getMidiEvent(), range));
                default -> throw new RuntimeException(
                        "invalid class for using assignRanges function: " + classToConvertTo
                );
            };

            out.add(instance);
        }

        return out;
    }


}
