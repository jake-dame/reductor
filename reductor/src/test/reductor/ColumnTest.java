package reductor;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


class ColumnTest {

    static { Context context = Context.createContext(480, 480); }

    @Test
    void test1() {

        // Basic thick texture with basic cases for LH and RH (both exceed span max)

        ArrayList<Note> list = Note.toList(List.of(
                "C3",
                "D3",
                "F#3",
                "A3",
                "D4",

                "E4",
                "F4",
                "F#4",
                "G4",
                "A4",

                "C5",
                "D5",
                "F#5",
                "A5",
                "D6"
        ));

        Column c = new Column(list);

        assertEquals(5, c.getLH().size());
        assertEquals(5, c.getMiddle().size());
        assertEquals(5, c.getRH().size());

        assertEquals(c.size(), c.getLH().size() + c.getMiddle().size() + c.getRH().size());

        assertFalse(c.isTwoHanded());
    }

    @Test
    void test2() {

        // Same as test1 but nothing in the middle (twoHanded() should be true)

        ArrayList<Note> list = Note.toList( List.of(
                "C3",
                "D3",
                "F#3",
                "A3",
                "D4",

                "C5",
                "D5",
                "F#5",
                "A5",
                "D6"
        ));

        Column c = new Column(list);

        assertEquals(5, c.getLH().size());
        assertEquals(0, c.getMiddle().size());
        assertEquals(5, c.getRH().size());

        assertEquals(c.size(), c.getLH().size() + c.getMiddle().size() + c.getRH().size());

        assertTrue(c.isTwoHanded());
    }

    @Test
    void test3() {

        // Should be split evenly, with edges on SPAN_MAX

        ArrayList<Note> list = Note.toList( List.of(
                "C3",
                "D4",

                "D#4",

                "E4",
                "F#5"
        ));

        Column c = new Column(list);

        assertEquals(2, c.getLH().size());
        assertEquals(1, c.getMiddle().size());
        assertEquals(2, c.getRH().size());

        assertEquals(c.size(), c.getLH().size() + c.getMiddle().size() + c.getRH().size());

        assertFalse(c.isTwoHanded());
    }

    @Test
    void test4() {

        // Should be split evenly, 1 middle

        ArrayList<Note> list = Note.toList( List.of(
                "C3",

                "D#4",

                "D6"
        ));

        Column c = new Column(list);

        assertEquals(1, c.getLH().size());
        assertEquals(1, c.getMiddle().size());
        assertEquals(1, c.getRH().size());

        assertEquals(c.size(), c.getLH().size() + c.getMiddle().size() + c.getRH().size());

        assertFalse(c.isTwoHanded());
    }

    @Test
    void test5() {

        // Should be split evenly; no middle

        ArrayList<Note> list = Note.toList( List.of(
                "C3",

                "D6"
        ));

        Column c = new Column(list);

        assertEquals(1, c.getLH().size());
        assertEquals(0, c.getMiddle().size());
        assertEquals(1, c.getRH().size());

        assertEquals(c.size(), c.getLH().size() + c.getMiddle().size() + c.getRH().size());

        assertTrue(c.isTwoHanded());
    }

    // OVERLAPPING

    @Test
    void test6() {

        // Should be all LH

        ArrayList<Note> list = Note.toList( List.of(
                "C3",
                "E3",
                "G3",
                "D4"
        ));

        Column c = new Column(list);

        assertEquals(4, c.getLH().size());
        assertEquals(0, c.getMiddle().size());
        assertEquals(0, c.getRH().size());

        assertEquals(c.size(), c.getLH().size() + c.getMiddle().size() + c.getRH().size());

        assertTrue(c.isTwoHanded());

    }

    @Test
    void test7() {

        // Should all be in RH

        ArrayList<Note> list =Note.toList( List.of(
                "C6",
                "E6",
                "G6",
                "D6"
        ));

        Column c = new Column(list);

        assertEquals(0, c.getLH().size());
        assertEquals(0, c.getMiddle().size());
        assertEquals(4, c.getRH().size());

        assertEquals(c.size(), c.getLH().size() + c.getMiddle().size() + c.getRH().size());

        assertTrue(c.isTwoHanded());
    }

