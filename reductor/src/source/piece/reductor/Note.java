package reductor;

import java.util.ArrayList;


/**
 * Represents a musical note, including pitch and duration.
 */
public final class Note implements Ranged, Comparable<Note>, Noted {


    private final Range range;
    private final long length; // TODO: can be removed eventually

    private int pitch;

    private Rhythm rhythm;

    private boolean isHeld;

    /// Primary constructor
    Note(int pitch, Range range) {
        this.range = range;
        this.length = this.range.length();

        assignPitch(pitch);
        this.rhythm = new Rhythm(this.length, Context.resolution());
        this.instrument = "";
        this.isHeld = false;
    }

    /// Copy constructor with another range.
    Note(Note other, Range range) {
        this.range = range;
        this.length = this.range.length();

        assignPitch(other.pitch);
        this.rhythm = other.rhythm;
        this.instrument = other.instrument;
        this.isHeld = other.isHeld;
    }

    /**
     * Constructor which takes a string to assign pitch. The {@link Note#range} is assigned null.
     *
     * @param str A string describing a pitch, such as {@code "A4"}, {@code "Ab"}, {@code "A#"}, {@code "Ax3"}, or {@code "Abb-1"}.
     * @see Pitch#toInt
     */
    Note(String str) { this(Pitch.toInt(str), new Range()); }

    /**
     * Constructor which takes a string to assign pitch, and a {@code Range}
     *
     * @param str A string describing a pitch, such as {@code "A4"}, {@code "Ab"}, {@code "A#"}, {@code "Ax3"}, or {@code "Abb-1"}.
     * @see Note#Note(String)
     */
    Note(String str, Range range) { this(Pitch.toInt(str), range); }

    /// Copy constructor
    Note(Note other) { this(other, other.getRange()); }

    private void assignPitch(int val) {
        if (val < 0 || val > 127) { throw new IllegalArgumentException("invalid pitch for note; must be in [0,127]"); }
        this.pitch = val;
        //this.pitch = clampToPianoRange(val); // can make decision later
    }


    public long length() { return this.length; }

    public int pitch() { return this.pitch; }

    public long start() { return this.range.low(); }
    public long stop() { return this.range.high(); }

    //public <T extends Noted & Ranged> boolean isHeld(T container) {
    //    return this.range.low() < container.getRange().low();
    //}

    public void setIsHeld(boolean val) { this.isHeld = val; }

    public boolean isWhiteKey() { return Pitch.isWhiteKey(this.pitch); }
    public boolean isBlackKey() { return !isWhiteKey(); } // not necessary but nice to have mnemonically

    public void octaveUp() { this.pitch = pitch + 12; }
    public void octaveDown() { this.pitch = pitch - 12; }

    /*=========
    * OVERRIDES
    * =======*/

    /// Note objects are compared by pitch only.
    @Override
    public int compareTo(Note other) { return Integer.compare(this.pitch, other.pitch()); }

    @Override
    public Range getRange() { return this.range != null ? new Range(this.range) : null; }

    //@Override
    //public boolean equals(Object o) {
    //    if (this == o) {
    //        return true;
    //    }
    //    if (!(o instanceof Note note)) {
    //        return false;
    //    }
    //    return pitch == note.pitch && length == note.length && Objects.equals(range, note.range) && Objects.equals(instrument, note.instrument);
    //}
    //
    //@Override
    //public int hashCode() {
    //    return Objects.hash(range, pitch, length, instrument);
    //}

    @Override
    public String toString() {

        String pitchStr = this.isHeld
                ? "-"+ Pitch.toStr(this.pitch, true).toLowerCase()
                : Pitch.toStr(this.pitch, true);

        return this.range + " " + pitchStr;

    }

    /*======
    * STATIC
    * ====*/

    public static int clampToPianoRange(int pitch) {

        final int PIANO_MAX_PITCH = 108;
        final int PIANO_MIN_PITCH = 21;
        final int OCTAVE = 12;

        if (pitch < PIANO_MIN_PITCH) {
            while (pitch < PIANO_MIN_PITCH) { pitch += OCTAVE; }
        }

        if (pitch > PIANO_MAX_PITCH) {
            while (pitch > PIANO_MAX_PITCH) { pitch -= OCTAVE;}
        }

        return pitch;
    }

    /*===
    * DEV
    * =*/

    private String instrument;

    Note(int pitch, Range range, String instrument) {
        this(pitch, range);
        this.instrument = instrument;
    }

    public String getInstrument() { return this.instrument; }

    @Override
    public ArrayList<Note> getNotes() {
        ArrayList<Note> out = new ArrayList<>();
        out.add(this);
        return out;
    }

    @Override
    public void setNotes(ArrayList<Note> notes) { }

}