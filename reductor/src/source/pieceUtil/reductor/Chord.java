package reductor;

import java.util.ArrayList;
import java.util.Comparator;

import static reductor.Pitch.*;


public class Chord implements Noted, Ranged {

    ArrayList<Note> notes;

    Range range;

    Rhythm rhythm;

    Chord(ArrayList<Note> notes) {
        this.notes = notes;
        this.range = Range.concatenate(notes);
        this.rhythm = new Rhythm(this.range.length(), Context.resolution());
    }

    Chord(Note... notes) {
        ArrayList<Note> list = new ArrayList<>();
        for (Note note : notes) { list.add( new Note(note) ); }
        this.notes = list;
        this.range = Range.concatenate(this.notes);
        this.rhythm = new Rhythm(this.range.length(), Context.resolution());
    }

    Chord(int... pitches) {
        ArrayList<Note> notes = new ArrayList<>();
        for (int pitch : pitches) { notes.add(new Note(pitch)); }
        this.notes = notes;
        this.range = Range.concatenate(notes);
        this.rhythm = new Rhythm(this.range.length(), Context.resolution());
    }

    // I-III-V
    public static Chord getTriad(int root, int mode) { return getTriad(root, mode, 0); };

    // Mode: 0 == Major, 1 == minor
    public static Chord getTriad(int root, int mode, int inversion) {

        if (mode != 0  &&  mode != 1) { throw new RuntimeException("invalid mode: " + mode); }

        int third = root + M3 - mode;
        int fifth = root + P5;

        // Clamp (but bring everything down too in order to maintain intervallic relationships)
        if (inversion == 1  &&  PIANO_MAX < root + OCTAVE
                || inversion == 2  &&  PIANO_MAX < third + OCTAVE) {
            root -= OCTAVE;
            third -= OCTAVE;
            fifth -= OCTAVE;
        }

        // DM/m 6-4 and FM/m 6-3 are the first valid inversions not rooted at a valid pitch
        // Might want to control for this later

        return switch (inversion) {
            case 0 -> new Chord (root, third, fifth);
            case 1 -> new Chord (third, fifth, root);
            case 2 -> new Chord (fifth, root, third);
            default -> throw new RuntimeException("invalid inversion value: " + inversion);
        };
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
            out.add( new Note(note, range) );
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
    public void setNotes(ArrayList<Note> notes) { }

    @Override
    public Range getRange() { return new Range(this.range); }


}
