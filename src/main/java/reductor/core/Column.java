package reductor.core;

//import reductor.devv.HandSplittingFunctions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;


/**
 * This is a special type of note container that is used for vertical analysis,
 * i.e., looking at all the notes played at one time, and their relationships to each other pitch-wise.
 * It is NOT for looking at neighboring notes. Notes held over from before can "bleed" into this Column (making the
 * Column impure), but no new Notes should start within this Column (and should start in the next Column).
 * <p>
 * In this sense, they are components of a unidirectional, left-to-right sequence (reflecting the semantics used in
 * music, as well).
 */
public class Column implements Ranged, Noted, Comparable<Column> {

    /**
     * The notes belonging to this Column.
     */
    final Bucket notes;

    /**
     * The range this Column covers, though that may differ from the unison of Ranges of its constituent notes due to
     * holdovers. To calculate the latter, use {@linkplain Column#getActualRange}.
     * */
    final Range range;

    /**
     * Pure Columns are those that contain only Notes that exactly match the Column's range (i.e. no notes extend
     * behind or ahead of the Column).
     */
    boolean isPure;
    /**
     * Semi-pure columns contain only Notes that exactly match the Column's range or extend past it, not behind it.
     * */
    boolean isSemiPure;

    ///**
    // * The algorithm used to decide the boundaries of notes that go to each hand. Uses a default function upon
    // * construction, but, when put into larger contexts, such as when this Column is part of a collection of Columns
    // * (e.g. in a {@linkplain Box}), the container might decide that a different heuristic might be better for
    // * splitting the hands, at which point it would call {@linkplain Column#splitHands}; an example of this would be if
    // * the hands could be split based on textural or melodic differences, rather than using defaults. A Column, of
    // * course, cannot know this until that time.
    // */
    Consumer<Column> splitFunc;

    Column LH;
    Column middle;
    Column RH;

    int leftThumb;
    int rightThumb;


    /**
     * Primary constructor which takes a list of {@linkplain Note}, and a {@linkplain Range}.
     */
    public Column(List<Note> notes, Range range) {

        this.range = new Range(range);

        this.notes = new Bucket(notes);
        this.notes.sort(Comparator.comparingInt(Note::pitch));

        this.isPure = true;
        this.isSemiPure = true;
        this.assignPurity();
        this.markHoldovers();

        this.LH = new Column(this, false);
        this.middle = new Column(this, false);
        this.RH = new Column(this, false);

        //this.splitHands(HandSplittingFunctions::defaultHandSplitter);
    }

    /**
     * A constructor used by the primary constructor to create "terminal" Columns to represent the hands.
     * The resulting Column takes all its properties from the primary constructor or "parent" object, except that its
     * own Column members are null, and other processing/construction helpers are not necessary to call.
     */
    private Column(Column other, boolean createHandColumns) {

        this.range = new Range(other.range);
        this.notes =  new Bucket();

        this.isPure = other.isPure;
        this.isSemiPure = other.isSemiPure;

        this.LH = null;
        this.middle = null;
        this.RH = null;

        this.splitFunc = null;
    }

    /**
     * Copy constructor.
     */
    public Column(Column other) {
        this.range = new Range(other.range);
        this.notes = new Bucket(other.getNotes());
        this.isPure = other.isPure;
        this.isSemiPure = other.isSemiPure;
        this.LH = new  Column(other.LH, false);
        this.middle = new Column(other.middle, false);
        this.RH = new Column(other.RH, false);
        this.splitFunc = other.splitFunc;
    }


    /* ====================
       CONSTRUCTION HELPERS
     * ==================== */


    /**
     * Checks all Notes to see if any bleed out of this Column. Unlike
     * {@linkplain Column#markHoldovers}, looks forward (notes extending past or to the right of this Column).
     */
    private void assignPurity() {

        for (Note note : this.notes) {
            if (this.range.compareTo(note.getRange()) != 0) {
                this.isPure = false;
            }
            if (this.range.getHigh() < note.getRange().getHigh()) {
                this.isPure = false;
                this.isSemiPure = false;
                return;
            }
        }

    }

    /**
     * Assigns a Note object as being held over if it extends to the left of this Column.
     * Notes in a Column are deep copies, so this does not affect the same held-over Note in its native Column.
     */
    private void markHoldovers() {

        Function<Note, Note> transformHoldovers = note ->
                note.start() < this.range.getLow()
                        ? note.setIsHeld(true)
                        : note;

        List<Note> notes = this.notes.stream()
                .map(transformHoldovers)
                .toList();

        this.notes.clear();
        this.notes.addAll(notes);
    }


