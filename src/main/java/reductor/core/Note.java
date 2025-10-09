package reductor.core;


import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static reductor.core.Hand.NONE;


public class Note implements Ranged, Noted, Comparable<Note> {


    private final Range range;
    private final Rhythm rhythm;
    private final Pitch pitch;

    private final String instrument;
    private final boolean isHeld;
    private Hand hand;


    private Note(
            Range range, Rhythm rhythm, Pitch pitch, String instrument, boolean isHeld,
            Hand hand
    ) {
        this.range = new Range(range);
        this.rhythm = new Rhythm(rhythm);
        this.pitch = new Pitch(pitch);
        this.instrument = instrument;
        this.isHeld = isHeld;
        this.hand = hand;
    }

    public Note(Note other) {
        this.range = new Range(other.range);
        this.rhythm = new Rhythm(other.rhythm);
        this.pitch = other.pitch;
        this.instrument = other.instrument;
        this.isHeld = other.isHeld;
        this.hand = other.hand;
    }


    // =======================================  BUILDER  ======================================== //


    /**
     * Returns a fresh instance of a {@link NoteBuilder}. ScorePartwiseFactory methods can be
     * chained onto it.
     */
    public static NoteBuilder builder() { return new NoteBuilder(); }

    /**
     * Returns an instance of a {@link NoteBuilder} whose defaults are set to be identical to the
     * passed Note. Use for repeating sequences of similar or barely different Notes, changing as
     * you need. Use in conjunction with the {@link NoteBuilder#then} method to chain notes
     * together without having to manually calculate start and stop ticks.
     */
    public static NoteBuilder builder(Note note) { return new NoteBuilder(note); }

    public static class NoteBuilder {


        // Note: the timing-based values must not be given defaults here. Keep them as nulls, and
        // assign defaults (e.g. 0L, 470L) in build(). They all inter-depend on each other to a
        // degree.

        private Note prev = null;
        private Range range = null;
        private Long start = null;
        private Long stop = null;
        private Rhythm rhythm = null;

        private Pitch pitch = new Pitch(60); // middle C (C4)
        private String instrument = "";
        private Boolean isHeld = false;
        private Hand hand = NONE;


        private NoteBuilder() { }

        /**
         * See {@link Note#builder(Note)}. Also acts as a copy constructor when only needing to
         * change one or two things.
         */
        private NoteBuilder(Note other) {
            this.prev = new Note(other);
            this.range = this.prev.range;
            this.start = this.prev.start();
            this.stop = this.prev.stop();
            this.rhythm = this.prev.rhythm;
            this.pitch = this.prev.pitch;
            this.instrument = this.prev.instrument;
            this.isHeld = this.prev.isHeld;
            this.hand = this.prev.hand;
        }


        public NoteBuilder start(long tick) {
            this.start = tick;
            return this;
        }

        public NoteBuilder stop(long tick) {
            this.stop = tick;
            return this;
        }

        // You are allowed to call start().stop(), or between() --  in any order or together,
        //     but the last called is what sticks.
        public NoteBuilder between(long start, long stop) {
            this.start = start;
            this.stop = stop;
            return this;
        }

        public NoteBuilder rhythm(Rhythm rhythm) {
            this.rhythm = new Rhythm(rhythm);
            return this;
        }

        public NoteBuilder rhythm(RhythmTypeAlpha type) {
            this.rhythm = Rhythm.fromType(type);
            return this;
        }

        public NoteBuilder pitch(Pitch pitch) {
            this.pitch = pitch;
            return this;
        }

        public NoteBuilder pitch(int val) {
            this.pitch = new Pitch(val);
            return this;
        }

        public NoteBuilder pitch(String s) {
            this.pitch = new Pitch(s);
            return this;
        }

        public NoteBuilder instrument(String instrument) {
            this.instrument = instrument;
            return this;
        }

        public NoteBuilder isHeld(boolean isHeld) {
            this.isHeld = isHeld;
            return this;
        }

        public NoteBuilder hand(Hand hand) {
            this.hand = hand;
            return this;
        }


        /**
         * This will only affect the final Note object if the builder itself was instantiated
         * with another Note object.
         * Otherwise, a call to this will not have any effect on the final Note.
         */
        public NoteBuilder then() {
            if (prev != null) {
                this.start = prev.stop() + 1;
                // nullify the stop, force user to set it or it will be given default later
                this.stop = null;
            }
            return this;
        }

