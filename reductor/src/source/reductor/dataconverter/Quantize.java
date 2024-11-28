package reductor.dataconverter;

import reductor.piece.Range;

import java.util.ArrayList;


public class Quantize {

    // Least Common Denominator before fractional ticks; assuming resolution of 480
    private final int LCD = 15;

    private final int TPQ;

    ArrayList<Range> ranges;

    Quantize(int TPQ, ArrayList<Range> passedRanges) {

        if (TPQ % 15 != 0) {throw new RuntimeException("this program does not support irregular resolutions: " + TPQ); }

        this.TPQ = TPQ;

        this.ranges = new ArrayList<>(passedRanges);
        ranges.sort(null);

        quantize();
    }

    public void quantize() {

        if (ranges == null || ranges.isEmpty()) { return; }

        /*
        For now, we will start the grid at the first range's low.
        In the future, we may incorporate shifting all notes to 0.
        However, that may be complex due to the case where the first range itself is irregular. At that point, you
        would have to make some consequential decisions right up front.
        */
        long startTick = ranges.getFirst().low();

        /*
        The math seems a little weird here, but if you read the Range documentation, you should know that ranges
        themselves should be half-open when compared to their duration. In this case, the duration of the grid range
        is equivalent to the LCD. We have to subtract one to make it representative of a Range
        */
        Range gridRange = new Range(startTick, startTick + (LCD - 1));

        for (Range range : ranges) {

            // If there was a long period of rests between two ranged objects, we need to increment the grid range,
            // but keep it stuck to the grid by only doing so by the LCD
            while (!gridRange.overlaps(range)) {
                    gridRange = Range.getShiftedInstance(range, LCD);
            }

            // Early break.
            if (range.compareTo(gridRange) == 0) { continue; }

            Range closestMatch = findClosestMatchToGridRange(range, gridRange);

            /*
            Now, for the real challenge: how to interpret the discrepancy between the current range and the closest
            match.

            We will classify "regular" rhythms as any rhythm that is a whole, quarter, 8th, etc. (i.e. a
            "power-of-two" rhythm). "Irregular" rhythms would be triplets, quadruplets (in 3/4 time), quintuplets,
            etc., where a quantity of notes that is NOT a power-of-two exists within a beat. (The same principle
            applies to triple meters such as 3/4 and 6/8, just with different rules).

            In 4/4 time, with resolution 480, a triplet 8th (three 8ths within the space of a quarter note) each have
             tick durations of 160 (160 * 3 = 480). 160 is NOT evenly divisible by 15, so it will inherently not snap
              to any grid. We need to avoid turning these into 8ths or 16ths.

             However, since these values are still ultimately deterministic and we can already calculate them
             dynamically with our RhythmType enum, the best we can do is:

                Find the closest matching rhythm (duration). We will have to do some extra work to find the place of
                second or third triplets, or second, third, fourth, or fifth quintuplets, etc., like:

                if (match is triplet)
                    if range is within threshold of triplet * 1
                        snap here
                    if range is within threshold of triplet * 2
                        snap here
                    if range is within threshold of triplet * 3
                        snap here
            */


        }

    }

    private Range findClosestMatchToGridRange(Range range, Range gridRange) {

        /*

          range 1:              0--------------29
          range 2:              0------------------31

          number line for grid: 0       15       30       45       60

          attempt 1:            0-------15
          attempt 2:            0-----------------30
          attempt 3:            0-------------------------45

          Attempt 1: not close enough to high ==> increase high by 15.
          Attempt 2: just over range 1, but not over range 2. However, close to both.
          Attempt 3: over range 2, but way further than attempt 2 got.

          Thresholds should be used to simulate rounding. In this case, we'll use half of the LCD -- 7.5, or, since
          we are using longs, we'll just go with 7 or 8.

         */

        Range temp = new Range(gridRange);

        final long ROUNDED_UP = 8;
        final long ROUNDED_DOWN = -7;

        long target;

        if (range.low() != temp.low()) {
            target = range.low() + ROUNDED_DOWN;
            while (temp.low() < target) { temp = temp.setLow(temp.low() + 15); }

            target = range.low() + ROUNDED_UP;
            while (target < temp.low()) { temp = temp.setLow(temp.low() - 15); }
        }

        if (range.high() != temp.high()) {
            target = range.high() + ROUNDED_DOWN;
            while (temp.high() < target) { temp = temp.setHigh(temp.high() + 15); }
        }

        return new Range(temp);
    }


}