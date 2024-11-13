package reductor;

import java.util.ArrayList;

// TODO: see Main comment concerning Buckets

public class Measure implements Ranged {

    private Notes notes;
    private int measureNumber;
    private final TimeSignature timeSig;
    private boolean isPickup;
    private Range range;

    Measure(Range range, int measureNumber, TimeSignature timeSignature, Notes notes) {

        assert notes != null; // TODO: remove once refactor is done

        this.notes = notes;
        this.range = range;
        this.timeSig = timeSignature;
        this.isPickup = false;
        this.measureNumber = measureNumber;
    }

    public int size() { return this.notes.size(); }
    public long length() { return this.getRange().length(); }
    public TimeSignature getTimeSignature() { return this.timeSig; } // TODO: deep copy?

    public boolean isPickup() { return this.isPickup; }
    public void setIsPickup(boolean val) { this.isPickup = val; }

    public Notes getNotes() { return this.notes; }
    public void setNotes(Notes notes) { this.notes = notes; }

    public int getMeasureNumber() { return measureNumber; }
    public void setMeasureNumber(int measureNumber) { this.measureNumber = measureNumber; }

    @Override
    public Range getRange() { return new Range(this.range); }

    @Override
    public String toString() {
        return this.range + ": " + this.timeSig + " and " + this.size() + " notes";
    }


    public static long getMeasureSize(TimeSignature timeSig) {
        return getMeasureSize(timeSig.getUpperNumeral(), timeSig.getLowerNumeral(), timeSig.getResolution());
    }

    private static long getMeasureSize(int upperNumeral, int lowerNumeral, int resolution) {

        // These need to be floats for stuff like 3/8 or 7/8
        float upper = (float) upperNumeral;
        float lower = (float) lowerNumeral;

        assert lower % 2 == 0; // will handle the day I come across an odd denominator
        assert lower >= 1; // sanity check prevent divide by 0

        // Get lower numeral to be in terms of quarter notes (4)
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

    @Override
    public long start() {
        return this.range.getLow();
    }

    @Override
    public void setRange(Range range) {
        this.range = range;
    }



}
