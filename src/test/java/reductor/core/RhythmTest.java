package reductor.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;


class RhythmTest {


    @Test
    void testEnumBases() {

        assertEquals(RhythmTypeAlpha.WHOLE, RhythmTypeAlpha.getEnumType(1920));
        assertEquals(RhythmTypeAlpha.HALF, RhythmTypeAlpha.getEnumType(960));
        assertEquals(RhythmTypeAlpha.QUARTER, RhythmTypeAlpha.getEnumType(480));
        assertEquals(RhythmTypeAlpha.EIGHTH, RhythmTypeAlpha.getEnumType(240));
        assertEquals(RhythmTypeAlpha.SIXTEENTH, RhythmTypeAlpha.getEnumType(120));
        assertEquals(RhythmTypeAlpha.THIRTY_SECOND, RhythmTypeAlpha.getEnumType(60));
        assertEquals(RhythmTypeAlpha.SIXTY_FOURTH, RhythmTypeAlpha.getEnumType(30));
        assertEquals(RhythmTypeAlpha.ONE_TWENTY_EIGHTH, RhythmTypeAlpha.getEnumType(15));

    }

    //@Test
    //void testEnumBases() {
    //
    //    assertEquals(RhythmTypeAlpha.WHOLE, RhythmTypeAlpha.getEnumType(RES * 4, RES));
    //
    //    assertEquals(RhythmTypeAlpha.HALF, RhythmTypeAlpha.getEnumType(RES * 2, RES));
    //    assertEquals(RhythmTypeAlpha.TRIPLET_HALF, RhythmTypeAlpha.getEnumType(((RES * 4)/3), RES));
    //    assertEquals(RhythmTypeAlpha.DOTTED_HALF, RhythmTypeAlpha.getEnumType((RES * 3), RES));
    //
    //    assertEquals(RhythmTypeAlpha.QUARTER, RhythmTypeAlpha.getEnumType(480, RES));
    //    assertEquals(RhythmTypeAlpha.TRIPLET_QUARTER, RhythmTypeAlpha.getEnumType(320, RES));
    //    assertEquals(RhythmTypeAlpha.DOTTED_QUARTER, RhythmTypeAlpha.getEnumType(720, RES));
    //
    //    assertEquals(RhythmTypeAlpha.EIGHTH, RhythmTypeAlpha.getEnumType(240, RES));
    //    assertEquals(RhythmTypeAlpha.TRIPLET_EIGHTH, RhythmTypeAlpha.getEnumType(160, RES));
    //    assertEquals(RhythmTypeAlpha.DOTTED_EIGHTH, RhythmTypeAlpha.getEnumType(360, RES));
    //
    //    assertEquals(RhythmTypeAlpha.SIXTEENTH, RhythmTypeAlpha.getEnumType(120, RES));
    //    assertEquals(RhythmTypeAlpha.TRIPLET_SIXTEENTH, RhythmTypeAlpha.getEnumType(80, RES));
    //    assertEquals(RhythmTypeAlpha.DOTTED_SIXTEENTH, RhythmTypeAlpha.getEnumType(180, RES));
    //
    //    assertEquals(RhythmTypeAlpha.THIRTY_SECOND, RhythmTypeAlpha.getEnumType(60, RES));
    //    assertEquals(RhythmTypeAlpha.TRIPLET_THIRTY_SECOND, RhythmTypeAlpha.getEnumType(40, RES));
    //    assertEquals(RhythmTypeAlpha.DOTTED_THIRTY_SECOND, RhythmTypeAlpha.getEnumType(90, RES));
    //
    //    assertEquals(RhythmTypeAlpha.SIXTY_FOURTH, RhythmTypeAlpha.getEnumType(30, RES));
    //    assertEquals(RhythmTypeAlpha.TRIPLET_SIXTY_FOURTH, RhythmTypeAlpha.getEnumType(20, RES));
    //    assertEquals(RhythmTypeAlpha.DOTTED_SIXTY_FOURTH, RhythmTypeAlpha.getEnumType(45, RES));
    //
    //    assertEquals(RhythmTypeAlpha.ONE_TWENTY_EIGHTH, RhythmTypeAlpha.getEnumType(15, RES));
    //    assertEquals(RhythmTypeAlpha.TRIPLET_ONE_TWENTY_EIGHTH, RhythmTypeAlpha.getEnumType(10, RES));
    //    assertEquals(RhythmTypeAlpha.DOTTED_ONE_TWENTY_EIGHTH, RhythmTypeAlpha.getEnumType(22, RES));
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