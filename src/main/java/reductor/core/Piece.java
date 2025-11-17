package reductor.core;

import reductor.app.Application;
import reductor.util.IntervalTree;

import java.util.ArrayList;
import java.util.List;

// TODO minTick should ALWAYS be 0. Not the tick of the first event. Otherwise, measure creation is absolutely


public class Piece implements Ranged, Noted {

    private final int resolution;

    private final Range range;

    private final IntervalTree<Note> notes;
    private final IntervalTree<Measure> measures;
    private final IntervalTree<Column> columns;

    private final IntervalTree<TimeSignature> timeSignatures;
    private final IntervalTree<KeySignature> keySignatures;
    private final IntervalTree<Tempo> tempos;


    public Piece(
            int resolution,
            Range range,
            IntervalTree<Note> notes,
            IntervalTree<TimeSignature> timeSignatures,
            IntervalTree<KeySignature> keySignatures,
            IntervalTree<Tempo> tempos,
            IntervalTree<Measure> measures,
            IntervalTree<Column> columns
    ) {
        this.resolution = resolution;
        this.range = range;
        this.notes = notes;
        this.timeSignatures = timeSignatures;
        this.keySignatures = keySignatures;
        this.tempos = tempos;
        this.measures = measures;
        this.columns = columns;

        //// Default to C major for the whole piece if no key sig data was given
        //if (keySignatures.isEmpty()) { keySignatures = new ArrayList<>( List.of( new KeySignature("C", this.range)) ); }
        //this.ma = new MeasuresAccessor();
    }


    //region getters

    public int getResolution() {
        return this.resolution;
    }

    public List<TimeSignature> getTimeSignatures() {
        return new ArrayList<>(this.timeSignatures.toList());
    }

    public List<KeySignature> getKeySignatures() {
        return new ArrayList<>(this.keySignatures.toList());
    }

    public List<Tempo> getTempos() {
        return new ArrayList<>(this.tempos.toList());
    }

    public List<Column> getColumns() {
        return new ArrayList<>( this.columns.toList() );
    }

    public List<Measure> getMeasures() {
        return new ArrayList<>( this.measures.toList() );
    }

    //endregion


    //region MeasureAccessor


    //private int byMeasureNumbering(int val) {
    //
    //    if (!hasPickup) {
    //
    //        if (val == 0) {
    //            throw new RuntimeException("can't ask for measure 0 because piece has no pickup");
    //        } else {
    //            val--;
    //        }
    //
    //    }
    //
    //    return val;
    //
    //}
    //
    //public int getNumMeasures() {
    //    return hasPickup ? measures.size() - 1 : measures.size();
    //}
    //
    //public Measure getMeasure(int val) throws RuntimeException {
    //    return measures.get( byMeasureNumbering(val) );
    //}
    //
    //public Measure getFirstMeasure() { return this.getMeasure(1); }
    //
    //public Measure getLastMeasure() { return this.getMeasure( getNumMeasures() ); }
    //
    //public ArrayList<Measure> getMeasures(int first, int last) {
    //
    //    if (first >= last) { throw new IllegalArgumentException("first should be less than last"); }
    //
    //    ArrayList<Measure> measures = new ArrayList<>();
    //    for (int i = first; i <= last; i++) {
    //        measures.add( this.getMeasure(i) );
    //    }
    //
    //    return measures;
    //}
    //
    //public ArrayList<Measure> getFirstNMeasures(int n) {
    //    if (getNumMeasures() < n) { n = getNumMeasures(); } // Clamp.
    //    if (n == 1) { return new ArrayList<>( List.of(getFirstMeasure()) ); }
    //    return getMeasures(1, n);
    //}
    //
    //public ArrayList<Measure> getLastNMeasures(int n) {
    //    if ( n < getNumMeasures()) { n = getNumMeasures(); } // Clamp.
    //    return getMeasures(this.getNumMeasures() - n, this.getNumMeasures());
    //}


    //endregion


    @Override public Range getRange() {
        return new Range(this.range);
    }

    @Override public ArrayList<Note> getNotes() {
        return new ArrayList<>(this.notes.toList());
    }


}
