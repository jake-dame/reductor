package reductor;

import java.sql.Time;
import java.util.ArrayList;


public class Measure implements Ranged, Noted {

    private final Bucket notes;
    private final ArrayList<Column> columns;

    private final Range range;

    private Integer number;

    private final TimeSignature timeSig;

    private Boolean isPickup;

    /// Primary constructor
    Measure(ArrayList<Column> columns, Range range, TimeSignature timeSig) {

        this.columns = new ArrayList<>(columns);
        this.columns.sort(null);

        ArrayList<Note> notes = new ArrayList<>();
        for (Column col : columns) { notes.addAll(col.getNotes()); }
        this.notes = new Bucket(notes);

        this.range = range;
        this.timeSig = timeSig;

        this.isPickup = null;
        this.number = null;
    }

    /// Copy constructor
    Measure(Measure other) {
        this.notes = new Bucket(other.getNotes());
        this.columns = new ArrayList<>(other.columns);
        this.columns.sort(null);
        this.number = other.number;
        this.range = other.range;
        this.timeSig = other.timeSig;
        this.isPickup = other.isPickup;
    }

    public int size() { return this.notes.size(); }

    public boolean isEmpty() { return this.notes.isEmpty(); }

    public long length() { return this.getRange().length(); }

    public boolean isPickup() { return this.isPickup; }
    public boolean setIsPickup(boolean val) {

        if (this.isPickup == null) {
            this.isPickup = val;
            return true;
        }

        return false;
    }

    public int getMeasureNumber() { return number; }
    public boolean setMeasureNumber(int measureNumber) {

        if (this.number == null) {
            this.number = measureNumber;
            return true;
        }

        return false;
    }

    public TimeSignature getTimeSignature() { return new TimeSignature(this.timeSig); }

    public Column getColumn(int index) { return this.columns.get(index); }

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
