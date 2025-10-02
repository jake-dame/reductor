package reductor.core;

public class Tempo implements Ranged {


    private final Range range;

    private int bpm;


    public Tempo(int bpm, Range range) {
        this.bpm = bpm;
        this.range = new Range(range);
    }

    public Tempo(Tempo other) {
        this.bpm = other.bpm;
        this.range = new Range(other.range);
    }

    public int getBpm() { return this.bpm; }
    public void setBpm(int val) {
        if (val < 0) { val = 0; }
        this.bpm = val;
    }

    @Override
    public Range getRange() { return new Range(this.range); }

    @Override
    public String toString() {
        return this.range + " " + this.bpm + " bpm";
    }


}
