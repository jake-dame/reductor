package reductor.core;


import java.util.regex.Matcher;


public class Pitch implements Comparable<Pitch> {

    private final String str;

    private final int letter;
    private final int accidental;
    private final int register;
    private final int value;


    public Pitch(String str) { this(str, true); }

    public Pitch(int midiValue) { this(PitchUtil.parseMidiValue(midiValue, true), true); }

    Pitch(Pitch other) {
        this.str = other.str;
        this.letter = other.letter;
        this.accidental = other.accidental;
        this.register = other.register;
        this.value= other.value;
    }

    private Pitch(String str, boolean unused) {

        Matcher matcher = PitchUtil.parse(str);

        this.str = str;

        String letter = matcher.group("letter").toLowerCase();
        String accidental = matcher.group("accidental");
        String register = matcher.group("register");

        accidental = accidental == null ? "" : accidental;

        final String DEFAULT_REGISTER = "-1";
        register = register == null ? "-1" : register;

        this.letter = PitchUtil.lettersStoI.get(letter);
        this.accidental = PitchUtil.accidentalsStoI.get(accidental);
        this.register = Integer.parseInt(register);

        // Special cases:
        //     b#-2    11        -2                      1 ==> 0
        //     bx-2    11        -2                      2 ==> 1
        //     abb9     9         9                     -2 ==> 127
        this.value = this.letter + ((this.register + 1) * 12) + this.accidental;
    }


    public int letter() { return letter; }
    public int accidental() { return accidental; }
    public int register() { return register; }
    public String toStr(boolean showRegister) {
        return showRegister ? this.str : this.letter + this.accidental + "";
    }

    public int value() { return this.value; }
    public int toInt() { return value(); } // convenience

    @Override
    public String toString() { return this.str; }

    @Override
    public int compareTo(Pitch o) { return Integer.compare(this.value, o.value); }


}
