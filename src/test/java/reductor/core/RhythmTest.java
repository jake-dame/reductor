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
