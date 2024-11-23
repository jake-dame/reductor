package reductor;

import java.util.ArrayList;
import java.util.Collection;


public interface DataConverter {

    ArrayList<Note> toNotes();

    ArrayList<TimeSignature> toTimeSignatures();

    default ArrayList<KeySignature> toKeySignatures() {
        return new ArrayList<>();
    }

    default ArrayList<Tempo> toTempos() {
        return new ArrayList<>();
    }

}
