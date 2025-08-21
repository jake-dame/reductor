package reductor.dataconverter.midi;

import reductor.midi.Event;
import reductor.midi.EventType;
import reductor.midi.NoteOffEvent;
import reductor.midi.NoteOnEvent;
import reductor.piece.*;

import javax.sound.midi.*;
import java.util.*;


/**
 * This utility class provides the necessary conversion methods to/from MIDI data to reductor in-house classes.
 * In the future, this could become an interface, with different implementing classes handling different kinds of
 * data (e.g. MusicXML).
 */
public class ConversionFromMidi {


    //public static Sequence toSequence(Piece piece) throws InvalidMidiDataException {
    //    ArrayList<MidiEvent> metaEvents = toMetaEvents(piece);
    //    ArrayList<MidiEvent> noteEvents = toNoteEvents(piece.getColumns(), 1);
    //    return toSequence(Piece.TPQ, metaEvents, noteEvents);
    //}
    //
    //@SafeVarargs
    //public static Sequence toSequence(int resolution, List<MidiEvent>... midiEventsLists) throws InvalidMidiDataException {
    //
    //    Sequence out = new Sequence(Sequence.PPQ, resolution);
    //    Track track = out.createTrack();
    //
    //    for (List<MidiEvent> list : midiEventsLists) {
    //        for (MidiEvent event : list) {
    //            track.add(event);
    //        }
    //    }
    //
    //    return out;
    //}
    //
    //public static ArrayList<MidiEvent> toMetaEvents(Piece piece) throws InvalidMidiDataException {
    //
    //    ArrayList<MidiEvent> addbacks = new ArrayList<>();
    //
    //    ArrayList<MidiEvent> timeSigEvents = new ArrayList<>();
    //    ArrayList<MidiEvent> keySigEvents = new ArrayList<>();
    //    ArrayList<MidiEvent> tempoEvents = new ArrayList<>();
    //
    //    for (TimeSignature timeSig : piece.getTimeSignatures()) {
    //        timeSigEvents.add( ConversionFromMidi.toTimeSignatureEvent(timeSig) );
    //    }
    //
    //    for (KeySignature keySig : piece.getKeySignatures()) {
    //        keySigEvents.add( ConversionFromMidi.toKeySignatureEvent(keySig) );
    //    }
    //
    //    for (Tempo tempo : piece.getTempos()) {
    //        tempoEvents.add( ConversionFromMidi.toTempoEvent(tempo) );
    //    }
    //
    //    addbacks.addAll(timeSigEvents);
    //    addbacks.addAll(keySigEvents);
    //    addbacks.addAll(tempoEvents);
    //
    //    return addbacks;
    //}


    /* ====
       NOTE
     * ==== */


