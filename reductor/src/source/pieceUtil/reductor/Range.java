package reductor;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public final class Range implements Comparable<Range>, Ranged {

    /// This range's low/lesser/left endpoint.
    private final long low;

    /// This range's high/greater/right endpoint.
    private final long high;

    /// Ranges must be constructed such that the low endpoint < high endpoint and 0 < low endpoint.
    Range(long low, long high) {

        if (high <= low) {
            throw new IllegalArgumentException("invalid range: [" + low + ", " + high + "]");
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
        this(0, 1);
    }


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
    public boolean containsEntirely(Range other) {
        return this.low <= other.low  &&  other.high <= this.high;
    }

    /// This is equivalent to `this.compareTo(other) == 0`
    public boolean alignsWith(Range other) {
        return this.compareTo(other) == 0;
    }

    /// Compares by low endpoint; ties are broken by the high endpoint.
    @Override
    public int compareTo(Range other) {
        return other.low != this.low ?
                Long.compare(this.low, other.low)
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

    /// Returns this range as a string in the form of "\[low, high]".
    @Override
    public String toString() {
        return "[" + this.low + ", " + this.high + "]";
    }

    /// Returns itself
    @Override
    public Range getRange() { return this; }


    /// Return a copy of the range shifted by an offset.
    public static Range getShiftedInstance(Range other, long offset) {

        if (offset < 0) {
            return new Range(
                    Math.max(other.low + offset, 0),
                    Math.max(other.high + offset, 1)
            );
        } else if (offset > 0) {
            return new Range(
                    Math.min(other.low + offset, Context.lastTick() - 1),
                    Math.min(other.high + offset, Context.lastTick())
            );
        } else {
            return new Range(other);
        }

    }

    /// Given multiple ranges, constructs a single range encompassing them.
    public static Range concatenate(List<Range> ranges) {
        if (ranges.isEmpty()) { throw new RuntimeException("can't concatenate empty list"); }
        ArrayList<Range> copy = new ArrayList<>(ranges);
        copy.sort(null);
        return new Range(copy.getFirst().low, copy.getLast().high);
    }


}