package reductor.dataconverter;

import org.junit.jupiter.api.Test;
import reductor.piece.Range;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;


class QuantizeTest {

    @Test
    void test() {

        ArrayList<Range> ranges = new ArrayList<>();
        ranges.add(new Range(0,29));
        ranges.add(new Range(0,31));
        ranges.add(new Range(1,29));
        ranges.add(new Range(1,31));

        Quantize q = new Quantize(480, ranges);

    }

}