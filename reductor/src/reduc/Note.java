package reduc;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.ShortMessage;
import java.util.ArrayList;

import static javax.sound.midi.ShortMessage.NOTE_OFF;
import static javax.sound.midi.ShortMessage.NOTE_ON;

/*
    Purpose: Represent, but not manipulate, Midi NOTE events.
*/
public class Note {

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

        if (midiEvents == null || midiEvents.isEmpty())
            throw new IllegalArgumentException("note events is null or empty");

        if (midiEvents.size() % 2 != 0 )
            throw new IllegalArgumentException("note events length is odd");

        if (midiEvents.getFirst().getMessage().getStatus() != NOTE_ON
                || midiEvents.getLast().getMessage().getStatus() != NOTE_OFF)
            throw new IllegalArgumentException("sequence begins with note off or ends with note on");

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

                int nextPitch = ((ShortMessage) nextEvent.getMessage()).getData1();

                if (nextPitch == pitch) {
                    endTick = nextEvent.getTick();
                    break;
                }
            }

            if (endTick == -1)
                throw new RuntimeException("no match, reached end of sequence");

            // Construct Note and add to list
            newList.add( new Note(startTick, endTick, pitch) );
        }

        if ( newList.size() != numEvents/2 )
            throw new RuntimeException("note list / note list size mismatch");

        return newList;
    }

    static ArrayList<MidiEvent> notesToEvents(ArrayList<Note> notes) {

        ArrayList<MidiEvent> list = new ArrayList<>();
        try {
            for (Note note : notes) {

                MidiEvent noteOnEvent = new MidiEvent(
                        new ShortMessage(NOTE_ON, note.pitch, 64),
                        note.startTick);

                list.add(noteOnEvent);

                MidiEvent noteOffEvent = new MidiEvent(
                        new ShortMessage(NOTE_OFF, note.pitch, 0),
                        note.endTick);

                list.add(noteOffEvent);
            }
        } catch (InvalidMidiDataException e) {
            throw new RuntimeException(e);
        }

        return list;
    }

    //@Override
    //public int compareTo(Note e) {
    //
    //    if (this.a != e.a) {
    //        return Long.compare(this.a, e.a);
    //    } else if (this.b != e.b) {
    //        return Long.compare(this.b, e.b);
    //    } else {
    //        return Integer.compare(this.pitch, e.pitch);
    //    }
    //
    //}

    @Override
    public String toString() {
        return String.format("[%d,%d: %s]", startTick, endTick, pitch);
    }

}