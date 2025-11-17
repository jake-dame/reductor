package reductor.util;


import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public final class PitchUtil {


    private static final Pattern PATTERN_PITCH = Pattern.compile("""
             (?x)^
             \\s*
             (?<letter>(?i:[A-G]))
             \\s*
             (?<accidental>\\#|x|bb|b)?
             \\s*
             (?<register>-2|-1|[0-9])?
             \\s*
             $
             """);

    public static final int MIN_MIDI = 0;
    public static final int MAX_MIDI = 127;

    public static final int MIN_PIANO = 21;
    public static final int MAX_PIANO = 108;

    public static final int m2 = 1;
    public static final int M2 = 2;
    public static final int m3 = 3;
    public static final int M3 = 4;
    public static final int P4 = 5;
    public static final int TT = 6;
    public static final int P5 = 7;
    public static final int m6 = 8;
    public static final int M6 = 9;
    public static final int m7 = 10;
    public static final int M7 = 11;
    public static final int OCTAVE = 12;

    public static final Map<Integer, String> pitchesItoS;

    public static final Map<String, Integer> lettersStoI;
    public static final Map<Integer, String> lettersItoS;
    public static final Map<String, Integer> accidentalsStoI;
    public static final Map<Integer, String> accidentalsItoS;

    static {

        lettersStoI = Map.of(
                "c", 0,
                "d", 2,
                "e", 4,
                "f", 5,
                "g", 7,
                "a", 9,
                "b", 11
        );
        lettersItoS = new HashMap<>();
        for (Map.Entry<String, Integer> entry : lettersStoI.entrySet()) {
            lettersItoS.put(entry.getValue(), entry.getKey());
        }

        accidentalsStoI = Map.of(
                "bb", -2,
                "b", -1,
                "", 0,
                "#", 1,
                "x", 2
        );
        accidentalsItoS = new HashMap<>();
        for (Map.Entry<String, Integer> entry : accidentalsStoI.entrySet()) {
            accidentalsItoS.put(entry.getValue(), entry.getKey());
        }

        pitchesItoS = new HashMap<>();
        for (int num = MIN_MIDI; num <= MAX_MIDI; num++) {
            String pitch = switch (num % 12) {
                case 0 -> "C" + (num / 12 - 1);
                case 1 -> "C#" + (num / 12 - 1);
                case 2 -> "D" + (num / 12 - 1);
                case 3 -> "D#" + (num / 12 - 1);
                case 4 -> "E" + (num / 12 - 1);
                case 5 -> "F" + (num / 12 - 1);
                case 6 -> "F#" + (num / 12 - 1);
                case 7 -> "G" + (num / 12 - 1);
                case 8 -> "G#" + (num / 12 - 1);
                case 9 -> "A" + (num / 12 - 1);
                case 10 -> "A#" + (num / 12 - 1);
                case 11 -> "B" + (num / 12 - 1);
                default -> "not a pitch";
            };
            pitchesItoS.put(num, pitch);
        }

    }


    private PitchUtil() { }


    public static Matcher parse(String str) {
        Matcher matcher = PATTERN_PITCH.matcher(str);
        if (!matcher.matches() || !isValidOrSpecialCase(matcher)) {
            // this is IllegalState rather than IllegalArgument because the Matcher object being
            //     null was bad state, I decided. Could be either though.
            throw new IllegalStateException("invalid pitch string: " + str);
        }
        return matcher;
    }

    // This pre-validates
    // Because of the 3 special/edge cases that are valid, we have to let -2 and 9
    //     through on the PATTERN, and check them here before passing them to the
    //     Pitch ctor.
    private static boolean isValidOrSpecialCase(Matcher matcher) {
        String letter = matcher.group("letter").toLowerCase();
        String accidental = matcher.group("accidental") == null ? "" : matcher.group("accidental");
        String register = matcher.group("register") == null ? "" : matcher.group("register");

        String pitch = letter + accidental;
        if (register.equals("-2") && !pitch.equals("bx") && !pitch.equals("b#")) {
            return false;
        }

        if (register.equals("9")) {
            if (letter.equals("b"))  { return false; }
            if (letter.equals("a") && !accidental.equals("bb"))  { return false; }
            if (letter.equals("g") && (accidental.equals("#") || accidental.equals("x"))) {
                return false;
            }
        }

        return true;
    }

    public static int validatePitch(int pitch) {

        if (pitch < MIN_MIDI || MAX_MIDI < pitch) {
            throw new IllegalArgumentException(
                    "given: %s. Valid pitch values are in [0,127].".formatted(pitch)
            );
        }

        return pitch;
    }

    public static boolean isPianoRange(int pitch) {
        return pitch < MIN_PIANO || MAX_PIANO < pitch;
    }

    public static boolean isWhiteKey(int pitch) {
        if (isPianoRange(pitch)) {
            throw new IllegalArgumentException("can only be called on piano range vals");
        }
        return lettersItoS.get(pitch % 12) != null;
    }

    public static boolean isBlackKey(int pitch) {
        return !isWhiteKey(pitch);
    }

    public static int clampToPianoRange(int pitch) {
        while (pitch < MIN_PIANO) { pitch += OCTAVE; }
        while (MAX_PIANO < pitch) { pitch -= OCTAVE; }
        return pitch;
    }

    public static int getRegister(int pitch) {
        validatePitch(pitch);
        return (pitch / 12) - 1;
    }

    public static int shift(int pitch, int intervallicDistance) {
        var shiftedPitch = pitch + intervallicDistance;
        return PitchUtil.validatePitch(shiftedPitch);
    }

    // This defaults to always spelling with sharps!
    public static String parseMidiValue(Number val, boolean showRegister)
            throws IllegalArgumentException {

        // This, plus the Number type, makes it so bytes (from raw MIDI data) can also be used
        int intValue = val.intValue() & 0xFF;

        String pitchStr = pitchesItoS.get(validatePitch(intValue));

        if (showRegister) {
            return pitchStr;
        } else {
            // Because pitchesItoS includes the register, it has to be trimmed off.
            // First check is for the single case of the -1 register
            if (pitchStr.charAt(pitchStr.length() - 2) == '-') {
                return pitchStr.substring(0, pitchStr.length() - 2);
            } else {
                return pitchStr.substring(0, pitchStr.length() - 1);
            }
        }

    }


}
