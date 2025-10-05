package reductor.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


class PitchTest {

    @Test
    void toInt() {
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
    void getRegister() {

        assertEquals(-1, Pitch.getRegister(0));
        assertEquals(-1, Pitch.getRegister(11));

        assertEquals(0, Pitch.getRegister(12));
        assertEquals(0, Pitch.getRegister(23));

        assertEquals(1, Pitch.getRegister(24));
        assertEquals(1, Pitch.getRegister(35));

        assertEquals(2, Pitch.getRegister(36));
        assertEquals(2, Pitch.getRegister(47));

        assertEquals(3, Pitch.getRegister(48));
        assertEquals(3, Pitch.getRegister(59));

        assertEquals(4, Pitch.getRegister(60));
        assertEquals(4, Pitch.getRegister(71));

        assertEquals(5, Pitch.getRegister(72));
        assertEquals(5, Pitch.getRegister(83));

        assertEquals(6, Pitch.getRegister(84));
        assertEquals(6, Pitch.getRegister(95));

        assertEquals(7, Pitch.getRegister(96));
        assertEquals(7, Pitch.getRegister(107));

        assertEquals(8, Pitch.getRegister(108));
        assertEquals(8, Pitch.getRegister(119));

        assertEquals(9, Pitch.getRegister(120));
        assertEquals(9, Pitch.getRegister(127));

    }

}