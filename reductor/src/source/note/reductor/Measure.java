package reductor;

public class Measure implements Ranged {

    private final int measureNumber;
    private final TimeSignatureEvent timeSignature;
    private final int upperNumeral;
    private final int lowerNumeral;
    private final Range range;
    private final boolean isPickup;
    private final boolean isRepeat;

    Measure(Range range, TimeSignatureEvent timeSignature) {
        this.range = range;
        this.timeSignature = timeSignature;
        this.upperNumeral = timeSignature.getUpperNumeral();
        this.lowerNumeral = timeSignature.getLowerNumeral();
        this.isPickup = false;
        this.isRepeat = false;
        this.measureNumber = assignMeasureNumber();
    }

    private int assignMeasureNumber() {
        return -1;
    }

    @Override
    public Range getRange() {
        return new Range(this.range);
    }


}
