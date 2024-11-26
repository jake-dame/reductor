package reductor.piece;


/*
Resolution and measure size: I decided that since Time Signature is nothing without resolution and Time Signature
ultimately determines measure size, these should all be handled and encapsulated by Time Signature.
 */
public class TimeSignature implements Ranged, Comparable<TimeSignature> {

    private final int numerator;
    private final int denominator;

    private final long measureSize;

    private final Range range;


    public TimeSignature(int numerator, int denominator, Range range) {
        this.numerator = numerator;
        this.denominator = denominator;
        this.range = new Range(range);
        this.measureSize = calculateMeasureSize(this.numerator, this.denominator);
    }

    TimeSignature(TimeSignature other) {
        this.numerator = other.numerator;
        this.denominator = other.denominator;
        this.range = new Range(other.range);
        this.measureSize = other.measureSize;
    }


    public int getNumerator() { return this.numerator; }
    public int getDenominator() { return this.denominator; }

    public long getMeasureSize() { return this.measureSize; }

    @Override
    public Range getRange() { return new Range(this.range); }

    @Override
    public String toString() { return this.numerator + "/" + this.denominator; }


    public static long calculateMeasureSize(TimeSignature timeSig) {
        return calculateMeasureSize(timeSig.getNumerator(), timeSig.getDenominator());
    }

    public static long calculateMeasureSize(int upperNumeral, int lowerNumeral) {

        // These need to be floats for stuff like 3/8 or 7/8
        float upper = (float) upperNumeral;
        float lower = (float) lowerNumeral;

        assert lower % 2 == 0; // will handle the day I come across an odd denominator
        assert lower >= 1; // sanity check prevent divide by 0

        // Get lower numeral to be in terms of quarter noteList (4)
        while (lower != 4) {

            if (lower > 4) {
                // e.g. 3/8 --> 1.5/4
                upper /= 2;
                lower /= 2;
            } else if (lower < 4) {
                // e.g. 2/2 --> 4/4
                upper *= 2;
                lower *= 2;
            }

        }

        // Quarters per measure * ticks per quarter
        float measureInTicks = upper * Piece.TPQ;

        // sanity check: to make sure there is no loss (compare to int version of itself) before converting to long
        assert measureInTicks == (int) measureInTicks;

        return (long) measureInTicks;

    }

    @Override
    public int compareTo(TimeSignature o) {
        if (this.denominator != o.denominator) { return -42; }
        return Integer.compare(this.numerator, o.numerator);
    }


}
