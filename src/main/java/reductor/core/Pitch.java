package reductor.core;


import java.util.HashMap;
import java.util.Map;
import java.util.Set;


/**
 * Utility class for pitch-related functionality.
 *<p>
 * Contains functionality related to:
 * <ul>
 *     <li> Validation: for both MIDI-spec and piano standard ranges </li>
 *     <li> Conversion: between int values and string values representing pitches </li>
 *     <li> "Getters": for register </li>
 *     <li> Interval constants and a function for shifting a pitch by interval </li>
 *     <li> Maps/sets for pitches and semitones </li>
 *     <li> Determining white key vs. black key pitches </li>
 * </ul>
 */
public final class Pitch {

    /** Integer value representing the lowest pitch represented in MIDI spec; corresponds to "C-1" (C negative one). */
    public static final int MIN_MIDI = 0;

    /** Integer value representing the highest pitch represented in MIDI spec; corresponds to "G9". */
    public static final int MAX_MIDI = 127;

    /** Integer value representing the lowest pitch playable on a standard piano; corresponds to "A0". */
    public static final int MIN_PIANO = 21;

    /** Integer value representing the highest pitch playable on a standard piano; corresponds to "C8". */
    public static final int MAX_PIANO = 108;

    /*
     INTERVALS (offset values)

     e.g. note.add(Pitch.M3)
          or note.add(-Pitch.M3)

     @see: docs/1-relevant-musical-terminology.md
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


    public static final Map<Integer, String> pitchesItoS;

    public static final Map<String, Integer> semitonesStoI;
    public static final Map<String, Integer> accidentalsStoI;

    static {

        semitonesStoI = Map.of("c", 0, "d", 2, "e", 4, "f", 5, "g", 7, "a", 9, "b", 11);

        accidentalsStoI = Map.of("bb", -2, "b", -1, "", 0, "#", 1, "x", 2);

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
        } // end for

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

        if(pitch < MIN_MIDI || MAX_MIDI < pitch) {
            throw new IllegalArgumentException("valid pitch values are in [0,127], not: " + pitch);
        }

        return pitch;
    }

    /**
     * @see #validatePitch(int)
     * @see Pitch#toInt(String)
     */
    public static int validatePitch(String pitch) {

        // The validation for string pitches is native within toInt()
        int intValue = Pitch.toInt(pitch);

        return validatePitch(intValue);
    }

    /**
     * Determines whether a given pitch is a white key on the piano.
     *
     * @param pitch A pitch that exists on the piano (i.e. in [21,108])
     * @return True if the pitch is a white key on the piano; false if it is a black key.
     * @throws IllegalArgumentException if the pitch does not exist on a piano (even though it may be a valid MIDI
     * pitch)
     */
    public static boolean isWhiteKey(int pitch) {

        if (pitch < MIN_PIANO ||  MAX_PIANO < pitch) {
            throw new IllegalArgumentException(
                    "that's not a white key or a black key because it doesn't exist on a piano"
            );
        }

        return Set.of(0, 2, 4, 5, 7, 9, 11).contains(pitch % 12);
    }

    /**
     * @see #isWhiteKey
     */
    public static boolean isBlackKey(int pitch) { return !isWhiteKey(pitch); }

    /**
     * Clamps a pitch value to be within piano range
     * <p>
     * This does not alter the semitone -- only the register.
     *
     * @param pitch A pitch to clamp to be within the range that exists on a standard piano
     * @return The same pitch, but either up/down an octave(s) to be within piano range.
     */
    public static int clampToPianoRange(int pitch) {
        while (pitch < MIN_PIANO) { pitch += OCTAVE; }
        while (MAX_PIANO < pitch) { pitch -= OCTAVE; }
        return pitch;
    }

    /**
     * Returns the register of a given pitch.
     *
     * @param pitch A valid pitch value; an int in {@code [0,127]}
     * @return The pitch's register.
     *
     * @see Pitch#toInt
     */
    public static int getRegister(int pitch) {

        validatePitch(pitch);

        // Needs to be -2; will always exit loop as at least -1 (which is valid)
        int register = -2; // e.g. 60 (middle C, or C4)
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
     * Converts an integer value (MIDI spec) to a string.
     * <p>
     * Currently: non-diatonic spelling is always the sharped degree (Ionian mode).
     *
     * @param val          A valid MIDI pitch value (an int in {@code [0, 127]}).
     * @param showRegister {@code true} if the string returned should include register, e.g. {@code "A#6"}; {@code false} if the string returned should just be the pitch, e.g. {@code "A#"}.
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
     * Parses an alphabetical/alphanumeric string into its corresponding MIDI pitch value,
     * which are in {@code [0, 127]}.
     * <p>
     * Valid strings:
     * <ul>
     *     <li>Must have a pitch. Valid pitch values are in upper-/lower-case {@code ['A', 'G']}</li>
     *     <li>May be followed by an accidental: {@code {"#", "b", "x", "bb"}}</li>
     *     <li>May terminate with a register in {@code ['-1', '9']}; if none is given, the default is {@code -1}</li>
     *     <li>Min is {@code "C-1"} and max is {@code "G9"} or valid enharmonic spellings of those pitches</li>
     * </ul>
     *
     * @param str A string describing a pitch, such as {@code "A4"}, {@code "Ab"}, {@code "A#"}, {@code "Ax3"}, or {@code "Abb-1"}.
     * @return The MIDI int value corresponding to the pitch represented by {@code str}.
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
            throw new IllegalArgumentException("invalid pitch; valid pitches are in upper or lower ['A','G']");
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