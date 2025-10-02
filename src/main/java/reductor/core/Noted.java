package reductor.core;


import java.util.ArrayList;


/** Noted denotes something as a container -- at some level -- of Note objects. */
public interface Noted {

    /**
     * Any implementing class should return the Note objects it contains. The implementing class specifies whether
     * the Notes are direct references, shallow or deep copies, are filtered/transformed, etc.
     */
    ArrayList<Note> getNotes();

}
