package reductor.core;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class Piece implements Ranged, Noted {

    public static int TPQ = 480;

    /// This Piece's range (start and end ticks)
    private final Range range;

    /// All this Piece's notes
    private final IntervalTree<Note> notes;

    /// All this Piece's notes partitioned into columns
    final IntervalTree<Column> columns;

    final IntervalTree<Measure> measures;
    public final MeasuresAccessor ma; // TODO not this

    private final IntervalTree<TimeSignature> timeSigs;
    private final IntervalTree<KeySignature> keySigs;
    private final IntervalTree<Tempo> tempos;

    Piece(
            ArrayList<Note> notes,
            ArrayList<TimeSignature> timeSignatures,
            ArrayList<KeySignature> keySignatures,
            ArrayList<Tempo> tempos,
            int ticksPerQuarter
    ) {

        Piece.TPQ = ticksPerQuarter;

        this.notes = new IntervalTree<>(notes);

        this.range = this.findPieceRange();

        this.timeSigs = new IntervalTree<>(timeSignatures);

        // Default to C major for the whole piece if no key sig data was given
        if (keySignatures.isEmpty()) { keySignatures = new ArrayList<>( List.of( new KeySignature("C", this.range)) ); }

        this.keySigs = new IntervalTree<>(keySignatures);
        this.tempos = new IntervalTree<>(tempos);

        this.columns = new IntervalTree<>(this.createColumns());

        this.measures = new IntervalTree<>(this.createMeasures());

        this.ma = new MeasuresAccessor();
        var list = measures.toList();
        System.out.println();
    }


    /* ====================
       CONSTRUCTION HELPERS
     * ==================== */


    /**
     * minTick should ALWAYS be 0. Not the tick of the first event. Otherwise, measure creation is absolutely
     * impossible.
     * */
    public Range findPieceRange() {
        long minTick = 0;
        long maxTick = this.notes.getLastTick();
        return new Range(minTick, maxTick + 1);
    }

    public ArrayList<Column> createColumns() {

        ArrayList<Column> out = new ArrayList<>();

        for (Range range : getColumnRanges()) {
            ArrayList<Note> matches = this.notes.query(range);
            out.add( new Column(matches, range) );
        }

        return out;
    }

    public ArrayList<Measure> createMeasures() {
        ArrayList<Measure> measures = new ArrayList<>();

        for (Range range : getMeasureRanges()) {
            ArrayList<Column> matches = this.columns.query(range);
            measures.add(new Measure(
                    matches,
                    range,
                    getTimeSigAt(range.low()),
                    getKeySigAt(range.low()),
                    getTempoAt(range.low()))
            );
        }

        return measures;
    }

    public ArrayList<Range> getColumnRanges() {

        ArrayList<Range> list = this.notes.toListOfRanges();

        Set<Long> set = new HashSet<>();
        for (Range range : list) { set.add(range.low()); }

        return Range.fromStartTicks(set, this.range.high());
    }

    public ArrayList<Range> getMeasureRanges() {

        Set<Long> startTicks = new HashSet<>();

        long marker = this.range.low();
        while (marker < this.range.high()) {
            startTicks.add(marker);
            marker += TimeSignature.calculateMeasureSize(getTimeSigAt(marker));
        }

        return Range.fromStartTicks(startTicks, this.range.high());
    }

    public TimeSignature getTimeSigAt(long point) { return timeSigs.query(point).getFirst(); }
    public KeySignature getKeySigAt(long point) { return keySigs.query(point).getFirst(); }
    public Tempo getTempoAt(long point) { return tempos.query(point).getFirst(); }


    /* ================
       INSTANCE METHODS
     * ================ */


    /**
     * Because there can be multiple tempi in a piece, you cannot just provide a bpm. You must scale every tempo
     * up/down together.
     *
     * @param scale A number like 1.5 (to up the tempo by 50%).
     */
    public void scaleTempo(float scale) {
        for (Tempo tempo : this.tempos.toList()) {
            int newBpm = (int) (tempo.getBpm() * scale);
            tempo.setBpm(newBpm);
        }
    }


    /* =======
       GETTERS
     * ======= */


    public ArrayList<Column> getColumns() { return new ArrayList<>( this.columns.toList() ); }
    public ArrayList<Measure> getMeasures() { return new ArrayList<>( this.measures.toList() ); }
    public ArrayList<TimeSignature> getTimeSignatures() { return new ArrayList<>(this.timeSigs.toList()); }
    public ArrayList<KeySignature> getKeySignatures() { return new ArrayList<>(this.keySigs.toList()); }
    public ArrayList<Tempo> getTempos() { return new ArrayList<>(this.tempos.toList()); }
    public long getTPQ() { return Piece.TPQ; }


    /* =========
       OVERRIDES
     * ========= */


    @Override
    public Range getRange() { return new Range(this.range); }

    @Override
    public ArrayList<Note> getNotes() { return new ArrayList<>(this.notes.toList()); }


    /// A non-static inner class of Piece that handles some of the complex logic with accessing measures, as well as
    /// processing certain properties of measures as a whole (like pickup assignment and measure numbering)
    public class MeasuresAccessor {

        boolean hasPickup;
        private final ArrayList<Measure> measures;

        private MeasuresAccessor() {
            this.measures = Piece.this.measures.toList();
            hasPickup = this.assignPickup();
            this.assignMeasureNumbers();
        }

        /* ==========
           PROCESSING
         * ========== */


        private boolean assignPickup() {

            if (measures.isEmpty()  ||  measures.size() == 1) { return false; }
            
            int i = 0;
            Measure firstMeasure = measures.get(i);
            while (firstMeasure.isEmpty()) {
                i++;
                firstMeasure = measures.get(i);
            }
            Measure secondMeasure = measures.get(i + 1);

            i = measures.size() - 1;
            Measure lastMeasure = measures.get(i);
            while (lastMeasure.isEmpty()) {
                i--;
                lastMeasure = measures.get(i);
            }
            Measure penultimateMeasure = measures.get(i - 1);

            TimeSignature firstTimeSig = firstMeasure.getTimeSignature();
            TimeSignature secondTimeSig = secondMeasure.getTimeSignature();

            TimeSignature penultimateTimeSig = penultimateMeasure.getTimeSignature();
            TimeSignature lastTimeSig = lastMeasure.getTimeSignature();

            boolean heuristic1 = firstTimeSig.compareTo(secondTimeSig) < 0;

            boolean heuristic2 = lastTimeSig.compareTo(penultimateTimeSig) < 0
                    && firstTimeSig.getDenominator() + lastTimeSig.getNumerator()  ==  firstTimeSig.getNumerator();

            // Right now, this is exactly an eighth rest (half of the value of a quarter)
            final long THRESHOLD = (long) (TPQ * 0.5);
            long amountOfRest = Math.abs(firstMeasure.getRange().low() - firstMeasure.getColumn(0).getRange().low());
            boolean heuristic3 = firstTimeSig.compareTo(secondTimeSig) == 0
                    &&  THRESHOLD < amountOfRest;

            if (heuristic1 || heuristic2 || heuristic3) {
                measures.getFirst().setIsPickup(true);
                return true;
            }

            return false;
        }

        private void assignMeasureNumbers() {

            int measureNumber = 1;

            if (hasPickup) { measureNumber--; }

            for (Measure m : measures) {
                m.setMeasureNumber(measureNumber++);
            }

        }


        /* =======
           GETTERS
         * ======= */


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

        public int getNumMeasures() {
            return hasPickup ? measures.size() - 1 : measures.size();
        }

        public Measure getMeasure(int val) throws RuntimeException {
            return measures.get( byMeasureNumbering(val) );
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
            if (n == 1) { return new ArrayList<>( List.of(getFirstMeasure()) ); }
            return getMeasures(1, n);
        }

        public ArrayList<Measure> getLastNMeasures(int n) {
            if ( n < getNumMeasures()) { n = getNumMeasures(); } // Clamp.
            return getMeasures(this.getNumMeasures() - n, this.getNumMeasures());
        }


    }



    // DEV

    public static Piece fromPhrase(ArrayList<Ranged> phrase, int TPQ, String name) {

        ArrayList<Note> notes = new ArrayList<>();
        ArrayList<TimeSignature> timeSignatures = new ArrayList<>();
        ArrayList<KeySignature> keySignatures  = new ArrayList<>();
        ArrayList<Tempo> tempos = new ArrayList<>();

        for (Ranged elem : phrase) {

            switch(elem) {
                case TimeSignature ts -> timeSignatures.add(ts);
                case KeySignature ks -> keySignatures.add(ks);
                case Tempo t -> tempos.add(t);
                case Note n -> notes.add(n);
                default -> {}
            }

        }

        return new Piece(
                notes,
                timeSignatures,
                keySignatures,
                tempos,
                TPQ
        );

    }


}