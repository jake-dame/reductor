package reduc;

import java.util.ArrayList;
import java.util.Collections;

import reduc.IntervalTree.Interval;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.Sequence;
import javax.sound.midi.Track;

public class Chord {

    ArrayList<Note> notes;

    final int high;
    final int low;

    final long tick;

    ArrayList<Interval> intervals;

    Chord(ArrayList<Note> notes) {
        if (notes == null) {
            throw new NullPointerException("notes is null");
        }
        Collections.sort(notes);
        this.notes = notes;
        this.low = notes.getFirst().pitch;
        this.high = notes.getLast().pitch;
        this.tick = notes.get(0).startTick; // experimental
        this.intervals = null;
    }

    Chord() {
        this.notes = null;
        this.high = -1;
        this.low = -1;
        this.tick = -1;
        this.intervals = null;
    }

    void add(Note note) {
        this.notes.add(note);
    }

    static void getIntervals() {

    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();

        builder.append(this.tick);
        builder.append(": [");

        for (Note note : notes) {
            builder.append(ReductorUtil.getPitchAndRegister(note.pitch));
            builder.append(", ");
        }

        builder.delete(builder.lastIndexOf(", "), builder.length());

        builder.append("]");

        return builder.toString();

    }

    ArrayList<MidiEvent> toMidiEvents() {
        return Note.notesToEvents(this.notes);
    }

}
