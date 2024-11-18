package reductor;

import javax.sound.midi.*;
import java.util.*;


public class Conversion {

    /* ============
    * FROM Midi
    * ===========*/

    public static ArrayList<Note> toNotes(ArrayList<NoteOnEvent> noteOnEvents) {
        ArrayList<Note> out = new ArrayList<>();
        noteOnEvents.forEach( on -> out.add( toNote(on) ) );
        return out;
    }

    public static Note toNote(NoteOnEvent on) {
        Range range = new Range(on.getTick(), on.getPartner().getTick());
        return new Note(on.getPitch(), range, on.getTrackName());
    }

    static ArrayList<TimeSignature> toTimeSigs(ArrayList<TimeSignatureEvent> timeSignatureEvents) throws RuntimeException {

        if (timeSignatureEvents.isEmpty()) {
            throw new RuntimeException("this program only supports MIDI files with time signature data");
        }

        // ensure these are in ascending order by tick, otherwise the range creation loop won't work
        timeSignatureEvents.sort(Comparator.comparingLong(TimeSignatureEvent::getTick));

        ArrayList<TimeSignature> out = new ArrayList<>();

        int size = timeSignatureEvents.size();
        for (int i = 0; i < size; i++) {

            var event = timeSignatureEvents.get(i);

            long tick = event.getTick();
            long nextTick;

            if (i == size - 1) {
                nextTick = Context.lastTick() + 1;
            } else {
                nextTick = timeSignatureEvents.get(i+1).getTick();
            }

            Range range = new Range(tick, nextTick - 1);

            TimeSignature timeSig = new TimeSignature(
                    Context.resolution(),
                    event.getUpperNumeral(),
                    event.getLowerNumeral(),
                    range
            );

            out.add(timeSig);
        }

        return out;

    }

    /* ============
     * TO Midi
     * ===========*/

    public  static <T extends Noted> ArrayList<MidiEvent> toMidiEvents(List<T> notedElems) throws InvalidMidiDataException {

        final ArrayList<MidiEvent> out = new ArrayList<>();
        final int MEDIAN_VELOCITY = 64;

        for (T elem : notedElems) {
            ArrayList<Note> notes = elem.getNotes();

            for (Note note : notes) {
                ShortMessage onMessage = new ShortMessage(ShortMessage.NOTE_ON, note.pitch(), MEDIAN_VELOCITY);
                MidiEvent noteOnEvent = new MidiEvent(onMessage, note.start());
                out.add(noteOnEvent);

                ShortMessage offMessage = new ShortMessage(ShortMessage.NOTE_OFF, note.pitch(), 0);
                MidiEvent noteOffEvent = new MidiEvent(offMessage, note.stop());
                out.add(noteOffEvent);
            }
        }

        return out;
    }

    public static Sequence toSequence(ArrayList<MidiEvent> midiEvents, int resolution) throws InvalidMidiDataException {

        Sequence out = new Sequence(Sequence.PPQ, resolution);
        Track track = out.createTrack();

        for (MidiEvent event : midiEvents) {
            //track.add( DeepCopy.copyEvent(event) ); // todo this was issue can't remember
            track.add(event);
        }

        return out;
    }

    //public static Sequence toSequencePlusAddbacks(List<Column> columns, ArrayList<MidiEvent> addBacks) throws InvalidMidiDataException {
    //    ArrayList<MidiEvent> midiEvents = new ArrayList<>(addBacks);
    //    for (Column column : columns) {
    //        midiEvents.addAll( column.getNotes().toMidiEvents() );
    //    }
    //    return toSequence(midiEvents);
    //}



    /**
     * Returns a {@link reductor.KeySignature} given a valid MIDI key signature event:
     *
     * @param event A {@link javax.sound.midi.MidiEvent}
     *
     * @return A {@link reductor.KeySignature} corresponding to the MIDI data.
     */
    public static KeySignature toKeySignature(MidiEvent event) {

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
            return new KeySignature(accidentalCount, mode);
        }

        throw new IllegalArgumentException("bytes representing modes must be 0 (major) or 1 (minor)");
    }


}
