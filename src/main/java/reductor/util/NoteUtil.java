package reductor.util;


import reductor.core.Note;
import reductor.core.Pitch;
import reductor.core.builders.NoteBuilder;

import java.util.ArrayList;
import java.util.List;


public class NoteUtil {

    /**
     * Given something like List.of("C", "E", "G", "Bb"), pops out a list of Note objects.
     * Helpful in test creation.
     */
    public static ArrayList<Note> toList(List<String> strings) {
        ArrayList<Note> out = new ArrayList<>();
        for (String str : strings) { out.add( NoteBuilder.builder().pitch(new Pitch(str)).build() ); }
        return out;
    }


}
