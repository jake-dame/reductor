package reductor;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


/**
 * Unit tests for the {@linkplain reductor.Note} class.
 *
 * @see reductor.PitchTest
 * @see reductor.RangeTest
 *
 */
class NoteTest {


    @Test
    void constructionWithNullRange() {

        Note note = new Note(60, null);
        assertEquals(60, note.pitch());
        assertNull(note.range());

        assertThrows(NullPointerException.class, note::start);
        assertThrows(NullPointerException.class, note::stop);

    }

    @Test
    void setPitch() {

        Note note = new Note(60, new Range(0, 1));
        assertEquals(60, note.pitch());

        note.setPitch(0);
        assertEquals(0, note.pitch());

        note.setPitch(127);
        assertEquals(127, note.pitch());

        assertThrows(IllegalArgumentException.class, () -> note.setPitch(-1));
        assertThrows(IllegalArgumentException.class, () -> note.setPitch(128));

    }


    @Test
    void copyConstruction() {

        Note note = new Note(60, new Range(0, 1));
        Note copy = new Note(note);
        assertEquals(note, copy);

    }

    @Test
    void stringIntConstruction() {

        Note note = new Note("C#", 4);
        assertEquals(Pitch.stringPitchToNumber("C#", 4), note.pitch());

    }


    @Test
    void equals() {

        Note note = new Note(60, new Range(0, 1));

        assertEquals(note, new Note(60, new Range(0, 1)));
        assertNotEquals(note, new Note(61, new Range(0, 1)));
        assertNotEquals(note, new Note(60, new Range(0, 2)));

    }


    @Test
    void range() {

        Note note = new Note(60, new Range(0, 1));
        assertEquals(new Range(0, 1), note.range());

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