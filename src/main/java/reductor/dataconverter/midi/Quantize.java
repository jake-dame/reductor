//package reductor.dataconverter;
//
//import reductor.piece.Range;
//import reductor.piece.Rhy;
//
//import java.util.*;
//
//
///**
// * A simplified MIDI quantization class with the following effects:
// * <ul>
// *     <li>Convert to 480 resolution</li>
// *     <li>Convert all ranges to be half-open. This means that if the MIDI in-file encoded NOTE ON/OFF events as
// *     overlapping (e.g. 0/480, 480/960 instead of 0/479, 480/959), they will be adjusted
// *     </li>
// *     <li>Snap every note duration to a fine granularity of 128th-note value (i.e. 14 ticks in length) </li>
// * </ul>
// * <p>
// * If <i>too</i> much quantization occurs, or the end result is irregular, a warning will be outputted that indicates
// * the file may not be a good candidate for this program.
// * </p>
// * */
//public class Quantize {
//
//    // Least Common Denominator before fractional ticks after converting to resolution of 480
//    private static final double LCD = 15;
//
//    private final ArrayList<QRange> scaledRanges;
//    private final ArrayList<QRange> quantizedRanges;
//
//    private final double scalingFactor;
//
//    /**
//     * Quantize is an internal class, and all ranges passed to this class will be valid MIDI ranges, so those checks
//     * are omitted here to avoid redundancy.
//     * */
//    Quantize(int tpq, ArrayList<Range> ranges) {
//
//        this.scalingFactor = 480.0 / tpq;
//
//        this.scaledRanges = convertTo480Resolution(ranges);
//        scaledRanges.sort(null);
//
//        this.quantizedRanges = quantize();
//    }
//
//    public static ArrayList<Range> quantize(int TPQ, ArrayList<Range> ranges) {
//
//        if (ranges == null) { throw new NullPointerException("range list is null or empty"); }
//        if (TPQ < 1) { throw new IllegalArgumentException("invalid TPQ: " + TPQ); }
//
//        Quantize q = new Quantize(TPQ, ranges);
//
//        return q.getQuantizedRanges();
//    }
//
//    private ArrayList<QRange> convertTo480Resolution(ArrayList<Range> ranges) {
//
//        ArrayList<QRange> out = new ArrayList<>();
//
//        for (Range range : ranges) {
//
//            QRange scaledRange = new QRange(
//                    range.low() * scalingFactor,
//                    range.high() * scalingFactor
//            );
//
//            out.add(scaledRange);
//        }
//
//        return out;
//    }
//
//    private ArrayList<QRange> quantize() {
//
//        ArrayList<QRange> out = new ArrayList<>();
//
//        for (QRange range : scaledRanges) {
//
//            QRange quantizedRange = quantizeRange(range);
//            out.add(quantizedRange);
//        }
//
//        return out;
//    }
//
//    private QRange quantizeRange(QRange range) {
//
//        Rhy rhy = Rhy.type(range.duration());
//        double gridWindowSize = rhy.base / rhy.divisor;
//        QRange gridWindow = new QRange(0.0, gridWindowSize - 1.0);
//
//        final double tolerance = 4.0;
//        while (gridWindow.overlappingRegion(range) < tolerance) { gridWindow.increment(gridWindow.duration() + 1); }
//
//        range.start = gridWindow.start;
//        range.stop = range.start + rhy.duration - 1;
//
//        return range;
//    }
//
//    /**
//     * The public API method for this class.
//     * */
//    public ArrayList<Range> getQuantizedRanges() {
//
//        ArrayList<Range> out = new ArrayList<>();
//
//        for (QRange qrange : this.quantizedRanges) {
//            out.add( new Range(
//                    (long) qrange.start,
//                    (long) qrange.stop)
//            );
//        }
//
//        return out;
//    }
//
//    /**
//     * QRange is just a representation of a range that is internal to the Quantize class, and is easier to work with
//     * for this task than {@linkplain reductor.piece.Range}. It is intended to be temporary/short-lived, mutable, and
//     * the low / high, or start / stop, members are doubles instead of longs as in the Range class. They are easily
//     * convertible to actual Ranges at the end of the process.
//     * */
//    public static class QRange implements Comparable<QRange> {
//
//        double start;
//        double stop;
//
//        public QRange(double start, double stop) {
//            this.start = start;
//            this.stop = stop;
//        }
//
//        QRange(QRange range) {
//            this.start = range.start;
//            this.stop = range.stop;
//        }
//
//        /**
//         * This represents the inclusive length of the range.
//         * */
//        public double duration() {
//            return stop - start;
//        }
//
//        boolean overlaps(QRange other) {
//            return this.start <= other.stop  &&  other.start <= this.stop;
//        }
//
//        void increment(double offset) {
//            this.start += offset;
//            this.stop += offset;
//        }
//
//        /**
//         * Returns the length of the overlapping, if any, region between two ranges.
//         * */
//        double overlappingRegion(QRange other) {
//            // If they don't overlap, the number will be negative, in which case, just return 0.
//            return Math.max(this.stop - other.start, 0);
//        }
//
//        /**
//         * Primarily ordered by start tick; secondarily ordered by stop tick.
//         * */
//        @Override
//        public int compareTo(QRange other) {
//            return other.start != this.start
//                    ? Double.compare(this.start, other.start)
//                    : Double.compare(this.stop, other.stop);
//        }
//
//        @Override
//        public String toString() {
//            return String.format("[%f,%f]", this.start, this.stop);
//        }
//
//    }
//
//}

package reductor.dataconverter.midi;

import reductor.piece.Range;
import reductor.piece.Rhy;

import java.util.*;


public class Quantize {


    public static Range quantize(Range inRange, int tpq) {

        long scale = 480 / tpq;

        long scaledLow = inRange.low() * scale;
        long scaledHigh = inRange.high() * scale;

        Range range = new Range(scaledLow, scaledHigh);

        Rhy rhy = Rhy.type(range.high() - range.low());

        double gridWindowSize = rhy.base / rhy.divisor;
        Range gridWindow = new Range(0, (long) (gridWindowSize - 1));

        final long tolerance = 4;
        while (gridWindow.overlappingRegion(range) < tolerance) {
            gridWindow = Range.getShiftedInstance(gridWindow, gridWindow.duration());
        }

        range = range.setLow(gridWindow.low());
        range = range.setHigh((long) (range.low() + rhy.duration - 1));

        return range;
    }

    public static ArrayList<Range> quantize(ArrayList<Range> ranges, int tpq) {

        ArrayList<Range> out = new ArrayList<>();

        for (Range range : ranges) {
            Range quantizedRange = quantize(range, tpq);
            out.add(quantizedRange);
        }

        return out;
    }

}