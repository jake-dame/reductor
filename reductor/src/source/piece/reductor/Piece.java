package reductor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.TreeSet;


public class Piece implements Ranged, Noted {

    final IntervalTree<Note> tree;

    final Range range;

    final int resolution;

    final NoteList noteList;
    final MeasureList measureList;

    final ArrayList<TimeSignature> timeSigs;
    final ArrayList<KeySignature> keySigs;
    ArrayList<Tempo> tempi; //acshually

    Piece(
            ArrayList<Note> notes,
            ArrayList<TimeSignature> timeSignatures,
            ArrayList<KeySignature> keySignatures,
            ArrayList<Tempo> tempos
    ) {

        this.timeSigs = new ArrayList<>(timeSignatures);
        this.keySigs = new ArrayList<>(keySignatures);
        this.tempi = new ArrayList<>(tempos);

        this.noteList = new NoteList(notes);

        this.tree = new IntervalTree<>(notes);

        this.range = this.findPieceRange();

        this.resolution = Context.resolution();

        this.measureList = new MeasureList(timeSigs, resolution, this.range.high());

        fillContainer(measureList.getAll());

    }

    private Range findPieceRange() {
        long minTick = this.tree.getFirstTick();
        long maxTick = this.tree.getLastTick();
        return new Range(minTick, maxTick + 1);
    }

    public void scaleTempo(float scale) {

        ArrayList<Tempo> newTempi = new ArrayList<>();

        for (Tempo tempo : this.tempi) {
            int newBpm = (int) (tempo.getBpm() * scale);
            Tempo newTempo = new Tempo(newBpm, tempo.getRange());
            newTempi.add(newTempo);
        }

        this.tempi = newTempi;
    }

    @Override
    public Range getRange() { return new Range(this.range); }

    @Override
    public ArrayList<Note> getNotes() { return this.noteList.getBackingList(); }

    @Override
    public void setNotes(ArrayList<Note> notes) { }


    /*===========
    * QUERY PIECE
    * =========*/


    public ArrayList<Column> queryWithRanges(ArrayList<Range> ranges) {

        ArrayList<Column> out = new ArrayList<>();

        for (Range range : ranges) {
            ArrayList<Note> matches = this.tree.query(range);
            if (!matches.isEmpty()) {
                out.add( new Column(matches, range));
            }
        }

        return out;
    }

    /// Get ranges: provide window size
    public static ArrayList<Range> getRanges(Range startStop, long windowSize) {

        ArrayList<Range> out = new ArrayList<>();

        long windowMin = startStop.low();
        long windowMax = windowMin + windowSize;

        long length = startStop.high();

        while (windowMin < length) {

            Range window = new Range(windowMin, windowMax - 1);
            out.add(window);
            windowMin += windowSize;
            windowMax += windowSize;

            // Clamp: e.g., range = [0,10], windowSize = 3; ranges = [0,3], [4,7], [8-10] <--last is clamped
            if (length < windowMax) { windowMax = length; }

        }

        return out;
    }

    /// Get ranges: provide number of ranges to get
    public static ArrayList<Range> getRanges(Range range, double divisor) {

        double windowSize = range.length() / divisor;

        // TODO: refine this and make decisions
        double leftover = windowSize - ((int) windowSize);
        if (leftover != 0) {
            System.out.println("loss of " + leftover);
        }

        return getRanges(range, (long) windowSize);

    }

    //public ArrayList<Column> getColumns() {
    //
    //    ArrayList<Column> out = new ArrayList<>();
    //
    //    //long windowSize = RhythmBase.getDuration(RhythmBase.ONE_TWENTY_EIGHTH, Context.resolution());
    //    long windowSize = 1;
    //
    //    long windowMin = this.range.low();
    //    long windowMax = windowMin + windowSize;
    //
    //    long length = this.range.high();
    //
    //    boolean seen = false; // debug
    //    while (windowMax < length) {
    //
    //        Range window = new Range(windowMin, windowMax);
    //
    //        ArrayList<Note> matches = this.tree.query(window);
    //
    //        boolean makeNewColumn = false;
    //        for (Note note : matches) {
    //            if (windowMax <= note.start()) {
    //                makeNewColumn = true;
    //                break;
    //            }
    //        }
    //
    //
    //        if (makeNewColumn) {
    //
    //            // debug
    //            if (windowMin >= 179200 && !seen) {
    //                System.out.println("hi");
    //                seen = true;
    //            }
    //            // debug
    //
    //            window = new Range(windowMin, windowMax - windowSize);
    //            //window = new Range(windowMin, windowMax - windowSize - 1);
    //            matches = this.tree.query(window);
    //            out.add( new Column(matches, window) );
    //            windowMin = windowMax;
    //            windowMax = windowMin + 1;
    //            // Clamp: e.g., range = [0,10], windowSize = 3; ranges = [0,3], [4,7], [8-10] <--last is clamped
    //            //if (length < windowMax) { windowMax = length; }
    //        } else {
    //            windowMax += windowSize;
    //        }
    //
    //    }
    //
    //    return new ArrayList<>(out);
    //}


    public ArrayList<Column> getColumns() {

        // TODO: this isn't going to work. The way it compares would excludes perfectly valid repeated chords
        Comparator<Column> comp = (col1, col2) -> col1.getNotes().equals(col2.getNotes()) ? 0 : 42;

        TreeSet<Column> set = new TreeSet<>(comp);

        ArrayList<Range> ranges = getRanges(this.range, RhythmBase.getDuration(RhythmBase.ONE_TWENTY_EIGHTH));

        for (Range range : ranges) {
            ArrayList<Note> matches = this.tree.query(range);
            if (!matches.isEmpty()) { set.add( new Column(matches, range)); }
        }

        return new ArrayList<>(set);

    }

    public static <T extends Noted> ArrayList<Note> shiftNotesToBeginning(T container) {
        return shiftNotes(container, 0);
    }

    /// Given any class that is Noted, shifts all its Notes to start at a provided tick.
    public static <T extends Noted> ArrayList<Note> shiftNotes(T container, long tick) {

        if (tick < 0) { throw new RuntimeException("cannot shift notes past 0"); }

        ArrayList<Note> copy = new ArrayList<>(container.getNotes());
        copy.sort(Comparator.comparingLong(Note::start));

        if (copy.isEmpty()) { return new ArrayList<>(); }

        long offset = tick - copy.getFirst().start() ;

        ArrayList<Note> out = new ArrayList<>();
        for (Note note : copy) {
            out.add( new Note(note, Range.getShiftedInstance(note.getRange(), offset)));
        }

        return out;
    }

    public <T extends Ranged & Noted> void fillContainer(ArrayList<T> rangedElems) {
        for (T elem : rangedElems) {
            ArrayList<Note> matches = this.tree.query(elem.getRange());
            elem.setNotes(matches);
        }
    }


}