package reductor.piece;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static reductor.piece.Hand.NONE;


/**
 * Represents a musical note. Follows the MIDI paradigm in terms of encoding timing and pitch.
 */
public class Note implements Ranged, Noted, Comparable<Note> {

    public static void main(String[] args) {
        Note note = Note.builder().pitch(60).start(0).stop(479).build();
    }


    Note(int pitch, Range range) {
        this.range = new Range(range);
        this.rhythm = Rhythm.fromRange(this.range);
        this.pitch = Pitch.validatePitch(pitch);
        this.instrument = "";
        this.isHeld = false;
        this.hand = NONE;
    }
    Note(String pitch) {
        this(Pitch.toInt(pitch), new Range());
    }

    // ↑ ↑ ↑ ↑ ↑ ↑  Old constructors that will be systematically and mercilessly destroyed  ↑ ↑ ↑ ↑ ↑ ↑

    private final Range range;
    private final Rhythm rhythm;

    private final int pitch;

    private final String instrument;

    private final boolean isHeld;

    private final Hand hand;

    /**
     * Primary constructor. Use a call to {@link Note#builder} for easier Note construction.
     */
    private Note(Range range, Rhythm rhythm, int pitch, String instrument, boolean isHeld, Hand hand) {
        this.range = new Range(range);
        this.rhythm = new Rhythm(rhythm);
        this.pitch = Pitch.validatePitch(pitch);
        this.instrument = instrument;
        this.isHeld = isHeld;
        this.hand = hand;

        // TODO REMOVE
        if (range.duration() < 15){ System.err.println("From Note: " + Pitch.toStr(pitch, true) + " " + range + " " + range.length()); }
    }

    /**
     * Copy constructor.
     */
    public Note(Note other) {
        this.range = new Range(other.range);
        this.rhythm = new Rhythm(other.rhythm);
        this.pitch = other.pitch;
        this.instrument = other.instrument;
        this.isHeld = other.isHeld;
        this.hand = other.hand;
    }


    /* =======
       BUILDER
     * ======= */


    /**
     * Returns a fresh instance of a {@link NoteBuilder}. Builder methods can be chained onto it.
     */
    public static NoteBuilder builder() { return new NoteBuilder(); }

    /**
     * Returns an instance of a {@link NoteBuilder} whose defaults are set to be identical to the passed Note.
     * Use for repeating sequences of similar or barely different Notes, changing as you need. Use in conjunction
     * with the {@link NoteBuilder#then()} method to chain notes together without having to manually calculate
     * start and stop ticks.
     */
    public static NoteBuilder builder(Note note) { return new NoteBuilder(note); }


    public static class NoteBuilder {

        // These first few fields are NOT given default values because they need to be handled carefully in the build
        // () function
        private Note prev = null;

        private Range range =  null;
        private Long start = null;
        private Long stop = null;

        private Rhythm rhythm = null;

        private Integer pitch = 60; // middle C (C4)

        private String instrument = "";

        private Boolean isHeld = false;

        private Hand hand = NONE;

        /**
         * See {@link Note#builder()}
         */
        private NoteBuilder() { }

        /**
         * See {@link Note#builder(Note)}. Also acts as a copy constructor when only needing to change one or two
         * things.
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

        public NoteBuilder rhythm(Rhythm rhythm) {
            this.rhythm = new Rhythm(rhythm);
            return this;
        }

        public NoteBuilder rhythm(RhythmType type) {
            this.rhythm = Rhythm.fromType(type);
            return this;
        }

        /**
         * This will only affect the final Note object if the builder itself was instantiated with another Note object.
         * Otherwise, a call to this will not have any effect on the final Note.
         */
        public NoteBuilder then() {
            if (prev != null) {
                this.start = prev.stop() + 1;
                this.stop = null; // nullify the stop, force user to set it or it will be given default later
            }
            return this;
        }

        /**
         * Give a positive int to shift up, and a negative one to shift down.
         * Better to use in conjunction with the static constants for intervallic distances in {@link Pitch}
         */
        public NoteBuilder jump(int intervallicDistance) {
            if (prev == null) { return this; }
            int shiftedPitch = this.pitch + intervallicDistance;
            this.pitch = Pitch.validatePitch(shiftedPitch);
            return this;
        }

        public NoteBuilder pitch(int pitch) {
            this.pitch = Pitch.validatePitch(pitch);
            return this;
        }

        public NoteBuilder pitch(String pitch) {
            this.pitch = Pitch.validatePitch(pitch);
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
         * This method mostly just returns the note, but do to the interdependency of start, stop, range, and rhythm,
         * some checks need to be done first.
         * <p>
         * I decided to implement it in such a way that allows the user to not have to provide everything, but still
         * get a valid object in the end (i.e. not throw exceptions). If impartial or no timing information is
         * provided, the Note will default to some variety of default MIDI quarter note, starting at tick 0.
         */
        public Note build() {

            // Create range from what was given.
            if (start == null) { this.start = 0L; }

            if (stop == null) {
                if (rhythm == null) { this.stop = this.start + 479L; }
                else { this.stop = this.start + rhythm.getDuration() - 1;}
            }

            this.range = new Range(this.start, this.stop);

            /*
             This could have been placed earlier (if rhythm == null), but involved more messy logic later. This is
             easier, and harmless to re-assign even if rhythm is already not null.
            */
            this.rhythm = Rhythm.fromRange(range);

            return new Note(this.range, this.rhythm, this.pitch, this.instrument, this.isHeld, this.hand);
        }

    }


    /* ======================================
       INSTANCE METHODS (CONVENIENCE GETTERS)
     * ====================================== */


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
       GETTERS + SETTERS
     * =================== */


    @Override public Range getRange() { return new Range(this.range); }
    public Note setRange(Range range) {
        return builder(this)
                .start(range.low())
                .stop(range.high())
                .build();
    }

    public Rhythm getRhythm() { return new Rhythm(this.rhythm); }

    public int pitch() { return this.pitch; }
    public Note setPitch(String pitch) { return this.setPitch(Pitch.toInt(pitch)); }
    public Note setPitch(int pitch) {
        return builder(this)
                .pitch(pitch)
                .build();
    }

    public boolean isHeld() { return isHeld; }
    public Note setIsHeld(boolean isHeld) {
        return builder(this)
                .isHeld(isHeld)
                .build();
    }

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

    @SuppressWarnings("UnnecessaryLocalVariable")
    @Override
    public String toString() {

        String pitchStr = this.isHeld
                ? "-"+ Pitch.toStr(this.pitch, true).toLowerCase()
                : Pitch.toStr(this.pitch, true);

        //return this.range + " " + pitchStr;
        return pitchStr;
    }


    /* ======
       STATIC
     * ====== */


    /**
     * Given something like List.of("C", "E", "G", "Bb"), pops out a bunch of Note objects.
     */
    public static ArrayList<Note> toList(List<String> strings) {
        ArrayList<Note> out = new ArrayList<>();
        for (String str : strings) { out.add( builder().pitch(str).build() ); }
        return out;
    }


}