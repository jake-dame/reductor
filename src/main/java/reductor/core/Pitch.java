package reductor.core;


import java.util.regex.Matcher;


public class Pitch implements Comparable<Pitch> {

    private final String str;

    private final int letter;
    private final int accidental;
    private final int register;
    private final int value;


    Pitch(String str) { this(str, true); }

    Pitch(int midiValue) { this(PitchUtil.parseMidiValue(midiValue, true), true); }

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

        var l = matcher.group("letter").toUpperCase();
        var a = matcher.group("accidental");
        var r = matcher.group("register");

        final String defaultAccidental = "";
        final String defaultRegister = "4";
        a = a == null ? defaultAccidental : a;
        r = r == null ? defaultRegister : r;

        this.letter = PitchUtil.lettersStoI.get(l);
        this.accidental = PitchUtil.accidentalsStoI.get(a);
        this.register = Integer.parseInt(r);
        this.value = letter + ((register + 1) * 12) + accidental;
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
