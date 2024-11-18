package reductor;

import java.util.Map;


public class KeySignature {

    static final Map<Integer, String> mapMajor;
    static final Map<Integer, String> mapMinor;


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


    public KeySignature(int accidentals, int mode) {

        assert -7 <= accidentals  &&  accidentals <= 7;
        assert mode == 0  ||  mode == 1;

        this.mode = mode;
        this.accidentals = accidentals;
    }


    /// Copy constructor
    public KeySignature(KeySignature other) {
        this.mode = other.mode;
        this.accidentals = other.accidentals;
    }

    public int getTonic() {
        String keyString = this.isMajor()
                ? mapMajor.get(this.accidentals)
                :  mapMinor.get(this.accidentals);
        return Pitch.toInt(keyString + "-1");
    }

    boolean isMajor() { return this.mode == 0; }
    boolean isMinor() { return !isMajor(); }

    // C/a is grouped with sharp-spelled keys (for now)
    boolean isSharp() { return this.accidentals >= 0; }
    boolean isFlat() { return !isSharp(); }

    @Override
    public String toString() {
        return mode == 0
                ? String.format("%s Major", mapMajor.get(this.accidentals))
                : String.format("%s minor", mapMinor.get(this.accidentals));
    }

}
