package reductor;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


class RhythmTest {

    static { Context context = Context.createContext(); }

    static private final int RES = 480;

    @Test
    void testEnumBases() {

        assertEquals(RhythmType.WHOLE, RhythmType.getEnumType(1920));
        assertEquals(RhythmType.HALF, RhythmType.getEnumType(960));
        assertEquals(RhythmType.QUARTER, RhythmType.getEnumType(480));
        assertEquals(RhythmType.EIGHTH, RhythmType.getEnumType(240));
        assertEquals(RhythmType.SIXTEENTH, RhythmType.getEnumType(120));
        assertEquals(RhythmType.THIRTY_SECOND, RhythmType.getEnumType(60));
        assertEquals(RhythmType.SIXTY_FOURTH, RhythmType.getEnumType(30));
        assertEquals(RhythmType.ONE_TWENTY_EIGHTH, RhythmType.getEnumType(15));

    }

    //@Test
    //void testEnumBases() {
    //
    //    assertEquals(RhythmType.WHOLE, RhythmType.getEnumType(RES * 4, RES));
    //
    //    assertEquals(RhythmType.HALF, RhythmType.getEnumType(RES * 2, RES));
    //    assertEquals(RhythmType.TRIPLET_HALF, RhythmType.getEnumType(((RES * 4)/3), RES));
    //    assertEquals(RhythmType.DOTTED_HALF, RhythmType.getEnumType((RES * 3), RES));
    //
    //    assertEquals(RhythmType.QUARTER, RhythmType.getEnumType(480, RES));
    //    assertEquals(RhythmType.TRIPLET_QUARTER, RhythmType.getEnumType(320, RES));
    //    assertEquals(RhythmType.DOTTED_QUARTER, RhythmType.getEnumType(720, RES));
    //
    //    assertEquals(RhythmType.EIGHTH, RhythmType.getEnumType(240, RES));
    //    assertEquals(RhythmType.TRIPLET_EIGHTH, RhythmType.getEnumType(160, RES));
    //    assertEquals(RhythmType.DOTTED_EIGHTH, RhythmType.getEnumType(360, RES));
    //
    //    assertEquals(RhythmType.SIXTEENTH, RhythmType.getEnumType(120, RES));
    //    assertEquals(RhythmType.TRIPLET_SIXTEENTH, RhythmType.getEnumType(80, RES));
    //    assertEquals(RhythmType.DOTTED_SIXTEENTH, RhythmType.getEnumType(180, RES));
    //
    //    assertEquals(RhythmType.THIRTY_SECOND, RhythmType.getEnumType(60, RES));
    //    assertEquals(RhythmType.TRIPLET_THIRTY_SECOND, RhythmType.getEnumType(40, RES));
    //    assertEquals(RhythmType.DOTTED_THIRTY_SECOND, RhythmType.getEnumType(90, RES));
    //
    //    assertEquals(RhythmType.SIXTY_FOURTH, RhythmType.getEnumType(30, RES));
    //    assertEquals(RhythmType.TRIPLET_SIXTY_FOURTH, RhythmType.getEnumType(20, RES));
    //    assertEquals(RhythmType.DOTTED_SIXTY_FOURTH, RhythmType.getEnumType(45, RES));
    //
    //    assertEquals(RhythmType.ONE_TWENTY_EIGHTH, RhythmType.getEnumType(15, RES));
    //    assertEquals(RhythmType.TRIPLET_ONE_TWENTY_EIGHTH, RhythmType.getEnumType(10, RES));
    //    assertEquals(RhythmType.DOTTED_ONE_TWENTY_EIGHTH, RhythmType.getEnumType(22, RES));
    //}

    @Test
    void testBasicRhythmValues() {

        Rhythm r1 = new Rhythm(480); // quarter
        Rhythm r2 = new Rhythm(720); // dotted quarter

        Rhythm r3 = new Rhythm(240); // eighth
        Rhythm r4 = new Rhythm(360); // dotted eighth
        Rhythm r5 = new Rhythm(160); // triplet eighth

        Rhythm r6 = new Rhythm(10000); // tied whole

    }

}