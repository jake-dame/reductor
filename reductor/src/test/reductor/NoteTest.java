package reductor;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


/**
 * Unit tests for the {@linkplain reductor.Note} class.
 *
 * @see reductor.RangeTest
 */
class NoteTest {

    static { Context context = Context.createContext(); }

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