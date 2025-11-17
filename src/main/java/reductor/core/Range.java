package reductor.core;


import java.util.*;


public class Range implements Ranged, Comparable<Range> {

    /** Lower/lesser/left endpoint of this Range. */
    private final int low;

    /** Upper/greater/right endpoint of this Range. */
    private final int high;


    /**
     * Creates a range with the given endpoints.
     * <p>
     * Valid ranges must be {@code 0 <= low < high}.
     *
     * @param low The lower/lesser/left endpoint of the Range to be constructed.
     * @param high The upper/greater/right endpoint of the Range to be constructed.
     * @throws IllegalArgumentException if {@code low < 0} or {@code high <= low}
     */
    public Range(int low, int high) {
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
        if (other == null) { throw new NullPointerException("Other Range can't be null"); }
        this(other.low, other.high);
    }

    /**
     * Creates a default Range object representing the range {@code [0,479]}.
     * <p>
     * {@code [0,479]} is the Range for most default quarter notes.
     */
    public Range() { this(0, 479); }


    public int getLow() { return low; }

    public int getHigh() { return high; }

    /**
     * Returns this Range's length (i.e. half-open span).
     * <p>
     * Example: for a range of {@code [0,479]}, this returns {@code 479}.
     * @see #duration()
     * */
    public int length() { return this.high - this.low; }

    /**
     * Returns this Range's length (i.e. inclusive span).
     * <p>
     * Example: for a range of {@code [0,479]}, this returns {@code 480}.
     * @see #length()
     * */
    public int duration() { return length() + 1; }

    /** Returns true if this Range overlaps the passed Range. */
    public boolean overlaps(Range other) { return this.low <= other.high && other.low <= this.high; }

    /** Returns true if this Range contains the passed point. */
    public boolean contains(int val) { return this.low <= val && val <= this.high; }
    /** Returns true if this range fully contains or perfectly overlaps with a passed range. */
    public boolean contains(Range other) { return this.low <= other.low && other.high <= this.high; }

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
        return this.low == range.low && this.high == range.high;
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
    public String toString() { return "[%d, %d]".formatted(this.low, this.high); }


    /** dev */
    public Range setLow(int val) { return new Range(val, this.high); }
    /** dev */
    public Range setHigh(int val) { return new Range(this.low, val); }
    /** dev */
    public double overlappingRegionLength(Range other) {
        // If they don't overlap, the number will be negative, in which case, just return 0.
        return Math.max(this.high - other.low, 0);
    }


}
