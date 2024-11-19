package reductor;

public class Tempo implements Ranged {

    private final int bpm;

    private final Range range;

    Tempo(int bpm, Range range) {
        this.bpm = bpm;
        this.range = new Range(range);
    }

    Tempo(Tempo other, int bpm) {
        this.bpm = bpm;
        this.range = new Range(other.range);
    }

    /// Copy constructor
    Tempo(Tempo other) {
        this.bpm = other.bpm;
        this.range = new Range(other.range);
    }

    public int getBpm() { return this.bpm; }

    @Override
    public Range getRange() { return new Range(this.range); }


}
