package reductor;

import javax.sound.midi.MidiEvent;
import java.util.ArrayList;
import java.util.Collections;

import static reductor.ReductorUtil.notesToMidiEvents;


/**
 * This is a collection for {@code Note} objects, but where the main comparisons and operations are not
 * concerned with <i>when</i> the notes occur, but with the "vertical" arrangements of those notes (i.e. pairs of musical
 * intervals that comprise chords). The number of {@code Note} elements in this will be pretty small (presumably less than 15),
 * so the "collection" operations for this class are kept pretty straightforward.
 */
public class Chord {

    /// The notes that comprise this {@code Chord} in ascending order
    ArrayList<Note> notes;

    /// The {@code Note} object representing the lowest pitch in this {@code Chord}
    Note low;

    /// The {@code Note} object representing the highest pitch in this {@code Chord}
    Note high;

    /// The approximate time this chord occurs in the {@code Sequence} from which it was sourced from
    long tick;

    Range range;


    /// Given a list of notes, constructs a {@code Chord} object
    Chord(ArrayList<Note> notes, long tick) {

        if (notes == null) {
            throw new NullPointerException("notes is null");
        }

        this.tick = tick;

        // Ascending by pitch
        Collections.sort(notes);

        this.notes = notes;

        if (!notes.isEmpty()) {
            this.low = this.notes.getFirst();
            this.high = this.notes.getLast();
        } else {
            this.low = null;
            this.high = null;
        }

    }


    /// Default constructor (used for testing)
    Chord() {
        this.notes = new ArrayList<>();
        this.low = null;
        this.high = null;
        this.tick = -1;
    }


    /// Adds a {@code Note} in order and updates {@code this.low/high} if the {@code Note} added changes either
    void add(Note other) {

        int index = Collections.binarySearch(this.notes, other);

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


    /// Returns a list of this {@code Chord}'s notes as a list of {@code MidiEvent}s
    ArrayList<MidiEvent> getMidiEvents() {
        return notesToMidiEvents(this.notes);
    }


    @Override
    public String toString() {

        if (notes.isEmpty()) {
            return tick + ": []";
        }

        StringBuilder builder = new StringBuilder();

        builder.append(this.tick);
        builder.append(": [");

        for (Note note : notes) {
            builder.append(reductor.Pitch.numericalPitchToString(note.pitch(), true));
            builder.append("-");
        }

        builder.delete(builder.lastIndexOf("-"), builder.length());

        builder.append("]");

        return builder.toString();

    }


}
