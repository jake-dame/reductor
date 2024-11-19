package reductor;

import java.util.Map;


public class KeySignature implements Ranged {

    private static final Map<Integer, String> mapMajor;
    private static final Map<Integer, String> mapMinor;


    static {

        mapMajor = Map.ofEntries(
                Map.entry(-7, "Cb"),
                Map.entry(-6, "Gb"),
                Map.entry(-5, "Db"),
                Map.entry(-4, "Ab"),
                Map.entry(-3, "Eb"),
                Map.entry(-2, "Bb"),
                Map.entry(-1, "F"),
                Map.entry(0, "C"),
                Map.entry(1, "G"),
                Map.entry(2, "D"),
                Map.entry(3, "A"),
                Map.entry(4, "E"),
                Map.entry(5, "B"),
                Map.entry(6, "F#"),
                Map.entry(7, "C#")
        );

        mapMinor = Map.ofEntries(
                Map.entry(-7, "Ab"),
                Map.entry(-6, "Eb"),
                Map.entry(-5, "Bb"),
                Map.entry(-4, "F"),
                Map.entry(-3, "C"),
                Map.entry(-2, "G"),
                Map.entry(-1, "D"),
                Map.entry(0, "A"),
                Map.entry(1, "E"),
                Map.entry(2, "B"),
                Map.entry(3, "F#"),
                Map.entry(4, "C#"),
                Map.entry(5, "G#"),
                Map.entry(6, "D#"),
                Map.entry(7, "A#")
        );

    }


    private final int mode;
    private final int accidentals;

    private final Range range;

    public KeySignature(int accidentals, int mode, Range range) {

        assert -7 <= accidentals  &&  accidentals <= 7;
        assert mode == 0  ||  mode == 1;

        this.range = new Range(range);
        this.mode = mode;
        this.accidentals = accidentals;
    }


    /// Copy constructor
    public KeySignature(KeySignature other) {
        this.range = new Range(other.range);
        this.mode = other.mode;
        this.accidentals = other.accidentals;
    }

    public int getTonic() {

        // This is weird but it works with what I have
        String keyString = this.isMajor()
                ? mapMajor.get(this.accidentals)
                : mapMinor.get(this.accidentals);

        return Pitch.toInt(keyString + "-1");
    }

    public int getMode() { return mode; }
    public int getAccidentals() { return accidentals; }

    boolean isMajor() { return this.mode == 0; }
    boolean isMinor() { return !isMajor(); }

    // C/a is grouped with sharp-spelled keys (for now)
    boolean isSharp() { return this.accidentals >= 0; }
    boolean isFlat() { return !isSharp(); }

    @Override
    public Range getRange() { return new Range(this.range); }

    @Override
    public String toString() { return toString(this.mode, this.accidentals); }


    public static String toString(int mode, int accidentals) {
        return mode == 0
                ? String.format("%s Major", mapMajor.get(accidentals))
                : String.format("%s minor", mapMinor.get(accidentals));
    }

}
