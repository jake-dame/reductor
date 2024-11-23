package reductor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import static org.junit.jupiter.api.Assertions.assertEquals;

/// This is a special type of note container that is used for vertical analysis
/// i.e. looking at all the notes played at one time, and their relationships to each other pitch-wise
/// It is not for looking at neighboring notes. Notes held from before can be in a Column, but no new notes shoulud
/// begin within a Column's range.
public class Column implements Ranged, Noted, Comparable<Column> {

    /// The noteList that comprise this {@code Column} in ascending order by pitch
    private final Bucket notes;

    /// The Range this column was created with and intended to represent, although due to notes whose durations are
    /// either shorter or longer than this, the actual range may vary. To get the actual range, use
    ///  {@link Column#getActualRange}
    private final Range range;

    // These and the whole mess with the constructors / recursive construction... can probably be converted to just
    // indices at some point, and getters that get sublists using those indices
    /// This Column's left hand notes.
    private Column LH;
    /// This Column's notes that fall between realistic hand coverage.
    private Column middle;
    /// This Column's right hand notes.
    private Column RH;

    /// Index in notes to grab the highest LH note
    private int leftThumb;
    /// Index in notes to grab the lowest RH note
    private int rightThumb;


    /// "Pure" Columns contain Notes that all have the same duration (i.e. a single chord that includes no notes that
    /// held over from previous columns).
    /// There should never be a Column made entirely of held over notes, because those notes should belong to the
    /// previous Column.
    private final boolean isPure;

    private int medianPitch;
    private int meanPitch;
    private int splitPoint;

    /// Given a list of {@link Note} objects, constructs a {@link Column} object
    public Column(ArrayList<Note> notes, Range range) {
        this.range = new Range(range);
        this.notes = new Bucket(notes);
        this.notes.sort(Comparator.comparingInt(Note::pitch));

        this.isPure = this.assignIsPure();
        this.LH = new Column(this.range, false);
        this.middle = new Column(this.range, false);
        this.RH = new Column(this.range, false);

        this.assignHands();
        this.assignHoldovers();

        assert this.size() == this.LH.size() + this.middle.size() + this.RH.size();
    }

    /// Copy constructor.
    public Column(Column other) {
        this.range = new Range(other.range);
        this.notes =  new Bucket(other.getNotes());
        this.isPure = other.isPure;
        this.LH = new  Column(other.LH, false);
        this.middle = new Column(other.middle, false);
        this.RH = new Column(other.RH, false);
    }


    /// Because Column has Column members, but that should only be 1-deep, use this private constructor in primary
    /// constructor
    private Column(Range range, boolean createHandColumns) {
        this.range = new Range(range);
        this.notes = new Bucket();
        this.isPure = this.assignIsPure();
    }

    private Column(Column other, boolean createHandColumns) {
        this.range = new Range(other.range);
        this.notes = new Bucket(other.getNotes());
        this.isPure = other.isPure;
    }

    /// Create a Column with a default range (the default quarter note, \[0,480])
    public Column(ArrayList<Note> notes) {
        this(new ArrayList<>(notes), new Range());
    }

    /// Create an empty Column with a specified range.
    public Column(Range range) {
        this(new ArrayList<>(), new Range(range));
    }

    /// Create an empty Column with a default range (the default quarter note, \[0,480])
    public Column() {
        this(new ArrayList<>(), new Range());
    }

    /// Helper to determine whether this Column of Notes is pure or not.
    private boolean assignIsPure() {
        for (Note note : this.notes) {
            if (this.range.compareTo(note.getRange()) != 0) { return false; }
        }
        return true;
    }

    public void assignHoldovers() {
        ArrayList<Note> toAdd = new ArrayList<>();
        ArrayList<Note> toRemove = new ArrayList<>();

        for (Note note : new ArrayList<>(this.notes)) { // Use a copy of the list to iterate safely
            if (note.start() < this.range.low()) {
                toRemove.add(note); // Mark the note for removal
                Note newNote = new Note(note);
                newNote.setIsHeld(true);
                toAdd.add(newNote); // Add the modified note to the temporary list
            }

            if (this.range.low() < note.start()) {
                System.out.println("this column has range" + this.range + " and this note:  " + note + " " + note.getRange());
            }
        }

        // Apply changes after iteration
        this.notes.removeAll(toRemove);
        this.notes.addAll(toAdd);
    }

