package reductor;

import java.util.ArrayList;


public class Measure implements Ranged, Noted {

    private final Bucket notes;
    private ArrayList<Column> columns;

    private final Range range;

    private int measureNumber;

    private TimeSignature timeSig;
    private KeySignature keySig;

    private boolean isPickup;

    /// Primary constructor
    Measure(ArrayList<Column> columns, Range range, TimeSignature timeSig) {

        this.columns = new ArrayList<>(columns);
        this.columns.sort(null);

        ArrayList<Note> notes = new ArrayList<>();
        for (Column col : columns) {
            notes.addAll(col.getNotes());
        }
        this.notes = new Bucket(notes);

        this.range = range;
        this.timeSig = timeSig;
        this.isPickup = false;
    }

    /// Copy constructor
    Measure(Measure other) {
        this.notes = new Bucket(other.notes);
        this.measureNumber = other.measureNumber;
        this.range = other.range;
        this.timeSig = other.timeSig;
        this.keySig = other.keySig;
        this.isPickup = other.isPickup;
    }

    public int size() { return this.notes.size(); }

    public boolean isEmpty() { return this.notes.isEmpty(); }

    public long length() { return this.getRange().length(); }

    public boolean isPickup() { return this.isPickup; }
    public void setIsPickup(boolean val) { this.isPickup = val; }

    public int getMeasureNumber() { return measureNumber; }
    public void setMeasureNumber(int measureNumber) { this.measureNumber = measureNumber; }

    public TimeSignature getTimeSignature() { return this.timeSig; }
    public void setTimeSignature(TimeSignature timeSignature) { this.timeSig = timeSignature; }

    public KeySignature getKeySignature() { return new KeySignature(this.keySig); }
    public void setKeySignature(KeySignature keySignature) { this.keySig = keySignature; }

    public Column getColumn(int index) {
        return this.columns.get(index);
    }

    @Override
    public Range getRange() { return new Range(this.range); }

    @Override
    public ArrayList<Note> getNotes() { return new ArrayList<>(this.notes); }

    @Override
    public String toString() {
        return "m." + this.getMeasureNumber() + " " + this.range + ": " + this.timeSig + " and " + this.size() + " " +
                "noteList";
    }

}
