//package reductor.core;
//
//import org.junit.jupiter.api.Test;
//import reductor.core.builders.NoteBuilder;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//
//
///**
// * Unit tests for the {@linkplain Note} class.
// *
// * @see RangeTest
// */
//class NoteTest {
//
//
//    @Test
//    void copyConstruction() {
//        //Note note = new Note(60, new Range(0, 1));
//        Note note = NoteBuilder.builder().pitch(60).start(0).stop(1).build();
//        Note copy = new Note(note);
//        assertEquals(note, copy);
//    }
//
//    @Test
//    void getRange() {
//        Note note = NoteBuilder.builder().pitch(60).range(0, 1).build();
//        assertEquals(new Range(0, 1), note.getRange());
//    }
//
//    @Test
//    void pitch() {
//        Note note = NoteBuilder.builder().pitch(60).range(0, 1).build();
//        assertEquals(60, note.pitch());
//    }
//
//    @Test
//    void start() {
//        Note note = NoteBuilder.builder().pitch(60).range(0, 1).build();
//        assertEquals(0, note.start());
//        assertEquals(note.getRange().getLow(), note.start());
//    }
//
//    @Test
//    void stop() {
//        Note note = NoteBuilder.builder().pitch(60).range(0, 1).build();
//        assertEquals(1, note.stop());
//        assertEquals(note.getRange().getHigh(), note.stop());
//    }
//
//    @Test
//    void duration() {
//        Note note = NoteBuilder.builder().pitch(60).range(0, 479).build();
//        assertEquals(480, note.duration());
//        assertEquals(note.duration(), note.getRhythm().getDuration());
//        assertEquals(note.duration(), note.getRange().length() + 1);
//    }
//
//    @Test
//    void length() {
//        Note note = NoteBuilder.builder().pitch(60).range(0, 479).build();
//        assertEquals(479, note.length());
//        assertEquals(note.length(), note.getRange().length());
//        assertEquals(note.length(), note.getRhythm().getDuration() - 1);
//    }
//
//    @Test
//    void builder() {
//
//        Note note1 = NoteBuilder.builder()
//                .pitch(60)
//                .isHeld(true)
//                .instrument("violin")
//                .start(0)
//                .stop(480 - 1)
//                .build();
//
//        Note note2 = NoteBuilder.builder()
//                .pitch(60)
//                .build();
//
//        Note note3 = NoteBuilder.then(note2)
//                .stop(1920)
//                .build();
//
//    }
//
//}
