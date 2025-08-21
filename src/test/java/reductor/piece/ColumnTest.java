package reductor.piece;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


class ColumnTest {


    @Test
    void givenEmpty() {
        Column c = new Column(new ArrayList<>(), new Range());

        assertEquals(0, c.getLeftHand().size());
        assertEquals(0, c.getMiddle().size());
        assertEquals(0, c.getRightHand().size());

        assertEquals(c.size(), c.getLeftHand().size() + c.getMiddle().size() + c.getRightHand().size());

        // Should technically still be two-handed
        assertTrue(c.isTwoHanded());
    }

    @Test
    void givenOneNoteInBass() {
        Column c = new Column( Note.toList( List.of("C3") ), new Range());

        assertEquals(1, c.getLeftHand().size());
        assertEquals(0, c.getMiddle().size());
        assertEquals(0, c.getRightHand().size());

        assertEquals(c.size(), c.getLeftHand().size() + c.getMiddle().size() + c.getRightHand().size());

        assertTrue(c.isTwoHanded());
    }

    @Test
    void givenOneNoteInTreble() {
        Column c = new Column( Note.toList( List.of("C5") ), new Range());

        assertEquals(0, c.getLeftHand().size());
        assertEquals(0, c.getMiddle().size());
        assertEquals(1, c.getRightHand().size());

        assertEquals(c.size(), c.getLeftHand().size() + c.getMiddle().size() + c.getRightHand().size());

        assertTrue(c.isTwoHanded());
    }

    @Test
    void givenMiddleC() {

        // Middle C and above should go to the RH (it is the decision boundary when distributing)

        Column col = new Column( Note.toList( List.of("C4") ), new Range());

        assertEquals(0, col.getLeftHand().size());
        assertEquals(0, col.getMiddle().size());
        assertEquals(1, col.getRightHand().size());

        assertEquals(col.size(), col.getLeftHand().size() + col.getMiddle().size() + col.getRightHand().size());

        assertTrue(col.isTwoHanded());
    }

    @Test
    void givenTwoNotesAboveMiddleC() {

        // There should be no middle or leftover here, and both treble notes go to hands (RH and LH)
        // The two notes are too far apart to be played by one hand (2 octaves apart)

        Column c = new Column( Note.toList( List.of("C5", "C7") ), new Range());

        assertEquals(1, c.getLeftHand().size());
        assertEquals(0, c.getMiddle().size());
        assertEquals(1, c.getRightHand().size());

        assertEquals(c.size(), c.getLeftHand().size() + c.getMiddle().size() + c.getRightHand().size());

        assertTrue(c.isTwoHanded());
    }

    @Test
    void spanMaxEdges() {

        // Should be split evenly, with edges on SPAN_MAX

        ArrayList<Note> list = Note.toList( List.of(
                "C3", // ↓
                "D4", // SPAN_MAX (from anchor)

                "D#4", // leftover (1 key in between the thumbs)

                "E4", // SPAN_MAX (from anchor)
                "F#5" // ↑
        ));

        Column c = new Column(list, new Range());

        assertEquals(2, c.getLeftHand().size());
        assertEquals(1, c.getMiddle().size());
        assertEquals(2, c.getRightHand().size());

        assertEquals(c.size(), c.getLeftHand().size() + c.getMiddle().size() + c.getRightHand().size());

        assertFalse(c.isTwoHanded());
    }

    @Test
    void notesMaxEdges() {

        ArrayList<Note> list = Note.toList( List.of(
                "C3", // ↓
                "D3",
                "E3",
                "F3",
                "G3",
                "A3", // NOTES_MAX (but only a major 6th from anchor)

                "A#3", // 1 key away from LH thumb
                "B4", // 1 key away from RH thumb

                "C5", // NOTES_MAX (but only a major 6th from anchor)
                "D5",
                "E5",
                "F5",
                "G5",
                "A5"  // ↑

        ));

        Column c = new Column(list, new Range());

        assertEquals(6, c.getLeftHand().size());
        assertEquals(2, c.getMiddle().size());
        assertEquals(6, c.getRightHand().size());

        assertEquals(c.size(), c.getLeftHand().size() + c.getMiddle().size() + c.getRightHand().size());

        assertFalse(c.isTwoHanded());
    }

    @Test
    void LHOnly() {

        ArrayList<Note> list = Note.toList( List.of(
                "C3", "E3", "G3"
        ));

        Column c = new Column(list, new Range());

        assertEquals(3, c.getLeftHand().size());
        assertEquals(0, c.getMiddle().size());
        assertEquals(0, c.getRightHand().size());

        assertEquals(c.size(), c.getLeftHand().size() + c.getMiddle().size() + c.getRightHand().size());

        assertTrue(c.isTwoHanded());

    }


