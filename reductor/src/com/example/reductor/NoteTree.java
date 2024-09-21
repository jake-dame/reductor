package com.example.reductor;

import javax.sound.midi.MidiEvent;
import javax.sound.midi.ShortMessage;
import java.util.ArrayList;

// Interval tree with notes and their time intervals
public class NoteTree {

    // This is not a node but a stepping stone for now just to get intervals
    private class Note {

        // The unique pitch (tone + register)
        int pitch_m;
        // The midi tick this note starts at
        long start_m;
        // The midi tick this note ends at
        long end_m;

        Note(int pitch, long start, long end) {
            pitch_m = pitch;
            start_m = start;
            end_m = end;
        }

        @Override
        public String toString() {
            return "[" + pitch_m + ", " + start_m + ", " + end_m + "]";
        }

    }

    // Might be useful at some point to have an ArrayList<Chord> for more complex arpeggiation, etc.
    // Granularity (list length) need not be in microseconds; perhaps max tick / smallest duration (note value)

    // Chords would just have an ArrayList<Note> of notes on at a given time
    //private class Chord {
    //
    //    ArrayList<Node> notesOn_m;
    //    long tick;
    //
    //    Chord(ArrayList<Node> notes) {
    //
    //    }
    //}

    final private ArrayList<Note> notes_m;

    NoteTree(ArrayList<MidiEvent> noteEvents) {
        notes_m = new ArrayList<>();
        createNotesList(noteEvents);
    }

    // Populates member list of `Note` objects with their respective duration/tick intervals
    private void createNotesList(ArrayList<MidiEvent> events) {

        // Shouldn't be a problem but jic
        int firstMsgStatus = events.getFirst().getMessage().getStatus();
        int lastMsgStatus = events.getLast().getMessage().getStatus();

        if (firstMsgStatus != 0x90 || lastMsgStatus != 0x80) {
            throw new RuntimeException("sequence begins with note off or ends with note on");
        }

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

            // If penultimate event, construct/add last `Note` and return
            if (i == events.size() - 1) {
                endTick = events.getLast().getTick();
                notes_m.add( new Note(pitch, startTick, endTick) );
                return;
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
                    //events.remove(nextEvent); // would also need to call .size() each iteration
                    break;
                }
            }

            if (endTick == -1) {
                throw new RuntimeException("reached end of sequence without finding matching NOTE OFF for: "
                        + pitch + " @ " + startTick);
            }

            // Construct `Note` and add to list
            Note note = new Note(pitch, startTick, endTick);
            notes_m.add(note);
        }

    }

    public void printNoteObjects() {
        for (Note note : notes_m) {
            System.out.println(note);
        }
    }

}