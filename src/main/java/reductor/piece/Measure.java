package reductor.piece;

import java.util.*;
import java.util.stream.Collectors;


public class Measure implements Ranged, Noted {

    private final Bucket notes;
    private final ArrayList<Column> columns;

    private final Range range;

    private Integer number;

    private final TimeSignature timeSig;
    private final KeySignature keySig;
    private final Tempo tempo;

    private Boolean isPickup;


    private boolean pickupSet;
    private boolean numberSet;

    /// Primary constructor
    Measure(ArrayList<Column> columns, Range range, TimeSignature timeSig, KeySignature keySig, Tempo tempo) {

        this.range = range;

        this.columns = new ArrayList<>(columns);
        this.columns.sort(null);

        /*
         There is a somewhat special case to consider here. It does not break the logic of the design of Piece
         necessarily, but it is unintuitive and not very elegant.

         Say you have a 2-measure Piece and these notes:
            B3 [0,249]  (an 8th)
            A3 [0,479]  (a quarter)
            G3 [0,3839] (a tied whole-whole)
            E3 [0,3839] (a tied whole-whole)
            C4 [0,3839] (a tied whole-whole)

          These will ALL go into 1 column. The column itself SPANS multiple measures. This does not break the Column
          design because:
            1. We would not want 3 separate columns here (one for the C major triad; 1 for the quarter A; 1 for the 8th B)
            2. These are all executed by the fingers at exactly the same time, just with different RELEASES

          Without any other starting ticks to break up the monolithic [0,3839] block, this all becomes one Column.

          This means that Measure 2, when it queries the Column tree, will have all 5 notes in it, further meaning
          that it will have 2 notes (the A and the B) in it that end far before the Measure actually starts!

          There are two approaches to fixing this:
            1. Query the Note tree instead of the Column tree to populate Measures (possible and not bad, just a design change)
            2. Omit Notes from this Measure's Notes list (but don't alter the Column) whose end ticks < this.range

          I've gone with the 2nd approach.
         */

        ArrayList<Note> notes = new ArrayList<>();
        for (Column col : columns) {
            for (Note note : col.getNotes()) {
                if (note.stop() < this.range.low()) { continue; }
                notes.add(note);
            }
        }

        this.notes = new Bucket(notes);


        this.timeSig = timeSig;
        this.keySig = keySig;
        this.tempo = tempo;

        this.isPickup = false;
        this.number = -1;

        this.pickupSet = false;
        this.numberSet = false;
    }

    /// Copy constructor
    Measure(Measure other) {
        this.notes = new Bucket(other.getNotes());
        this.columns = new ArrayList<>(other.columns);
        this.columns.sort(null);
        this.number = other.number;
        this.range = other.range;
        this.timeSig = other.timeSig;
        this.keySig = other.keySig;
        this.tempo = other.tempo;
        this.isPickup = other.isPickup;
        this.pickupSet = other.pickupSet;
        this.numberSet = other.numberSet;
        this.comp = other.comp;
    }

    public int size() { return this.notes.size(); }

    public boolean isEmpty() { return this.notes.isEmpty(); }

    public long length() { return this.getRange().length(); }

    public boolean isPickup() { return this.isPickup; }
    public boolean setIsPickup(boolean val) {

        if (!this.pickupSet) {
            this.isPickup = val;
            this.pickupSet = true;
            return true;
        }

        return false;
    }

    public int getMeasureNumber() { return number; }
    public boolean setMeasureNumber(int measureNumber) {

        if (!this.numberSet) {
            this.number = measureNumber;
            this.numberSet = true;
            return true;
        }

        return false;
    }

    public TimeSignature getTimeSignature() { return new TimeSignature(this.timeSig); }
    public KeySignature getKeySignature() { return new KeySignature(this.keySig); }
    public Tempo getTempo() { return new Tempo(tempo); }

    public Column getColumn(int index) { return this.columns.get(index); }

    @Override
    public Range getRange() { return new Range(this.range); }

    //@Override
    //public ArrayList<Note> getNotes() { return new ArrayList<>(this.notes); }

    @Override
    public ArrayList<Note> getNotes() {

        ArrayList<Note> notes = this.columns.stream()
                .flatMap(col -> col.getNotes().stream())
                .collect(Collectors.toCollection(ArrayList::new));

        return new ArrayList<>();
    }

    @Override
    public String toString() {
        String number = this.number != null ? this.number + "" : "-1";
        return "m." + number + " " + this.range + ": " + this.timeSig + " and " + this.size() + " " +
                "notes";
    }

    ////////////////

    Comparator<Note> comp = new Comparator<>() {
        @Override
        public int compare(Note note, Note other) {
            if (note.pitch() != other.pitch()) { return Integer.compare(note.pitch(), other.pitch()); }
            return note.getRange().compareTo(other.getRange());
        }
    };

    public ArrayList<Note> getNotesProper() {

        Set<Note> set = new TreeSet<>(comp);
        set.addAll(this.notes);

        return new ArrayList<>(set);
    }

    public ArrayList<Note> getRhNotes() {

        Set<Note> set = new TreeSet<>(comp);
        for (Column c : this.columns) {
            for (Note note : c.getRightHand().notes) {
                if (!(note.stop() < this.range.low())) {
                    set.add(note);
                }
            }
        }

        for (Note note : set) { note.setHand(Hand.RIGHT); }

        return new ArrayList<>(set);
    }

    public ArrayList<Note> getMiddleNotes() {

        Set<Note> set = new TreeSet<>(comp);
        for (Column c : this.columns) {
            for (Note note : c.getMiddle().notes) {
                if (!(note.stop() < this.range.low())) {
                    set.add(note);
                }
            }
        }

        for (Note note : set) { note.setHand(Hand.NONE); }

        return new ArrayList<>(set);
    }

    public ArrayList<Note> getLhNotes() {

        Set<Note> set = new TreeSet<>(comp);
        for (Column c : this.columns) {
            for (Note note : c.getLeftHand().notes) {
                if (!(note.stop() < this.range.low())) {
                    set.add(note);
                }
            }
        }

        for (Note note : set) { note.setHand(Hand.LEFT); }

        return new ArrayList<>(set);
    }

}
