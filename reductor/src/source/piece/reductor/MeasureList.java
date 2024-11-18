package reductor;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Queue;


public class MeasureList implements Noted {

    private final ArrayList<Measure> measures;
    private final boolean hasPickup;

    MeasureList(ArrayList<TimeSignature> timeSigs, int resolution, long pieceLength) {
        this.measures = this.createEmptyMeasures(timeSigs, resolution, pieceLength);
        this.hasPickup = checkForPickup();
        this.assignMeasureNumbers();
    }

    private ArrayList<Measure> createEmptyMeasures(ArrayList<TimeSignature> timeSigs, int resolution,
                                                   long pieceLength) {

        Queue<Measure> measuresQueue = new ArrayDeque<>();
        Queue<TimeSignature> timeSigsQueue = new ArrayDeque<>(timeSigs);

        TimeSignature currTimeSig = timeSigsQueue.remove();
        long currMeasureSize = currTimeSig.getMeasureSize();

        long startTick = 0;
        long endTick = currMeasureSize;

        while (startTick < pieceLength) {

            Range range;
            if (startTick + currMeasureSize == pieceLength) {
                range = new Range(startTick, endTick);
            } else {
                range = new Range(startTick, endTick - 1);
            }

            Measure measure = new Measure(range, currTimeSig, new NoteList());

            measuresQueue.add(measure);

            /*
            `if (!tQueue.isEmpty()  &&  tQueue.peek().getTick() <= startTick) {`

             If you ever want to get rid of timesignatures have ranges, all you need to do to refactor is switch these
             out. and you can delete all the code related to timesignatures having ranges...
            */
            if (!currTimeSig.getRange().contains(endTick)  &&  !timeSigsQueue.isEmpty()) {
                currTimeSig = timeSigsQueue.remove();
                currMeasureSize = currTimeSig.getMeasureSize();
            }

            startTick = endTick;
            endTick += currMeasureSize;
        }

        return new ArrayList<>(measuresQueue);
    }

    private boolean checkForPickup() {

        TimeSignature timeSig1 = this.measures.get(0).getTimeSignature();
        TimeSignature timeSig2 = this.measures.get(1).getTimeSignature();

        //boolean implementHowMuchInitialRestConstitutesAPickup = false;
        //if (timeSig1 == timeSig2) {
        //    if (implementHowMuchInitialRestConstitutesAPickup) {
        //        this.measureList.get(0).setIsPickup(true);
        //        return true;
        //    }
        //}

        if (timeSig1.getLowerNumeral() == timeSig2.getLowerNumeral()
                && timeSig1.getUpperNumeral() < timeSig2.getUpperNumeral()) {
            this.measures.get(0).setIsPickup(true);
            return true;
        }

        return false;
    }

    private void assignMeasureNumbers() {

        int measureNumber = 1;

        if (hasPickup) {
            measureNumber--;
        }

        for (Measure m : this.measures) {
            m.setMeasureNumber(measureNumber++);
        }

    }

    public ArrayList<Range> getRanges() {
        ArrayList<Range> ranges = new ArrayList<>();
        measures.forEach(m -> ranges.add(m.getRange()));
        return ranges;
    }


    /*===============
    * GETTERS
    * =============== */

    private int byMeasureNumbering(int val) {

        if (!hasPickup) {

            if (val == 0) {
                throw new RuntimeException("can't ask for measure 0 because piece has no pickup");
            } else {
                val--;
            }

        }

        return val;

    }

    public ArrayList<Measure> getAll() { return new ArrayList<>(this.measures); }

    public int getNumMeasures() {
        return hasPickup ? this.measures.size() - 1 : this.measures.size();
    }

    public Measure getMeasure(int val) throws RuntimeException {
        return this.measures.get( byMeasureNumbering(val) );
    }

    public Measure getFirstMeasure() { return this.getMeasure(1); }
    public Measure getLastMeasure() { return this.getMeasure( getNumMeasures() ); }

    public ArrayList<Measure> getMeasures(int first, int last) {

        if (first >= last) { throw new IllegalArgumentException("first should be less than last"); }

        ArrayList<Measure> measures = new ArrayList<>();
        for (int i = first; i <= last; i++) {
            measures.add( this.getMeasure(i) );
        }

        return measures;
    }

    public ArrayList<Measure> getFirstNMeasures(int n) {
        if (getNumMeasures() < n) { n = getNumMeasures(); } // Clamp.
        return getMeasures(0, n);
    }

    public ArrayList<Measure> getLastNMeasures(int n) {
        if ( n < getNumMeasures()) { n = getNumMeasures(); } // Clamp.
        return getMeasures(this.getNumMeasures() - n, this.getNumMeasures());
    }

    @Override
    public ArrayList<Note> getNotes() {
        ArrayList<Note> out = new ArrayList<>();
        for (Measure measure : this.measures) {
            out.addAll(measure.getNotes());
        }
        return out;
    }

    @Override
    public void setNotes(ArrayList<Note> notes) { }


}
