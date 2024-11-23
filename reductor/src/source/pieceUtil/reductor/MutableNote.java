package reductor;

import java.util.ArrayList;


public class MutableNote implements Ranged, Noted, Comparable<MutableNote> {

    private Range range;
    private long start;
    private Rhythm rhythm;

    private int pitch;

    private final String instrument;
    private boolean isHeld;


    MutableNote() {

        this.range = null;
        this.start = -1;
        this.rhythm = null;

        this.pitch = -1;

        this.instrument = "";
        this.isHeld = false;
    }


    /* =========================
       BUILDER METHODS / SETTERS
     * ========================= */

    public MutableNote setRange(Range range) {
        this.range = range;
        this.rhythm = Rhythm.fromRange(this.range);
        return this;
    }
    public MutableNote setStart(long tick) {
        this.start = tick;
        return this;
    }
    public MutableNote setRhythm(RhythmBase rhythmBase) {
        this.rhythm = Rhythm.fromType(rhythmBase);
        this.range = Rhythm.toRange(this.start(), this.rhythm);
        return this;
    }

    public MutableNote setPitch(int pitch) {
        this.pitch = Pitch.validatePitch(pitch);
        return this;
    }

    public MutableNote setPitch(String str) {
        return this.setPitch(Pitch.toInt(str));
    }

    public MutableNote setHeld(boolean held) {
        isHeld = held;
        return this;
    }


    /* ================
       INSTANCE METHODS
     * ================ */


    /// Give a positive int to shift up, and a negative one to shift down.
    public void shift(int intervallicDistance) {
        int shiftedPitch = this.pitch + intervallicDistance;
        this.pitch = Pitch.validatePitch(shiftedPitch);
    }

    public void octaveUp() { this.pitch = pitch + 12; }
    public void octaveDown() { this.pitch = pitch - 12; }


    /* =======
       GETTERS
     * ======= */


    public Range range() { return this.range; }
    public long start() { return this.range.low(); }
    public long stop() { return this.range.high(); }
    public Rhythm rhythm() { return this.rhythm; }

    public int pitch() { return this.pitch; }

    public String instrument() { return this.instrument; }
    public boolean isHeld() { return isHeld; }

    public long duration() { return this.range.length(); }

    public boolean isWhiteKey() { return Pitch.isWhiteKey(this.pitch); }
    public boolean isBlackKey() { return Pitch.isBlackKey(this.pitch); }


    /* =========
       OVERRIDES
     * ========= */

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
    public String toString() {

        String pitchStr = this.isHeld
                ? "-"+ Pitch.toStr(this.pitch, true).toLowerCase()
                : Pitch.toStr(this.pitch, true);

        return this.range + " " + pitchStr;
    }

    //public static MutableNote then(MutableNote previous) {
    //
    //    if (previous == null  ||  previous.range == null) {
    //        throw new IllegalStateException("bad note in then chain: " + previous);
    //    }
    //
    //    return new MutableNote().setStart(previous.stop() + 1);
    //}
    //
    //public static MutableNote then(String pitch, RhythmBase rhythm) {
    //
    //    return new MutableNote().setStart(previous.stop() + 1);
    //
    //}

    /**
     * @param pitch The pitch
     * @param rhythm The rhythm
     * @param deltaTime The time after this note ends you wish the next (output) note to begin. 0 for no gap.
     * @return A new instance of a MutableNote "chained" to the last.
     * @see Pitch#toStr
     * @see RhythmBase
     */
    public MutableNote then(String pitch, RhythmBase rhythm, long deltaTime) {
        return new MutableNote().setStart(this.stop() + 1 + deltaTime);
    }

    /*
        ArrayList<Note> m1 = Sequence.ofNotes(0) // parameter is startTick
            .then("f#", QUARTER, 0)
            .then("f#", QUARTER, 0)
            .then("g", QUARTER, 0)
            .then("a", QUARTER, 0); // this should stop at tick 1919

         Also, would be SO cool to allow Pitch value to be negative to indicate down, like:

         // Alberti demo
         // Note: if you only give an int to then(), it is PITCH
         ArrayList<Note> measure1 = Sequence.ofNotes(0) // parameter is startTick
            .then("c", QUARTER, 0)
            .then(p5) // up a p5
            .then(-m3) // down a m3
            .then(m3); // up a p5

        You would just have to validatePitch(this.pitch + pitch) in then().
        OR clamp...

        ArrayList<Chord> m1_LH = Sequence.ofChords(0)
            .then(QUARTER, "f#", "a#", "c#") // because vararg has to be last...
            .then(QUARTER, "f#", "b", "d#")
            .then(QUARTER, "f#", "a#", "c#")
            .then(QUARTER, "e#", "b", "c#")

            OR

        ArrayList<Chord> m1_LH = Sequence.ofChords(0)
            .then(EIGHTH, "g3, "b3", "e4")
            .thenRepeat() // no parameter is a copy constructor essentially with then()'s start-chaining capabilities

            // OR

            .thenRepeat(7); // repeat seven more times






     */


}
