package reductor.core;


import reductor.util.KeySignatureUtil;

import java.util.regex.Matcher;

import static reductor.util.KeySignatureUtil.*;


public class KeySignature implements Ranged {


    private final int mode;
    private final int accidentals;

    private final Range range;

    public KeySignature(int accidentals, int mode, Range range) {

        if (accidentals < -7 || 7 < accidentals) {
            throw new IllegalArgumentException("invalid accidentals value: " + accidentals);
        }

        if (mode < 0 || 1 < mode) {
            throw new IllegalArgumentException("invalid mode value: " + mode);
        }

        this.range = new Range(range);
        this.mode = mode;
        this.accidentals = accidentals;
    }

    public KeySignature(KeySignature other) {
        this.range = new Range(other.range);
        this.mode = other.mode;
        this.accidentals = other.accidentals;
    }


    public KeySignature(String str, Range range) {

        Matcher matcher = KeySignatureUtil.parse(str);

        String letter = matcher.group("letter");
        String accidental = matcher.group("accidental");
        String mode = matcher.group("mode");

        boolean isMinorKey = false;
        if (mode == null) {
            if (Character.isLowerCase(letter.charAt(0))) {
                isMinorKey = true;
            }
        } else {
            isMinorKey = mode.equals("m")
                    || mode.equalsIgnoreCase("min")
                    || mode.equalsIgnoreCase("minor");
        }

        if (accidental != null) {
            if ((accidental.equals("#") || accidental.equals("sharp"))) {
                letter += "#";
            } else {
                letter += "b";
            }
        }

        this.mode = isMinorKey ? 1 : 0;
        this.accidentals = isMinorKey ? keysMinorStoI.get(letter.toLowerCase()) :
                keysMajorStoI.get(letter.toUpperCase());
        this.range = range;
    }


    //public KeySignature(String str, Range range) {
    //
    //    Matcher matcher = KeySignatureUtil.parse(str);
    //
    //    String letter = matcher.group("letter");
    //    String accidental = matcher.group("accidental");
    //    String mode = matcher.group("mode");
    //
    //    boolean minorKey = mode != null
    //            && (mode.equals("m")
    //            || mode.equalsIgnoreCase("min")
    //            || mode.equalsIgnoreCase("minor"));
    //
    //    boolean sharpKey = accidental != null
    //            && (accidental.equals("#") || accidental.equals("sharp"));
    //
    //    letter += sharpKey ? "#" : (accidental == null ? "" : "b");
    //
    //    this.mode = minorKey ? 1 : 0;
    //    this.accidentals = minorKey ? keysMinorStoI.get(letter.toLowerCase()) :
    //            keysMajorStoI.get(letter.toUpperCase());
    //    this.range = range;
    //}

    // This is weird but it works with what I have
    // "tonic" ==> scale degree 1
    public static int getTonic(KeySignature keySignature) {
        String keyString = keySignature.isMajor() ?
                keysMajorItoS.get(keySignature.accidentals()) :
                keysMinorItoS.get(keySignature.accidentals());
        return new Pitch(keyString + "-1").value();
    }

    public int mode() { return mode; }

    public int accidentals() { return accidentals; }

    boolean isMajor() { return this.mode == 0; }

    boolean isMinor() { return !isMajor(); }

    // C Major and A minor are grouped with the sharp keys, for now.
    boolean isSharp() { return this.accidentals >= 0; }

    public boolean isFlat() { return !isSharp(); }

    @Override
    public Range getRange() { return new Range(this.range); }

    @Override
    public String toString() {
        return toString(this.mode, this.accidentals);
    }

    public static String toString(int mode, int accidentals) {
        return mode == 0
                ? String.format("%s Major", keysMajorItoS.get(accidentals))
                : String.format("%s minor", keysMinorItoS.get(accidentals));
    }


}
