package reductor;


public class TimeSignature implements Ranged {

    private final int upperNumeral;
    private final int lowerNumeral;
    private final int resolution;
    private Range range;
    private final TimeSignatureEvent event; // TODO: remove once get/setRange() is implemented


    TimeSignature(TimeSignatureEvent timeSignatureEvent) {
        this.upperNumeral = timeSignatureEvent.getUpperNumeral();
        this.lowerNumeral = timeSignatureEvent.getLowerNumeral();

        this.resolution = timeSignatureEvent.getResolution();

        this.range = null;

        this.event = timeSignatureEvent;
    }


    public int getUpperNumeral() { return this.upperNumeral; }
    public int getLowerNumeral() { return this.lowerNumeral; }
    public int getResolution() { return this.resolution; }

    @Override
    public Range getRange() { return new Range(this.range); }

    @Override
    public String toString() {
        return this.upperNumeral + "/" + this.lowerNumeral;
    }



    @Override
    public long start() {
        return this.range.getLow();
    }

    @Override
    public void setRange(Range range) {
        this.range = range;
    }


    // TODO: this is just a quick fix until getRange() is implemented
    public long getTick() {
        return this.event.getTick();
    }


}