    public static ArrayList<Note> toNotes(ArrayList<NoteOnEvent> noteOnEvents, ArrayList<NoteOffEvent> noteOffEvents) throws UnpairedNoteException {

        // Defensive stuff
        ArrayList<NoteOnEvent> noteOnEventsCopy = new ArrayList<>(noteOnEvents);
        ArrayList<NoteOffEvent> noteOffEventsCopy = new ArrayList<>(noteOffEvents);

        noteOnEventsCopy.sort(Comparator.comparingLong(NoteOnEvent::getTick));
        noteOffEventsCopy.sort(Comparator.comparingLong(NoteOffEvent::getTick));

        // Would like easy removals, and don't really need random access during the algorithm
        LinkedList<NoteOnEvent> ons = new LinkedList<>(noteOnEvents);
        LinkedList<NoteOffEvent> offs = new LinkedList<>(noteOffEvents);

        ArrayList<Note> outNotes = new ArrayList<>();

        /*
         This is just a matching/pairing algorithm, with a few MIDI-specific edge cases to look for:
             1. Stuck notes: note on events that are not paired by the end of the list of offs

                 1.1 Semi-stuck notes: notes that are stuck for a time:
                         A quarter note C @ 0 --> never turned off
                         A quarter note C @ 480 --> never turned off
                         A quarter note C @ 960 --> never turned off
                         A quarter note C @ 1440 --> turned off @ 1919

                     Each quarter is not turned off except the last, and the last off event turns ALL of them off.
                     Although each is "effectively" turned off because MIDI does not allow multiple note events of
                     the same pitch to occur simultaneously (see the note about Case 3 below), the implication here
                     is that the on event never received an off event means two things:
                         1.1.1 This algorithm would treat the first three as stuck notes AND
                         1.1.2 Their constructed Ranges would not be [0, 479], but [0, 480] without special care!


             2. Redundant offs: extraneous offs sent for ons that have already been shut off
             3. Extra ons: when two notes with the same pitch are turned on at the same tick:
                     On channel 1 -- A whole note C @ 0 --> _should_ be turned off @ 1919
                     On channel 1 -- A quarter note C @ 0 --> _should_ turned off @ 479
                 "Every" C, even though there is really only ever 1, will be turned off at the first off pitch. All
                 subsequent offs become case 2!
                 In this algorithm, since I want to prevent a Range of [0,0] from being created, both the whole note's on and
                 off will be unpaired (both at tick 0), _as well as_ the whole notes extraneous off at 1919.
                 This occurs when multiple-features in notation software allow this sort of thing (like in a piano
                 score), OR the reverse (see the next paragraph).

             Case 3 is interesting because MIDI spec does not handle or allow two on events corresponding to the same
             pitch to happen. This won't matter if they are on different channels, of course, but when combining to
             the same channel, or track (as in the case of reduction -- a violin and trumpet both starting C's at the
             same time -- extra care needs to be taken. This is a job for the reverse conversion algorithm below.
         */

        NoteOnEvent on;
        NoteOffEvent off;
        Iterator<NoteOnEvent> onsIterator = ons.iterator();
        //Iterator<NoteOffEvent> offsIterator = offs.iterator();

        while(onsIterator.hasNext()) {
            on = onsIterator.next();

            Iterator<NoteOffEvent> offsIterator = offs.iterator();
            while(offsIterator.hasNext()) {
                off = offsIterator.next();

                if (on.getPitch() == off.getPitch()) {

                    if (off.getTick() != on.getTick()) {
                        Note note = Note.builder()
                                .pitch(on.getPitch())
                                .start(on.getTick())
                                .stop(off.getTick())
                                .instrument(on.getTrackName())
                                .build();

                        outNotes.add(note);
                    }

                    // Overlapping pairs (on @ 0, off @ 0) removed here as well
                    onsIterator.remove();
                    offsIterator.remove();
                    break;
                }
            }

        }

        // For testing / debugging purposes
        ArrayList<NoteOnEvent> unpairedOns = new ArrayList<>(ons);
        ArrayList<NoteOffEvent> unpairedOffs = new ArrayList<>(offs);

        if (!ons.isEmpty()) {
            throw new UnpairedNoteException("unpaired note on", new Throwable(unpairedOns.toString()));
        }

        if (!offs.isEmpty()) {
            throw new UnpairedNoteException("unpaired note off", new Throwable(unpairedOffs.toString()));
        }

        return outNotes;
    }


    /* =============
     * KEY SIGNATURE
     * ===========*/


    /**
     * Returns a {@link KeySignature} given a valid MIDI key signature event:
     *
     * @param event A {@link javax.sound.midi.MidiEvent}
     *
     * @return A {@link KeySignature} corresponding to the MIDI data.
     */
    public static KeySignature toKeySignature(MidiEvent event, Range range) {

        byte[] data;
        if (event.getMessage() instanceof MetaMessage mm) {
            if (mm.getType() == EventType.KEY_SIGNATURE.getTypeCode()) {
                data = mm.getData();
            } else {
                throw new RuntimeException("toKeySignature was given an event that is not a key signature event");
            }
        } else {
            throw new RuntimeException("toKeySignature was given an event that is not a key signature event");
        }

        int accidentalCount = data[0];
        int mode = data[1];

        if (accidentalCount < -7 || accidentalCount > 7) {
            throw new IllegalArgumentException("bytes representing accidental counts must be between -7 and 7");
        }

        if (mode == 0  ||  mode == 1) {
            return new KeySignature(accidentalCount, mode, range);
        }

        throw new IllegalArgumentException("bytes representing modes must be 0 (major) or 1 (minor)");
    }


