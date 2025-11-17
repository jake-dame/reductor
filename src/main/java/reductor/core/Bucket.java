package reductor.core;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;


/**
 * An ArrayList&lt;Note&gt; wrapper class.
 * <p>
 * Buckets themselves are Noted (meaning they contain Notes), however, this class is primarily meant to serve as the
 * backbone of the Noted interface itself. All Noted implementations (except the primitive Note itself) have a Bucket
 * member (which acts as their note container/collection).
 */
public class Bucket extends AbstractList<Note> implements Noted {

    private final ArrayList<Note> notes;


    public Bucket(List<Note> notes) {
        if (notes == null) { notes = new ArrayList<>(); }
        this.notes = new ArrayList<>(notes);
        //this.notes = new ArrayList<>();
        //for (Note note : notes) { this.notes.add( new Note(note) ); } // deep copy?
        this.notes.sort(null);
    }

    public <T extends Noted> Bucket(T noteContainer) {
        this(noteContainer.getNotes());
    }

    public Bucket() {
        this(new ArrayList<>());
    }


    public <T> Map<T, List<Note>> getMap(Function<Note, T> methodRef) {
        return notes.stream().collect(Collectors.groupingBy(methodRef));
    }


    //region AbstractList

    @Override public int size() { return this.notes.size(); }

    @Override public boolean isEmpty() { return this.notes.isEmpty(); }

    @Override public Note get(int index) { return this.notes.get(index); }
    @Override public Note getFirst() { return this.notes.isEmpty() ? null : this.notes.getFirst(); }
    @Override public Note getLast() { return this.notes.isEmpty() ? null : this.notes.getLast(); }

    @Override public boolean add(Note note) { return this.notes.add(note); }
    @Override public void add(int index, Note note) { this.notes.add(index, note); }
    @Override public void addFirst(Note note) { this.notes.addFirst(note); }
    @Override public void addLast(Note note) { this.notes.addLast(note); }
    @Override public boolean addAll(Collection<? extends Note> notes) { return this.notes.addAll(notes); }

    @Override public Note remove(int index) { return this.notes.remove(index); }
    @Override public boolean remove(Object note) { return this.notes.remove(note); }
    @Override public Note removeFirst() { return this.notes.removeFirst(); }
    @Override public Note removeLast() { return this.notes.removeLast(); }


    @Override public void sort(Comparator<? super Note> c) { this.notes.sort(c); }
    @Override public void forEach(Consumer<? super Note> action) { this.notes.forEach(action); }

    @Override public Iterator<Note> iterator() { return this.notes.iterator(); }

    //endregion


    public static Bucket fromNotes(ArrayList<Note> notes) { return new Bucket(notes); }

    @Override public ArrayList<Note> getNotes() { return new ArrayList<>(this.notes); }

    @Override public String toString() {
        if (notes.isEmpty()) { return " { }"; }
        StringBuilder builder = new StringBuilder();
        builder.append(" -> {  ");
        for (Note note : notes) {
            builder.append(note);
            builder.append("  ");
        }
        builder.append("  }");
        return builder.toString();
    }


}