    /* ================
       INSTANCE METHODS
     * ================ */


    /**
     * Re-calculates the hand distribution based on the passed function representing another heuristic algorithm.
     */
    //boolean splitHands(Consumer<Column> splitFunc) {
    //
    //    if (LH == null && RH == null && middle == null) { return false; }
    //
    //    this.splitFunc = splitFunc;
    //    this.splitFunc.accept(this);
    //    return true;
    //}

    /**
     * Returns the distance between this Column's lowest and highest notes, in terms of pitch.
     */
    int getOverallSpan() { return this.getHighNote().pitch() - this.getLowNote().pitch(); }

    /**
     * This is pretty meaningless on its own, but, when used as a comparison to the split point,
     * it can provide insight into whether the hand distribution is even or uneven. For instance, if the median pitch
     * is relatively close to the split point, it means the hands are pretty evenly distributed. If the median pitch
     * is relatively far from the split point, it means that the note density in one of the hands is significantly
     * higher than in the other.
     */
    int getMedianPitch() { return this.notes.get( this.notes.size() / 2 ).pitch(); }

    /**
     * This is more used to compare the mean pitch *between* other columns to help look for contours in the hand
     * lines over time, and can help identify more probably melody vs. accompaniment lines.
     */
    int getMeanPitch()  {
        int sum = 0;
        for (Note note : this.notes) { sum += note.pitch(); }
        return sum / this.notes.size();
    }

    /**
     * This returns the imaginary pitch that represents exactly halfway between the two thumbs. If the thumbs cross
     * (extremely rare, but not impossible, this returns -1).
     */
    int getSplitPointPitch() {

        int rhThumbPitch = this.notes.get(this.rightThumb).pitch();
        int dist = getSplitSpan();

        if (0 <= getSplitSpan()) { return rhThumbPitch - (dist / 2); }

        return -1;
    }

    /**
     * This measures the distance between the thumbs, indicating how far apart or close together the hands are.
     */
    int getSplitSpan() {

        int rhThumbPitch = this.notes.get(this.rightThumb).pitch();
        int lhThumbPitch = this.notes.get(this.leftThumb).pitch();

        return rhThumbPitch - lhThumbPitch;
    }

    /**
     * Inserts a Note, in order, to this Column.
     */
    void add(Note other) {
        int index = Collections.binarySearch(this.notes, other);
        if (index < 0) { index = -(index + 1); }
        this.notes.add(index, other);
    }

    /**
     * Removes and returns a Note from this Column.
     */
    Note remove(int index) { return this.notes.remove(index); }


    /* =======
       GETTERS
     * ======= */


    public int size() { return this.notes.size(); }
    boolean isEmpty() { return this.notes.isEmpty(); }

    public Column getLeftHand() { return LH; }
    public Column getMiddle() { return middle; }
    public Column getRightHand() { return RH; }

    Note getLowNote() { return this.notes.getFirst(); }
    Note getHighNote() { return this.notes.getLast(); }

    boolean isPure() { return this.isPure; }
    boolean isSemiPure() { return this.isSemiPure; }

    /**
     * Returns false if there are more notes in this Column than can be realistically played by two hands.
     */
    public boolean isTwoHanded() { return middle.isEmpty(); }
    boolean isTwoHandedForRachmaninoff() { return true; }

    /**
     * See {@linkplain Column#range}.
     */
    Range getActualRange() { return RangeUtil.concatenate(this.notes); }


    /* =========
       OVERRIDES
     * ========= */


    @Override
    public Range getRange() { return new Range(this.range); }

    @Override
    public ArrayList<Note> getNotes() { return new ArrayList<>(this.notes); }

    @Override
    public int compareTo(Column other) { return this.range.compareTo(other.range); }

    @Override
    public String toString() {

        String lh = LH.notes.stream()
                .map(Note::toString)
                .collect(Collectors.joining(", "));

        String m = middle.notes.stream()
                .map(Note::toString)
                .collect(Collectors.joining(", "));

        String rh = RH.notes.stream()
                .map(Note::toString)
                .collect(Collectors.joining(", "));

        if (lh.isEmpty()) { lh = "   "; }
        if (m.isEmpty()) { m = "   "; }
        if (rh.isEmpty()) { rh = "   "; }

        return String.format("%s => LH: %s  ->  M: %s  ->  RH: %s",
                this.range, lh, m, rh
        );
    }

}
