package reductor;

import java.util.ArrayList;


public class Measure implements Ranged, Noted {

    private final NoteList noteList;

    private final Range range;

    private int measureNumber;

    private TimeSignature timeSig;
    private KeySignature keySig;

    private boolean isPickup;

    /// Primary constructor
    Measure(Range range, TimeSignature timeSignature, NoteList noteList) {
        this.noteList = noteList;
        this.range = range;
        this.timeSig = timeSignature;
        this.isPickup = false;
    }

    /// Copy constructor
    Measure(Measure other) {
        this.noteList = new NoteList(other.noteList);
        this.measureNumber = other.measureNumber;
        this.range = other.range;
        this.timeSig = other.timeSig;
        this.keySig = other.keySig;
        this.isPickup = other.isPickup;
    }

    public int size() { return this.noteList.size(); }

    public boolean isEmpty() { return this.noteList.isEmpty(); }

    public long length() { return this.getRange().length(); }

    public boolean isPickup() { return this.isPickup; }
    public void setIsPickup(boolean val) { this.isPickup = val; }

    public int getMeasureNumber() { return measureNumber; }
    public void setMeasureNumber(int measureNumber) { this.measureNumber = measureNumber; }

    public TimeSignature getTimeSignature() { return this.timeSig; }
    public void setTimeSignature(TimeSignature timeSignature) { this.timeSig = timeSignature; }

    public KeySignature getKeySignature() { return new KeySignature(this.keySig); }
    public void setKeySignature(KeySignature keySignature) { this.keySig = keySignature; }


    @Override
    public Range getRange() { return new Range(this.range); }

    @Override
    public ArrayList<Note> getNotes() { return new ArrayList<>(this.noteList.getBackingList()); }

    @Override
    public void setNotes(ArrayList<Note> notes) { }

    @Override
    public String toString() {
        return "m." + this.getMeasureNumber() + " " + this.range + ": " + this.timeSig + " and " + this.size() + " " +
                "noteList";
    }

}
