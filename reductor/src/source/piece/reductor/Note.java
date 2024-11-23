package reductor;

import java.util.ArrayList;
import java.util.List;


/**
 * Represents a musical note, including pitch and duration.
 */
public class Note implements Ranged, Noted, Comparable<Note> {


    private final Range range;
    private final Rhythm rhythm;

    private final int pitch;

    private final String instrument;
    private final boolean isHeld;

    /// Primary constructor which takes a pitch and a {@link Range}
    Note(int pitch, Range range) {

        this.range = new Range(range);
        this.rhythm = Rhythm.fromRange(this.range);

        this.pitch = Pitch.validatePitch(pitch);

        this.instrument = "";
        this.isHeld = false;
    }

    /// Pitch-only constructor; assigns default {@link Range}
    Note(int pitch) {
        this(pitch, new Range());
    }

    /**
     * Constructor which takes a string to assign pitch, and a {@link Range}.
     *
     * @param pitch A string describing a pitch, such as {@code "A4"}, {@code "Ab"}, {@code "A#"}, {@code "Ax3"}, or {@code "Abb-1"}.
     * @see Pitch#toInt
     */
    Note(String pitch, Range range) {
        this(Pitch.toInt(pitch), range);
    }

    /**
     * Pitch-only constructor; assigns default {@link Range}.
     *
     * @param pitch A string describing a pitch, such as {@code "A4"}, {@code "Ab"}, {@code "A#"}, {@code "Ax3"}, or {@code "Abb-1"}.
     * @see Pitch#toInt
     */
    Note(String pitch) {
        this(Pitch.toInt(pitch), new Range());
    }

    /// Copy constructor
    Note(Note other) {

        this.range = new Range(other.range);
        this.rhythm = other.rhythm;

        this.pitch = other.pitch;

        this.instrument = other.instrument;
        this.isHeld = other.isHeld;
    }

    /// Copy constructor for "settable" pitch
    Note(Note other, int pitch) {

        this.range = new Range(other.range);
        this.rhythm = other.rhythm;

        this.pitch = pitch;

        this.instrument = other.instrument;
        this.isHeld = other.isHeld;
    }

    /// Copy constructor for "settable" range
    Note(Note other, Range range) {

        this.range = new Range(range);
        this.rhythm = other.rhythm;

        this.pitch = other.pitch;

        this.instrument = other.instrument;
        this.isHeld = other.isHeld;
    }

    /// Copy constructor for "settable" isHeld
    Note(Note other, boolean isHeld) {

        this.range = new Range(other.range);
        this.rhythm = other.rhythm;

        this.pitch = other.pitch;

        this.instrument = other.instrument;
        this.isHeld = isHeld;
    }


    /* =======
       BUILDER
     * ======= */


    /* ===================
       CONVENIENCE METHODS
     * =================== */


    /**
     * Convenience method to get the (inclusive) length of the duration of this Note's {@link Rhythm}
     */
    public long duration() { return this.rhythm.getDuration(); }
    /**
     * Convenience method to get the (half-open) length of the MIDI range of this Note's {@link Range}
     */
    public long length() { return this.range.length(); }
    /**
     * Convenience method equivalent to {@code getRange().low()}.
     */
    public long start() { return this.range.low(); }
    /**
     * Convenience method equivalent to {@code getRange().high()}.
     */
    public long stop() { return this.range.high(); }
    /**
     * Convenience method that passes the call on to the {@link Pitch} utility class.
     */
    public boolean isWhiteKey() { return Pitch.isWhiteKey(this.pitch); }
    /**
     * Convenience method that passes the call on to the {@link Pitch} utility class.
     */
    public boolean isBlackKey() { return Pitch.isBlackKey(this.pitch); }


    /* ===================
       GETTERS + "SETTER"S
     * =================== */


    @Override public Range getRange() { return new Range(this.range); }
    public Note setRange(Range range) { return new Note(this, range); }

    public int pitch() { return this.pitch; }
    private Note setPitch(int pitch) { return new Note(this, pitch); }

    public boolean isHeld() { return isHeld; }
    public Note setIsHeld(boolean isHeld) { return new Note(this, isHeld); }

    public String getInstrument() { return this.instrument; }


    /* =========
       OVERRIDES
     * ========= */

    /**
     * Compares two Note objects by pitch only.
     */
    @Override
    public int compareTo(Note other) { return Integer.compare(this.pitch, other.pitch()); }

    /**
     * Returns this, wrapped in a List, to conform with the {@link Noted}.
     * Note is a "leaf" of the {@link Noted} composite design pattern.
     */
    @Override
    public ArrayList<Note> getNotes() { return new ArrayList<>( List.of(this) ); }

    @Override
    public String toString() {

        String pitchStr = this.isHeld
                ? "-"+ Pitch.toStr(this.pitch, true).toLowerCase()
                : Pitch.toStr(this.pitch, true);

        return this.range + " " + pitchStr;
        //return pitchStr;
    }


    /* ======
       STATIC
     * ====== */


    /// Given something like List.of("C", "E", "G", "Bb"), pops out a bunch of Note objects.
    public static ArrayList<Note> toList(List<String> strings) {
        ArrayList<Note> out = new ArrayList<>();
        for (String str : strings) { out.add(new Note(str)); }
        return out;
    }


}