    @Test
    void test8() {

        // Less then SPAN_MAX, but exceeds NOTE_MAX

        ArrayList<Note> list = Note.toList( List.of(
                "C3",
                "D3",
                "E3",
                "F3",
                "G3",
                "A3",
                "D4"
        ));

        Column c = new Column(list);

        assertEquals(6, c.getLH().size());
        assertEquals(0, c.getMiddle().size());
        assertEquals(1, c.getRH().size());

        assertEquals(c.size(), c.getLH().size() + c.getMiddle().size() + c.getRH().size());

        assertTrue(c.isTwoHanded());
    }


    @Test
    void test9() {

        // 2 hands worth of notes, but everything above middle C

        ArrayList<Note> list = Note.toList( List.of(
                "C5",
                "D5",
                "E5",
                "F5",
                "G5",
                "A5",

                "C6",
                "D6",
                "E6",
                "F6",
                "G6",
                "A6"
        ));

        Column c = new Column(list);

        assertEquals(6, c.getLH().size());
        assertEquals(0, c.getMiddle().size());
        assertEquals(6, c.getRH().size());

        assertEquals(c.size(), c.getLH().size() + c.getMiddle().size() + c.getRH().size());

        assertTrue(c.isTwoHanded());
    }


    @Test
    void test10() {

        // The last chord of chopin op28 no 20.Technically should be C4 in the RH, but there is really no way to tell
        // without multi-column analysis where it should be reliably

        ArrayList<Note> list = Note.toList( List.of(
                "C3",
                "G3",
                "C4",

                "Eb4",
                "G4",
                "C5"
        ));

        Column c = new Column(list);

        assertEquals(3, c.getLH().size());
        assertEquals(0, c.getMiddle().size());
        assertEquals(3, c.getRH().size());

        assertEquals(c.size(), c.getLH().size() + c.getMiddle().size() + c.getRH().size());

        assertTrue(c.isTwoHanded());
    }

    @Test
    void getSplitPointPitchUpperMedian() {

        ArrayList<Note> list = Note.toList( List.of(
                "C3",
                "E3",
                "G3",

                "C5",
                "E5",
                "G5"
        ));

        Column c = new Column(list);

        assertEquals(3, c.getLH().size());
        assertEquals(0, c.getMiddle().size());
        assertEquals(3, c.getRH().size());

        assertEquals(c.size(), c.getLH().size() + c.getMiddle().size() + c.getRH().size());

        assertTrue(c.isTwoHanded());

        assertEquals(Pitch.toInt("E4"), c.getSplitPointPitch());
    }

    @Test
    void getSplitPointPitchTrueMedian() {

        ArrayList<Note> list = Note.toList( List.of(
                "C3",
                "E3",
                "G3",

                "B4",
                "E5",
                "G5"
        ));

        Column c = new Column(list);

        assertEquals(3, c.getLH().size());
        assertEquals(0, c.getMiddle().size());
        assertEquals(3, c.getRH().size());

        assertEquals(c.size(), c.getLH().size() + c.getMiddle().size() + c.getRH().size());

        assertTrue(c.isTwoHanded());

        assertEquals(Pitch.toInt("Eb4"), c.getSplitPointPitch());
    }


    //@Test
    //void distributionTest1() {
    //
    //    // First chord of Liszt-Beethoven: Symphony 3 scherzo
    //
    //    ArrayList<Note> list = Note.fromList(( List.of(
    //            "Ab2",
    //            "Eb3",
    //
    //            "Ab3",
    //            "C4"
    //    ));
    //
    //    Column c = new Column(list);
    //
    //    assertEquals(2, c.getLH().size());
    //    assertEquals(0, c.getMiddle().size());
    //    assertEquals(2, c.getRH().size());
    //
    //    assertEquals(c.size(), c.getLH().size() + c.getMiddle().size() + c.getRH().size());
    //
    //    assertTrue(c.isTwoHanded());
    //
    //}
    //
    //@Test
    //void distributionTest2() {
    //
    //    // First "chord" of Mozart K. 545 i
    //
    //    ArrayList<Note> list = Note.fromList( List.of(
    //            "C4",
    //
    //            "C5"
    //    ));
    //
    //    Column c = new Column(list);
    //
    //    assertEquals(1, c.getLH().size());
    //    assertEquals(0, c.getMiddle().size());
    //    assertEquals(1, c.getRH().size());
    //
    //    assertEquals(c.size(), c.getLH().size() + c.getMiddle().size() + c.getRH().size());
    //
    //    assertTrue(c.isPure());
    //    assertTrue(c.isTwoHanded());
    //
    //}



}