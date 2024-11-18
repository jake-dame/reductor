package reductor;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


class ColumnTest {

    static {
        Context context = Context.createContext(480, 480);
    }

    @Test
    void test1() {


        ArrayList<Note> list = new ArrayList<>( List.of(
                new Note("C3", new Range(0,480)),
                new Note("D3", new Range(0,480)),
                new Note("F#3", new Range(0,480)),
                new Note("A3", new Range(0,480)),
                new Note("D4", new Range(0,480)),

                new Note("E4", new Range(0,480)),
                new Note("F4", new Range(0,480)),
                new Note("F#4", new Range(0,480)),
                new Note("G4", new Range(0,480)),
                new Note("A4", new Range(0,480)),

                new Note("C5", new Range(0,480)),
                new Note("D5", new Range(0,480)),
                new Note("F#5", new Range(0,480)),
                new Note("A5", new Range(0,480)),
                new Note("D6", new Range(0,480))
        ));

        Column c = new Column(list, new Range(0,480));

        assertEquals(5, c.getLH().size());
        assertEquals(5, c.getMiddle().size());
        assertEquals(5, c.getRH().size());

        assertEquals(c.size(), c.getLH().size() + c.getMiddle().size() + c.getRH().size());

        assertTrue(c.isPure());
        assertFalse(c.isTwoHanded());

    }

    @Test
    void test2() {

        ArrayList<Note> list = new ArrayList<>( List.of(
                new Note("C3", new Range(0,480)),
                new Note("D3", new Range(0,480)),
                new Note("F#3", new Range(0,480)),
                new Note("A3", new Range(0,480)),
                new Note("D4", new Range(0,480)),

                new Note("C5", new Range(0,480)),
                new Note("D5", new Range(0,480)),
                new Note("F#5", new Range(0,480)),
                new Note("A5", new Range(0,480)),
                new Note("D6", new Range(0,480))
        ));

        Column c = new Column(list, new Range(0,480));

        assertEquals(5, c.getLH().size());
        assertEquals(0, c.getMiddle().size());
        assertEquals(5, c.getRH().size());

        assertEquals(c.size(), c.getLH().size() + c.getMiddle().size() + c.getRH().size());

        assertTrue(c.isPure());
        assertTrue(c.isTwoHanded());

    }

    @Test
    void test3() {

        //

        ArrayList<Note> list = new ArrayList<>( List.of(
                new Note("C3", new Range(0,480)),
                new Note("D4", new Range(0,480)),

                new Note("D#4", new Range(0,480)),

                new Note("E4", new Range(0,480)),
                new Note("F5", new Range(0,480))
        ));

        Column c = new Column(list, new Range(0,480));

        assertEquals(2, c.getLH().size());
        assertEquals(1, c.getMiddle().size());
        assertEquals(2, c.getRH().size());

        assertEquals(c.size(), c.getLH().size() + c.getMiddle().size() + c.getRH().size());

        assertTrue(c.isPure());
        assertFalse(c.isTwoHanded());

    }

    @Test
    void test4() {

        // Based solely on split point, not notes-per-hand, with guy in the middle assigned to neither

        ArrayList<Note> list = new ArrayList<>( List.of(
                new Note("C3", new Range(0,480)),

                new Note("D#4", new Range(0,480)),

                new Note("D6", new Range(0,480))
        ));

        Column c = new Column(list, new Range(0,480));

        assertEquals(1, c.getLH().size());
        assertEquals(1, c.getMiddle().size());
        assertEquals(1, c.getRH().size());

        assertEquals(c.size(), c.getLH().size() + c.getMiddle().size() + c.getRH().size());

        assertTrue(c.isPure());
        assertFalse(c.isTwoHanded());

    }

    @Test
    void test5() {

        // Based solely on split point, not notes-per-hand

        ArrayList<Note> list = new ArrayList<>( List.of(
                new Note("C3", new Range(0,480)),

                new Note("D6", new Range(0,480))
        ));

        Column c = new Column(list, new Range(0,480));

        assertEquals(1, c.getLH().size());
        assertEquals(0, c.getMiddle().size());
        assertEquals(1, c.getRH().size());

        assertEquals(c.size(), c.getLH().size() + c.getMiddle().size() + c.getRH().size());

        assertTrue(c.isPure());
        assertTrue(c.isTwoHanded());

    }