        /**
         * Give a positive int to shift up, and a negative one to shift down.
         * Better to use in conjunction with the static constants for intervallic distances in
         * {@link PitchUtil}
         */
        public NoteBuilder jump(int intervallicDistance) {
            if (prev == null) { return this; }
            this.pitch = new Pitch(this.pitch.toInt() + intervallicDistance);
            return this;
        }


        /**
         * This method mostly just returns the note, but do to the interdependency of start,
         * stop, range, and rhythm, some checks need to be done first.
         * <p>
         * I decided to implement it in such a way that allows the user to not have to provide
         * everything, but still get a valid object in the end (i.e. not throw exceptions). If
         * impartial or no timing information is provided, the Note will default to some variety
         * of default MIDI quarter note, starting at tick 0.
         */
        public Note build() {

            if (start == null) {
                this.start = 0L;
            }

            if (stop == null) {

                if (rhythm == null) {
                    this.stop = this.start + 479L;
                } else {
                    this.stop = this.start + rhythm.getDuration() - 1;
                }

            }

            this.range = new Range(this.start, this.stop);

            /*
             This could have been placed earlier (if rhythm == null), but involved more messy
             logic later. This is
             easier, and harmless to re-assign even if rhythm is already not null.
            */
            this.rhythm = Rhythm.fromRange(range);

            return new Note(
                    this.range, this.rhythm, this.pitch, this.instrument, this.isHeld,
                    this.hand
            );
        }


    }


    // ===================================  INSTANCE METHODS  =================================== //


    /**
     * Convenience method. For {@code [0,479]}, the duration is {@code 480 }.
     *
     * @see Note#length()
     */
    public long duration() { return this.rhythm.getDuration(); }

    /**
     * Convenience method. For {@code [0,479]}, the length is {@code 479 }.
     *
     * @see Range#length()
     * @see Note#duration()
     */
    public long length() { return this.range.length(); }

    /**
     * Convenience method that calls this Note's {@link Range#low()}.
     */
    public long start() { return this.range.low(); }

    /**
     * Convenience method that calls this Note's {@link Range#high()}.
     */
    public long stop() { return this.range.high(); }

    public int pitch() { return this.pitch.value(); }

    @Override
    public Range getRange() { return new Range(this.range); }

    public Note setRange(Range range) {
        return builder(this)
                .start(range.low())
                .stop(range.high())
                .build();
    }

    public Rhythm getRhythm() { return new Rhythm(this.rhythm); }

    public Pitch getPitch() { return this.pitch; }

    public Note setPitch(int pitch) {
        return builder(this)
                .pitch(new Pitch(pitch))
                .build();
    }

    public boolean isHeld() { return isHeld; }

    public Note setIsHeld(boolean isHeld) {
        return builder(this)
                .isHeld(isHeld)
                .build();
    }

    public String getInstrument() { return this.instrument; }

    public Hand getHand() { return this.hand; }


    // ======================================  OVERRIDES  ======================================= //


    /**
     * Compares two Note objects by pitch only.
     */
    @Override
    public int compareTo(Note other) { return this.pitch.compareTo(other.pitch); }

    /**
     * Returns this, wrapped in a List, to conform with the {@link Noted}.
     * Note is a "leaf" of the {@link Noted} composite design pattern.
     */
    @Override
    public ArrayList<Note> getNotes() { return new ArrayList<>(List.of(this)); }

    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (!(o instanceof Note note)) { return false; }
        return pitch == note.pitch
                && isHeld == note.isHeld
                && Objects.equals(range, note.range)
                && Objects.equals(rhythm, note.rhythm)
                && Objects.equals(instrument, note.instrument);
    }

    @Override
    public int hashCode() { return Objects.hash(range, rhythm, pitch, instrument, isHeld); }

    @Override
    public String toString() {
        String pitchStr = this.pitch.toStr(true).toLowerCase();
        pitchStr = this.isHeld ? "-" + pitchStr : pitchStr;
        return pitchStr + " " + this.range;
    }


    // =========================================  DEV  ========================================== //


    public RhythmTypeBeta devGetRhy() { return RhythmTypeBeta.type(this.range); }

    public Hand devGetHand() { return this.hand; }

    public void devSetHand(Hand val) { this.hand = val; }


}
