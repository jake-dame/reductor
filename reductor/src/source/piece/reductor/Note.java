package reductor;

import java.util.ArrayList;
import java.util.List;


/**
 * Represents a musical note, including pitch and duration.
 */
public class Note implements Ranged, Noted, Comparable<Note> {


    private final Range range;
    private final long length; // can be removed eventually

    private int pitch;

    private final Rhythm rhythm;

    private boolean isHeld;

    /// Primary constructor
    Note(int pitch, Range range) {
        this.pitch = Pitch.validatePitch(pitch);
        this.range = range;
        this.length = this.range.length();
        this.rhythm = new Rhythm(this.length, Context.resolution());
        this.instrument = "";
        this.isHeld = false;
    }

    /**
     * Constructor which takes a string to assign pitch. The {@link Note#range} is assigned null.
     *
     * @param str A string describing a pitch, such as {@code "A4"}, {@code "Ab"}, {@code "A#"}, {@code "Ax3"}, or {@code "Abb-1"}.
     * @see Pitch#toInt
     */
    Note(String str) {
        this(Pitch.toInt(str), new Range());
    }

    /**
     * Constructor which takes a string to assign pitch, and a {@code Range}
     *
     * @param str A string describing a pitch, such as {@code "A4"}, {@code "Ab"}, {@code "A#"}, {@code "Ax3"}, or {@code "Abb-1"}.
     * @see Pitch#toInt
     */
    Note(String str, Range range) {
        this(Pitch.toInt(str), range);
    }



    /// Copy constructor
    Note(Note other) {
        this(other, other.getRange());
    }

    Note(int pitch) {
        this(pitch, new Range());
    }

    Note(Note other, Range range) {
        this.pitch = Pitch.validatePitch(other.pitch);
        this.range = range;
        this.length = this.range.length();
        this.rhythm = new Rhythm(this.length, Context.resolution());
        this.instrument = other.instrument;
        this.isHeld = other.isHeld;
    }

    public long length() { return this.length; }

    public int pitch() { return this.pitch; }
    private void setPitch(int val) { this.pitch = Pitch.validatePitch(pitch); }

    public long start() { return this.range.low(); }
    public long stop() { return this.range.high(); }

    public boolean isHeld() { return isHeld; }
    public void setIsHeld(boolean val) { this.isHeld = val; }

    public boolean isWhiteKey() { return Pitch.isWhiteKey(this.pitch); }
    public boolean isBlackKey() { return Pitch.isBlackKey(this.pitch); }

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

    /// Given something like List.of("C", "E", "G", "Bb"), pops out a bunch of Note objects.
    public static ArrayList<Note> toList(List<String> strings) {
        ArrayList<Note> out = new ArrayList<>();
        for (String str : strings) { out.add(new Note(str)); }
        return out;
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