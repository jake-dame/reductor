package reductor.midi.importer;

import reductor.core.Range;
import reductor.core.RangeUtil;

import java.util.*;


public class Quantize {


    public static Range quantize(Range inRange, int tpq) {

        int scale = 480 / tpq;

        int scaledLow = inRange.getLow() * scale;
        int scaledHigh = inRange.getHigh() * scale;

        Range range = new Range(scaledLow, scaledHigh);

        QuantizerRhythmType rhy = QuantizerRhythmType.type(range.getHigh() - range.getLow());

        double gridWindowSize = rhy.base / rhy.divisor;
        Range gridWindow = new Range(0, (int) (gridWindowSize - 1));

        final long tolerance = 4;
        while (gridWindow.overlappingRegionLength(range) < tolerance) {
            gridWindow = RangeUtil.getShiftedInstance(gridWindow, gridWindow.duration());
        }

        range = range.setLow(gridWindow.getLow());
        range = range.setHigh((int) (range.getLow() + rhy.duration - 1));

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
