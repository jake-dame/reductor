package reductor.core;


import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;


public class RangeUtil {


    /** Return a copy of the passed Range, shifted by an offset. */
    public static Range getShiftedInstance(Range other, int offset) {
        // TODO: this does not currently clamp or validate offset.
        //       clamping would require global context value for final tick
        return new Range(other.getLow() + offset, other.getHigh() + offset);
    }

    /**
     * Returns a copy of the passed Range with both endpoints increased by the passed addend.
     * <p>
     * Acts as a {@code +=} operator of sorts, for a single Range object.
     *
     * @param range The Range to make a copy of.
     * @param addend The amount to increase each endpoint.
     */
    public static Range add(Range range, int addend) {
        return new Range(range.getLow() + addend, range.getHigh() + addend);
    }

    /**
     * Constructs a single Range from multiple Ranges.
     * <p>
     * Example: If given {@code [0,479]}, {@code [480,959]}, {@code [960,1919]}, returns {@code [0,1919]}.
     *
     * @param rangedElems Any Collection of {@link Ranged} objects.
     * @return A single Range object encompassing all Ranges in {@code rangedElems}.
     * @param <T> Any element that implements the {@link Ranged} interface.
     */
    public static <T extends Ranged> Range concatenate(Collection<T> rangedElems) {

        if (rangedElems.isEmpty()) { throw new RuntimeException("can't concatenate empty list"); }

        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        for (T elem : rangedElems) {
            if (elem.getRange().getLow() < min) { min = elem.getRange().getLow(); }
            if (elem.getRange().getHigh() > max) { max = elem.getRange().getHigh(); }
        }

        return new Range(min, max);
    }

    /**
     * Splits two passed overlapping Ranges into three distinct regions.
     * <p>
     * If the Ranges do not overlap at all, just returns copies of those Ranges.
     * <pre>
     * Given:
     *     0         20
     *     |-----------|
     *           |-----------|
     *          10           30
     *
     * Returns:
     *           10  20
     *     |----||----||----|
     *     0   10      20  30
     * </pre>
     *
     * @return An ArrayList of Ranges representing the original:
     *         <ol>
     *             <li>Left-only region</li>
     *             <li>Shared region</li>
     *             <li>Right-only region</li>
     *         </ol>
     *         If no overlap exists, the list contains copies of {@code r1} and {@code r2}.
     */
    public static ArrayList<Range> splitIntoThree(Range r1, Range r2) {

        if (!r1.overlaps(r2)) {
            return new ArrayList<>( List.of(new Range(r1), new Range(r2)) );
        }

        // Find the rightmost left endpoint
        int midLeft = Math.max(r1.getLow(), r2.getLow());

        // Find the leftmost right endpoint.
        int midRight = Math.min(r1.getHigh(), r2.getHigh());

        Range leftRange = new Range(r1.getLow(), midLeft);
        Range middleRange = new Range(midLeft, midRight);
        Range rightRange = new Range(midRight, r2.getHigh());

        return new ArrayList<>( List.of (leftRange, middleRange, rightRange) );
    }

    /**
     * Creates ranges from a set of points.
     * <p>
     * The points mark the start of each interval, and {@code lastEndpoint}
     * marks the end of the final interval.
     * <p>
     * It also is more reusable by not requiring actual Ranges. This is how Measures, Time Signatures, etc.,
     * are created from raw MIDI data.
     *
     * @param points A set of points on the number line (like start ticks).
     * @param lastEndpoint The (LTR) terminus of the number line.
     * @return A list of Ranges representing the intervals between the points provided.
     * @throws IllegalArgumentException if {@code lastEndpoint <= max(points)}
     */
    public static ArrayList<Range> fromStartTicks(Set<Integer> points, long lastEndpoint) {

        if (points == null || points.isEmpty()) { return new ArrayList<>(); }

        ArrayList<Integer> pointsCopy = new ArrayList<>(points);

        // Passing null invokes Long class default comparator (ascending)
        pointsCopy.sort(null);
        if (lastEndpoint <= pointsCopy.getLast()) {
            throw new IllegalArgumentException("last endpoint should be greater than last point");
        }

        ArrayList<Range> out = new ArrayList<>();
        int size = pointsCopy.size();
        for (int i = 0; i < size; i++) {
            int tick = Math.toIntExact(pointsCopy.get(i));
            int nextTick = Math.toIntExact(i < size - 1 ? pointsCopy.get(i + 1) : lastEndpoint);
            Range range = new Range(tick, nextTick - 1); // ranges are half-open
            out.add(range);
        }

        return out;
    }

    /**
     * Splits a single Range up by a uniform window size.
     *
     * @param range The Range to be partitioned.
     * @param windowSize The desired length of the output Ranges.
     * @return An ArrayList of Ranges, each* of length {@code windowSize}, falling within {@code range}.
     *         <br>*Remainders at the right end are handled by clamping.
     */
    public static ArrayList<Range> partition(Range range, int windowSize) {

        ArrayList<Range> out = new ArrayList<>();

        int windowMin = range.getLow();
        int windowMax = windowMin + windowSize;

        int length = range.getHigh();

        while (windowMin < length) {

            Range window = new Range(windowMin, windowMax - 1);
            out.add(window);
            windowMin += windowSize;
            windowMax += windowSize;

            // Clamp: e.g., range = [0,10], windowSize = 3; ranges = [0,3], [4,7], [8-10] <--last is clamped
            if (length < windowMax) { windowMax = length; }

        }

        return out;
    }

    /**
     * Overload of {@link #partition(Range, int)} that allows for a divisor specifier rather than
     * a window length specifier
     *
     * @param range The Range to be partitioned.
     * @param divisor The number of Ranges to create from {@code range}.
     */
    public static ArrayList<Range> partition(Range range, double divisor) {

        double windowSize = range.length() / divisor;

        double leftover = windowSize - ((int) windowSize);
        if (leftover != 0) {
            System.out.println("loss of " + leftover);
        }

        return partition(range, (long) windowSize);

    }


    public static List<Range> listOf(Integer... nums) {
        if (nums.length % 2 != 0) { throw new RuntimeException("odd number of ints in list factory"); }
        List<Range> list = new ArrayList<>();
        for (int i = 0; i < nums.length; i+=2) {
            list.add(new Range(nums[i], nums[i+1]));
        }
        return list;
    }



}
