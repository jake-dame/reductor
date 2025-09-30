package reductor.piece;


import java.util.*;


public class Range implements Ranged, Comparable<Range> {

    /** Lower/lesser/left endpoint of this Range. */
    private final long low;

    /** Upper/greater/right endpoint of this Range. */
    private final long high;

    // ==========================  CONSTRUCTORS  ========================== //


    /**
     * Creates a range with the given endpoints.
     * <p>
     * Valid ranges must be {@code 0 <= low < high}.
     *
     * @param low The lower/lesser/left endpoint of the Range to be constructed.
     * @param high The upper/greater/right endpoint of the Range to be constructed.
     * @throws IllegalArgumentException if {@code low < 0} or {@code high <= low}
     */
    public Range(long low, long high) {

        if (high <= low) {
            throw new IllegalArgumentException("given an invalid range: [" + low + ", " + high + "]");
        }

        if (low < 0) {
            throw new IllegalArgumentException("range cannot be constructed with negative numbers");
        }

        this.low = low;
        this.high = high;
    }

    /**
     * Creates a deep copy of the passed Range.
     *
     * @param other The other Range object to copy.
     * @throws NullPointerException If the passed Range is null.
     * */
    public Range(Range other) {

        if (other == null) {
            throw new NullPointerException("Other Range can't be null");
        }

        this(other.low, other.high);
    }

    /**
     * Creates a default Range object representing the range {@code [0,479]}.
     * <p>
     * {@code [0,479]} is the Range for most default quarter notes.
     */
    public Range() {
        this(0, 479);
    }


    // =========================  INSTANCE METHODS ========================= //


    /** Returns this Range's low. */
    public long low() { return low; }

    /** Returns this Range's high. */
    public long high() { return high; }

    /**
     * Returns this Range's length (i.e. half-open span).
     * <p>
     * Example: for a range of {@code [0,479]}, this returns {@code 479}.
     * @see #duration()
     * */
    public long length() { return this.high - this.low; }

    /**
     * Returns this Range's length (i.e. inclusive span).
     * <p>
     * Example: for a range of {@code [0,479]}, this returns {@code 480}.
     * @see #length()
     * */
    public long duration() { return length() + 1; }

    /** Returns true if this Range overlaps the passed Range. */
    public boolean overlaps(Range other) {
        return this.low <= other.high  &&  other.low <= this.high;
    }

    /** Returns true if this Range contains the passed scalar value. */
    public boolean contains(long val) {
        return this.low <= val  &&  val <= this.high;
    }

    /** Returns true if this range fully contains or perfectly overlaps with a passed range. */
    public boolean contains(Range other) {
        return this.low <= other.low   &&  other.high <= this.high;
    }


    // ============================  OVERRIDES  ============================ //


    /**
     * Compares by low endpoint first; the high endpoint wins ties.
     *
     * @param other the Range to compare against
     * @return A negative value if this Range is lesser, {@code 0} if they are the same, and a positive
     *         value if this Range is greater.
     */
    @Override
    public int compareTo(Range other) {
        return other.low != this.low
                ? Long.compare(this.low, other.low)
                : Long.compare(this.high, other.high);
    }

    /**
     * Returns {@code true} if this range is equal to the specified object.
     * <p>
     * Ranges are equal if both their low and high endpoints match,
     * or if the passed Object is this.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (!(o instanceof Range range)) { return false; }
        return this.low == range.low  &&  this.high == range.high;
    }

    /** Returns a hash code derived from this range's endpoints. */
    @Override
    public int hashCode() {
        return Objects.hash(this.low, this.high);
    }

    /** Returns this (satisfying contract with {@link Ranged} interface). */
    @Override
    public Range getRange() { return this; }

    /** Returns a string representation of this range in the form {@code "[low, high]"}. */
    @Override
    public String toString() {
        return "[" + this.low + ", " + this.high + "]";
    }


    // =============================  STATIC  ============================= //


    /** Return a copy of the passed Range, shifted by an offset. */
    public static Range getShiftedInstance(Range other, long offset) {
        // TODO: this does not currently clamp or validate offset.
        //       clamping would require global context value for final tick
        return new Range(other.low + offset, other.high + offset);
    }