    /* =====
     * TEMPO
     * ===*/


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
            if (mm.getType() == EventType.SET_TEMPO.getTypeCode()) {
                data = mm.getData();
            } else {
                throw new RuntimeException("toTempo was given an event that is not a set tempo event");
            }
        } else {
            throw new RuntimeException("toTempo was given an event that is not a set tempo event");
        }

        return new Tempo( convertMicrosecondsToBPM(data), range );
    }

    /**
     * Given a value in microseconds (per quarter note), converts to beats-per-minute (bpm),
     * which is what humans use to specify tempo. Easily retrieved with getData() on set tempo
     * messages from (Java) MetaMessage class.
     *
     * @param data The tempo as a number split into three LTR bytes
     * @return The same tempo in beats-per-minute
     */
    public static int convertMicrosecondsToBPM(byte[] data) {

        int byteIndex = 0;
        long microsecondsPerQuarterNote = 0;

        while (byteIndex < data.length) {
            microsecondsPerQuarterNote <<= 8;
            microsecondsPerQuarterNote |= (data[byteIndex] & 0xFF);
            byteIndex++;
        }

        final int microsecondsPerMinute = 60_000_000;

        // This cast is fine because none of the numbers here, if valid MIDI spec,
        //     will never get remotely near INTEGER_MAX.
        return microsecondsPerMinute / (int) microsecondsPerQuarterNote;
    }


    /* ==============
     * TIME SIGNATURE
     * ============*/


    public static TimeSignature toTimeSignature(MidiEvent event, Range range) {

        byte[] data;
        if (event.getMessage() instanceof MetaMessage mm) {
            if (mm.getType() == EventType.TIME_SIGNATURE.getTypeCode()) {
                data = mm.getData();
            } else {
                throw new RuntimeException("toTempo was given an event that is not a set tempo event");
            }
        } else {
            throw new RuntimeException("toTempo was given an event that is not a set tempo event");
        }

        int upperNumeral = data[0] & 0xFF;
        int lowerNumeralExponent = data[1] & 0xFF;
        int lowerNumeral = (int) Math.pow(2, lowerNumeralExponent);

        int clockTicksPerTick = data[2] & 0xFF; // don't delete
        int thirtySecondsPerBeat = data[3] & 0xFF; // don't delete

        return new TimeSignature(
                upperNumeral,
                lowerNumeral,
                range
        );

    }


    /* ====
     * MISC
     * ==*/


    //public static ArrayList<TimeSignature> hi(ArrayList<TimeSignatureEvent> timeSignatureEvents) {
    //
    //    if (timeSignatureEvents == null  ||  timeSignatureEvents.isEmpty()) { return new ArrayList<>(); }
    //    ArrayList<TimeSignatureEvent> events = new ArrayList<>(timeSignatureEvents);
    //
    //    Set<Long> set = new HashSet<>();
    //    for (TimeSignatureEvent event : events) { set.add(event.getTick()); }
    //
    //    ArrayList<Range> ranges = Range.fromStartTicks(set, Context.finalTick());
    //
    //    ArrayList<TimeSignature> out = new ArrayList<>();
    //
    //    for (int i = 0; i < ranges.size(); i++) {
    //        out.add(toTimeSignature(events.get(i).getMidiEvent(), ranges.get(i)));
    //    }
    //
    //    return out;
    //}

    public static <E extends Event<?>, C extends Ranged> ArrayList<C>
    assignRanges(List<E> midiEvents, long sequenceLengthInTicks, Class<C> classToConvertTo) {

        if (midiEvents.isEmpty()) { return new ArrayList<>(); }
        ArrayList<E> eventsCopy = new ArrayList<>(midiEvents);
        eventsCopy.sort(Comparator.comparingLong(Event::getTick));

        ArrayList<C> out = new ArrayList<>();

        int size = eventsCopy.size();
        for (int i = 0; i < size; i++) {

            Event<?> event = eventsCopy.get(i);

            long tick = event.getTick();
            long nextTick;

            nextTick = i == size - 1
                    ? sequenceLengthInTicks + 1
                    : eventsCopy.get(i+1).getTick();

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
                default -> throw new RuntimeException("invalid class for using assignRanges function: " + classToConvertTo);
            };

            out.add(instance);
        }

        return out;
    }

    /*
     Any of the "text-based" events (Text, Lyrics, etc.) will follow the exact same pattern as toTrackNameEvent()
     */
}