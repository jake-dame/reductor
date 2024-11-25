package reductor;

import java.util.HashMap;
import java.util.Map;


public class KeySignature implements Ranged {

    private static final Map<Integer, String> mapMajor;
    private static final Map<Integer, String> mapMinor;

    private static final Map<String, Integer> reverseMapMajor;
    private static final Map<String, Integer> reverseMapMinor;


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
                Map.entry(-7, "ab"),
                Map.entry(-6, "eb"),
                Map.entry(-5, "bb"),
                Map.entry(-4, "f"),
                Map.entry(-3, "c"),
                Map.entry(-2, "g"),
                Map.entry(-1, "d"),
                Map.entry(0, "a"),
                Map.entry(1, "e"),
                Map.entry(2, "b"),
                Map.entry(3, "f#"),
                Map.entry(4, "c#"),
                Map.entry(5, "g#"),
                Map.entry(6, "d#"),
                Map.entry(7, "a#")
        );

        reverseMapMajor = new HashMap<>();
        for (Map.Entry<Integer, String> entry : mapMajor.entrySet()) {
            reverseMapMajor.put(entry.getValue(), entry.getKey());
        }

        reverseMapMinor = new HashMap<>();
        for (Map.Entry<Integer, String> entry : mapMinor.entrySet()) {
            reverseMapMinor.put(entry.getValue(), entry.getKey());
        }

    }


    private final int mode;
    private final int accidentals;

    private final Range range;

    public KeySignature(int accidentals, int mode, Range range) {

        validateKeySignature(mode, accidentals);

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

    /// Case-sensitive. Upper for major, lower for minor.
    public KeySignature(String str, Range range) {

        this.range = range;

        if (str == null || str.isEmpty() || 2 < str.length()) {
            throw new IllegalArgumentException("string is null, empty, or too short/long");
        }

        this.mode = Character.isUpperCase(str.charAt(0)) ? 0 : 1;

        this.accidentals = this.mode == 0
                ? reverseMapMajor.get(str)
                : reverseMapMinor.get(str);
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

    public static void validateKeySignature(int mode, int accidentals) {

        if ( accidentals < -7 || 7 < accidentals) {
            throw new IllegalArgumentException("number of accidentals should be in [-7,7]");
        }

        if (mode < 0  ||  1 < mode ) {
            throw new IllegalArgumentException("mode should be 0 for major or 1 for minor");
        }

    }

}
