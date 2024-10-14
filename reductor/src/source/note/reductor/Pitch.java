package reductor;

import java.util.HashMap;
import java.util.Map;


public class Pitch {


    //static final Map<Integer, String> semitonesItoS;
    static final Map<String, Integer> semitonesStoI;

    //static final Map<Integer, String> accidentalsItoS;
    static final Map<String, Integer> accidentalsStoI;

    static final Map<Integer, String> pitchesItoS;
    //static final Map<String, Integer> pitchesStoI;

    static final Map<Integer, String> pitches;


    static {

        semitonesStoI = Map.of("c", 0, "d", 2, "e", 4, "f", 5, "g", 7, "a", 9, "b", 11);

        accidentalsStoI = Map.of("bb", -2, "b", -1, "", 0, "#", 1, "x", 2);

        pitches = Map.ofEntries(
                Map.entry(0, "c"),
                Map.entry(1, "c"),
                Map.entry(2, "d"),
                Map.entry(3, "d"),
                Map.entry(4, "e"),
                Map.entry(5, "f"),
                Map.entry(6, "f"),
                Map.entry(7, "g"),
                Map.entry(8, "g"),
                Map.entry(9, "a"),
                Map.entry(10, "a"),
                Map.entry(11, "b"),
                Map.entry(12, "c")
        );

        pitchesItoS = new HashMap<>();
        for (int num = 0; num < 128; num++) {
            String pitch = switch (num % 12) {
                case 0 -> "C" + (num/12 - 1);
                case 1 -> "C#" + (num/12 - 1);
                case 2 -> "D" + (num/12 - 1);
                case 3 -> "D#" + (num/12 - 1);
                case 4 -> "E" + (num/12 - 1);
                case 5 -> "F" + (num/12 - 1);
                case 6 -> "F#" + (num/12 - 1);
                case 7 -> "G" + (num/12 - 1);
                case 8 -> "G#" + (num/12 - 1);
                case 9 -> "A" + (num/12 - 1);
                case 10 -> "A#" + (num/12 - 1);
                case 11 -> "B" + (num/12 - 1);
                default -> "";
            };
            pitchesItoS.put(num, pitch);
        }


    }


    private Pitch() { }


    static String toStr(Number val, KeyContext keyContext, boolean showRegister) {

        int intValue = val.intValue() & 0xFF;
        if (intValue > 127) {
            throw new IllegalArgumentException("pitch values must be between 0 and 127");
        }

        String pitchStr = getPitchStr(intValue, keyContext);

        if (showRegister) {
            return pitchStr;
        } else {
            return pitchStr.substring(0, pitchStr.length() - 1);
        }

    }


    private static String getPitchStr(int intValue, KeyContext keyContext) {

        String pitchStr = pitchesItoS.get(intValue);

        if (keyContext != null) {

            if (keyContext.isFlat()) {
                int num = 42;
            }

        }

        return pitchStr;
    }


    static int toInt(String str, int register) {

        if (register < -1 || register > 9) {
            throw new IllegalArgumentException("valid registers are in [-1,9]");
        }

        // debug
        if (str == null) {
            System.out.println();
            return -1;
        }
        // debug
        str = str.trim().toLowerCase();

        if (str.isEmpty()  ||  str.length() > 3) {
            throw new IllegalArgumentException("string is empty or too long");
        }

        // These two checks are clunky but don't fit any (easy) algorithm neatly.
        // They check for valid enharmonic spellings of min/max.
        if (register == -1  &&  (str.equals("b#")  ||  str.equals("dbb"))) { return 0; }
        if (register == 9  &&  (str.equals("fx")  ||  str.equals("abb"))) { return 127; }

        Integer semitone = semitonesStoI.get( str.substring(0,1) );
        if (semitone == null) {
            throw new IllegalArgumentException("invalid pitch (valid pitches are in upper/lower [A,G])");
        }

        if (str.length() > 1) {
            Integer accidentalAdjustment = accidentalsStoI.get( str.substring(1) );
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


}
