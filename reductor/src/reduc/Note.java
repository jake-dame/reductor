package reduc;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.ShortMessage;
import java.util.ArrayList;

import static javax.sound.midi.ShortMessage.NOTE_OFF;
import static javax.sound.midi.ShortMessage.NOTE_ON;
import static reduc.ReductorUtil.MESSAGE_TYPE_NOTE_OFF;
import static reduc.ReductorUtil.MESSAGE_TYPE_NOTE_ON;

/*
    Purpose: Represent, but not manipulate, Midi NOTE events.
*/
public class Note implements Comparable<Note> {

    long startTick, endTick;
    int pitch;

    Note(long start, long end, int pitch) {
        this.pitch = pitch;
        this.startTick = start;
        this.endTick = end;
    }

    // Copy constructor
    Note(Note note) {
        this.startTick = note.startTick;
        this.endTick = note.endTick;
        this.pitch = note.pitch;
    }

    // this is mostly for testing
    Note(Note note, int pitch) {
        this.startTick = note.startTick;
        this.endTick = note.endTick;
        this.pitch = pitch;
    }

    // this is mostly for testing
    Note() {
        this.pitch = -1;
        this.startTick = -1;
        this.endTick = -1;
    }


    static ArrayList<Note> eventsToNotes(ArrayList<MidiEvent> midiEvents) {

        checkMidiEventsIsValid(midiEvents);
        // TODO: sort events explicitly to not rely on java midi library.
        //  Could create two lists ONs and OFFs and pluck out stuff like that (could also remove and
        //  check both lists are empty at the end, but probably unnecessary and would be low performance)

        ArrayList<Note> newList = new ArrayList<>();
        int numEvents = midiEvents.size();

        /* NOTE ON loop */
        for (int i = 0; i < numEvents; i++) {

            MidiEvent event = midiEvents.get(i);
            ShortMessage msg = (ShortMessage) event.getMessage();

            // Skip NOTE OFFs for outer loop
            if (msg.getCommand() != NOTE_ON)  continue;

            int pitch = msg.getData1();
            long startTick = event.getTick();
            long endTick = -1;

            // If penultimate note, construct/add last Note and return
            if (i == numEvents - 1) {
                endTick = midiEvents.getLast().getTick();
                newList.add( new Note(startTick, endTick, pitch) );
                return newList;
            }

            /* NOTE OFF loop */
            for (int j = i + 1; j < numEvents; j++) {

                MidiEvent nextEvent = midiEvents.get(j);
                ShortMessage nextMsg = (ShortMessage) nextEvent.getMessage();

                // Skip NOTE ONs for inner loop
                if (nextMsg.getCommand() != NOTE_OFF)  continue;

                int nextPitch = nextMsg.getData1();
                if (nextPitch == pitch) {
                    endTick = nextEvent.getTick();
                    break;
                }

            } // end inner loop

            if (endTick == -1) {
                // Reached end of sequence and endTick is still -1
                throw new RuntimeException("missing note off");
            } else {
                newList.add( new Note(startTick, endTick, pitch) );
            }

        } // end outer loop

        if ( newList.size() != numEvents / 2 ) {
            // A missing note off would have been caught above
            // If we are here, it's because 1+ note offs are remaining (i.e. missing a note on)
            throw new RuntimeException("missing note on");
        }

        return newList;
    }


    private static void checkMidiEventsIsValid(ArrayList<MidiEvent> midiEvents) {

        if (midiEvents == null) {
            throw new NullPointerException("note events is null");
        }

        if (midiEvents.isEmpty()) {
            throw new IllegalArgumentException("note events is empty");
        }

        if (midiEvents.size() % 2 != 0 ) {
            throw new IllegalStateException("note events length is odd");
        }

        if (midiEvents.getFirst().getMessage().getStatus() != NOTE_ON
                || midiEvents.getLast().getMessage().getStatus() != NOTE_OFF) {
            throw new IllegalStateException("sequence begins with note off or ends with note on");
        }

    }

    static ArrayList<MidiEvent> notesToEvents(ArrayList<Note> notes) {

        ArrayList<MidiEvent> list = new ArrayList<>();
        try {

            for (Note note : notes) {

                MidiEvent noteOnEvent = new MidiEvent(
                        new ShortMessage(MESSAGE_TYPE_NOTE_ON, note.pitch, 64),
                        note.startTick
                );

                list.add(noteOnEvent);

                MidiEvent noteOffEvent = new MidiEvent(
                        new ShortMessage(MESSAGE_TYPE_NOTE_OFF, note.pitch, 0),
                        note.endTick
                );

                list.add(noteOffEvent);

            }
        } catch (InvalidMidiDataException e) {
            throw new RuntimeException(e);
        }

        return list;
    }

    @Override
    public int compareTo(Note other) {
        return Integer.compare(this.pitch, other.pitch);
    }

    @Override
    public String toString() {
        return String.format("[%d,%d: %s]", startTick, endTick, ReductorUtil.getNote(pitch) );
    }

}