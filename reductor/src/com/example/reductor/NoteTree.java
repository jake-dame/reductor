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

    // A list of notes with [pitch, start, end];
    ArrayList<Note> notes_m;

    // Minimum tick (tick of first note)
    long min;

    // Maximum tick (length of sequence in ticks)
    long max;

    private class Node {
        Node left_m, right_m;
        Note note;
    }

    private Node root_m;

    NoteTree(ArrayList<MidiEvent> noteEvents) {

        constructNotesList(noteEvents);

    }

    private void constructNotesList(ArrayList<MidiEvent> events) {

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

        notes_m = new ArrayList<>();
        createNotesList(events);

        min = Long.MAX_VALUE;
        max = -1;
        getMinMax();
    }

    private void getMinMax() {

        for (Note note : notes_m) {
            if (note.start_m < min ) {
                min = note.start_m;
            }
            if (note.end_m > max ) {
                max = note.end_m;
            }
        }

        if (max == -1 || min == Long.MAX_VALUE) {
            throw new IllegalArgumentException("invalid min/max tick");
        }
    }

    // Populates member list of `Note` objects with their respective duration/tick intervals
    private void createNotesList(ArrayList<MidiEvent> events) {

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
            if (i == size - 1) {
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

        if (notes_m.size() != (size / 2)) {
            System.out.println("Note tree size mismatch");
        }
    }

    public void printNoteObjects() {
        for (Note note : notes_m) {
            System.out.println(note);
        }
        System.out.println("size: " + notes_m.size());
        System.out.println("min: " + min + " max: " + max);
    }

}