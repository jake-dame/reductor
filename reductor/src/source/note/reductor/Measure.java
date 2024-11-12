package reductor;

import java.util.ArrayList;


public class Measure implements Ranged {

    private Notes notes;

    private int measureNumber;
    private final Range range;

    private final TimeSignature timeSig;
    private final int upperNumeral;
    private final int lowerNumeral;

    private boolean isPickup;
    private final boolean isRepeat;

    Measure(Range range, int measureNumber, TimeSignature timeSignature, ArrayList<Note> notes) {

        this.range = range;
        this.timeSig = timeSignature;
        this.upperNumeral = timeSignature.getUpperNumeral();
        this.lowerNumeral = timeSignature.getLowerNumeral();
        this.isPickup = false;
        this.isRepeat = false;
        this.measureNumber = measureNumber;

        if (notes == null) {
            this.notes = null;
        } else {
            this.notes = new Notes(notes);
        }

    }

    public int size() {
        if (notes == null) { return 0; }
        return this.notes.size();
    }

    public long length() {
        return this.getRange().length();
    }

    public TimeSignature getTimeSignature() {
        return this.timeSig; // todo deep copy?
    }

    @Override
    public Range getRange() {
        return new Range(this.range);
    }

    public boolean isPickup() {
        return this.isPickup;
    }

    void setMeasureNumber(int val) {
        this.measureNumber = val;
    }

    void setIsPickup(boolean val) {
        this.isPickup = val;
    }

    void setNotes(Notes notes) {
        this.notes = notes;
    }

    @Override
    public String toString() {
        return this.range + ": " + this.upperNumeral + "/" + this.lowerNumeral + " and " + this.size() + " notes"; // todo idk man
    }

    public static long getMeasureSize(int upperNumeral, int lowerNumeral, int resolution) {

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

    public static long getMeasureSize(TimeSignature timeSig) {
        return getMeasureSize(timeSig.getUpperNumeral(), timeSig.getLowerNumeral(), timeSig.getResolution());
    }


}
