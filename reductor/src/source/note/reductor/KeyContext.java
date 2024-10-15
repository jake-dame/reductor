package reductor;


import java.util.Map;


public class KeyContext {


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


    int mode;
    int accidentals;
    int tonic;


    private KeyContext(KeySignatureEvent event) {

        this.mode = event.mode;
        this.accidentals = event.accidentals;
        this.tonic = getTonic();

    }

    public static KeyContext getKeyContextObject(KeySignatureEvent event) {

        if (event == null) {
            return null;
        }

        return new KeyContext(event);
    }

    int getTonic() {

        String keyString;
        if (isMajor()) {
            keyString = mapMajor.get(accidentals);
        } else {
            keyString = mapMinor.get(accidentals);
        }

        return Pitch.toInt(keyString + "-1");

    }


    boolean isMajor() {

        return mode == 0;

    }


    boolean isMinor() {

        return mode == 1;

    }


    boolean isSharp() {

        return accidentals >= 0;

    }


    boolean isFlat() {

        return accidentals < 0;

    }


    @Override
    public String toString() {

        byte[] bytes = new byte[2];
        bytes[0] = (byte) accidentals;
        bytes[1] = (byte) mode;
        return getKeySignature(bytes);

    }


    /**
     * Returns a string given a valid "Key signature" meta message data:
     * <li> Accidentals are [-7,-1] for flats; [1,7] for sharps; 0 for none
     * <li> Mode is 0 for major; 1 for minor
     *
     * @param bytes The data from a "Key signature" meta message
     * @return The key signature as a string
     */
    static String getKeySignature(byte[] bytes) {

        int accidentalCount = bytes[0];
        int mode = bytes[1];

        if (accidentalCount < -7  ||  accidentalCount > 7) {
            throw new IllegalArgumentException("bytes representing accidental counts must be between -7 and 7");
        }

        String str;
        if (mode == 0) {
            return String.format("%s Major", mapMajor.get(accidentalCount));
        } else if (mode == 1) {
            return String.format("%s minor", mapMinor.get(accidentalCount));
        } else {
            throw new IllegalArgumentException("bytes representing modes must be 0 (major) or 1 (minor)");
        }

    }


}
