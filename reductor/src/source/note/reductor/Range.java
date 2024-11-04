package reductor;

import java.util.Objects;


public class Range implements Comparable<Range>, Ranged {


    private final long low;
    private final long high;


    Range(long low, long high) {

        if (high <= low) {
            throw new IllegalArgumentException("invalid range (high is <= low)");
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


    public boolean overlaps(Range other) {

        if (other.low > this.high) {
            return false;
        }

        if (other.high < this.low) {
            return false;
        }

        return true;

    }


    @Override
    public int compareTo(Range other) {

        if (other.low != this.low) {
            return Long.compare(this.low, other.low);
        }

        return Long.compare(this.high, other.high);

    }


    @Override
    public boolean equals(Object other) {

        if (this == other) {
            return true;
        }

        if (!(other instanceof Range range)) {
            return false;
        }

        return low == range.low && high == range.high;

    }

    @Override
    public int hashCode() {
        return Objects.hash(low, high);
    }

    @Override
    public String toString() {
        return "[" + low + ", " + high + "]";
    }

    @Override
    public Range range() {
        return this;
    }

    public long low() {
        return low;
    }

    public long high() {
        return high;
    }


}
