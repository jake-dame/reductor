package reductor.core;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static reductor.dev.Catalog.*;


class PieceTest {


    @Test
    void gettingMeasuresPieceNoPickup() {

        /*
         FACT SHEET for Chopin Prelude Op.28 No. 20 in C minor
             + tick length: 24960
             + MeasureList in actual score: 13, fermata whole on last
         */
        var piece = PieceFactory.getTester(Path.of(
                "assets/pieces/chopin-prelude-c-minor/chopin-prelude-c-minor.mid"));

        // Pickup measure.
        assertFalse(piece.ma.hasPickup);
        assertThrows(RuntimeException.class, () -> piece.ma.getMeasure(0));
        assertFalse(piece.ma.getMeasure(1).isPickup());

        // Num measures and edges/
        assertEquals(13, piece.ma.getNumMeasures());
        assertThrows(RuntimeException.class, () -> piece.ma.getMeasure(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> piece.ma.getMeasure(14));


        // First measure.
        assertEquals(1, piece.ma.getMeasure(1).getMeasureNumber());
        assertEquals(1, piece.ma.getFirstMeasure().getMeasureNumber());

        // Last measure.
        assertEquals(13, piece.ma.getMeasure(piece.ma.getNumMeasures()).getMeasureNumber());
        assertEquals(13, piece.ma.getLastMeasure().getMeasureNumber());
    }

    @Test
    void gettingMeasuresPiecePickup() {

        /*
         FACT SHEET for Chopin Prelude Op.28 No. 4 in E minor
             + tick length: 48480
             + MeasureList in actual score: 25 full + quarter pickup; fermata whole on last
         */
        var piece = PieceFactory.getTester(Path.of(
                "assets/pieces/chopin-prelude-e-minor/chopin-prelude-e-minor.mid"));

        // Pickup measure.
        assertTrue(piece.ma.hasPickup);
        Measure pickup = piece.ma.getMeasure(0);
        assertEquals(0, pickup.getMeasureNumber());
        assertTrue(pickup.isPickup());

        // Num measures and edges.
        assertEquals(25, piece.ma.getNumMeasures());
        assertThrows(IndexOutOfBoundsException.class, () -> piece.ma.getMeasure(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> piece.ma.getMeasure(26));

        // First measure.
        assertEquals(1, piece.ma.getMeasure(1).getMeasureNumber());
        assertEquals(1, piece.ma.getFirstMeasure().getMeasureNumber());

        // Last measure.
        assertEquals(25, piece.ma.getMeasure(piece.ma.getNumMeasures()).getMeasureNumber());
        assertEquals(25, piece.ma.getLastMeasure().getMeasureNumber());
    }

    @Test
    void pseudoPickupTest() {

        var piece = PieceFactory.getTester(PSEUDO_ANACRUSIS_TEST);

        // Pickup measure.
        assertTrue(piece.ma.hasPickup);
        Measure pickup = piece.ma.getMeasure(0);
        assertEquals(0, pickup.getMeasureNumber());
        assertTrue(pickup.isPickup());

        // Num measures and edges.
        assertEquals(2, piece.ma.getNumMeasures());
        assertThrows(IndexOutOfBoundsException.class, () -> piece.ma.getMeasure(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> piece.ma.getMeasure(26));

        // First measure.
        assertEquals(1, piece.ma.getMeasure(1).getMeasureNumber());
        assertEquals(1, piece.ma.getFirstMeasure().getMeasureNumber());

        // Last measure.
        assertEquals(2, piece.ma.getMeasure(piece.ma.getNumMeasures()).getMeasureNumber());
        assertEquals(2, piece.ma.getLastMeasure().getMeasureNumber());
    }


    /// For getColumns() test see


    @Test
    void getMeasureRanges() {

        /*
               TimeSig
            no.      Range          length  notes

            m1  6/4: [0, 2879]       2880   6
            m2  5/4: [2880, 5279]    2400   5
            m3  4/4: [5280, 7199]    1920   4
            m4  3/4: [7200, 8639]    1440   3
            m5  2/4: [8640, 9599]    960    2
            m6  12/8:[9600, 12479]   2880   12
            m7  9/8: [12480, 14639]  2160   9
            m8  7/8: [14640, 16319]  1680   7
            m9  6/8: [16320, 17759]  1440   6
            m10 5/8: [17760, 18959]  1200   5
            m11 4/8: [18960, 19919]  960    4
            m12 3/8: [19920, 20639]  720    3
            m13 2/2: [20640, 22559]  1920   2
            m14 3/2: [22560, 25439]  2880   3
        */

        Range m1 = new Range(0,2879);
        Range m2 = new Range(2880,5279);
        Range m3 = new Range(5280,7199);
        Range m4 = new Range(7200,8639);
        Range m5 = new Range(8640,9599);
        Range m6 = new Range(9600,12479);
        Range m7 = new Range(12480,14639);
        Range m8 = new Range(14640,16319);
        Range m9 = new Range(16320,17759);
        Range m10 = new Range(17760,18959);
        Range m11 = new Range(18960,19919);
        Range m12 = new Range(19920,20639);
        Range m13 = new Range(20640,22559);
        Range m14 = new Range(22560,25439);

        ArrayList<Range> expected = new ArrayList<>( List.of(
                m1, m2, m3, m4, m5, m6, m7, m8, m9, m10, m11, m12, m13, m14
        ) );

        var piece = PieceFactory.getTester(Path.of(
                "assets/tests/MeasureSizeTest.mid"));

        ArrayList<Range> actual = piece.getMeasureRanges();
        actual.sort(null);

        assertEquals(expected, actual);
    }


}
