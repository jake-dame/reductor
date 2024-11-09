package reductor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;


/**
 * This is a collection for {@code Note} objects, but where the main comparisons and operations are not
 * concerned with <i>when</i> the notes occur, but with the "vertical" arrangements of those notes (i.e. pairs of musical
 * intervals that comprise chords). The number of {@code Note} elements in this will be pretty small (presumably less than 15),
 * so the "collection" operations for this class are kept pretty straightforward.
 */
public class Chord implements Ranged, Comparable<Chord> {

    /// The notes that comprise this {@code Chord} in ascending order by pitch
    Notes notes;
    /// The {@code Note} object representing the lowest pitch in this {@code Chord}
    Note low;
    /// The {@code Note} object representing the highest pitch in this {@code Chord}
    Note high;
    /// The window of time this chord's notes spans
    Range range;
    /// This is the actual window that the chord was created using
    Range chunk;

    /// Given a list of notes, constructs a {@code Chord} object
    Chord(ArrayList<Note> notes, Range chunk) {
        assert notes != null;
        this.chunk = chunk;
        this.range = findRange(notes);
        notes.sort(Comparator.comparingInt(Note::getPitch));
        this.notes = new Notes(notes);
        if (!notes.isEmpty()) {
            this.low = this.notes.getFirst();
            this.high = this.notes.getLast();
        } else {
            this.low = null;
            this.high = null;
        }
    }

    private Range findRange(ArrayList<Note> notes) {
        long min = 0;
        long max = 0;
        for (Note note : notes) {
            if (note.start() < min) {
                min = note.start();
            }
            if (note.stop() > max) {
                max = note.stop();
            }
        }
        if (min >= max) {
            return null;
        } else {
            return new Range(min, max);
        }
    }

    /// Adds a {@code Note} in order and updates {@code this.low/high} if the {@code Note} added changes either
    void add(Note other) {
        int index = Collections.binarySearch(this.notes.getList(), other);
        if (index < 0) {
            index = -(index + 1);
        }
        this.notes.add(index, other);
        if (index == 0) {
            this.low = this.notes.getFirst();
        }
        if (index == this.notes.size() - 1) {
            this.high = this.notes.getFirst();
        }
    }

    public boolean isEmpty() {
        return this.notes.isEmpty();
    }

    @Override
    public String toString() {
        if (notes.isEmpty()) {
            return range.getLow() + ": []";
        }
        StringBuilder builder = new StringBuilder();
        builder.append(this.range.getLow());
        builder.append(": [");
        for (Note note : notes.getList()) {
            builder.append(reductor.Pitch.toStr(note.getPitch(), true));
            builder.append(" ");
        }
        builder.delete(builder.lastIndexOf(" "), builder.length());
        builder.append("]");
        return builder.toString();
    }

    public Notes getNotes() {
        return this.notes;
    }

    public Note getNote(int chordIndex) {
        return this.notes.get(chordIndex);
    }

    public Note getLow() {
        return this.low;
    }

    public Note getHigh() {
        return this.high;
    }

    public int size() {
        return this.notes.size();
    }

    @Override
    public Range getRange() {
        return this.range == null ? null : new Range(this.range);
    }

    @Override
    public int compareTo(Chord o) {
        return this.range.compareTo(o.getRange());
    }


}
