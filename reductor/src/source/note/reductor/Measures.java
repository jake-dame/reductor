package reductor;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Queue;


public class Measures {

    // TODO: getMeasureNumber() will work correctly, but when you debug, the measureNumber member will still
    // be off by 1... The only way to fix this is with the dummy.

    private final ArrayList<Measure> measures;
    private final ArrayList<TimeSignature> timeSigs;
    private final long pieceLength;
    //private final boolean zeroIndexed;

    private Measures(ArrayList<TimeSignature> list) {
        this.timeSigs = list;
        this.pieceLength = Piece.LENGTH;
        this.measures = this.createEmptyMeasures();
        this.fillMeasures();
        //this.zeroIndexed = this.probablyHasPickup();
    }

    public static Measures createMeasures(ArrayList<TimeSignature> list) {
        // This could be a publicly constructed class and just be a dumb object,
        // but something really solid needs to be signified that absolutely NO
        // metric analysis/reduction/decisions etc. can be made if there are no time signature event data
        if (list.isEmpty()) { return null; }
        return new Measures(list);
    }


    //// TODO: I'm not sure this belongs in this class whether you keep it or not. It needs the full list, yes, but
    ////  could maybe be a static method in TimeSignature
    //private void assignTimeSigRanges() {
    //
    //    Queue<TimeSignature> queue = new ArrayDeque<>(this.timeSigs);
    //
    //    TimeSignature curr;
    //    TimeSignature next;
    //    long startTick;
    //    long endTick;
    //    while (!queue.isEmpty()) {
    //
    //        curr = queue.remove();
    //        startTick = curr.getTick();
    //
    //        if (!queue.isEmpty()) {
    //            endTick = queue.peek().getTick();
    //        } else {
    //            // overcompensate in order to make the last time signature completely inclusive
    //            endTick = pieceLength + 1;
    //        }
    //
    //        Range range = new Range(startTick, endTick - 1);
    //        curr.setRange(range);
    //
    //    }
    //
    //}

    private ArrayList<Measure> createEmptyMeasures() {

        Queue<Measure> mQueue = new ArrayDeque<>();
        Queue<TimeSignature> tQueue = new ArrayDeque<>(timeSigs);

        TimeSignature currTimeSig = tQueue.remove();
        long currMeasureSize = Measure.getMeasureSize(currTimeSig);

        long startTick = 0;
        long endTick = currMeasureSize;

        int measureCount = 0;
        while (startTick < this.pieceLength) {

            Range range;
            if (tQueue.isEmpty()) {
                range = new Range(startTick, endTick);
            } else {
                range = new Range(startTick, endTick - 1);
            }

            Measure measure = new Measure(range, measureCount, currTimeSig, new Notes());
            mQueue.add(measure);
            measureCount++;

            /*
            `if (!tQueue.isEmpty()  &&  tQueue.peek().getTick() <= startTick) {`

             If you ever want to get rid of timesignatures have ranges, all you need to do to refactor is switch these
             out. and you can delete all the code related to timesignatures having ranges...
            */
            if (!currTimeSig.getRange().contains(endTick)  &&  !tQueue.isEmpty()) {
                currTimeSig = tQueue.remove();
                currMeasureSize = Measure.getMeasureSize(currTimeSig);
            }

            startTick = endTick;
            endTick += currMeasureSize;
        }

        return new ArrayList<>(mQueue);
    }

    private boolean probablyHasPickup() {

        TimeSignature timeSig1 = this.measures.get(0).getTimeSignature();
        TimeSignature timeSig2 = this.measures.get(1).getTimeSignature();

        // TODO: some heuristic for how much rest should precede notes in the case of a pseudo-pickup
        // TODO: could also have harmonically made heuristic that if the measure has a V-I intro
        Boolean condition = null;

        if (timeSig1 == timeSig2 && condition) {
            this.measures.get(0).setIsPickup(true);
            return true;
        }

        if (timeSig1.getLowerNumeral() == timeSig2.getLowerNumeral()
                && timeSig1.getUpperNumeral() < timeSig2.getUpperNumeral()) {
            this.measures.get(0).setIsPickup(true);
            return true;
        }

        return false;
    }

    private void fillMeasures() {
        var ranges = this.getRanges();
    }

    private ArrayList<Range> getRanges() {
        ArrayList<Range> ranges = new ArrayList<>();
        measures.forEach(m -> ranges.add(m.getRange()));
        return ranges;
    }

    private void assignNoteMeterInformation(Note note) {
        // TODO: divide measure or notes out into beats
    }


    /*===============
    * GETTERS
    * =============== */

    //// TODO: should this return num measures or (10 + pickup == 10)
    //public int size() {
    //    return this.measures.size();
    //}
    //
    //public int numMeasures() {
    //
    //}
    //
    //public Measure getFirst() {
    //    return this.getMeasure(1);
    //}
    //
    //public Measure getLast() {
    //    return this.getMeasure( this.size() );
    //}
    //
    //public ArrayList<Measure> getList() {
    //    return this.measures;
    //}
    //
    //public ArrayList<Measure> getMeasures(int start, int stop) {
    //
    //    ArrayList<Measure> measures = new ArrayList<>();
    //
    //    for (int i = start; i <= stop; i++) {
    //        measures.add( this.getMeasure(i));
    //    }
    //
    //    return measures;
    //
    //}
    //
    //public Measure getMeasure(int measureNumber) {
    //    if (zeroIndexed) { measureNumber--; }
    //    if (this.size() < measureNumber) { throw new RuntimeException("no"); }
    //    if (measureNumber < 0) { throw new RuntimeException("no"); } // todo more informative message and probably
    //    // two of these, one for trying to access measure 0 in a Measures with no pickup, and one for just plain
    //    // stupid like -1
    //    return this.measures.get(measureNumber);
    //}
    //
    //
    /// Get measures by specifying measure numbers
    //public ArrayList<Measure> getMeasures(int first, int last) {
    //    if (first <= last) { throw new IllegalArgumentException("first should be less than last"); }
    //    if (first < 0  ||  last >= this.measures.size()) { throw new ArrayIndexOutOfBoundsException("first can't be negative"); }
    //    return new ArrayList<>( this.measures.subList(first, last - 1) );
    //}


}
