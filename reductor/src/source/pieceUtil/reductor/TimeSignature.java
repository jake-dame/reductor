package reductor;

/*
Resolution and measure size: I decided that since Time Signature is nothing without resolution and Time Signature
ultimately determines measure size, these should all be handled and encapsulated by Time Signature.
 */
public class TimeSignature implements Ranged {

    private final int resolution;

    private final int upperNumeral;
    private final int lowerNumeral;

    private final long measureSize;

    private final Range range;


    TimeSignature(int resolution, int upperNumeral, int lowerNumeral, Range range) {
        this.resolution = resolution;
        this.upperNumeral = upperNumeral;
        this.lowerNumeral = lowerNumeral;
        this.range = new Range(range);
        this.measureSize = calculateMeasureSize(this.upperNumeral, this.lowerNumeral, this.resolution);
    }


    public int getResolution() { return this.resolution; }

    public int getUpperNumeral() { return this.upperNumeral; }
    public int getLowerNumeral() { return this.lowerNumeral; }

    public long getMeasureSize() { return this.measureSize; }

    @Override
    public Range getRange() { return new Range(this.range); }

    @Override
    public String toString() { return this.upperNumeral + "/" + this.lowerNumeral; }


    public static long calculateMeasureSize(TimeSignature timeSig) {
        return calculateMeasureSize(timeSig.getUpperNumeral(), timeSig.getLowerNumeral(), timeSig.getResolution());
    }

    public static long calculateMeasureSize(int upperNumeral, int lowerNumeral, int resolution) {

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
        float measureInTicks = upper * resolution;

        // sanity check: to make sure there is no loss (compare to int version of itself) before converting to long
        assert measureInTicks == (int) measureInTicks;

        return (long) measureInTicks;

    }


}
