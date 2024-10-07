package reductor;

import static reductor.Pitch.numericalPitchToString;

public class Interval implements Comparable<Interval> {

    long begin;
    long end;
    Note note;

    Interval(Note note) {
        this.begin = note.begin;
        this.end = note.end;
        this.note = note;
    }

    Interval() {
        this.begin = -1;
        this.end = -1;
        this.note = null;
    }

    Interval(long low, long high) {

        if (low >= high) {
            throw new IllegalArgumentException("invalid interval");
        }

        this.begin = low;
        this.end = high;
        this.note = null;
    }

    boolean overlaps(Interval other) {
        return !(other.begin > this.end || other.end < this.begin);
    }

    // Compare first by low endpoint, then by high endpoint
    @Override public int compareTo(Interval other) {
        if(this.begin == other.begin) {
            return Long.compare(this.end, other.end);
        } else {
            return Long.compare(this.begin, other.begin);
        }
    }

    @Override public String toString() {
        return String.format("[%d,%d: %s]", this.begin, this.end, numericalPitchToString(this.note.pitch, true));
    }
}
