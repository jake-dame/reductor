package reductor.core.builders;

import reductor.app.Application;
import reductor.core.*;
import reductor.util.IntervalTree;
import reductor.util.TimeUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


@SuppressWarnings("FieldMayBeFinal")
public class PieceBuilder {

    private int resolution;

    private List<TimeSignature> timeSigsList;
    private List<KeySignature> keySigsList;
    private List<Tempo> temposList;

    private List<Measure> measuresList;

    private List<Note> notesList;


    public PieceBuilder(int resolution) {
        if (resolution < 1 || 32767 < resolution) {
            throw new IllegalArgumentException("valid resolutions are in [1,32767]");
        }
        this.resolution = resolution;
        timeSigsList = new ArrayList<>();
        keySigsList = new ArrayList<>();
        temposList = new ArrayList<>();
        measuresList = new ArrayList<>();
        notesList = new ArrayList<>();
    }
    public static PieceBuilder builder(int resolution) {
        return new PieceBuilder(resolution);
    }


    // region builder methods

    public PieceBuilder measure(Measure v) {
        this.measuresList.add(v);
        return this;
    }
    public PieceBuilder measure(List<Measure> v) {
        v.forEach(this::measure);
        return this;
    }

    public PieceBuilder note(Note v) {
        this.notesList.add(v);
        return this;
    }
    public PieceBuilder note(List<Note> v) {
        v.forEach(this::note);
        return this;
    }

    public PieceBuilder timeSignature(TimeSignature v) {
        this.timeSigsList.add(v);
        return this;
    }
    public PieceBuilder timeSignature(List<TimeSignature> v) {
        v.forEach(this::timeSignature);
        return this;
    }

    public PieceBuilder keySignature(KeySignature v) {
        this.keySigsList.add(v);
        return this;
    }
    public PieceBuilder keySignature(List<KeySignature> v) {
        v.forEach(this::keySignature);
        return this;
    }

    public PieceBuilder tempo(Tempo v) {
        this.temposList.add(v);
        return this;
    }
    public PieceBuilder tempo(List<Tempo> v) {
        v.forEach(this::tempo);
        return this;
    }

    //endregion


    //region PieceAssembler

    private static class PieceAssembler {

        private int resolution;

        private Range range;

        private IntervalTree<Note> notes;
        private IntervalTree<Measure> measures;
        private IntervalTree<Column> columns;

        private IntervalTree<TimeSignature> timeSigs;
        private IntervalTree<KeySignature> keySigs;
        private IntervalTree<Tempo> tempos;


        private PieceAssembler(int resolution, List<Note> notes, List<Measure> measures, List<TimeSignature> timeSigs,
                               List<KeySignature> keySigs, List<Tempo> tempos) {
            this.resolution = resolution;
            this.notes = new IntervalTree<>(notes);
            this.range = findPieceRange();

            if (timeSigs.isEmpty()) {
                timeSigs.add(new TimeSignature(4, 4, range));
                System.err.println("constructing piece without timeSig");
            }

            this.timeSigs = new IntervalTree<>(timeSigs);
            this.keySigs = new IntervalTree<>(keySigs);
            this.tempos = new IntervalTree<>(tempos);
            this.columns = assembleColumns();
            this.measures = assembleMeasures();
        }

        private Range findPieceRange() {
            int minTick = 0;
            int maxTick = this.notes.getLastTick();
            return new Range(minTick, maxTick + 1);
        }

        public TimeSignature getTimeSigAt(int point) {
            return this.timeSigs.query(point).getFirst();
        }
        public KeySignature getKeySigAt(int point) {
            return this.keySigs.query(point).getFirst();
        }
        public Tempo getTempoAt(int point) {
            return this.tempos.query(point).getFirst();
        }

        private IntervalTree<Column> assembleColumns() {
            List<Column> columns = new ArrayList<>();
            for (Range range : computeColumnRanges()) {
                List<Note> matches = this.notes.query(range);
                columns.add( new Column(matches, range) );
            }
            return new IntervalTree<>(columns);
        }
        private List<Range> computeColumnRanges() {
            List<Range> list = this.notes.toListRangesOnly();
            Set<Integer> set = new HashSet<>();
            for (Range range : list) {
                set.add(range.getLow());
            }
            return RangeUtil.fromStartTicks(set, this.range.getHigh());
        }

        private IntervalTree<Measure> assembleMeasures() {
            List<Measure> measures = new ArrayList<>();
            for (Range range : computeMeasureRanges(this.notes, this.range.getHigh())) {
                List<Column> matches = this.columns.query(range);
                measures.add(new Measure(matches, range, getTimeSigAt(range.getLow()),
                        getKeySigAt(range.getLow()), getTempoAt(range.getLow()))
                );
            }
            return new IntervalTree<>(measures);
        }
        private List<Range> computeMeasureRanges(IntervalTree<Note> notes, int lastTick) {
            Set<Integer> startTicks = new HashSet<>();
            int marker = this.range.getLow();
            while (marker < this.range.getHigh()) {
                startTicks.add(marker);
                marker += TimeUtil.calculateMeasureDuration(this.resolution, getTimeSigAt(marker));
            }
            return RangeUtil.fromStartTicks(startTicks, this.range.getHigh());
        }

        private static class MeasureAssigner {

            private boolean hasPickup;
            private final List<Measure> measures;

            private MeasureAssigner(IntervalTree<Measure> measures) {
                this.measures = measures.toList();
                hasPickup = this.assignPickup();
                this.assignMeasureNumbers();
            }

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
                        && firstTimeSig.denominator() + lastTimeSig.numerator()  ==  firstTimeSig.numerator();

                // Right now, this is exactly an eighth rest (half of the value of a quarter)
                final long THRESHOLD = (long) (Application.resolution * 0.5);
                long amountOfRest = Math.abs(firstMeasure.getRange().getLow() - firstMeasure.getColumn(0).getRange().getLow());
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
                if (hasPickup) {
                    measureNumber--;
                }
                for (Measure m : measures) {
                    m.setMeasureNumber(measureNumber++);
                }
            }

        }


    }

    //endregion


    //region build

    public Piece build() {

        var assembler = new PieceAssembler(this.resolution, this.notesList, this.measuresList,
                this.timeSigsList, this.keySigsList, this.temposList);

        return new Piece(
                assembler.resolution, assembler.range, assembler.notes, assembler.timeSigs,
                assembler.keySigs, assembler.tempos, assembler.measures, assembler.columns
        );
    }

    //endregion

    public static PieceBuilder from(Range range) {
        return new PieceBuilder(480)
                .keySignature(new KeySignature("C", range))
                .timeSignature(new TimeSignature(4, 4, range));
                //.tempo(120, range);
    }


}
