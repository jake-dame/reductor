package reductor;

import javax.sound.midi.ShortMessage;
import java.util.ArrayList;
import java.util.Collections;

import static reductor.NoteEvent.assignPartners;


public class Conversion {


    private Conversion() { }


    static ArrayList<Note> pairAndCreateNotes(ArrayList<NoteEvent> noteEvents) {

        assert noteEvents != null;
        assert noteEvents.size() > 1;
        assert noteEvents.getFirst() instanceof NoteOnEvent;
        assert noteEvents.size() % 2 == 0;

        Collections.sort(noteEvents);

        ArrayList<Note> outList = new ArrayList<>();

        final int length = noteEvents.size();

        // NOTE ON loop (outer loop)
        for (int i = 0; i < length; i++) {

            NoteEvent event = noteEvents.get(i);

            if (event instanceof NoteOffEvent) {
                // Skip note off events
                continue;
            }

            NoteOnEvent onEvent = (NoteOnEvent) event;

            ShortMessage message = onEvent.getMessage();

            // If penultimate note, construct/add last Note and return
            if (i == length - 1) {

                NoteOffEvent offEvent = (NoteOffEvent) noteEvents.getLast();

                assignPartners(onEvent, offEvent);

                Range range = new Range(onEvent.tick(), offEvent.tick());
                Note note =  new Note(onEvent.pitch(), range);
                outList.add(note);
                return outList;

            }

            // NOTE OFF loop
            boolean matchFound = false;
            for (int j = i + 1; j < length; j++) {

                matchFound = false;

                NoteEvent nextEvent = noteEvents.get(j);

                if (nextEvent instanceof NoteOnEvent) {
                    // Skip note on events
                    continue;
                }

                NoteOffEvent offEvent = (NoteOffEvent) nextEvent;

                if (onEvent.pitch() == offEvent.pitch()
                        // This clause handles tied notes with same pitch
                        &&  onEvent.tick() != offEvent.tick()) {

                    matchFound = true;

                    assignPartners(onEvent, offEvent);

                    Range range = new Range(onEvent.tick(), offEvent.tick());
                    Note noteObj = new Note(onEvent.pitch(), range);

                    outList.add(noteObj);
                    break;

                }

            } // end inner loop

            if (!matchFound) {
                throw new RuntimeException("reached the end of the list of note events and did not find a matching note off event for the note on event");
            }

        } // end outer loop

        return outList;
    }


}
