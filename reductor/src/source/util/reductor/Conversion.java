package reductor;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.ShortMessage;
import java.util.ArrayList;
import java.util.Comparator;

public class Conversion {

    private Conversion() { }

    static ArrayList<Note> eventsToNotes(ArrayList<MidiEvent> midiEvents) {

        checkAndSortList(midiEvents);

        final var outList = new ArrayList<Note>();

        final int length = midiEvents.size();

        boolean seenNoteOn = false;

        // NOTE ON loop (outer loop)
        for (int i = 0; i < length; i++) {

            MidiEvent event = midiEvents.get(i);
            assert event.getMessage() instanceof ShortMessage;

            ShortMessage msg = (ShortMessage) event.getMessage();

            // These two checks ensure we don't end up with an unpaired note off that somehow
            // slipped into the beginning of the list
            if (msg.getCommand() == Constants.NOTE_OFF) {
                if (!seenNoteOn) {
                    throw new RuntimeException("a note off occurred in the list before a note on");
                }
            }

            if (msg.getCommand() == Constants.NOTE_ON) {
                seenNoteOn = true;
            }

            // This skips non-NOTE_ON messages for the outer loop
            if (msg.getCommand() != Constants.NOTE_ON) {
                continue;
            }

            int pitch = msg.getData1();
            assert pitch >= 0 && pitch <= 127;

            long startTick = event.getTick();
            assert startTick >= 0;

            long endTick = -1;

            // If penultimate note, construct/add last Note and return
            if (i == length - 1) {
                endTick = midiEvents.getLast().getTick();

                if (((ShortMessage) midiEvents.getLast().getMessage()).getData1() != pitch) {
                    throw new IllegalArgumentException("last note off was a mismatch");
                }

                outList.add( new Note( pitch, new Range(startTick, endTick) ) );
                return outList;
            }

            // NOTE OFF loop
            for (int j = i + 1; j < length; j++) {

                MidiEvent nextEvent = midiEvents.get(j);
                assert nextEvent.getMessage() instanceof ShortMessage;

                ShortMessage nextMsg = (ShortMessage) nextEvent.getMessage();

                if (nextMsg.getCommand() != Constants.NOTE_OFF) {
                    // Skip over NOTE ON or other events
                    continue;
                }

                int nextPitch = nextMsg.getData1();

                if (nextPitch == pitch
                        // This handles tied notes with same pitch
                        &&  nextEvent.getTick() != startTick) {
                    endTick = nextEvent.getTick();
                    assert endTick > startTick;
                    break;
                }

            } // end inner loop

            if (endTick == -1) {
                throw new RuntimeException("reached the end of the list of midievents and did not find a match for the note on event");
            }

            Note noteObj = new Note( pitch, new Range(startTick, endTick) );
            outList.add(noteObj);

        } // end outer loop

        return outList;
    }

    private static void checkAndSortList(ArrayList<MidiEvent> midiEvents) {

        if (midiEvents == null) {
            throw new NullPointerException("list of midi events to convert to Notes is null");
        }

        if (midiEvents.size() < 2) {
            throw new IllegalArgumentException("list of midi events to convert to Notes cannot be size 0 or 1");
        }

        //if (midiEvents.size() % 2 != 0 ) {
        //    throw new IllegalStateException("unpaired events are present in the list of midi events to convert to Notes ");
        //}

        Comparator<MidiEvent> comparator = new Comparator<>() {
            @Override
            public int compare(MidiEvent event1, MidiEvent event2) {
                return Long.compare(event1.getTick(), event2.getTick());
            }
        };

        midiEvents.sort(comparator);

    }


    static ArrayList<MidiEvent> notesToMidiEvents(ArrayList<Note> notes) {

        final ArrayList<MidiEvent> outList = new ArrayList<>();

        final int medianVelocity = 64;

        try {

            for (Note note : notes) {

                ShortMessage onMessage = new ShortMessage(Constants.NOTE_ON, note.pitch(), medianVelocity);
                MidiEvent noteOnEvent = new MidiEvent(onMessage, note.start());
                outList.add(noteOnEvent);

                ShortMessage offMessage = new ShortMessage(Constants.NOTE_OFF, note.pitch(), 0);
                MidiEvent noteOffEvent = new MidiEvent(offMessage, note.stop());
                outList.add(noteOffEvent);

            }

        } catch (InvalidMidiDataException e) {
            throw new RuntimeException(e);
        }

        return outList;
    }


}
