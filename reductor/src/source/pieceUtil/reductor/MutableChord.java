package reductor;

import java.util.ArrayList;


public class MutableChord implements Ranged, Noted {

    private ArrayList<MutableNote> notes;
    private Rhythm rhythm;
    private Range range;

    private MutableNote root;

    MutableChord() {
        this.notes = new ArrayList<>();
        this.rhythm = null;
        this.range = null;
    }

    public MutableChord setRoot(MutableNote note) {
        this.root = note;
        this.range = note.getRange();
        if (this.rhythm == null) { this.rhythm = Rhythm.fromRange(this.range); }
        return this;
    }

    public MutableChord add(MutableNote note) {
        note.setRange(this.range);
        note.setRhythm(this.rhythm);
        note.setStart(this.range.low());
        this.notes.add(note);
        return this;
    }

    public MutableChord add(int interval) {
        MutableNote note = new MutableNote()
                .setPitch(this.root.pitch())
                .setRange(this.range);
        return this.add(note);
    }

    public MutableChord add(String str) {

        // TODO: allow String constructor with NO REGISTER GIVEN and a - to go down from current root or nothing for up
        //  e.g. chord.add("B") should add a B up from the root; chord.add("-B") should add a B down

        MutableNote note = new MutableNote()
                .setPitch(Pitch.toInt(str))
                .setRange(this.range);
        return this.add(note);
    }

    public int size() { return this.notes.size(); }

    // DON'T USE THESE
    @Override  public ArrayList<Note> getNotes() { return null; }
    @Override  public void setNotes(ArrayList<Note> notes) { }

    @Override
    public Range getRange() { return new Range(this.range); }

}
