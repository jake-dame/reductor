package reductor.piece;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;


/**
 * Utility class that handles a lot of pitch-related stuff, for both MIDI and piano-ranged pitches, including pitch
 * validation, conversion between string and int, and register calculations.
 */
public class Pitch {

    /// Pitch: C-1
    public static final int MIDI_MIN = 0;
    /// Pitch: G9
    public static final int MIDI_MAX = 127;

    /// Pitch: A0
    public static final int PIANO_MIN = 21;
    /// Pitch: C8
    public static final int PIANO_MAX = 108;

    /*
     INTERVALS

     e.g. note.add(Pitch.M3)
          or note.add(-Pitch.M3)
    */
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


    // Yes, this actually stores all 128 valid pitches. No, I am not interested in doing this programmatically.
    public static final Map<Integer, String> pitchesItoS;

    public static final Map<String, Integer> semitonesStoI;
    public static final Map<String, Integer> accidentalsStoI;

    static {

        semitonesStoI = Map.of("c", 0, "d", 2, "e", 4, "f", 5, "g", 7, "a", 9, "b", 11);

        accidentalsStoI = Map.of("bb", -2, "b", -1, "", 0, "#", 1, "x", 2);

        pitchesItoS = new HashMap<>();
        for (int num = MIDI_MIN; num <= MIDI_MAX; num++) {
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


    private Pitch() { }


    /**
     * Checks whether a given integer represents a valid MIDI pitch or not (i.e. is in [0,127]).
     *
     * @return Just returns the same pitch back to you, so it can be used like a setter of sorts too.
     * @param pitch The value to validate as a pitch.
     * @throws IllegalArgumentException if the pitch is invalid.
     */
    public static int validatePitch(int pitch) {

        if(pitch < MIDI_MIN || MIDI_MAX < pitch) {
            throw new IllegalArgumentException("valid pitch values are in [0,127], not: " + pitch);
        }

        return pitch;
    }

    public static int validatePitch(String pitch) {
        int intValue = Pitch.toInt(pitch);
        return validatePitch(intValue);
    }

    /**
     *
     * @param pitch A pitch that exists on the piano (i.e. in [21,108])
     * @return True if the pitch is a white key on the piano; false if it is a black key.
     * @throws IllegalArgumentException if the pitch does not exist on a piano (even though it may be a valid MIDI
     * pitch)
     */
    public static boolean isWhiteKey(int pitch) {

        if (pitch < PIANO_MIN  ||  PIANO_MAX < pitch) {
            throw new IllegalArgumentException(
                    "that's not a white key or a black key because it doesn't exist on a piano"
            );
        }

        return Set.of(0, 2, 4, 5, 7, 9, 11).contains(pitch % 12);
    }

    /**
     * @see Pitch#isWhiteKey
     */
    public static boolean isBlackKey(int pitch) {
        return !isWhiteKey(pitch);
    }

    /**
     * Clamps a pitch value to be within piano range (does not change the semitone, only the register).
     *
     * @param pitch A pitch to clamp to be within the range that exists on a standard piano
     * @return The same pitch, but either up/down an octave(s) to be within piano range.
     */
    public static int clampToPianoRange(int pitch) {
        while (pitch < PIANO_MIN) { pitch += OCTAVE; }
        while (PIANO_MAX < pitch) { pitch -= OCTAVE; }
        return pitch;
    }

    /**
     * Returns the register (octave) that a passed pitch is in. For information on valid registers, see
     * {@link Pitch#toInt}
     *
     * @param pitch A valid pitch value
     * @return The register the pitch exists in.
     * @see Pitch#toInt
     */
    public int getRegister(int pitch) {

        validatePitch(pitch);

        // e.g. 60 (middle C, or C4)
        int register = -1;
        while (0 <= pitch) {
            pitch -= 12; // 60, 48, 36, 24, 12, 0, break
            register++;  // -1,  0,  1,  2,  3, 4, n/a
        }

        return register;
    }

    public static int shift(int pitch, int intervallicDistance) {
        var shiftedPitch = pitch + intervallicDistance;
        return Pitch.validatePitch(shiftedPitch);
    }


    /**
     * Converts a MIDI pitch integer value to a string (currently: non-diatonic spelling is always the sharped degree (Ionian mode)).
     *
     * @param val          A valid MIDI pitch value (an int in {@code [0, 127]}.
     * @param showRegister {@code true} if the string returned should be in alphanumeric notation (includes register); {@code false} if the string returned should just be the pitch.
     * @return The string representation of {@code val}.
     * @throws IllegalArgumentException If the passed value is not in {@code [0, 127]}.
     */
   public static String toStr(Number val, boolean showRegister) throws IllegalArgumentException {

        // This, plus the Number type, makes it so bytes (from raw MIDI data) can also be used
        int intValue = val.intValue() & 0xFF;

        validatePitch(intValue);

        String pitchStr = pitchesItoS.get(intValue);

        if (showRegister) {
            return pitchStr;
        } else {

            // Because pitchesItoS includes the register, it has to be trimmed off.

            // This is for the single case of the -1 register
            if (pitchStr.charAt(pitchStr.length() - 2) == '-') {
                return pitchStr.substring(0, pitchStr.length() - 2);
            } else {
                return pitchStr.substring(0, pitchStr.length() - 1);
            }

        }

    }

    /**
     * Parses an {alphabetical} or alphanumeric string into its corresponding MIDI pitch value,
     * which are in {@code [0, 127]}. Valid strings:
     *
     * <ul>
     *     <li>Must have a pitch in upper or lower {@code ['A', 'G']}</li>
     *     <li>May be followed by an accidental ({@code "#"}, {@code "b"}, {@code "x"}, or {@code "bb"})</li>
     *     <li>May terminate with a register in {@code ['-1', '9']}; if none is given, default is the {@code -1} register</li>
     *     <li>Min is {@code "C-1"} and max is {@code "G9"} (or valid enharmonic spellings)</li>
     * </ul>
     *
     * @param str A string describing a pitch, such as {@code "A4"}, {@code "Ab"}, {@code "A#"}, {@code "Ax3"}, or {@code "Abb-1"}.
     * @return Returns the MIDI int value corresponding to the pitch described by {@code str}.
     * @throws IllegalArgumentException If the input string is invalid.
     */
    public static int toInt(String str) {

        if (str == null || str.isEmpty() || str.length() > 5) {
            throw new IllegalArgumentException("string is null, empty, or too long");
        }

        str = str.toLowerCase().trim();

        // Clunky but checks special cases: valid enharmonic spellings of min/max.
        switch (str) {
            case "b#-2", "dbb-1":
                return 0;
            case "bx-2":
                return 1;
            case "fx9", "abb9":
                return 127;
        }

        Integer semitone = semitonesStoI.get(str.substring(0, 1));

        if (semitone == null) {
            throw new IllegalArgumentException("invalid pitch; valid pitches are in upper or lower ['a','g']");
        }

        // It's just a pitch, so return it early
        if (str.length() == 1) { return semitone; }

        // Trim off the pitch char.
        str = str.substring(1);

        // Default register should just be -1.
        int register = -1;
        String accidental;

        /*
         Register is kind of messy because it can be either "-1" (two chars) or '0' - '9'
         Afterward, we want just the accidental symbol left over, since it will be easiest to
         parse that on its own (since it can also be 1 (e.g. "#") or 2 chars (e.g. "bb").
        */
        String[] arr = new String[2];
        if (str.contains("-")) {
            if (str.charAt(str.length() - 1) != '1') {
                throw new IllegalArgumentException("if there is a hyphen it needs to apply to 1");
            } else {
                accidental = str.split("-")[0];
            }
        } else {
            char last = str.charAt(str.length() - 1);
            if (Character.isDigit(last)) {
                if (last < '0' || last > '9') {
                    throw new IllegalArgumentException("register char must be in ['-1','9']");
                } else {
                    register = last - '0';
                    accidental = str.substring(0, str.length() - 1);
                }
            } else {
                accidental = str;
            }
        }

        Integer accidentalAdjustment = accidentalsStoI.get(accidental);

        if (accidentalAdjustment == null) {
            throw new IllegalArgumentException("invalid accidental; can be one of the following: \"#\", \"b\", \"x\", or \"bb\"");
        }

        /*
         Midi registers are sort of offset, start at -1; so we add one and it makes the math correct (e.g. -1
         is really the zero-eth register, really, so -1 + 1 * 12 = 0, plus the semitone,
         plus -2,-1,0,1, or 2 depending on the value returned for accidentalAdjustment.
        */
        return semitone + ((register + 1) * 12) + accidentalAdjustment;
    }

}