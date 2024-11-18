package reductor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import static org.junit.jupiter.api.Assertions.assertEquals;

/// This is a special type of note container that is used for vertical analysis
/// i.e. looking at all the notes played at one time, and their relationships to each other pitch-wise
/// It is not for looking at neighboring notes. Notes held from before can be in a Column, but no new notes shoulud
/// begin within a Column's range.
public class Column implements Ranged, Noted {

    /// The noteList that comprise this {@code Column} in ascending order by pitch
    private final NoteList notes;

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
        this.notes = new NoteList(notes);
        this.notes.getBackingList().sort(Comparator.comparingInt(Note::pitch));
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
        this.notes =  new NoteList(other.notes);
        this.isPure = other.isPure;
        this.LH = new  Column(other.LH, false);
        this.middle = new Column(other.middle, false);
        this.RH = new Column(other.RH, false);
    }


    /// Because Column has Column members, but that should only be 1-deep, use this private constructor in primary
    /// constructor
    private Column(Range range, boolean createHandColumns) {
        this.range = new Range(range);
        this.notes = new NoteList();
        this.isPure = this.assignIsPure();
    }

    private Column(Column other, boolean createHandColumns) {
        this.range = new Range(other.range);
        this.notes = new NoteList(other.notes);
        this.isPure = other.isPure;
    }

    /// Create a Column with a default range (the default quarter note, \[0,480])
    public Column(ArrayList<Note> notes) {
        this(new ArrayList<>(notes), new Range(0,480));
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
        for (Note note : this.notes.getBackingList()) {
            if (this.range.compareTo(note.getRange()) != 0) { return false; }
        }
        return true;
    }

    public void assignHoldovers() {
        //for (Note note : this.notes.getBackingList()) {
        //    if (note.start() < this.range.low()) {
        //        this.notes.getBackingList().remove(note);
        //        Note newNote = new Note(note);
        //        newNote.setIsHeld(true);
        //        this.notes.add( new Note(note) );
        //    }
        //    if (this.range.low() < note.start()) {
        //        System.out.println("note started " + (note.start() - this.range.low())+ " ticks after column start");
        //    }
        //}
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

        int i = 0;
        int anchorPitch = notes.get(i).pitch();
        while (i < size) {
            Note note = notes.get(i);
            int spanFromAnchor = Math.abs(note.pitch() - anchorPitch);

            if (SPAN_MAX < spanFromAnchor) { break; }
            if (NOTES_MAX == LH.size()) { break; }

            LH.add(note);
            i++;
        }
        this.leftThumb = i - 1;

        i = size - 1;
        anchorPitch = notes.get(i).pitch();
        while (0 < i) {
            Note note = notes.get(i);
            int distanceFromAnchor = anchorPitch - note.pitch();

            if (SPAN_MAX < distanceFromAnchor) { break; }
            if (NOTES_MAX < RH.size()) { break; }
            if (i <= this.leftThumb ) { break; }

            RH.add(note);
            i--;
        }
        this.rightThumb = i + 1;

        // Grab what notes are left over between the two hands, if any
        for (i = this.leftThumb  + 1; i < this.rightThumb; i++) {
            Note note = notes.get(i);
            this.middle.add(note);
        }

        // This would have to be done either, just backwards, if starting top-down
        if (RH.isEmpty()  &&  MIDDLE_C < LH.getLowNote().pitch()) {
            for (i = 0; i < LH.size(); i++) { RH.add( LH.getNotes().get(i) ); }
            while (LH.size() != 0) { LH.notes.getBackingList().removeFirst(); }
        }

    }

    public void add(Note other) {
        int index = Collections.binarySearch(this.notes.getBackingList(), other);
        if (index < 0) { index = -(index + 1); }
        this.notes.add(index, other);
    }

    public Note remove(int index) {
        return this.notes.getBackingList().remove(index);
    }

    public int size() { return this.notes.size(); }
    public boolean isEmpty() { return this.notes.isEmpty(); }

    public Column getLH() { return new Column(this.LH, true); }
    public Column getMiddle() { return new Column(this.middle, false); }
    public Column getRH() { return new Column(this.RH, false); }

    public Note getLowNote() { return this.notes.getFirst(); }
    public Note getHighNote() { return this.notes.getLast(); }

    public boolean isPure() { return this.isPure; }
    public boolean isTwoHanded() { return middle.isEmpty(); }

    public int getMedian() {
        return this.notes.get( this.notes.size() / 2 ).pitch();
    }

    public int getMean()  {
        int sum = 0;
        for (Note note : this.notes.getBackingList()) { sum += note.pitch(); }
        return sum / this.notes.size();
    }

    public int getSplitPoint() {

        int dist = this.notes.get(this.rightThumb).pitch()
                - this.notes.get(this.rightThumb).pitch();

        if (dist > 0) {
            return dist / 2;
        }

        return -1;
    }

    public Range getActualRange() {

        long min = 0;
        long max = 1;

        for (Note note : this.notes.getBackingList()) {
            if (note.start() < min) { min = note.start(); }
            if (max < note.stop()) { max = note.stop(); }
        }

        return new Range(min, max);
    }

    @Override
    public Range getRange() { return new Range(this.range); }

    @Override
    public ArrayList<Note> getNotes() { return new ArrayList<>(this.notes.getBackingList()); }

    @Override
    public void setNotes(ArrayList<Note> notes) { }

    @Override
    public String toString() { return this.range.toString() + this.notes; }



}
