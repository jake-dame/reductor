package reductor;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


/**
 * Unit tests for the {@linkplain reductor.Note} class.
 *
 * @see reductor.RangeTest
 */
class NoteTest {

    static { Context context = Context.createContext(480, 480); }

    // I thought this better to test here rather than in a separate Pitch class test because
    // this is where it's most consequential. Additionally, if it works here, it works everywhere else
    @Test
    void stringIntConstruction() {
        // bottom edge (switch/case)
        assertEquals(0, new Note("c").pitch());
        assertEquals(0, new Note("b#-2").pitch());
        assertEquals(0, new Note("dbb-1").pitch());
        assertEquals(1, new Note("bx-2").pitch());
        // top edge (switch/case)
        assertEquals(127, new Note("g9").pitch());
        assertEquals(127, new Note("abb9").pitch());
        assertEquals(127, new Note("fx9").pitch());
        // vanilla enharmonic (not switch/case)
        assertEquals(66, new Note("ex4").pitch());
        assertEquals(66, new Note("f#4").pitch());
        assertEquals(66, new Note("gb4").pitch());
        // enharmonic at register edges
        assertEquals(60, new Note("c4").pitch());
        assertEquals(60, new Note("b#3").pitch());
        assertEquals(60, new Note("dbb4").pitch());
        // register, no accidental
        assertEquals(0, new Note("c-1").pitch());
        assertEquals(12, new Note("c0").pitch());
        // accidental, no register
        assertEquals(5, new Note("gbb").pitch());
        assertEquals(6, new Note("gb").pitch());
        assertEquals(7, new Note("g").pitch());
        assertEquals(8, new Note("g#").pitch());
        assertEquals(9, new Note("gx").pitch());
        // empty
        assertThrows(RuntimeException.class, () -> new Note(""));
        // only register or only accidental
        assertThrows(RuntimeException.class, () -> new Note("-1"));
        assertThrows(RuntimeException.class, () -> new Note("9"));
        assertThrows(RuntimeException.class, () -> new Note("#"));
        // special cases -> B and B-flat are not "accidental only" cases!
        assertEquals(10, new Note("bb").pitch());
        assertEquals(11, new Note("b").pitch());
        assertEquals(10, new Note("bb-1").pitch());
        assertEquals(11, new Note("b-1").pitch());
        // only register+accidental
        assertThrows(RuntimeException.class, () -> new Note("#-1"));
        assertThrows(RuntimeException.class, () -> new Note("#9"));
        assertThrows(RuntimeException.class, () -> new Note("bb9"));
        // invalid pitch
        assertThrows(RuntimeException.class, () -> new Note("h4"));
        // invalid register
        assertThrows(RuntimeException.class, () -> new Note("c-2"));
        assertThrows(RuntimeException.class, () -> new Note("g#9"));
        // double digit register
        assertThrows(RuntimeException.class, () -> new Note("c-10"));
        assertThrows(RuntimeException.class, () -> new Note("c10"));
        // invalid accidental
        assertThrows(RuntimeException.class, () -> new Note("$"));
        assertThrows(RuntimeException.class, () -> new Note("g$"));
        assertThrows(RuntimeException.class, () -> new Note("g$$"));
        assertThrows(RuntimeException.class, () -> new Note("g$4"));
        assertThrows(RuntimeException.class, () -> new Note("g$$4"));
    }

    @Test
    void copyConstruction() {
        Note note = new Note(60, new Range(0, 1));
        Note copy = new Note(note);
        assertEquals(note, copy);
    }

    @Test
    void equals() {
        Note note = new Note(60, new Range(0, 1));
        assertEquals(note, new Note(60, new Range(0, 1)));
        assertNotEquals(note, new Note(61, new Range(0, 1)));
        assertNotEquals(note, new Note(60, new Range(0, 2)));
    }

    @Test
    void getRange() {
        Note note = new Note(60, new Range(0, 1));
        assertEquals(new Range(0, 1), note.getRange());
    }

    @Test
    void pitch() {
        Note note = new Note(60, new Range(0, 1));
        assertEquals(60, note.pitch());
    }

    @Test
    void start() {
        Note note = new Note(60, new Range(0, 1));
        assertEquals(0, note.start());
    }

    @Test
    void stop() {
        Note note = new Note(60, new Range(0, 1));
        assertEquals(1, note.stop());
    }


}