package reductor.piece;

import java.util.ArrayList;

/// Any class implementing Noted should be considered a "note container"
public interface Noted {

    /// Any implementing class should return the Note objects it contains. Whether they are direct references,
    ///  shallow, or deep copies; are filtered/transformed at all is up to the implementing class.
    ArrayList<Note> getNotes();

}
