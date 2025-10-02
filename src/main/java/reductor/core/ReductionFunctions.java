package reductor.core;

import java.util.ArrayList;
import java.util.Comparator;


public class ReductionFunctions {

    // Only melody octave doubling is checked for
    static void removeOctaveDoubling(Column col) {

        ArrayList<Note> notes = new ArrayList<>(col.getNotes());
        notes.sort(Comparator.comparingInt(Note::pitch));

        ArrayList<Note> fromTop = new ArrayList<>(notes.reversed());

        ArrayList<Note> toRemove = new ArrayList<>();
        for (Note top : fromTop) {
            for (Note bottom : fromTop) {
                if (top.pitch() - 12 == bottom.pitch()) {
                    if (60 <= bottom.pitch()) { toRemove.add(bottom); }
                }
            }
        }

        col.getNotes().removeAll(toRemove);
    }

}
