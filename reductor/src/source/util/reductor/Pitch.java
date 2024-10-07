package reductor;

import java.util.HashMap;
import java.util.Map;


public class Pitch {

    private static final Map<String, Integer> semitonesMap;
    private static final Map<String, Integer> accidentalsMap;
    private static final Map<Integer, String> mapPitches;
    private static final Map<Integer, String> mapPitchesAndRegister;
    private static final Map<Integer, String> mapMajor;
    private static final Map<Integer, String> mapMinor;

    static {

        semitonesMap = Map.of("c", 0, "d", 2, "e", 4, "f", 5, "g", 7, "a", 9, "b", 11);

        accidentalsMap = Map.of("#", 1, "x", 2, "b", -1, "bb", -2);

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
                Map.entry(-5, "d"),
                Map.entry(-4, "f"),
                Map.entry(-3, "g"),
                Map.entry(-2, "h"),
                Map.entry(-1, "u"),
                Map.entry(0, "i"),
                Map.entry(1, "o"),
                Map.entry(2, "p"),
                Map.entry(3, "xz"),
                Map.entry(4, "c"),
                Map.entry(5, "v"),
                Map.entry(6, "b"),
                Map.entry(7, "n")
        );

        mapPitches = new HashMap<>();
        for (int num = 0; num < 128; num++) {
            String space = "";
            String pitch = switch (num % 12) {
                case 0 -> "C";
                case 1 -> "C#";
                case 2 -> "D";
                case 3 -> "D#";
                case 4 -> "E";
                case 5 -> "F";
                case 6 -> "F#";
                case 7 -> "G";
                case 8 -> "G#";
                case 9 -> "A";
                case 10 -> "A#";
                case 11 -> "B";
                default -> "";
            };
            mapPitches.put(num, pitch);
        }

        mapPitchesAndRegister = new HashMap<>();
        for (int num = 0; num < 128; num++) {
            String space = ""; // this exists because I change my mind sometimes
            String pitch = switch (num % 12) {
                case 0 -> "C" + space + (num/12 - 1);
                case 1 -> "C#" + space + (num/12 - 1);
                case 2 -> "D" + space + (num/12 - 1);
                case 3 -> "D#" + space + (num/12 - 1);
                case 4 -> "E" + space + (num/12 - 1);
                case 5 -> "F" + space + (num/12 - 1);
                case 6 -> "F#" + space + (num/12 - 1);
                case 7 -> "G" + space + (num/12 - 1);
                case 8 -> "G#" + space + (num/12 - 1);
                case 9 -> "A" + space + (num/12 - 1);
                case 10 -> "A#" + space + (num/12 - 1);
                case 11 -> "B" + space + (num/12 - 1);
                default -> "";
            };
            mapPitchesAndRegister.put(num, pitch);
        }


    }


    /**
     * Given a numerical value for a pitch (valid in Midi are in [0,127]),
     * returns the string value (name) of the pitch. Because it is near impossible to know
     * the precise spelling without broad context, all accidentals are sharps, and the mode
     * is C Major.
     *
     * @param val A numerical value representing a Midi pitch
     * @return The input value as its String representation
     */
    static String numericalPitchToString(Number val, boolean showRegister) {

        int intValue = val.intValue() & 0xFF;
        if (intValue > 127) {
            throw new IllegalArgumentException("pitch values must be between 0 and 127");
        }

        if (showRegister) {
            return mapPitchesAndRegister.get(intValue);
        } else {
            return mapPitches.get(intValue);
        }

    }


    /**
     * Converts a pitch and register specification into a integer, if it is a valid MIDI pitch value.
     *
     * <li> Valid pitches are upper or lower "A" through "G".
     * <li> Valid accidentals: "b" -> flat, "#" -> sharp, "bb" -> double-flat, "x" -> double sharp.
     * <li> Valid registers are in the range -1 to 9.
     * <li> Supports enharmonic spellings.
     *
     * @param str A string such as "A", "Ab", "A#", "Ax", or "Abb"
     * @param register A register (octave) in [-1, 9]
     * @return An int value that is equivalent to the inputted string (according to the MIDI standard)
     */
    static int stringPitchToNumber(String str, int register) {

        if (register < -1 || register > 9) {
            throw new IllegalArgumentException("valid registers are in [-1,9]");
        }

        str = str.trim().toLowerCase();

        if (str.isEmpty()  ||  str.length() > 3) {
            throw new IllegalArgumentException("string is empty or too long");
        }

        // These two checks are clunky but don't fit any (easy) algorithm neatly.
        // They check for valid enharmonic spellings of min/max.
        if (register == -1  &&  (str.equals("b#")  ||  str.equals("dbb"))) { return 0; }
        if (register == 9  &&  (str.equals("fx")  ||  str.equals("abb"))) { return 127; }

        Integer semitone = semitonesMap.get( str.substring(0,1) );
        if (semitone == null) {
            throw new IllegalArgumentException("invalid pitch (valid pitches are in upper/lower [A,G])");
        }

        if (str.length() > 1) {
            Integer accidentalAdjustment = accidentalsMap.get( str.substring(1) );
            if (accidentalAdjustment == null) {
                throw new IllegalArgumentException("invalid accidental (valid accidentals are \"#\", \"b\", \"x\", or \"bb\")");
            }
            semitone += accidentalAdjustment;
        }

        if (register == 9 && semitone > 7) {
            throw new IllegalArgumentException("G is the highest valid pitch in register 9");
        }

        return semitone + (register + 1) * 12;
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
