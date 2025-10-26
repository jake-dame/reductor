package reductor.core;

import java.util.*;
import java.util.stream.Collectors;

import static reductor.core.RhythmTypeAlpha.QUARTER;


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

    Comparator<Note> comp = new Comparator<>() {
        @Override
        public int compare(Note note, Note other) {
            if (note.getPitch() != other.getPitch()) { return Integer.compare(note.pitch(),
                    other.pitch()); }
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

        for (Note note : set) { note.devSetHand(Hand.RIGHT); }

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

        for (Note note : set) { note.devSetHand(Hand.NONE); }

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

        for (Note note : set) { note.devSetHand(Hand.LEFT); }

        return new ArrayList<>(set);
    }

    //public Measure(Integer number) {
    //
    //}


    // ========================================================================================== //

    //public static <T extends Ranged> MeasureBuilder<T> builder(long startTick) {
    //    return new MeasureBuilder<>(startTick);
    //}
    //
    //public static class MeasureBuilder<T extends Ranged>  {
    //
    //    private TimeSignature timeSig = null;
    //    private Integer pickupBeats = null;
    //    private KeySignature keySig = null;
    //    private Tempo tempo = null;
    //
    //    private Integer number;
    //
    //    private final long startTick;
    //
    //    private final Stack<T> stack = new Stack<>();
    //
    //    private MeasureBuilder(long startTick) { this.startTick = Math.max(startTick, 0); }
    //
    //    public MeasureBuilder<T> number(Integer number) {
    //        this.number = number;
    //        return this;
    //    }
    //
    //
    //
    //
    //    // ====================================== METADATA ====================================== //
    //
    //
    //    public MeasureBuilder<T> keySignature(String str) {
    //        keySig = new KeySignature(str, new Range(startTick, Long.MAX_VALUE));
    //        return this;
    //    }
    //
    //    public MeasureBuilder<T> timeSignature(int numerator, int denominator) {
    //        timeSig = new TimeSignature(numerator, denominator, new Range(startTick, Long.MAX_VALUE));
    //        return this;
    //    }
    //
    //    public MeasureBuilder<T> timeSignature(TimeSignature timeSig) {
    //        this.timeSig = timeSig;
    //        return this;
    //    }
    //
    //    public MeasureBuilder<T> tempo(int bpm) {
    //        tempo = new Tempo(bpm, new Range(startTick, Long.MAX_VALUE));
    //        return this;
    //    }
    //
    //    public MeasureBuilder<T> pickupOf(int numBeats) {
    //        this.pickupBeats = numBeats;
    //        return this;
    //    }
    //
    //    // ======================================== THEN ======================================== //
    //
    //    public MeasureBuilder<T> then(Pitch... pitches) {
    //        RhythmTypeAlpha rhythm = stack.isEmpty()
    //                ? QUARTER
    //                : RhythmTypeAlpha.getEnumType(stack.peek().getRange().length() + 1L);
    //        return then(rhythm, pitches);
    //    }
    //
    //    public MeasureBuilder<T> then(RhythmTypeAlpha rhythm, Pitch... pitches) {
    //
    //        long start = stack.isEmpty() ? 0 : stack.peek().getRange().high() + 1;
    //
    //        Note note = null;
    //        Chord chord = null;
    //        if (pitches.length == 1) {
    //            note = Note.builder()
    //                    .pitch(pitches[0])
    //                    .start(start)
    //                    .stop(start + rhythm.getDuration() - 1)
    //                    .build();
    //        } else {
    //            chord = Chord.builder()
    //                    .add(pitches)
    //                    .start(start)
    //                    .stop(start + rhythm.getDuration() - 1)
    //                    .build();
    //        }
    //
    //        T elem = null;
    //        if (note != null) { elem = (T) note; }
    //        if (chord != null) { elem = (T) chord; }
    //
    //        if (elem != null) { stack.add(elem); };
    //
    //        return this;
    //    }
    //
    //
    //    // ======================================  BUILD  ======================================= //
    //
    //
    //    public Measure build() {
    //
    //        // Pre-process elems
    //        ArrayList<T> out = new ArrayList<>(stack);
    //
    //        handleMetaElements(out);
    //
    //        Measure measure = new Measure();
    //
    //        return measure;
    //    }
    //
    //    private void handleMetaElements(ArrayList<T> out) {
    //
    //        // Handle creation of Meta elements and final tick.
    //        final long DEFAULT_END = timeSig != null
    //                ?  Piece.TPQ * (long) timeSig.numerator()
    //                :  Piece.TPQ * 4L;
    //
    //        final long FINAL_TICK = Math.max(stack.peek().getRange().high(), DEFAULT_END);
    //
    //        if (timeSig == null) {
    //            timeSig = new TimeSignature(4, 4, new Range(startTick, FINAL_TICK));
    //        }
    //
    //        if (pickupBeats != null) {
    //
    //            double multiplier = (double) pickupBeats / timeSig.denominator();
    //            double length = multiplier * timeSig.range().length();
    //            long endTick = (long) length - 1;
    //
    //            TimeSignature pickupSig = new TimeSignature(
    //                    pickupBeats,
    //                    timeSig.denominator(),
    //                    new Range(startTick, endTick)
    //            );
    //
    //            out.add((T) pickupSig);
    //
    //            timeSig = new TimeSignature(
    //                    timeSig.numerator(),
    //                    timeSig.denominator(),
    //                    new Range(pickupSig.getRange().high() + 1, FINAL_TICK)
    //            );
    //            System.out.println();
    //        }
    //
    //        if (keySig == null) {
    //            keySig = new KeySignature("C", new Range(startTick, FINAL_TICK));
    //        } else {
    //            keySig = new KeySignature(
    //                    keySig.accidentals(),
    //                    keySig.mode(),
    //                    new Range(this.startTick, FINAL_TICK)
    //            );
    //        }
    //
    //        if (tempo == null) {
    //            tempo = new Tempo(120, new Range(startTick, FINAL_TICK));
    //        } else {
    //            tempo = new Tempo(
    //                    tempo.getBpm(),
    //                    new Range(this.startTick, FINAL_TICK)
    //            );
    //        }
    //
    //        out.add((T) timeSig);
    //        out.add((T) keySig);
    //        out.add((T) tempo);
    //    }
    //
    //}


}
