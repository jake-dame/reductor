package reduc;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.ShortMessage;
import java.util.ArrayList;

import static javax.sound.midi.ShortMessage.NOTE_OFF;
import static javax.sound.midi.ShortMessage.NOTE_ON;

/*
    Represents a comparable MidiEvent and has some static conversion functions too (from Java MidiEvent)
*/
class Event {

    long startTick, endTick;
    int pitch;

    Event(long start, long end, int pitch) {
        this.pitch = pitch;
        this.startTick = start;
        this.endTick = end;
    }

    // this is mostly for testing
    Event(Event event, int pitch) {
        this.startTick = event.startTick;
        this.endTick = event.endTick;
        this.pitch = pitch;
    }

    // Copy constructor
    Event(Event event) {
        this.startTick = event.startTick;
        this.endTick = event.endTick;
        this.pitch = event.pitch;
    }

    static ArrayList<Event> midiEventsToEvents(ArrayList<MidiEvent> midiEvents) {

        if (midiEvents.isEmpty())
            throw new IllegalArgumentException("note events is empty");

        if (midiEvents.size() % 2 != 0 )
            throw new IllegalArgumentException("note events length is odd");

        if (midiEvents.getFirst().getMessage().getStatus() != NOTE_ON
                || midiEvents.getLast().getMessage().getStatus() != NOTE_OFF)
            throw new IllegalArgumentException("sequence begins with note off or ends with note on");

        ArrayList<Event> newList = new ArrayList<>();
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

            // If penultimate event, construct/add last Event and return
            if (i == numEvents - 1) {
                endTick = midiEvents.getLast().getTick();
                newList.add( new Event(startTick, endTick, pitch) );
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

            // Construct Event and add to list
            newList.add( new Event(startTick, endTick, pitch) );
        }

        if ( newList.size() != numEvents/2 )
            throw new RuntimeException("note list / event list size mismatch");

        return newList;
    }

    static ArrayList<MidiEvent> eventsToMidiEvents(ArrayList<Event> events)
            throws InvalidMidiDataException {

        ArrayList<MidiEvent> list = new ArrayList<>();

        for (Event event : events) {

            MidiEvent noteOnEvent = new MidiEvent(
                    new ShortMessage(NOTE_ON, event.pitch, 64),
                    event.startTick);

            list.add(noteOnEvent);

            MidiEvent noteOffEvent = new MidiEvent(
                    new ShortMessage(NOTE_OFF, event.pitch, 0),
                    event.endTick);

            list.add(noteOffEvent);
        }

        return list;
    }

    //@Override
    //public int compareTo(Event e) {
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
        return "[" + MidiUtility.getNote(pitch) + ", " + startTick + ", " + endTick + "]";
    }

}