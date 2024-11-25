package reductor;

import java.util.*;

import static reductor.Pitch.*;


public class Chord implements Noted, Ranged {

    private final Range range;
    private final Rhythm rhythm;

    private final Deque<Note> notes;

    private Chord(ArrayList<Note> notes, Range range) {
        this.notes = new ArrayDeque<>(notes);
        this.range = new Range(range);
        this.rhythm = Rhythm.fromRange(this.range);
    }

    public Chord(Chord other) {
        this.range = new Range(other.range);
        this.rhythm = new Rhythm(other.rhythm);
        this.notes = new ArrayDeque<>();
        for (Note note : other.notes) { this.notes.add(new Note(note)); }
    }


    /* =======
       BUILDER
     * ======= */


    public static ChordBuilder builder() { return new ChordBuilder(); }

    /**
     * Since this is used for startAfter() capabilities, but can also be used to reduce creation tax if multiple
     * chords are similar, the other Chord's notes are ALSO retained, since this must act like a copy constructor.
     * If more notes are added with add(), they will be added to this Chord's notes, along with the previous Chord's
     * notes. To start fresh in terms of notes, but retain the timing-chaining properties, make a call to the clear
     * () function before adding any new notes.
     */
    public static ChordBuilder builder(Chord chord) { return new ChordBuilder(chord); }

    public static class ChordBuilder {

        private Chord prev = null;
        private Deque<Note> prevNotes = null;

        private Range range = null;
        private Long start = null;
        private Long stop = null;
        private Rhythm rhythm = null;

        private final ArrayList<Integer> pitches = new ArrayList<>();


        private ChordBuilder() { }

        /**
         * @see Chord#builder(Chord) 
         * */
        private ChordBuilder(Chord other) {
            this.prev = new Chord(other);
            this.prevNotes = this.prev.notes;
            this.range = this.prev.range;
            this.rhythm = this.prev.rhythm;
        }


        public ChordBuilder start(long tick) {
            this.start = tick;
            return this;
        }

        public ChordBuilder stop(long tick) {
            this.stop = tick;
            return this;
        }

        public ChordBuilder rhythm(Rhythm rhythm) {
            this.rhythm = new Rhythm(rhythm);
            return this;
        }

        public ChordBuilder rhythm(RhythmType type) {
            this.rhythm = Rhythm.fromType(type);
            return this;
        }

        public ChordBuilder then() {
            if (prev != null) {
                this.start = prev.stop() + 1;
                this.stop = null; // nullify the stop, force user to set it or it will be given default later
            }
            return this;
        }

        public ChordBuilder clear() {
            if (prevNotes != null) { prevNotes.clear(); }
            return this;
        }

        public ChordBuilder add(int... pitches) {
            for (int pitch : pitches) { this.pitches.add( pitch ); }
            return this;
        }

        public ChordBuilder add(String... pitches) {
            for (String pitch : pitches) { this.pitches.add( Pitch.toInt(pitch)); }
            return this;
        }

        public Chord build() {

            // Rhythm stuff
            if (start == null) { this.start = 0L; }

            if (stop == null) {
                if (rhythm == null) { this.stop = this.start + 479L; }
                else { this.stop = this.start + rhythm.getDuration() - 1;}
            }

            this.range = new Range(this.start, this.stop);

            this.rhythm = Rhythm.fromRange(range);

            /* Chords are designed so that every Note contained within (as well as the Chord itself) have the same
            range and rhythm. Therefore, the actual Note objects cannot be constructed until this point. */
            ArrayList<Note> notes = new ArrayList<>();

            if (prevNotes != null) {
                for (Note note : prevNotes) {
                    this.pitches.add(note.pitch());
                }
            }

            for (int pitch : this.pitches) {
                notes.add( Note.builder()
                        .pitch(pitch)
                        .start(this.start)
                        .stop(this.stop)
                        .build()
                );
            }

            return new Chord(new ArrayList<>(notes), new Range(this.range));
        }


    }


    // Returns true if the chord was inverted (inversion remained within piano range); false if not
    // This doesn't alter any notes, it just changes the order of the notes in this Chord's container.
    public boolean invert(int inversions) {

        boolean inverted = false;

        if (0 < inversions) {

            while(inversions != 0) {
                if (notes.peekFirst().pitch() + 12 <= PIANO_MAX) {
                    this.notes.addLast(this.notes.removeFirst());
                    inverted = true;
                }
                inversions--;
            }

        } else if (inversions < 0) {

            while(inversions != 0) {
                if (PIANO_MIN <= notes.peekLast().pitch() - 12) {
                    this.notes.addFirst(this.notes.removeLast());
                    inverted = true;
                }
                inversions++;
            }

        }

        return inverted;
    }

    public boolean octaveUp() { return this.invert(notes.size()); }
    public boolean octaveDown() { return this.invert(notes.size()); }

    public static Chord getTriad(int root, int mode, int inversion) {

        if (mode != 0  &&  mode != 1) { throw new RuntimeException("invalid mode: " + mode); }
        if (inversion < 0  ||  2 < inversion) { throw new RuntimeException("invalid inversion: " + inversion); }

        Chord chord = Chord.builder().add(root, root + M3 - mode, root + P5).build();
        chord.invert(inversion);
        return chord;
    }

    public static ArrayList<Note> arpeggiate(Chord chord) {
        ArrayList<Note> in = new ArrayList<>(chord.notes);
        in.sort(Comparator.comparingInt(Note::pitch));

        long numNotes = chord.size();
        long span = chord.getRange().length() + 1;

        long partitionSize = (span / numNotes);

        Range range = new Range(chord.getRange().low(), chord.getRange().low() + partitionSize - 1);
        ArrayList<Note> out = new ArrayList<>();
        for (Note note : chord.notes) {
            out.add( note.setRange(range) ); // this is a new instance of a Note
            range = Range.add(range, partitionSize);
        }

        return out;
    }

    public int size() { return this.notes.size(); }
    public long start() { return this.range.low(); }
    public long stop() { return this.range.high(); }

    /* =========
       OVERRIDES
     * ========= */

    @Override
    public ArrayList<Note> getNotes() { return new ArrayList<>(this.notes); }

    @Override
    public Range getRange() { return new Range(this.range); }

    @Override
    public String toString() {
        return this.range + " => " + this.notes;
    }


}