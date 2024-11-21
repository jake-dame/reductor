package reductor;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


class RhythmTest {

    static { Context context = Context.createContext(); }

    static private final int RES = 480;

    @Test
    void testEnumBases() {

        assertEquals(RhythmBase.WHOLE, RhythmBase.getEnumType(1920));
        assertEquals(RhythmBase.HALF, RhythmBase.getEnumType(960));
        assertEquals(RhythmBase.QUARTER, RhythmBase.getEnumType(480));
        assertEquals(RhythmBase.EIGHTH, RhythmBase.getEnumType(240));
        assertEquals(RhythmBase.SIXTEENTH, RhythmBase.getEnumType(120));
        assertEquals(RhythmBase.THIRTY_SECOND, RhythmBase.getEnumType(60));
        assertEquals(RhythmBase.SIXTY_FOURTH, RhythmBase.getEnumType(30));
        assertEquals(RhythmBase.ONE_TWENTY_EIGHTH, RhythmBase.getEnumType(15));

    }

    //@Test
    //void testEnumBases() {
    //
    //    assertEquals(RhythmBase.WHOLE, RhythmBase.getEnumType(RES * 4, RES));
    //
    //    assertEquals(RhythmBase.HALF, RhythmBase.getEnumType(RES * 2, RES));
    //    assertEquals(RhythmBase.TRIPLET_HALF, RhythmBase.getEnumType(((RES * 4)/3), RES));
    //    assertEquals(RhythmBase.DOTTED_HALF, RhythmBase.getEnumType((RES * 3), RES));
    //
    //    assertEquals(RhythmBase.QUARTER, RhythmBase.getEnumType(480, RES));
    //    assertEquals(RhythmBase.TRIPLET_QUARTER, RhythmBase.getEnumType(320, RES));
    //    assertEquals(RhythmBase.DOTTED_QUARTER, RhythmBase.getEnumType(720, RES));
    //
    //    assertEquals(RhythmBase.EIGHTH, RhythmBase.getEnumType(240, RES));
    //    assertEquals(RhythmBase.TRIPLET_EIGHTH, RhythmBase.getEnumType(160, RES));
    //    assertEquals(RhythmBase.DOTTED_EIGHTH, RhythmBase.getEnumType(360, RES));
    //
    //    assertEquals(RhythmBase.SIXTEENTH, RhythmBase.getEnumType(120, RES));
    //    assertEquals(RhythmBase.TRIPLET_SIXTEENTH, RhythmBase.getEnumType(80, RES));
    //    assertEquals(RhythmBase.DOTTED_SIXTEENTH, RhythmBase.getEnumType(180, RES));
    //
    //    assertEquals(RhythmBase.THIRTY_SECOND, RhythmBase.getEnumType(60, RES));
    //    assertEquals(RhythmBase.TRIPLET_THIRTY_SECOND, RhythmBase.getEnumType(40, RES));
    //    assertEquals(RhythmBase.DOTTED_THIRTY_SECOND, RhythmBase.getEnumType(90, RES));
    //
    //    assertEquals(RhythmBase.SIXTY_FOURTH, RhythmBase.getEnumType(30, RES));
    //    assertEquals(RhythmBase.TRIPLET_SIXTY_FOURTH, RhythmBase.getEnumType(20, RES));
    //    assertEquals(RhythmBase.DOTTED_SIXTY_FOURTH, RhythmBase.getEnumType(45, RES));
    //
    //    assertEquals(RhythmBase.ONE_TWENTY_EIGHTH, RhythmBase.getEnumType(15, RES));
    //    assertEquals(RhythmBase.TRIPLET_ONE_TWENTY_EIGHTH, RhythmBase.getEnumType(10, RES));
    //    assertEquals(RhythmBase.DOTTED_ONE_TWENTY_EIGHTH, RhythmBase.getEnumType(22, RES));
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