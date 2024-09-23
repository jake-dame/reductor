package reduc;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.ShortMessage;
import java.util.ArrayList;

/*
    Represents a MidiEvent and has some static conversion functions too (from Java MidiEvent)
*/
public class Event implements Comparable<Event> {

    int pitch;
    // ticks
    long start, end;

    Event(int pitch, long start, long end) {
        this.pitch = pitch;
        this.start = start;
        this.end = end;
    }

    public static ArrayList<Event> midiEventsToEvents(ArrayList<MidiEvent> events) {

        if (events.isEmpty()) {
            throw new IllegalArgumentException("note events is empty");
        }

        if (events.size() % 2 != 0 ) {
            throw new IllegalArgumentException("note events length is odd");
        }

        int firstMsgStatus = events.getFirst().getMessage().getStatus();
        int lastMsgStatus = events.getLast().getMessage().getStatus();
        if (firstMsgStatus != 0x90 || lastMsgStatus != 0x80) {
            throw new RuntimeException("sequence begins with note off or ends with note on");
        }

        ArrayList<Event> notes = new ArrayList<>();

        int size = events.size();

        // NOTE ON loop
        for (int i = 0; i < size; i++) {

            MidiEvent event = events.get(i);

            // Skip NOTE OFFs for outer loop
            if (event.getMessage().getStatus() != 0x90) {
                continue;
            }

            int pitch = ((ShortMessage) event.getMessage()).getData1();
            long startTick = event.getTick();
            long endTick = -1;

            // If penultimate event, construct/add last `Event` and return
            if (i == size - 1) {
                endTick = events.getLast().getTick();
                notes.add( new Event(pitch, startTick, endTick) );
                return notes;
            }

            // "NOTE OFF" loop: Start from current index, search until matching NOTE OFF event is found
            for (int j = i + 1; j < size; j++) {

                MidiEvent nextEvent = events.get(j);

                // If this is not a NOTE OFF event, ignore it
                if (nextEvent.getMessage().getStatus() != 0x80) {
                    continue;
                }

                int nextPitch = ((ShortMessage) nextEvent.getMessage()).getData1();

                if (nextPitch == pitch) {
                    endTick = nextEvent.getTick();
                    // would alter passed object
                    // would need to call .size() everywhere
                    //events.remove(nextEvent);
                    break;
                }
            }

            if (endTick == -1) {
                throw new RuntimeException("reached end of sequence without finding matching NOTE OFF for: "
                        + pitch + " @ " + startTick);
            }

            // Construct `Event` and add to list
            Event note = new Event(pitch, startTick, endTick);
            notes.add(note);
        }

        if (notes.size() != (size / 2)) {
            System.out.println("note list / event list size mismatch");
        }

        return notes;
    }

    public static ArrayList<MidiEvent> eventsToMidiEvents(ArrayList<Event> events)
            throws InvalidMidiDataException {

        ArrayList<MidiEvent> list = new ArrayList<>();

        for (Event event : events) {

            MidiEvent noteOnEvent = new MidiEvent(
                    new ShortMessage(ShortMessage.NOTE_ON, event.pitch, 64),
                    event.start);

            list.add(noteOnEvent);

            MidiEvent noteOffEvent = new MidiEvent(
                    new ShortMessage(ShortMessage.NOTE_ON, event.pitch, 0),
                    event.end);

            list.add(noteOffEvent);
        }

        return list;
    }

    @Override
    public int compareTo(Event o) {
        // TODO asdf
        return 0;
    }

    @Override
    public String toString() {
        return "[" + MidiUtility.getNote(pitch) + ", " + start + ", " + end + "]";
    }

}