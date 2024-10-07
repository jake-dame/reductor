package reductor;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static reductor.Pitch.stringPitchToNumber;


class PitchTest {


    /// Test for {@link reductor.Pitch#stringPitchToNumber(String, int)}
    @Test
    void testStringPitchToNumber() {

        // test min, and min - 1
        assertEquals(stringPitchToNumber("C", -1), 0);
        assertEquals(stringPitchToNumber("B#", -1), 0);
        assertEquals(stringPitchToNumber("Dbb", -1), 0);

        assertThrows(IllegalArgumentException.class, () -> stringPitchToNumber("B", -2));

        // test max, and max + 1
        assertEquals(stringPitchToNumber("G", 9), 127);
        assertEquals(stringPitchToNumber("Fx", 9), 127);
        assertEquals(stringPitchToNumber("Abb", 9), 127);

        assertThrows(IllegalArgumentException.class, () -> stringPitchToNumber("G#", 9));

        // test register addition is correct
        assertEquals(stringPitchToNumber("C", 0), 12);
        assertEquals(stringPitchToNumber("C", 1), 24);
        assertEquals(stringPitchToNumber("C", 2), 36);
        assertEquals(stringPitchToNumber("C", 9), 120);

        // test enharmonic spellings + valid edge spellings
        int register = 5;
        assertEquals(stringPitchToNumber("F#", register), stringPitchToNumber("Gb", register));
        assertEquals(stringPitchToNumber("Fx", register), stringPitchToNumber("G", register));
        assertEquals(stringPitchToNumber("Gbb", register), stringPitchToNumber("F", register));
        assertEquals(stringPitchToNumber("Ab", register), stringPitchToNumber("G#", register));

        // test invalid string values
        assertThrows(IllegalArgumentException.class, () -> stringPitchToNumber("h", 0));
        assertThrows(IllegalArgumentException.class, () -> stringPitchToNumber("#", 0));
        assertThrows(IllegalArgumentException.class, () -> stringPitchToNumber("#A", 0));
        assertThrows(IllegalArgumentException.class, () -> stringPitchToNumber("A#1", 0));
        assertThrows(IllegalArgumentException.class, () -> stringPitchToNumber("A1#", 0));
        assertThrows(IllegalArgumentException.class, () -> stringPitchToNumber("Abb1", 0));
        assertThrows(IllegalArgumentException.class, () -> stringPitchToNumber("A1#", 0));

        // test invalid registers not already tested
        assertThrows(IllegalArgumentException.class, () -> new Note("C", 10));

    }


}