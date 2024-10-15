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

    //static final Map<Integer, String> pitches;


    static {

        semitonesStoI = Map.of("c", 0, "d", 2, "e", 4, "f", 5, "g", 7, "a", 9, "b", 11);

        accidentalsStoI = Map.of("bb", -2, "b", -1, "", 0, "#", 1, "x", 2);

        //pitches = Map.ofEntries(
        //        Map.entry(0, "c"),
        //        Map.entry(1, "c"),
        //        Map.entry(2, "d"),
        //        Map.entry(3, "d"),
        //        Map.entry(4, "e"),
        //        Map.entry(5, "f"),
        //        Map.entry(6, "f"),
        //        Map.entry(7, "g"),
        //        Map.entry(8, "g"),
        //        Map.entry(9, "a"),
        //        Map.entry(10, "a"),
        //        Map.entry(11, "b"),
        //        Map.entry(12, "c")
        //);

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


    /**

     * Converts a MIDI pitch integer value to a string (currently: non-diatonic spelling is always the sharped degree (Ionian mode)).
     *
     * @param val A valid MIDI pitch value (an int in {@code [0, 127]}.
     * @param showRegister {@code true} if the string returned should be in alphanumeric notation (includes register); {@code false} if the string returned should just be the pitch.
     * @return The string representation of {@code val}.
     * @throws IllegalArgumentException If the passed value is not in {@code [0, 127]}.
     */
    static String toStr(Number val, boolean showRegister) {

        int intValue = val.intValue() & 0xFF;
        if (intValue > 127) {
            throw new IllegalArgumentException("pitch values must be between 0 and 127");
        }

        String pitchStr = pitchesItoS.get(intValue);

        if (showRegister) {
            return pitchStr;
        } else {
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
    static int toInt(String str) {

        if (str == null || str.isEmpty() || str.length() > 5) {
            throw new IllegalArgumentException("string is null, empty, or too long");
        }

        str = str.toLowerCase().trim();

        // Clunky but checks special cases: valid enharmonic spellings of min/max.
        switch (str) {
            case "b#-2", "dbb-1": return 0;
            case "bx-2": return 1;
            case "fx9", "abb9": return 127;
        }

        Integer semitone = semitonesStoI.get(str.substring(0, 1));
        if (semitone == null) {
            throw new IllegalArgumentException("invalid pitch; valid pitches are in upper or lower ['a','g']");
        }

        if (str.length() == 1) {
            // It's just a pitch, so just return the pitch.
            return semitone;
        }

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
            if(Character.isDigit(last)) {
               if(last < '0' || last > '9') {
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