    private void assignHands() {

        final int size = this.notes.size();

        final int MIDDLE_C = 60;

        if (notes.isEmpty()) { return; }

        if (size == 1) {

            if (notes.getFirst().pitch() < MIDDLE_C) {
                LH.add(notes.getFirst());
            } else {
                RH.add(notes.getFirst());
            }

            return;
        }

        final int SPAN_MAX = 14; // major 9th. I can't reach a 10th of any kind except double black keys
        final int NOTES_MAX = 6; // I have yet to come across a piano chord with 7+ notes

        // Fill up LH (start from bottom)
        int i = 0;
        int anchorPitch = notes.get(i).pitch();
        while (i < size) {
            Note currNote = notes.get(i);
            int distanceFromAnchor = Math.abs(currNote.pitch() - anchorPitch);

            if (SPAN_MAX < distanceFromAnchor) { break; }
            if (NOTES_MAX == LH.size()) { break; }

            LH.add(currNote); i++;
        }
        this.leftThumb = i - 1;

        // Fill up RH (start from top)
        i = size - 1;
        anchorPitch = notes.get(i).pitch();
        while (0 < i) {
            Note currNote = notes.get(i);
            int distanceFromAnchor = Math.abs(currNote.pitch() - anchorPitch);

            if (SPAN_MAX < distanceFromAnchor) { break; }
            if (NOTES_MAX == RH.size()) { break; }
            if (i <= this.leftThumb ) { break; } // also

            RH.add(currNote); i--;
        }
        this.rightThumb = i + 1;

        // Grab what notes are left over between the two hands, if any, and give them to "middle"
        for (i = this.leftThumb + 1; i <= this.rightThumb - 1; i++) {
            this.middle.add( this.notes.get(i) );
        }

        // If there were only enough notes for LH, but they are all above middle C, transfer them to RH
        if (RH.isEmpty()  &&  MIDDLE_C < LH.getLowNote().pitch()) {
            for (i = 0; i < LH.size(); i++) { RH.add( LH.getNotes().get(i) ); }
            while (0 < LH.size()) { LH.notes.remove(0); }
        }

        //// If both hands are playing relatively close to each other but the density of each group is thinner, it
        //// probably means the notes are meant to be evenly distributed between the hands
        //// See: Mozart K545 i or Liszt-Beethoven Symphony 3 scherzo
        //if (getOverallSpan() <= 3 * 12) {
        //    this.redistributeHands();
        //}

        // TODO: loop through all, assign scores, and say LH should be here, RH should be here, the "unreachable area"

    }

    private void redistributeHands() {






    }

    public void add(Note other) {
        int index = Collections.binarySearch(this.notes, other);
        if (index < 0) { index = -(index + 1); }
        this.notes.add(index, other);
    }

    public Note remove(int index) { return this.notes.remove(index); }

    public int size() { return this.notes.size(); }
    public boolean isEmpty() { return this.notes.isEmpty(); }

    public Column getLH() { return new Column(this.LH, true); }
    public Column getMiddle() { return new Column(this.middle, false); }
    public Column getRH() { return new Column(this.RH, false); }

    public Note getLowNote() { return this.notes.getFirst(); }
    public Note getHighNote() { return this.notes.getLast(); }

    public boolean isPure() { return this.isPure; }
    public boolean isTwoHanded() { return middle.isEmpty(); }

    public int getOverallSpan() {
        return this.getHighNote().pitch() - this.getLowNote().pitch();
    }

    public int getMedianPitch() {
        return this.notes.get( this.notes.size() / 2 ).pitch();
    }

    public int getMeanPitch()  {
        int sum = 0;
        for (Note note : this.notes) { sum += note.pitch(); }
        return sum / this.notes.size();
    }

    ///
    public int getSplitPointPitch() {

        int rhThumbPitch = this.notes.get(this.rightThumb).pitch();
        int lhThumbPitch = this.notes.get(this.leftThumb).pitch();
        int dist = rhThumbPitch - lhThumbPitch;

        if (-1 < dist) {
            // Halfway between the thumbs
            return rhThumbPitch - (int) (dist / 2);
        }

        // If the thumbs cross
        return -1;
    }

    public Range getActualRange() { return Range.concatenate(this.notes); }

    @Override
    public Range getRange() { return new Range(this.range); }

    @Override
    public ArrayList<Note> getNotes() { return new ArrayList<>(this.notes); }

    @Override
    public int compareTo(Column other) { return this.range.compareTo(other.range); }

    @Override
    public String toString() { return this.range.toString() + this.notes; }



}
