package reductor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ChordTest {

    private ArrayList<Note> list_CDFGB;

    @BeforeEach
    void setUp() {

        list_CDFGB = new ArrayList<>();
        list_CDFGB.add( new Note("C", 4) );
        list_CDFGB.add( new Note("D", 4) );
        list_CDFGB.add( new Note("F", 4) );
        list_CDFGB.add( new Note("G", 4) );
        list_CDFGB.add( new Note("B", 4) );

        Collections.shuffle(list_CDFGB);

    }

    @Test
    void TestConstruction() {

        Chord chord = new Chord( list_CDFGB );
        assertEquals(list_CDFGB.getFirst().pitch, chord.low.pitch);
        assertEquals(list_CDFGB.getLast().pitch, chord.high.pitch);
        assertEquals(5, chord.notes.size());

    }


}