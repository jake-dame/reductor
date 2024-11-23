package reductor;

import java.util.*;

import static reductor.Pitch.*;


public class Chord implements Noted, Ranged {

    private Deque<Note> notes;

    private Range range;

    private Rhythm rhythm;

    Chord(ArrayList<Note> notes) {
        this.notes = new ArrayDeque<>(notes);
        this.range = Range.concatenate(notes);
        this.rhythm = new Rhythm(this.range.length());
    }

    Chord(Note... notes) {
        this.notes = new ArrayDeque<>(Arrays.asList(notes));
        this.range = Range.concatenate(this.getNotes());
        this.rhythm = new Rhythm(this.range.length());
    }

    Chord(int... pitches) {
        ArrayList<Note> list = new ArrayList<>();
        for (int pitch : pitches) { list.add(new Note(pitch)); }

        this.notes = new ArrayDeque<>(list);
        this.range = Range.concatenate(list);
        this.rhythm = new Rhythm(this.range.length());
    }

    // Returns true if the chord was inverted (inversion remained within piano range); false if not
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

        Chord chord = new Chord(root, root + M3 - mode, root + P5);
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
            //out.add( new Note(note, range) ); // TODO: use setRange() instead
            range = Range.add(range, partitionSize);
        }

        return out;
    }

    public static ArrayList<Note> alberti(Chord chord) {
        return null;
    }

    public int size() { return this.notes.size(); }

    @Override
    public ArrayList<Note> getNotes() { return new ArrayList<>(this.notes); }

    @Override
    public Range getRange() { return new Range(this.range); }


}
