package reductor.core;

public record Tempo(int bpm, Range range) implements Ranged {

    public Tempo(Tempo other) {
        this(other.bpm, new Range(other.range));
    }

    public int getBpm() { return this.bpm; }

    @Override
    public Range getRange() { return new Range(this.range); }

    @Override
    public String toString() { return "%s: %s bpm".formatted(range, bpm); }


}
