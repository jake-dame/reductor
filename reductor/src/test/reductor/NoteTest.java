package reductor;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


/**
 * Unit tests for the {@linkplain reductor.Note} class.
 *
 * @see reductor.RangeTest
 */
class NoteTest {

    @Test
    void constructionWithNullGetRange() {
        Note note = new Note(60, null);
        assertEquals(60, note.getPitch());
        assertNull(note.getRange());
        assertThrows(NullPointerException.class, note::start);
        assertThrows(NullPointerException.class, note::stop);
    }

    @Test
    void setGetPitch() {
        Note note = new Note(60, new Range(0, 1));
        assertEquals(60, note.getPitch());
        note.setPitch(0);
        assertEquals(0, note.getPitch());
        note.setPitch(127);
        assertEquals(127, note.getPitch());
        assertThrows(IllegalArgumentException.class, () -> note.setPitch(-1));
        assertThrows(IllegalArgumentException.class, () -> note.setPitch(128));
    }

    @Test
    void copyConstruction() {
        Note note = new Note(60, new Range(0, 1));
        Note copy = new Note(note);
        assertEquals(note, copy);
    }

    // I thought this better to test here rather than in a separate Pitch class test because
    // this is where it's most consequential. Additionally, if it works here, it works everywhere else
    @Test
    void stringIntConstruction() {
        // bottom edge (switch/case)
        assertEquals(0, new Note("c").getPitch());
        assertEquals(0, new Note("b#-2").getPitch());
        assertEquals(0, new Note("dbb-1").getPitch());
        assertEquals(1, new Note("bx-2").getPitch());
        // top edge (switch/case)
        assertEquals(127, new Note("g9").getPitch());
        assertEquals(127, new Note("abb9").getPitch());
        assertEquals(127, new Note("fx9").getPitch());
        // vanilla enharmonic (not switch/case)
        assertEquals(66, new Note("ex4").getPitch());
        assertEquals(66, new Note("f#4").getPitch());
        assertEquals(66, new Note("gb4").getPitch());
        // enharmonic at register edges
        assertEquals(60, new Note("c4").getPitch());
        assertEquals(60, new Note("b#3").getPitch());
        assertEquals(60, new Note("dbb4").getPitch());
        // register, no accidental
        assertEquals(0, new Note("c-1").getPitch());
        assertEquals(12, new Note("c0").getPitch());
        // accidental, no register
        assertEquals(5, new Note("gbb").getPitch());
        assertEquals(6, new Note("gb").getPitch());
        assertEquals(7, new Note("g").getPitch());
        assertEquals(8, new Note("g#").getPitch());
        assertEquals(9, new Note("gx").getPitch());
        // empty
        assertThrows(RuntimeException.class, () -> new Note(""));
        // only register or only accidental
        assertThrows(RuntimeException.class, () -> new Note("-1"));
        assertThrows(RuntimeException.class, () -> new Note("9"));
        assertThrows(RuntimeException.class, () -> new Note("#"));
        // special cases -> B and B-flat are not "accidental only" cases!
        assertEquals(10, new Note("bb").getPitch());
        assertEquals(11, new Note("b").getPitch());
        assertEquals(10, new Note("bb-1").getPitch());
        assertEquals(11, new Note("b-1").getPitch());
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
    void getPitch() {
        Note note = new Note(60, new Range(0, 1));
        assertEquals(60, note.getPitch());
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