    @Test
    void RHOnly() {

        // Should all be in RH

        ArrayList<Note> list =Note.toList( List.of(
                "C6", "E6", "G6"
        ));

        Column c = new Column(list, new Range());

        assertEquals(0, c.getLeftHand().size());
        assertEquals(0, c.getMiddle().size());
        assertEquals(3, c.getRightHand().size());

        assertEquals(c.size(), c.getLeftHand().size() + c.getMiddle().size() + c.getRightHand().size());

        assertTrue(c.isTwoHanded());
    }

    @Test
    void reassignmentOfLHToTreble() {

        // Exactly 2-hands-worth of notes, but everything above middle C

        ArrayList<Note> list = Note.toList( List.of(
            "C5", "D5", "E5", "F5", "G5", "A5",          "C6", "D6", "E6", "F6", "G6", "A6"
        ));

        Column c = new Column(list, new Range());

        assertEquals(6, c.getLeftHand().size());
        assertEquals(0, c.getMiddle().size());
        assertEquals(6, c.getRightHand().size());

        assertEquals(c.size(), c.getLeftHand().size() + c.getMiddle().size() + c.getRightHand().size());

        assertTrue(c.isTwoHanded());
    }

    @Test
    void splitPointWithEvenNumberSemitonesBetween() {

        // Step-wise contrary motion --> the thumbs CANNOT meet on the same key (E and Eb instead)
        // So the upper semitone is grabbed à la upper median.

        ArrayList<Note> list = Note.toList( List.of(
                "C3", "E3", "G3",         "C5", "E5", "G5"
        ));

        Column c = new Column(list, new Range());

        assertEquals(3, c.getLeftHand().size());
        assertEquals(0, c.getMiddle().size());
        assertEquals(3, c.getRightHand().size());

        assertEquals(c.size(), c.getLeftHand().size() + c.getMiddle().size() + c.getRightHand().size());

        assertTrue(c.isTwoHanded());

        assertEquals(Pitch.toInt("E4"), c.getSplitPointPitch());
    }

    @Test
    void splitPointWithOddNumberSemitonesBetween() {

        // Step-wise contrary motion --> the thumbs meet on the same key (Eb)

        ArrayList<Note> list = Note.toList( List.of(
                "C3", "E3", "G3",      "B4", "E5", "G5"
        ));                          // ^ down 1

        Column c = new Column(list, new Range());

        assertEquals(3, c.getLeftHand().size());
        assertEquals(0, c.getMiddle().size());
        assertEquals(3, c.getRightHand().size());

        assertEquals(c.size(), c.getLeftHand().size() + c.getMiddle().size() + c.getRightHand().size());

        assertTrue(c.isTwoHanded());

        assertEquals(Pitch.toInt("Eb4"), c.getSplitPointPitch());
    }


    /* Below are "real-life" tests -- actual chords from actual pieces (sources from score, not MIDI) */


    //@Test
    //void chopinPreludeCMinor() {
    //
    //    // What's being tested: not really anything other than this is nearly impossible to do right with only basic
    //    // analysis tools
    //
    //    // Chopin op28 no20: very last chord
    //
    //    // In score:
    //    // LH: "C3", "G3"
    //    // RH: "C4", "Eb4", "G4", "C5"
    //
    //    // Unfortunately the program cannot know how to do this correctly without horizontal analysis
    //
    //    ArrayList<Note> list = Note.toList( List.of(
    //            "C3",
    //            "G3",
    //            "C4",
    //
    //            "Eb4",
    //            "G4",
    //            "C5"
    //    ));
    //
    //    Column c = new Column(list);
    //
    //    assertEquals(3, c.getLH().size());
    //    assertEquals(0, c.getMiddle().size());
    //    assertEquals(3, c.getRH().size());
    //
    //    assertEquals(c.size(), c.getLH().size() + c.getMiddle().size() + c.getRH().size());
    //
    //    assertTrue(c.isTwoHanded());
    //}
    //
    //@Test
    //void lisztBeethovenEroicaScherzo() {
    //
    //    // What's being tested: 2 notes in each hand, within the span of a major 10th
    //
    //    // First chord of Liszt-Beethoven: Symphony 3 (iii)
    //
    //    ArrayList<Note> list = Note.toList( List.of(
    //            "Ab2", "Eb3",       "Ab3", "C4"
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
    //void mozartSonataK545i() {
    //
    //    // What's being tested: redistribution with two monophonic lines
    //
    //    // First column/"chord" of Mozart K545 (i)
    //
    //    ArrayList<Note> list = Note.toList( List.of(
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