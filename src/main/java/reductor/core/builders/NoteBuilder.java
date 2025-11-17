package reductor.core.builders;


import reductor.core.*;
import reductor.util.PitchUtil;


public class NoteBuilder {

    private Rhythm rhythm = null;
    private Pitch pitch = null;
    private String instrument = null;
    private Boolean isHeld = null;
    private Hand hand = null;

    private Range range = null;
    // If the user does not supply range but adds these
    // Range objects don't have setters, so must be constructed all at once
    private Integer start = null;
    private Integer stop = null;

    public NoteBuilder(){}

    public static NoteBuilder builder() { return new NoteBuilder(); }


    //region builder methods

    public NoteBuilder range(Range range) {
        this.range = range;
        return this;
    }
    public NoteBuilder range(Integer start, Integer stop) {
        return this.range(new Range(start, stop));
    }

    public NoteBuilder start(Integer v) {
        this.start = v;
        return this;
    }
    public NoteBuilder start(int v) {
        return this.start(Integer.valueOf(v));
    }

    public NoteBuilder stop(Integer v) {
        this.stop = v;
        return this;
    }
    public NoteBuilder stop(int v) {
        return this.stop(Integer.valueOf(v));
    }

    public NoteBuilder rhythm(Rhythm v) {
        this.rhythm = new Rhythm(v);
        return this;
    }
    public NoteBuilder rhythm(RhythmType v) {
        return this.rhythm(Rhythm.fromType(v));
    }

    public NoteBuilder pitch(Pitch v) {
        this.pitch = v;
        return this;
    }
    public NoteBuilder pitch(int v) {
        return this.pitch(new Pitch(v));
    }
    public NoteBuilder pitch(String v) {
        return this.pitch(new Pitch(v));
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

    //endregion


    //region build

    public Note build() {
        if (start == null) { this.start = 0; }
        if (stop == null) {
            this.stop = rhythm == null ?
                    this.stop = this.start + 1
                    : this.start + rhythm.getDuration() - 1;
        }

        if (this.range == null) {
           this.range = new Range(this.start, this.stop);
        }

        //// eagerly assigning this allows the start or stop or range valid Note
        //this.range = new Range(this.start, this.stop);

        /*
         This could have been placed earlier (if rhythm == null), but involved more messy
         logic later. This is
         easier, and harmless to re-assign even if rhythm is already not null.
        */
        this.rhythm = Rhythm.fromRange(range);

        if (this.pitch == null) { this.pitch = new Pitch("C4"); }
        if (this.isHeld == null) { this.isHeld = false; }
        if (this.instrument == null) { this.instrument = ""; }
        if (this.hand == null) { this.hand = Hand.NONE; }

        return new Note(
                this.range,
                this.rhythm,
                this.pitch,
                this.instrument,
                this.isHeld,
                this.hand
        );
    }

    //endregion


    //region convenience

    public static NoteBuilder from(Note other) {

        Range r = other.getRange();

        Range range = r != null ? new Range(other.getRange()) : null;
        Integer low = r != null ? other.getRange().getLow() : null;
        Integer high = r != null ? other.getRange().getHigh() : null;

        return NoteBuilder.builder()
                .range(range)
                .start(low)
                .stop(high)
                .rhythm(other.getRhythm())
                .pitch(other.getPitch())
                .instrument(other.getSourceInstrument())
                .isHeld(other.getIsHeld())
                .hand(other.getHand());
    }

    public static Note of(String pitch, int start, int stop) {
        return NoteBuilder.builder()
                .pitch(pitch)
                .range(new Range(start, stop))
                .build();
    }

    /**
     * Useful for chaining identical notes together without having to do tick math.
     * Copies the passed note and automatically sets its start to 1 tick after the passed note's stop.
     */
    public static NoteBuilder then(Note n) {
        return NoteBuilder.from(n).start(n.stop() + 1).stop(null);
    }

    /**
     * Give a positive int to shift up, and a negative one to shift down.
     * Better to use in conjunction with the static constants for intervallic distances in
     * {@link PitchUtil}
     */
    public static NoteBuilder jump(Note n, int intervallicDistance) {
        Pitch pitch = new Pitch(n.getPitch().toInt() + intervallicDistance);
        return NoteBuilder.from(n).pitch(pitch);
    }

    //endregion


}
