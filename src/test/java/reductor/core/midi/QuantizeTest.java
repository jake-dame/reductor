package reductor.core.midi;

import org.junit.jupiter.api.Test;
import reductor.core.Range;
import reductor.core.RhythmTypeBeta;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static reductor.core.midi.Quantize.quantize;


class QuantizeTest {

    private static final int TPQ = 480;

    private static class TestPair {

        private final Range offset;
        private final Range expected;

        private final long start;
        private final RhythmTypeBeta type;
        private final int tpq;

        private TestPair(long start, RhythmTypeBeta type, int tpq) {
            this.start = start;
            this.type = type;
            this.tpq = tpq;

            this.expected = new Range(start, (long) (start + type.duration - 1));
            this.offset = offset(expected);
        }

        public Range getOffset() { return this.offset; }
        public Range getExpected() { return this.expected; }

        public static TestPairBuilder get() {
            return new TestPairBuilder();
        }

        static class TestPairBuilder {

            private RhythmTypeBeta type;
            private long start;
            private int tpq;


            TestPairBuilder type(RhythmTypeBeta type) {
                this.type = type;
                return this;
            }

            TestPairBuilder startAt(long tick) {
                this.start = tick;
                return this;
            }

            TestPairBuilder resolution(int tpq) {
                this.tpq = tpq;
                return this;
            }

            TestPair build() {
                return new TestPair(start, type, tpq);
            }

        }

    }

    public static Range offset(Range range) {
        return offset(range.low(), RhythmTypeBeta.type(range));
    }

    public static Range offset(long startTick, RhythmTypeBeta rhy) {

        long stop = (long) (rhy.duration - 1);

        ArrayList<Long> offsets = new ArrayList<>();

        long maxOffset = (long) (Math.log(rhy.duration));

        long i = -maxOffset;
        while (i < maxOffset) {
            if (i == 0) { i++; continue;}
            offsets.add(i);
            i++;
        }

        long offsetStart = offsets.get(new Random().nextInt(offsets.size()));
        long offsetStop = offsets.get(new Random().nextInt(offsets.size()));

        long newStart = Math.max(startTick + offsetStart, 0);
        long newStop = stop + offsetStop - 1;

        return new Range(newStart, newStop);
    }

    //@Test
    //void test() {
    //
    //    TestPair pair = TestPair.get()
    //            .startAt(0)
    //            .type(r_4)
    //            .resolution(240)
    //            .build();
    //
    //    System.out.println(pair.getOffset());
    //    assertEquals(pair.getExpected(), pair.getOffset());
    //
    //}

    @Test
    void test1() {

        ArrayList<Range> ranges = new ArrayList<>( List.of(
                new Range(0, 481)
        ));

        var quantized = quantize(ranges, 480);
        assertEquals(new Range(0, 479), quantized.getFirst());

    }

    @Test
    void test2() {

        ArrayList<Range> ranges = new ArrayList<>( List.of(
                new Range(480, 961)
        ));

        var quantized = quantize(ranges, 480);
        assertEquals(new Range(480, 959), quantized.getFirst());

    }

    @Test
    void test3() {

        ArrayList<Range> ranges = new ArrayList<>( List.of(
                new Range(479, 961)
        ));

        var quantized = quantize(ranges, 480);
        assertEquals(new Range(480, 959), quantized.getFirst());

    }

    @Test
    void test4() {

        ArrayList<Range> ranges = new ArrayList<>( List.of(
                new Range(0, 161)
        ));

        var quantized = quantize(ranges, 480);
        assertEquals(new Range(0, 159), quantized.getFirst());
        assertEquals(RhythmTypeBeta.r_8in3, RhythmTypeBeta.type(quantized.getFirst().duration() + 1));

    }

    @Test
    void test5() {

        ArrayList<Range> ranges = new ArrayList<>( List.of(
                new Range(159, 321)
        ));

        var quantized = quantize(ranges, 480);
        assertEquals(new Range(160, 319), quantized.getFirst());
        assertEquals(RhythmTypeBeta.r_8in3, RhythmTypeBeta.type(quantized.getFirst().duration() + 1));

    }

    @Test
    void test6() {

        /*
        new Range(0, 480),
        new Range(480, 800),
        new Range(800, 960),
        new Range(960, 1120),
        new Range(1120, 1280),
        new Range(1280, 1440),
        new Range(1440, 1800),
        new Range(1800, 1920)
        */

        ArrayList<Range> ranges = new ArrayList<>( List.of(
                new Range(0, 480 + 2), // qtr

                new Range(480 - 1, 800 + 2), // trip 1 (qtr)
                new Range(800, 960 + 2), // trip 2 (8)

                new Range(960 - 1, 1120 + 2), // trip 1  (8)
                new Range(1120 - 1, 1280 + 2), // trip 2 (8)
                new Range(1280 - 1, 1440 + 2), // trip 3 (8)

                new Range(1440 - 1, 1800 + 2), // dotted 8
                new Range(1800 - 1, 1920 + 2)  // 16th
        ));

        var quantized = quantize(ranges, 480);
        quantized.sort(null);
        assertEquals(8, quantized.size());

        assertEquals(new Range(0, 480 - 1), quantized.get(0));
        assertEquals(new Range(480, 800 - 1), quantized.get(1));
        assertEquals(new Range(800, 960 - 1), quantized.get(2));
        assertEquals(new Range(960, 1120 - 1), quantized.get(3));
        assertEquals(new Range(1120, 1280 - 1), quantized.get(4));
        assertEquals(new Range(1280, 1440 - 1), quantized.get(5));
        assertEquals(new Range(1440, 1800 - 1), quantized.get(6));
        assertEquals(new Range(1800, 1920 - 1), quantized.get(7));

        assertEquals(RhythmTypeBeta.r_4, RhythmTypeBeta.type(quantized.get(0)));
        assertEquals(RhythmTypeBeta.r_4in3, RhythmTypeBeta.type(quantized.get(1)));
        assertEquals(RhythmTypeBeta.r_8in3, RhythmTypeBeta.type(quantized.get(2)));
        assertEquals(RhythmTypeBeta.r_8in3, RhythmTypeBeta.type(quantized.get(3)));
        assertEquals(RhythmTypeBeta.r_8in3, RhythmTypeBeta.type(quantized.get(4)));
        assertEquals(RhythmTypeBeta.r_8in3, RhythmTypeBeta.type(quantized.get(5)));
        assertEquals(RhythmTypeBeta.r_8dot, RhythmTypeBeta.type(quantized.get(6)));
        assertEquals(RhythmTypeBeta.r_16, RhythmTypeBeta.type(quantized.get(7)));

    }

    @Test
    void test7() {

        assertEquals(new Range(0, 15 - 1), quantize(new Range(0,16), 480));
        assertEquals(new Range(15, 30 - 1), quantize(new Range(14,32), 480));
        assertEquals(new Range(30, 45 - 1), quantize(new Range(29,44), 480));
        assertEquals(new Range(45, 60 - 1), quantize(new Range(43,58), 480));

    }


}
