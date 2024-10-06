package reduc;

import java.util.ArrayList;
import java.util.Collections;

import reduc.IntervalTree.Interval;

public class Chord {

    ArrayList<Note> notes;

    final int high;
    final int low;

    ArrayList<Interval> intervals;

    Chord(ArrayList<Note> notes) {
        if (notes == null) {
            throw new NullPointerException("notes is null");
        }
        Collections.sort(notes);
        this.notes = notes;
        this.low = notes.getFirst().pitch;
        this.high = notes.getLast().pitch;
        this.intervals = null;
    }

    Chord() {
        this.notes = null;
        this.high = -1;
        this.low = -1;
        this.intervals = null;
    }

    static void getIntervals() {

    }

}
