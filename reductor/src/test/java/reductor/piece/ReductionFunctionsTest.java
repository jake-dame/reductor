package reductor.piece;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


class ReductionFunctionsTest {

    @Test
    void removeOctaveDoubling() {

        Note n1 = new Note("c2");
        Note n2 = new Note("g2");
        Note n3 = new Note("c3");
        Note n4 = new Note("g3");
        Note n5 = new Note("c4");
        Note n6 = new Note("e4");
        Note n7 = new Note("g4");
        Note n8 = new Note("c5");
        Note n9 = new Note("c6");

        ArrayList<Note> notes = new ArrayList<>( List.of(
                n1, n2, n3, n4, n5, n6, n7, n8, n9
        ) );

        Column col = new Column(notes, new Range());

        ReductionFunctions.removeOctaveDoubling(col);

        assertFalse(col.getNotes().contains(n3));
        assertFalse(col.getNotes().contains(n4));
        assertFalse(col.getNotes().contains(n8));

    }

}