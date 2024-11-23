package reductor;

public class Rhythm {


    // in enum ordinal order: whole, half, quarter, 8th, 16th, 32nd, 64th, 128th
    // `base` is the nearest note value in that list _without going over_
    private RhythmBase base;
    // This can be gotten from the enum at any time, just nice to have calculated once
    private final long baseDuration;
    // The duration passed to the constructor
    private final long actualDuration;

    private boolean isTriplet;
    // There is absolutely no way to tell between these two from MIDI data, for equivalent durations
    // If it's not a triplet or dotted, it gets flagged as some sort of tied
    private boolean isDotted;
    private boolean isTied;
    // Trill, tremolo, grace, turn, etc. Anything less than a 32nd note.
    private boolean isOrnament;


    public static Rhythm fromRange(Range range) {
        return new Rhythm(range.length() + 1);
    }

    public static Rhythm fromType(RhythmBase enumVal) {
        return new Rhythm(enumVal.getDuration());
    }

    private Rhythm(long actualDuration) {

        this.base = RhythmBase.getEnumType(actualDuration);
        this.actualDuration = actualDuration;
        this.baseDuration = this.base.getDuration();

        checkTriplet();

        /*
         if this is:
            == 0: we are done. it is exactly a quarter, 16th, etc.
             > 0: it is greater than its base, so it is some kind of dotted
             < 0: if exactly half, then dotted; if not, some kind of tie
        */
        if (!isTriplet) {
            long remainder = Math.abs(this.baseDuration - this.actualDuration);
            if (remainder != 0) {
                if (remainder == baseDuration / 2) {
                    isDotted = true;
                } else {
                    isTied = true;
                }
            }
        }

        if (this.base.compareTo(RhythmBase.THIRTY_SECOND) > 0) {
            isOrnament = true;
        }

    }

    private void checkTriplet() {

        /*
         Yes this is messy, but here's what it does (using triplet EIGHTHs as an example:

         1.) The base is assigned so that, based on the passed duration, the nearest rhythm value is assigned, _without
         going over_. This is so that the Rhythm class can use the base as a "species" of sort.
                + "This note is some species of EIGHTH note: tied, dotted, tripleted, etc.
         2.) However, due to how triplets work, a triplet EIGHTH is actually defined by being part of a trio that fits
          into a QUARTER note, i.e., the next biggest rhythm value.
                + Triplet HALF noteList fit into a WHOLE note
                + Triple QUARTER noteList fit into a HALF note
                + etc.
         3.) But, because the actual duration of triplet noteList are LESS than their species version (i.e. a triplet
         EIGHTH is smaller in duration than an EIGHTH), they actually get assigned the next smallest base/species (e
         .g. a triplet EIGHTH gets assigned as "some form of SIXTEENTH")
         4.) To check for triplet values, we need to check if something is exactly (or within some threshold of)
         one-third the value of the ENCLOSING base (e.g. a QUARTER encloses the duration of 3 tripleted EIGHTHs);
         since we are already bumped down to a SIXTEENTH due to the fact that the duration is less than an EIGHTH, we
         have to actually check up 2.
         5.) This also just leads to some messy boundary checks due to the limitations of the enum values array itself

         So:
            + If the passed duration is exactly one-third the value of the base TWO bases up from me, I am a triplet of
            that base.

        */

        RhythmBase enclosingRhythm;

        int ordinal = this.base.ordinal();
        if (ordinal == 0) {
            // this is a WHOLE note
            return;
        } else if (ordinal == 1) {
            // this is a HALF note
            enclosingRhythm = RhythmBase.values()[ordinal - 1];
        } else {
            // this is anything else
            enclosingRhythm =  RhythmBase.values()[ordinal - 2];
        }

        var tripletedValue = enclosingRhythm.getDuration() / 3;
        if (actualDuration == tripletedValue) {
            isTriplet = true;
            // Re-assign to be more accurate ("some species of")
            this.base = RhythmBase.values()[base.ordinal() - 1];
        }

    }

    public long getDuration() { return this.actualDuration; }

    public boolean isTriplet() { return this.isTriplet; }
    public boolean isDotted() { return this.isDotted; }
    public boolean isTied() { return this.isTied; }
    public boolean isOrnament() { return this.isOrnament; }

    @Override public String toString() {
        String str = "";
        if (isDotted) { str += "DOTTED"; }
        if (isTriplet) { str += "TRIPLET"; }
        if (isTied) { str += "TIED "; }
        return str + " " + this.base;
    }

    public static Range toRange(long startTick, Rhythm rhythm) {
        return new Range(startTick, rhythm.getDuration());
    }


}