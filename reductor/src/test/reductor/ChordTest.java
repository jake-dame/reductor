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
        list_CDFGB.add(new Note(60, new Range(0, 1)));
        list_CDFGB.add(new Note(62, new Range(0, 2)));
        list_CDFGB.add(new Note(64, new Range(0, 3)));
        list_CDFGB.add(new Note(65, new Range(4, 5)));
        list_CDFGB.add(new Note(67, new Range(6, 7)));
        Collections.shuffle(list_CDFGB);
    }

    @Test
    void TestConstruction() {
        Chord chord = new Chord(list_CDFGB, null);
        assertEquals(list_CDFGB.getFirst().getPitch(), chord.getLow().getPitch());
        assertEquals(list_CDFGB.getLast().getPitch(), chord.getHigh().getPitch());
        assertEquals(0, chord.range.getLow());
        assertEquals(7, chord.range.getHigh());
        assertEquals(5, chord.size());
    }


}