    /**
     * Returns a copy of the passed Range with both endpoints increased by the passed addend.
     * <p>
     * Acts as a {@code +=} operator of sorts, for a single Range object.
     *
     * @param range The Range to make a copy of.
     * @param addend The amount to increase each endpoint.
     */
    public static Range add(Range range, long addend) {
        return new Range(range.low + addend, range.high + addend);
    }

    /**
     * Constructs a single Range from multiple Ranges.
     * <p>
     * Example: If given {@code [0,479]}, {@code [480,959]}, {@code [960,1919]}, returns {@code [0,1919]}.
     *
     * @param rangedElems Any Collection of {@link Ranged} objects.
     * @return A single Range object encompassing all Ranges in {@code rangedElems}.
     * @param <T> Any element that implements the {@link Ranged} interface.
     */
    public static <T extends Ranged> Range concatenate(Collection<T> rangedElems) {

        if (rangedElems.isEmpty()) { throw new RuntimeException("can't concatenate empty list"); }

        long min = Long.MAX_VALUE;
        long max = Long.MIN_VALUE;
        for (T elem : rangedElems) {
            if (elem.getRange().low() < min) { min = elem.getRange().low(); }
            if (elem.getRange().high() > max) { max = elem.getRange().high(); }
        }

        return new Range(min, max);
    }

    /**
     * Splits two passed overlapping Ranges into three distinct regions.
     * <p>
     * If the Ranges do not overlap at all, just returns copies of those Ranges.
     * <pre>
     * Given:
     *     0         20
     *     |-----------|
     *           |-----------|
     *          10           30
     *
     * Returns:
     *           10  20
     *     |----||----||----|
     *     0   10      20  30
     * </pre>
     *
     * @return An ArrayList of Ranges representing the original:
     *         <ol>
     *             <li>Left-only region</li>
     *             <li>Shared region</li>
     *             <li>Right-only region</li>
     *         </ol>
     *         If no overlap exists, the list contains copies of {@code r1} and {@code r2}.
     */
    public static ArrayList<Range> splitIntoThree(Range r1, Range r2) {

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
     * Creates ranges from a set of points.
     * <p>
     * The points mark the start of each interval, and {@code lastEndpoint}
     * marks the end of the final interval.
     * <p>
     * This design avoids requiring the caller to manually append the final endpoint to the input set.
     * It also is more reusable by not requiring actual Ranges. This is how Measures, Time Signatures, etc.,
     * are created from raw MIDI data.
     *
     * @param points A set of points on the number line.
     * @param lastEndpoint The right terminus of the number line.
     * @return A list of Ranges representing the intervals between the points provided.
     * @throws IllegalArgumentException if {@code lastEndpoint <= max(points)}
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

    /**
     * Splits a single Range up by a uniform window size.
     *
     * @param range The Range to be partitioned.
     * @param windowSize The desired length of the output Ranges.
     * @return An ArrayList of Ranges, each* of length {@code windowSize}, falling within {@code range}.
     *         <br>*Remainders at the right end are handled by clamping.
     */
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

    /**
     * Overload of {@link #partition(Range, long)} that allows for a divisor specifier rather than
     * a window length specifier
     *
     * @param range The Range to be partitioned.
     * @param divisor The number of Ranges to create from {@code range}.
     */
    public static ArrayList<Range> partition(Range range, double divisor) {

        double windowSize = range.length() / divisor;

        double leftover = windowSize - ((int) windowSize);
        if (leftover != 0) {
            System.out.println("loss of " + leftover);
        }

        return partition(range, (long) windowSize);

    }


    // ===============================  DEV  =============================== //


    /** dev. This is for Phrase Builder `bookmark` member ONLY. For now. */
    Range(long low, long high, boolean hi) {
        this.low = low;
        this.high = high;
    }

    /** dev */
    public Range setLow(long val) { return new Range(val, this.high); }

    /** dev */

    public Range setHigh(long val) { return new Range(this.low, val); }

    /** dev */
    public double overlappingRegion(Range other) {
        // If they don't overlap, the number will be negative, in which case, just return 0.
        return Math.max(this.high - other.low, 0);
    }


}