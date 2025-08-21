package reductor.piece;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;


/**
 * Unit tests for the {@linkplain Note} class.
 *
 * @see RangeTest
 */
class NoteTest {


    @Test
    void copyConstruction() {
        Note note = new Note(60, new Range(0, 1));
        Note copy = new Note(note);
        assertEquals(note, copy);
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
        assertEquals(note.getRange().low(), note.start());
    }

    @Test
    void stop() {
        Note note = new Note(60, new Range(0, 1));
        assertEquals(1, note.stop());
        assertEquals(note.getRange().high(), note.stop());
    }

    @Test
    void duration() {
        Note note = new Note(60, new Range(0, 479));
        assertEquals(480, note.duration());
        assertEquals(note.duration(), note.getRhythm().getDuration());
        assertEquals(note.duration(), note.getRange().length() + 1);
    }

    @Test
    void length() {
        Note note = new Note(60, new Range(0, 479));
        assertEquals(479, note.length());
        assertEquals(note.length(), note.getRange().length());
        assertEquals(note.length(), note.getRhythm().getDuration() - 1);
    }

    @Test
    void builder() {

        Note note1 = Note.builder()
                .pitch(60)
                .isHeld(true)
                .instrument("violin")
                .start(0)
                .stop(480 - 1)
                .build();

        Note note2 = Note.builder()
                .pitch(60)
                .build();

        Note note3 = Note.builder(note2)
                .then()
                .stop(1920)
                .build();

    }

}