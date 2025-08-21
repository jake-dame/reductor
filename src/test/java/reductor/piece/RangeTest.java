package reductor.piece;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;


public class RangeTest {


    @Test
    void construction() {
        Range range = new Range(0, 1);
        assertEquals(0, range.low(), "low should match first arg");
        assertEquals(1, range.high(), "high should match second arg");
    }

    @Test
    void copyConstruction() {
        Range original = new Range(0, 1);
        Range copy = new Range(original);
        assertEquals(original.low(), copy.low(), "copy low should match original low");
        assertEquals(original.high(), copy.high(), "copy high should match original high");
    }

    @Test
    void invalidConstruction() {
        assertThrows(IllegalArgumentException.class, () -> new Range(10, 9), "should throw on high > low");
        assertThrows(IllegalArgumentException.class, () -> new Range(10, 10), "should throw on high == low");
        assertThrows(IllegalArgumentException.class, () -> new Range(-1, 9), "should throw on negative low");
        assertThrows(IllegalArgumentException.class, () -> new Range(1, -9), "should throw on negative high");
    }

    /// This may seem stupid to test, but if you ever change inclusivity properties for Range...
    @Test
    void length() {
        assertEquals(10, new Range(0,10).length());
        assertEquals(9, new Range(1,10).length());
        assertEquals(9, new Range(0,9).length());
    }

    @SuppressWarnings("SimplifiableAssertion")
    @Test
    void equals() {
        Range target = new Range(10, 20);
        assertTrue(target.equals(new Range(10, 20)), "target should equal other");
        assertFalse(target.equals(new Range(10, 21)), "same low should not result in equality");
        assertFalse(target.equals(new Range(9, 20)), "same high should not result in equality");
    }

    @Test
    void overlapsLeftEndpoint() {
        Range target = new Range(100, 200);
        assertTrue(new Range(200, 300).overlaps(target),
                "should overlap if left endpoint == target left");
        assertTrue(new Range(199, 300).overlaps(target),
                "should overlap if left endpoint < target left");
        assertFalse(new Range(201, 300).overlaps(target),
                "should not overlap if left endpoint > target left");
    }

    @Test
    void overlapsRightEndpoint() {
        Range target = new Range(100, 200);
        assertTrue(new Range(0, 100).overlaps(target),
                "should overlap if right endpoint == target right endpoint");
        assertTrue(new Range(0, 101).overlaps(target),
                "should overlap if right endpoint > target right endpoint");
        assertFalse(new Range(0, 99).overlaps(target),
                "should not overlap if right endpoint < target right endpoint");
    }

    @Test
    void containsRange() {

        Range target1 = new Range(0,100);
        Range target2  = new Range(100,200);

        // left endpoint edges: zero
        assertTrue(target1.contains( new Range(0,50) ));
        assertTrue(target1.contains( new Range(1,50) ));

        // exact alignment
        assertTrue(target1.contains( new Range(0,100) ));
        assertTrue(new Range(0,100).contains(target1));

        // right endpoint edges
        assertTrue(target1.contains( new Range(50,99) ));
        assertTrue(target1.contains( new Range(50,100) ));
        assertFalse(target1.contains( new Range(50,101) ));

        // both endpoint edges
        assertFalse(target2.contains( new Range(99,101) ));
        assertTrue(target2.contains( new Range(100,200) ));
        assertTrue(target2.contains( new Range(101,199) ));

        // left endpoint: non-zero
        assertFalse(target2.contains( new Range(99,150) ));
        assertTrue(target2.contains( new Range(100,150) ));
        assertTrue(target2.contains( new Range(101,150) ));
    }

    @Test
    void containsPoint() {

        Range target = new Range(0,479);

        // 0
        assertFalse(target.contains(-1));
        assertTrue(target.contains(0));
        assertTrue(target.contains(1));

        // 480
        assertTrue(target.contains(479));
        assertFalse(target.contains(480));
        assertFalse(target.contains(481));
    }

    @Test
    void compareToEquality() {
        Range target = new Range(100, 200);
        assertEquals(0, target.compareTo(new Range(100, 200)), "target should be considered equal with other");
    }

