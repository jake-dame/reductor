package reductor.dataconverter.midi;

import reductor.core.Range;
import reductor.core.RhythmTypeBeta;

import java.util.*;


public class Quantize {


    public static Range quantize(Range inRange, int tpq) {

        long scale = 480 / tpq;

        long scaledLow = inRange.low() * scale;
        long scaledHigh = inRange.high() * scale;

        Range range = new Range(scaledLow, scaledHigh);

        RhythmTypeBeta rhy = RhythmTypeBeta.type(range.high() - range.low());

        double gridWindowSize = rhy.base / rhy.divisor;
        Range gridWindow = new Range(0, (long) (gridWindowSize - 1));

        final long tolerance = 4;
        while (gridWindow.overlappingRegion(range) < tolerance) {
            gridWindow = Range.getShiftedInstance(gridWindow, gridWindow.duration());
        }

        range = range.setLow(gridWindow.low());
        range = range.setHigh((long) (range.low() + rhy.duration - 1));

        return range;
    }

    public static ArrayList<Range> quantize(ArrayList<Range> ranges, int tpq) {

        ArrayList<Range> out = new ArrayList<>();

        for (Range range : ranges) {
            Range quantizedRange = quantize(range, tpq);
            out.add(quantizedRange);
        }

        return out;
    }

}