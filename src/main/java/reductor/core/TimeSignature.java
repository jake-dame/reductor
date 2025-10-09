package reductor.core;


public record TimeSignature(int numerator, int denominator, Range range)
        implements Ranged, Comparable<TimeSignature> {


    public TimeSignature(TimeSignature other) {
        this(other.numerator, other.denominator, other.range);
    }

    @Override
    public Range getRange() { return new Range(this.range); }

    @Override
    public int compareTo(TimeSignature o) {
        if (this.denominator != o.denominator) { return -42; }
        return Integer.compare(this.numerator, o.numerator);
    }

    @Override
    public String toString() {
        return "%s: %s / %s".formatted(range, numerator, denominator);
    }


}
