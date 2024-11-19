package reductor;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;


/// This is a wrapper class for `ArrayList<Note>` because I got tired of writing `ArrayList<Note>` and it made it so I
/// could write a custom toString(). That is all. Also you can directly construct a bunch of Note objects from a list
///  of MIDI note events.
public class NoteList {

    private final ArrayList<Note> notes;

    NoteList(ArrayList<Note> notes) {
        if (notes == null) { throw new NullPointerException("null list past to NoteList constructor"); }
        //this.notes = new ArrayList<>();
        //for (Note note : notes) { this.notes.add( new Note(note) ); }
        this.notes = new ArrayList<>(notes);
        this.notes.sort(Comparator.comparingLong(Note::start));
    }


    NoteList(boolean hi, List<String> list) {
        ArrayList<Note> notes = new ArrayList<>();
        for (String str : list) { notes.add(new Note(str)); }
        this.notes = notes;
    }

    NoteList() {
        this.notes = new ArrayList<>();
    }


    /// Copy Constructor
    NoteList(NoteList noteList) {
        if (noteList == null) { throw new NullPointerException("null list past to NoteList constructor"); }
        this.notes = new ArrayList<>(noteList.getBackingList());
        this.notes.sort(Comparator.comparingLong(Note::start));
    }




    /// Can get a map of these noteList by a method reference to any of the Note class methods. e.g. To get a map of
    /// NoteList keyed on duration, you would pass Note::getOriginalChannel().
    public <T> Map<T, List<Note>> getMap(Function<Note, T> methodRef) {
        return notes.stream().collect(Collectors.groupingBy(methodRef));
    }

    public ArrayList<Note> getBackingList() {
        return this.notes; // TODO: too exposed
    }

    //public void clampNotes(Range range) {
    //    // this sets a new range for every note for now but that shouldn't affect anything
    //    for (Note note : this.notes) {
    //        long newLow = Math.max(note.getRange().getLow(), range.getLow());
    //        long newHigh = Math.min(note.getRange().getHigh(), range.getHigh());
    //        note.setRange(new Range(newLow, newHigh));
    //    }
    //}

    /*===============
    * WRAPPER Methods
    * ============= */

    public int size() { return this.notes.size(); }
    public boolean isEmpty() { return this.notes.isEmpty(); }

    public boolean add(Note note) { return this.notes.add(note); }
    public void add(int index, Note note) { this.notes.add(index, note); }
    public boolean addAll(Collection<? extends Note> notes) { return this.notes.addAll(notes); }

    public boolean remove(Note note) { return this.notes.remove(note); }
    public Note remove(int index) { return this.notes.remove(index); }

    public Note get(int index) { return this.notes.get(index); }
    public Note getFirst() { return this.notes.isEmpty() ? null : this.notes.getFirst(); }
    public Note getLast() { return this.notes.isEmpty() ? null : this.notes.getLast(); }

    @Override
    public String toString() {

        if (notes.isEmpty()) {
            return " { }";
        }

        StringBuilder builder = new StringBuilder();
        builder.append(" -> {");

        for (Note note : notes) {
            builder.append(note);
            builder.append(", ");
        }

        builder.delete(builder.lastIndexOf(", "), builder.length());
        builder.append("}");

        return builder.toString();
    }

}