    @Test
    void compareToWithPrimaryOrdering() {
        Range target = new Range(100, 200);
        assertEquals(-1, target.compareTo(new Range(101, 200)), "target should be considered less than other");
        assertEquals(1, target.compareTo(new Range(99, 200)), "target should be considered greater than other");
    }

    @Test
    void compareToWithSecondaryOrdering() {
        Range target = new Range(100, 200);
        assertEquals(-1, target.compareTo(new Range(100, 201)), "target should be considered less than other");
        assertEquals(1, target.compareTo(new Range(100, 199)), "target should be considered greater than other");
    }

    @Test
    void concatenate() {

        ArrayList<Range> ranges = new ArrayList<>( List.of(
                new Range(100, 200),
                new Range(200, 400),
                new Range(200, 300),
                new Range(0, 1),
                new Range(10, 20)
        ));

        assertEquals(new Range(0, 400), Range.concatenate(ranges));
    }

    @Test
    void splitOverlapping() {

        Range r1 = new Range(0,20);
        Range r2 = new Range(10,30);

        var actual = Range.splitOverlapping(r1, r2);

        Range e1 = new Range(0,10);
        Range e2 = new Range(10,20);
        Range e3 = new Range(20,30);
        ArrayList<Range> expected = new ArrayList<>( List.of(e1, e2, e3));

        assertEquals(actual, expected);
    }

    @Test
    void fromStartTicks() {

        /*
         For a piece with 1 quarter, 3 triplet (8ths), and 3 8ths, all occurring in the same beat:

            List: [0, 480], [0, 160], [160, 320], [320, 480], [0, 240], [240, 480]
             len:    480       160        160         160        240        240

                        0                      480
            RH quarter: |-----------------------|
                               160     320
            RH (trips): |-------|-------|-------|
                                   240
            LH (8ths):  |-----------|-----------|
                        ↑       ↑   ↑   ↑       ↑
                        |       |   |   |       |
                        0      160 240 320     480

            So, columns should be: [0, 160], [160, 240], [240, 320], [320, 480]

            fromStartTicks(), when given a Set of start ticks, as well as a tick to end at (i.e. 480),
             can create these ranges (half-open)
         */

        Range quarter = new Range(0, 480-1);
        Range trip1 = new Range(0, 160-1);
        Range trip2 = new Range(160, 320-1);
        Range trip3 = new Range(320, 480-1);
        Range eighth1 = new Range(0, 240-1);
        Range eighth2 = new Range(240, 480-1);

        ArrayList<Range> noteRanges = new ArrayList<>( List.of(
                quarter, trip1, trip2, trip3, eighth1, eighth2
        ));

        ArrayList<Range> expected = new ArrayList<>( List.of(
                new Range(0, 160-1),
                new Range(160,240-1),
                new Range(240, 320-1),
                new Range(320, 480-1)
        ));

        IntervalTree<Range> tree = new IntervalTree<>(noteRanges);
        ArrayList<Range> list = tree.toListOfRanges();

        Set<Long> set = new HashSet<>();
        for (Range range : list) {
            set.add(range.low());
        }

        ArrayList<Range> actual = Range.fromStartTicks(set, 480L);

        assertEquals(expected, actual);

    }

    //@Test
    //void fromStartTicksWithFile() throws InvalidMidiDataException, UnpairedNoteException {
    //
    //
    //    Application dh = new Application();
    //    Piece piece = dh.getPiece(COLUMN_TEST_2);
    //
    //    var cols = piece.getColumns();
    //
    //    ArrayList<Range> actual = new ArrayList<>();
    //
    //    for (Column col : cols) {
    //        actual.add(col.getRange());
    //    }
    //
    //    actual.sort(null);
    //
    //    ArrayList<Range> expected = new ArrayList<>( List.of(
    //            new Range(0, 160-1),
    //            new Range(160,240-1),
    //            new Range(240, 320-1),
    //            new Range(320, 480-1)
    //    ));
    //
    //    assertEquals(expected, actual);
    //}


}