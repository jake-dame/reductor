package reductor.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;


class RhythmTest {


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
