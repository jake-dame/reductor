package reductor.piece;

import java.util.ArrayList;

/// Any class implementing Noted should be considered a "note container"
public interface Noted {

    /*
     fromNotes() belongs to the the interface since it is static, including the implementation.
     If you call Noted.fromNotes(), it will execute the body seen below. The only way to avoid this is
     to override it in a class the implements Noted, which will then execute that code instead.

     The reason this is needed like this instead of like the two below is that if it were like this:

        Noted fromNotes(ArrayList<Note> notes);

     then it would be an _instance_ method, meaning you would need an object itself to call it on. Which doesn't
     make much sense for a method that is supposed to act like a (factory) constructor.
     */

    //static Noted fromNotes(ArrayList<Note> notes) {
    //    throw new UnsupportedOperationException("shouldn't be calling the static method");
    //}

    /// Any implementing class should return the Note objects it contains. Whether they are direct references,
    ///  shallow, or deep copies is up to the implementing class.
    ArrayList<Note> getNotes();

}
