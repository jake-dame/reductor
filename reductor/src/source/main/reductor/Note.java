package reductor;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.ShortMessage;
import java.util.ArrayList;
import java.util.Comparator;

import static javax.sound.midi.ShortMessage.NOTE_OFF;
import static reductor.Pitch.numericalPitchToString;
import static reductor.Pitch.stringPitchToNumber;

/**
 * Can represent a pitch, or a pair of MIDI events (ON + OFF) as a single entity.
 */
public class Note implements Comparable<Note> {

    final long begin, end;
    final int pitch;

    /// Primary constructor
    Note(long begin, long end, int pitch) {
        this.pitch = pitch;
        this.begin = begin;
        this.end = end;
    }

    //region <Other constructors>

    /// Copy constructor
    Note(Note note) {
        this.begin = note.begin;
        this.end = note.end;
        this.pitch = note.pitch;
    }

    Note (Interval interval) {
        this(interval.note);
    }

    /**
     * Constructor which only assigns pitch. The {@code start} and {@code begin}
     * fields are meaningless when using this constructor. See {@link reductor.Pitch#stringPitchToNumber}.
     *
     * @param str A string such as "A", "Ab", "A#", "Ax", or "Abb"
     * @param register A register (octave) in [-1, 9]
     */
    Note(String str, int register) {
        this.pitch = stringPitchToNumber(str, register);
        this.begin = -1;
        this.end = -1;
    }

    /// (used for testing)
    Note(Note note, int pitch) {
        this.begin = note.begin;
        this.end = note.end;
        this.pitch = pitch;
    }

    /// Default constructor (used for testing)
    Note() {
        this.pitch = -1;
        this.begin = -1;
        this.end = -1;
    }



    //endregion


    static ArrayList<Note> midiEventsToNotes(ArrayList<MidiEvent> midiEvents) {

        checkMidiEventsIsValid(midiEvents);

        final var outList = new ArrayList<Note>();

        final int numEvents = midiEvents.size();

        // NOTE ON loop (outer loop)
        for (int i = 0; i < numEvents; i++) {

            MidiEvent event = midiEvents.get(i);
            assert event.getMessage() instanceof ShortMessage;

            ShortMessage msg = (ShortMessage) event.getMessage();

            if (msg.getCommand() != MessageType.NOTE_ON) {
                // Skip over NOTE OFF or other events
                continue;
            }

            int pitch = msg.getData1();
            assert pitch >= 0 && pitch <= 127;

            long startTick = event.getTick();
            assert startTick >= 0;

            long endTick = -1;

            // If penultimate note, construct/add last Note and return
            if (i == numEvents - 1) {
                endTick = midiEvents.getLast().getTick();

                if (((ShortMessage) midiEvents.getLast().getMessage()).getData1() != pitch) {
                    throw new IllegalArgumentException("last note off was a mismatch");
                }

                outList.add( new Note(startTick, endTick, pitch) );
                return outList;
            }

            /* NOTE OFF loop */
            for (int j = i + 1; j < numEvents; j++) {

                MidiEvent nextEvent = midiEvents.get(j);
                assert event.getMessage() instanceof ShortMessage;

                ShortMessage nextMsg = (ShortMessage) nextEvent.getMessage();

                if (nextMsg.getCommand() != NOTE_OFF) {
                    // Skip over NOTE ON or other events
                    continue;
                }

                int nextPitch = nextMsg.getData1();
                if (nextPitch == pitch) {
                    endTick = nextEvent.getTick();
                    assert endTick > startTick;
                    break;
                }

            } // end inner loop

            if (endTick == -1) {
                // Reached end of sequence and endTick is still -1
                throw new RuntimeException("missing note off");
            } else {
                outList.add( new Note(startTick, endTick, pitch) );
            }

        } // end outer loop

        if ( outList.size() != numEvents / 2 ) {
            // A missing note off would have been caught above
            // If we are here, it's because 1+ note offs are remaining (i.e. missing a note on)
            throw new RuntimeException("missing note on");
        }

        return outList;
    }

    /**
     * Does a bunch of preliminary checks and sorts a list of {@link javax.sound.midi.MidiEvent}s
     * in preparation for conversion to {@code Note}s.
     *
     * @param midiEvents
     */
    private static void checkMidiEventsIsValid(ArrayList<MidiEvent> midiEvents) {

        if (midiEvents == null) {
            throw new NullPointerException("list of midi events is null");
        }

        if (midiEvents.isEmpty()) {
            throw new IllegalArgumentException("list of midi events is empty");
        }

        if (midiEvents.size() % 2 != 0 ) {
            throw new IllegalStateException(
                    "unpaired events are present in the list of midi events"
            );
        }

        Comparator<MidiEvent> comparator = new Comparator<>() {
            @Override
            public int compare(MidiEvent event1, MidiEvent event2) {
                return Long.compare(event1.getTick(), event2.getTick());
            }
        };

        midiEvents.sort(comparator);

        int firstMessageType = midiEvents.getFirst().getMessage().getStatus();
        int lastMessageType = midiEvents.getLast().getMessage().getStatus();

        if (firstMessageType != MessageType.NOTE_ON
                || lastMessageType != MessageType.NOTE_OFF) {
            throw new IllegalStateException(
                    "sequence of midi events begins with a NOTE OFF or ends with a NOTE ON"
            );
        }

    }


    static ArrayList<MidiEvent> notesToMidiEvents(ArrayList<Note> notes) {

        final var outList = new ArrayList<MidiEvent>();
        final int mezzoForte = 64;

        try {

            for (Note note : notes) {

                assert note.begin < note.end  &&  note.begin >= 0  &&  note.end <= 1;
                assert note.pitch >= 0  &&  note.pitch <= 127;

                ShortMessage onMessage = new ShortMessage(MessageType.NOTE_ON, note.pitch, mezzoForte);
                MidiEvent noteOnEvent = new MidiEvent(onMessage, note.begin);
                outList.add(noteOnEvent);

                ShortMessage offMessage = new ShortMessage(MessageType.NOTE_OFF, note.pitch, 0);
                MidiEvent noteOffEvent = new MidiEvent(offMessage, note.end);
                outList.add(noteOffEvent);

            }

        } catch (InvalidMidiDataException e) {
            throw new RuntimeException(e);
        }

        return outList;
    }

    /**
     * Compares {@code Note} objects by pitch only.
     */
    @Override
    public int compareTo(Note other) {
        return Integer.compare(this.pitch, other.pitch);
    }

    /**
     * If the {@code Note} is displayed as a pitch only, it indicates that its tick values are meaningless.
     */
    @Override
    public String toString() {

        if(this.begin == -1  ||  this.end == -1) {
            return numericalPitchToString(pitch, false);
        } else {
            return String.format("%s [%d,%d]", numericalPitchToString(pitch, false), begin, end);
        }

    }

}