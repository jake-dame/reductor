package reductor;

import java.util.*;

// TODO: best practice for default constructor?? null? 0,1?

// TODO: if low + high are final numerical primitives...can't I just expose
//  them directly, like how Array.length is always an exposed field?


public class Range implements Ranged, Comparable<Range> {

    /// This range's low/lesser/left endpoint.
    private final long low;

    /// This range's high/greater/right endpoint.
    private final long high;


    /// Primary constructor.
    /// Ranges must be constructed such that the low endpoint < high endpoint and 0 < low endpoint.
    Range(long low, long high) {

        if (high <= low) {
            throw new IllegalArgumentException("given an invalid range: [" + low + ", " + high + "]");
        }

        if (low < 0) {
            throw new IllegalArgumentException("range cannot be constructed with negative numbers");
        }

        this.low = low;
        this.high = high;
    }

    /// Copy constructor
    Range(Range other) {
        this(other.low, other.high);
    }

    /// Default constructor
    Range() {
        this(0, 480);
    }


    /* ================
     * INSTANCE METHODS
     * ============= */


    /// Returns this range's low.
    public long low() { return low; }

    /// Returns this range's high.
    public long high() { return high; }

    /// Returns this range's (half-open) length.
    public long length() { return this.high - this.low; }

    /// Returns true if this range overlaps another.
    public boolean overlaps(Range other) {
        return this.low <= other.high  &&  other.low <= this.high;
    }

    /// Returns true if this range contains the passed value.
    public boolean contains(long val) {
        return this.low <= val  &&  val <= this.high;
    }

    /// Returns true if this range fully contains or perfectly overlaps with a passed range.
    public boolean contains(Range other) {
        return this.low <= other.low   &&  other.high <= this.high;
    }


    /* =========
     * OVERRIDES
     * ====== */


    /// Compares by low endpoint; ties are broken by the high endpoint.
    @Override
    public int compareTo(Range other) {
        return other.low != this.low
                ? Long.compare(this.low, other.low)
                : Long.compare(this.high, other.high);
    }

    /// Returns true is this range is to be considered equal to another object.
    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (!(o instanceof Range range)) { return false; }
        return this.low == range.low  &&  this.high == range.high;
    }

    /// Returns a unique hash value based on this range's properties;
    @Override
    public int hashCode() {
        return Objects.hash(this.low, this.high);
    }

    /// Returns itself (so that it conforms with the Ranged interface)
    @Override
    public Range getRange() { return this; }

    /// Returns this range as a string in the form of "\[low, high]".
    @Override
    public String toString() {
        return "[" + this.low + ", " + this.high + "]";
    }


    /* ======================
     * STATIC UTILITY METHODS
     * =================== */


    /// Return a copy of the range shifted by an offset.
    public static Range getShiftedInstance(Range other, long offset) {

        if (offset < 0) {
            return new Range(
                    Math.max(other.low + offset, 0),
                    Math.max(other.high + offset, 1)
            );
        } else if (offset > 0) {
            return new Range(
                    Math.min(other.low + offset, Context.finalTick() - 1),
                    Math.min(other.high + offset, Context.finalTick())
            );
        } else {
            return new Range(other);
        }

    }

    /// If Java had operator overloading, this would be equivalent to: newRange += 480
    /// Except also makes a new instance because Ranges are immutable
    public static Range add(Range range, long addend) {
        return new Range(range.low + addend, range.high + addend);
    }

    /// Given multiple ranges, constructs a single range encompassing them.
    public static <T extends Ranged> Range concatenate(List<T> rangedElems) {
        if (rangedElems.isEmpty()) { throw new RuntimeException("can't concatenate empty list"); }

        long min = Long.MAX_VALUE;
        long max = Long.MIN_VALUE;
        for (T elem : rangedElems) {
            if (elem.getRange().low() < min) { min = elem.getRange().low(); }
            if (elem.getRange().high() > max) { max = elem.getRange().high(); }
        }

        return new Range(min, max);
    }

    /// Given two overlapping ranges, outputs three Range instances of: left, overlapping, and right regions.
    /// If the two ranges don't overlap, just returns two ranges identical to the input ranges.
    public static ArrayList<Range> splitOverlapping(Range r1, Range r2) {

        /*
            Given:
                0         20
                |-----------|
                      |-----------|
                     10           30

            Returns:
                      10  20
                |----||----||----|
                0   10      20  30
        */

        if (!r1.overlaps(r2)) {
            return new ArrayList<>( List.of(new Range(r1), new Range(r2)) );
        }

        // Find the rightmost left endpoint
        long midLeft = Math.max(r1.low(), r2.low());

        // Find the leftmost right endpoint.
        long midRight = Math.min(r1.high(), r2.high());

        Range leftRange = new Range(r1.low(), midLeft);
        Range middleRange = new Range(midLeft, midRight);
        Range rightRange = new Range(midRight, r2.high());

        return new ArrayList<>( List.of (leftRange, middleRange, rightRange) );
    }

    /**
     * Given a bunch of points on the number line, orders them and then creates ranges representing the intervals
     * between those points. You must provide the end of the number line (i.e. right endpoint of the last interval).
     * <p>
     * This could have been done several ways, including accepting a list of Ranges (less reusable) and
     * not asking for the last endpoint (this would put the onus on the user to manually add that to the set of
     * points before passing it to this function).
     *
     * @param points A set of points on the number line.
     * @param lastEndpoint Where the number line should end.
     * @return A list of {@link Range} representing the intervals between the points provided.
     */
    public static ArrayList<Range> fromStartTicks(Set<Long> points, long lastEndpoint) {

        if (points == null  ||  points.isEmpty()) { return new ArrayList<>(); }

        ArrayList<Long> pointsCopy = new ArrayList<>(points);

        // Sort ascending.
        pointsCopy.sort(null);

        if (lastEndpoint <= pointsCopy.getLast()) {
            throw new IllegalArgumentException("last endpoint should be greater than last point");
        }

        ArrayList<Range> out = new ArrayList<>();

        int size = pointsCopy.size();
        for (int i = 0; i < size; i++) {
            long tick = pointsCopy.get(i);
            long nextTick = i < size - 1 ? pointsCopy.get(i+1) : lastEndpoint;
            Range range = new Range(tick, nextTick - 1);
            out.add(range);
        }

        out.sort(null); // temporary

        return out;
    }

    /// Get ranges: provide window size
    public static ArrayList<Range> partition(Range range, long windowSize) {

        ArrayList<Range> out = new ArrayList<>();

        long windowMin = range.low();
        long windowMax = windowMin + windowSize;

        long length = range.high();

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
    public static ArrayList<Range> partition(Range range, double divisor) {

        double windowSize = range.length() / divisor;

        // TODO: refine this and make decisions
        double leftover = windowSize - ((int) windowSize);
        if (leftover != 0) {
            System.out.println("loss of " + leftover);
        }

        return partition(range, (long) windowSize);

    }


}