    // OVERLAPPING

    @Test
    void test6() {

        // Should be all LH

        ArrayList<Note> list = new ArrayList<>( List.of(
                new Note("C3", new Range(0,480)),
                new Note("E3", new Range(0,480)),
                new Note("G3", new Range(0,480)),
                new Note("D4", new Range(0,480))
        ));

        Column c = new Column(list, new Range(0,480));

        assertEquals(4, c.getLH().size());
        assertEquals(0, c.getMiddle().size());
        assertEquals(0, c.getRH().size());

        assertEquals(c.size(), c.getLH().size() + c.getMiddle().size() + c.getRH().size());

        assertTrue(c.isPure());
        assertTrue(c.isTwoHanded());

    }

    @Test
    void test7() {

        // LH should be empty

        ArrayList<Note> list = new ArrayList<>( List.of(
                new Note("C6", new Range(0,480)),
                new Note("E6", new Range(0,480)),
                new Note("G6", new Range(0,480)),
                new Note("D6", new Range(0,480))
        ));

        Column c = new Column(list, new Range(0,480));

        assertEquals(0, c.getLH().size());
        assertEquals(0, c.getMiddle().size());
        assertEquals(4, c.getRH().size());

        assertEquals(c.size(), c.getLH().size() + c.getMiddle().size() + c.getRH().size());

        assertTrue(c.isPure());
        assertTrue(c.isTwoHanded());

    }

    @Test
    void test8() {

        // All within one hand span, but too many notes for one hand

        ArrayList<Note> list = new ArrayList<>( List.of(
                new Note("C3", new Range(0,480)),
                new Note("D3", new Range(0,480)),
                new Note("E3", new Range(0,480)),
                new Note("F3", new Range(0,480)),
                new Note("G3", new Range(0,480)),
                new Note("A3", new Range(0,480)),
                new Note("D4", new Range(0,480))
        ));

        Column c = new Column(list, new Range(0,480));

        assertEquals(6, c.getLH().size());
        assertEquals(0, c.getMiddle().size());
        assertEquals(1, c.getRH().size());

        assertEquals(c.size(), c.getLH().size() + c.getMiddle().size() + c.getRH().size());

        assertTrue(c.isPure());
        assertTrue(c.isTwoHanded());

    }


    @Test
    void test9() {

        // 2 hands worth of notes, but everything above middle C

        ArrayList<Note> list = new ArrayList<>( List.of(
                new Note("C5", new Range(0,480)),
                new Note("D5", new Range(0,480)),
                new Note("E5", new Range(0,480)),
                new Note("F5", new Range(0,480)),
                new Note("G5", new Range(0,480)),
                new Note("A5", new Range(0,480)),

                new Note("C6", new Range(0,480)),
                new Note("D6", new Range(0,480)),
                new Note("E6", new Range(0,480)),
                new Note("F6", new Range(0,480)),
                new Note("G6", new Range(0,480)),
                new Note("A6", new Range(0,480))

                ));

        Column c = new Column(list, new Range(0,480));

        assertEquals(6, c.getLH().size());
        assertEquals(0, c.getMiddle().size());
        assertEquals(6, c.getRH().size());

        assertEquals(c.size(), c.getLH().size() + c.getMiddle().size() + c.getRH().size());

        assertTrue(c.isPure());
        assertTrue(c.isTwoHanded());

    }


    @Test
    void test10() {

        // The last chord of chopin op28 no 20.Technically should be C4 in the RH, but there is really no way to tell
        // without multi-column analysis where it should be reliably

        ArrayList<Note> list = new ArrayList<>( List.of(
                new Note("C3", new Range(0,480)),
                new Note("G3", new Range(0,480)),
                new Note("C4", new Range(0,480)),

                new Note("Eb4", new Range(0,480)),
                new Note("G4", new Range(0,480)),
                new Note("C5", new Range(0,480))

                ));

        Column c = new Column(list, new Range(0,480));

        assertEquals(3, c.getLH().size());
        assertEquals(0, c.getMiddle().size());
        assertEquals(3, c.getRH().size());

        assertEquals(c.size(), c.getLH().size() + c.getMiddle().size() + c.getRH().size());

        assertTrue(c.isPure());
        assertTrue(c.isTwoHanded());

    }




}