package reductor;

import java.util.ArrayList;
import java.util.List;


public class MutableNote implements Ranged, Noted, Comparable<MutableNote> {

    private Range range;
    private Rhythm rhythm;

    private int pitch;

    private long start;
    private long stop;

    private boolean isHeld;

    MutableNote() {
        this.start = -1;
        this.pitch = -1;
        this.rhythm = null;
        this.isHeld = false;
    }

    public long start() { return this.range.low(); }
    public MutableNote setStart(long tick) {
        this.start = tick;
        return this;
    }

    public long stop() { return this.range.high(); }

    public Rhythm rhythm() { return this.rhythm; }
    public MutableNote setRhythm(RhythmBase rhythmBase) {
        this.rhythm = new Rhythm(RhythmBase.getDuration(rhythmBase));
        if (this.range == null) { this.range = Rhythm.toRange(this.rhythm, this.start()); }
        return this;
    }

    public MutableNote setRhythm(Rhythm rhythm) {
        this.rhythm = rhythm; // TODO copy constructor for rhythm
        if (this.range == null) { this.range = Rhythm.toRange(this.rhythm, this.start()); }
        return this;
    }


    public int pitch() { return this.pitch; }
    public MutableNote setPitch(String str) { return this.setPitch(Pitch.toInt(str)); }
    public MutableNote setPitch(int pitch) {
        this.pitch = Pitch.validatePitch(pitch);
        return this;
    }

    public boolean isHeld() { return isHeld; }
    MutableNote setHeld(boolean held) {
        isHeld = held;
        return this;
    }

    public Range range() { return this.range; }
    public MutableNote setRange(Range range) {
        this.range = range;
        if (this.rhythm == null) { this.rhythm = Rhythm.fromRange(this.range); }
        return this;
    }

    /// Give a positive int to shift up, and a negative one to shift down.
    public void shift(int intervallicDistance) {
        int shiftedPitch = this.pitch + intervallicDistance;
        this.pitch = Pitch.validatePitch(shiftedPitch);
    }

    public long duration() { return this.range.length(); }

    public boolean isWhiteKey() { return Pitch.isWhiteKey(this.pitch); }
    public boolean isBlackKey() { return Pitch.isBlackKey(this.pitch); }

    public void octaveUp() { this.pitch = pitch + 12; }
    public void octaveDown() { this.pitch = pitch - 12; }

    /*=========
     * OVERRIDES
     * =======*/

    /// Note objects are compared by pitch only.
    @Override
    public int compareTo(MutableNote other) { return Integer.compare(this.pitch, other.pitch()); }


    @Override
    public Range getRange() { return this.range != null ? new Range(this.range) : null; }

    @Override
    public ArrayList<Note> getNotes() {
        ArrayList<Note> out = new ArrayList<>();
        out.add( new Note(this.pitch, this.range) );
        return out;
    }

    @Override
    public void setNotes(ArrayList<Note> notes) { }

    @Override
    public String toString() {

        String pitchStr = this.isHeld
                ? "-"+ Pitch.toStr(this.pitch, true).toLowerCase()
                : Pitch.toStr(this.pitch, true);

        return this.range + " " + pitchStr;
    }

}
