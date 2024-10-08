package reductor;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


public class RangeTest {


    @Test
    void construction() {

        Range range =  new Range(0,1);
        assertEquals(0, range.low(), "low should match first arg");
        assertEquals(1, range.high(), "high should match second arg");

    }

    @Test
    void copyConstruction() {

        Range original =  new Range(0,1);
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


    @SuppressWarnings("SimplifiableAssertion")
    @Test
    void equals() {

        Range target = new Range(10, 20);
        assertTrue(target.equals( new Range(10, 20) ), "target should equal other");
        assertFalse(target.equals( new Range(10, 21) ), "same low should not result in equality");
        assertFalse(target.equals( new Range(9, 20) ), "same high should not result in equality");

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

        assertTrue(new Range(0,100).overlaps(target),
                "should overlap if right endpoint == target right endpoint");
        assertTrue(new Range(0,101).overlaps(target),
                "should overlap if right endpoint > target right endpoint");
        assertFalse(new Range(0,99).overlaps(target),
                "should not overlap if right endpoint < target right endpoint");

    }


    @Test
    void compareToEquality() {

        Range target = new Range(100, 200);
        assertEquals(0, target.compareTo( new Range(100, 200) ), "target should be considered equal with other");

    }


    @Test
    void compareToWithPrimaryOrdering() {

        final long HIGH = 200;
        Range target = new Range(100, HIGH);
        assertEquals(-1, target.compareTo( new Range(101, HIGH) ), "target should be considered less than other");
        assertEquals(1, target.compareTo( new Range(99, HIGH) ), "target should be considered greater than other");

    }


    @Test
    void compareToWithSecondaryOrdering() {

        final long LOW = 100;
        Range target = new Range(LOW, 200);
        assertEquals(-1, target.compareTo( new Range(LOW, 201) ), "target should be considered less than other");
        assertEquals(1, target.compareTo( new Range(LOW, 199) ), "target should be considered greater than other");

    }


}