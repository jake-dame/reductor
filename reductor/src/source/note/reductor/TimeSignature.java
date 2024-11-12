package reductor;


public class TimeSignature {

    private final int upperNumeral;
    private final int lowerNumeral;
    private final int resolution;
    private final TimeSignatureEvent event;
    private final Range range;

    // todo this whole constructor is a quick fix
    TimeSignature(TimeSignatureEvent e) {
        this.upperNumeral = e.getUpperNumeral();
        this.lowerNumeral = e.getLowerNumeral();
        this.resolution = Piece.RESOLUTION;
        this.event = e;
        this.range = null;
    }


    //TimeSignature(int upperNumeral, int lowerNumeral, int resolution) {
    //    this.range = null;
    //    this.upperNumeral = upperNumeral;
    //    this.lowerNumeral = lowerNumeral;
    //    this.resolution = resolution;
    //}
    //
    //TimeSignature(TimeSignatureEvent timeSignatureEvent) {
    //    this(timeSignatureEvent.getUpperNumeral(), timeSignatureEvent.getLowerNumeral(), timeSignatureEvent.getResolution());
    //    this.event = timeSignatureEvent;
    //}

    public int getUpperNumeral() {
        return this.upperNumeral;
    }

    public int getLowerNumeral() {
        return this.lowerNumeral;
    }

    public int getResolution() {
        return this.resolution;
    }

    // TODO: this is just a quick fix until range is implemented
    public long getTick() {
        return this.event.getTick();
    }

    @Override
    public String toString() {
        return this.upperNumeral + "/" + this.lowerNumeral + " " + this.range;
    }


}
