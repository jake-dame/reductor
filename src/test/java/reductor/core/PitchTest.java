package reductor.core;


import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class PitchTest {


    @Test
    void ctor_LowerBound() {
        // bottom edge
        assertEquals(0, new Pitch("c").value());
        assertEquals(0, new Pitch("dbb-1").value());
        // special cases with valid register of -2
        assertEquals(0, new Pitch("b#-2").value());
        assertEquals(1, new Pitch("bx-2").value());
    }

    @Test
    void ctor_StringsUsingB() {
        // This is not an invalid string -- it is B-flat.
        assertEquals(10, new Pitch("bb").value());
        // Similarly, this is not an accidental-only string -- it's just a B.
        assertEquals(11, new Pitch("b").value());

        assertEquals(23, new Pitch("b0").value());
        assertEquals(10, new Pitch("bb-1").value());
        assertEquals(11, new Pitch("b-1").value());
        assertEquals(60, new Pitch("b#3").value());
    }

    @Test
    void ctor_UpperBound() {
        // top edge
        assertEquals(127, new Pitch("g9").value());
        assertEquals(127, new Pitch("fx9").value());
        // special case of valid register of 9
        assertEquals(127, new Pitch("abb9").value());
    }

    @Test
    void ctor_enharmonicSpelling() {
        /* Can't find a key that you can represent as all 4 (or 5 if counting natural)
        accidentals. So need two different pitches to test. */
        // enharmonic (with F#)
        assertEquals(66, new Pitch("ex4").value());
        assertEquals(66, new Pitch("f#4").value());
        assertEquals(66, new Pitch("gb4").value());
        // enharmonic (with A)
        assertEquals(69, new Pitch("gx4").value());
        assertEquals(69, new Pitch("a4").value());
        assertEquals(69, new Pitch("Bbb4").value());
        // enharmonic at a register boundary
        assertEquals(60, new Pitch("c4").value());
        assertEquals(60, new Pitch("b#3").value());
        assertEquals(60, new Pitch("dbb4").value());
    }


}
