package reductor.core;


import reductor.core.builders.NoteBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class Note implements Ranged, Noted, Comparable<Note> {

    private final Range range;

    private final Rhythm rhythm;

    private final Pitch pitch;

    private final String sourceInstrument;

    private final boolean isHeld;

    private final Hand hand;

    public Note(
            Range range,
            Rhythm rhythm,
            Pitch pitch,
            String instrument,
            boolean isHeld,
            Hand hand
    ) {
        this.range = new Range(range);
        this.rhythm = new Rhythm(rhythm);
        this.pitch = new Pitch(pitch);
        this.sourceInstrument = instrument;
        this.isHeld = isHeld;
        this.hand = hand;
    }


    //region getters/setters

    // Note itself is a leaf element in this interface/composite pattern
    @Override public ArrayList<Note> getNotes() {
        return new ArrayList<>(List.of(this));
    }

    @Override public Range getRange() {
        return new Range(this.range);
    }
    public Note setRange(Range range) {
        return NoteBuilder.from(this).range(range).build();
    }

    public Rhythm getRhythm() {
        return new Rhythm(this.rhythm);
    }
    public Note setRhythm(Rhythm rhythm) {
        return NoteBuilder.from(this).rhythm(rhythm).build();
    }

    public Pitch getPitch() {
        return new Pitch(this.pitch);
    }
    public Note setPitch(int pitch) {
        return NoteBuilder.from(this).pitch(pitch).build();
    }

    public boolean getIsHeld() {
        return isHeld;
    }
    public Note setIsHeld(boolean isHeld) {
        return NoteBuilder.from(this).isHeld(isHeld).build();
    }

    public String getSourceInstrument() {
        return this.sourceInstrument;
    }

    public Hand getHand() {
        return this.hand;
    }
    public Note setHand(Hand hand) {
        return NoteBuilder.from(this).hand(hand).build();
    }


    //endregion

    //region convenience


    // convenience; [0,479], the duration is 480
    public long duration() { return this.rhythm.getDuration(); }
    // convenience; [0,479], the length is 479
    public long length() { return this.range.length(); }
    // convenience
    public int start() { return this.range.getLow(); }
    // convenience
    public int stop() { return this.range.getHigh(); }
    // convenience
    public int pitch() { return this.pitch.value(); }



    //endregion

    @Override public boolean equals(Object o) {
        if (this == o) { return true; }
        if (!(o instanceof Note note)) { return false; }
        return this.pitch.value() == note.pitch.value() && this.range.compareTo(note.range) == 0;
    }
    @Override public int hashCode() {
        return Objects.hash(range, rhythm, pitch, sourceInstrument, isHeld);
    }

    @Override public int compareTo(Note other) {
        return this.pitch.compareTo(other.pitch);
    }

    @Override public String toString() {
        String pitchStr = this.pitch.toStr(true).toLowerCase();
        pitchStr = this.isHeld ? "-" + pitchStr : pitchStr;
        return pitchStr + " " + this.range;
    }


}
