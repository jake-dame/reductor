package reductor.util;


import org.audiveris.proxymusic.Attributes;
import org.audiveris.proxymusic.ScorePartwise;
import org.audiveris.proxymusic.ScorePartwise.Part.Measure;
import reductor.app.Application;
import reductor.core.TimeSignature;

import java.util.List;
import java.util.Set;


public class TimeUtil {

    private TimeUtil() { }

    /*
    Instead of checks for denominator in range, denominator power of 2, etc., since this is a small, finite set to choose from
    This program will not support speculative or experimental music (in terms of meter/rhythm)
    */
    public static Set<Integer> validDenominators = Set.of(2, 4, 8, 16, 32, 64, 128);

    public static int calculateMeasureDuration(int subdivisions, int numerator, int denominator) {

        if (!TimeUtil.validDenominators.contains(denominator)) {
            throw new IllegalArgumentException(
                    "this program only supports beat types: 2, 4, 8, 16, 32, 64, and 128; not: "
                            + denominator
            );
        }

        if (256 < numerator) {
            throw new IllegalArgumentException("no sire, you mustn't");
        }

        if (9999 < subdivisions) {
            throw new IllegalArgumentException("perhaps, milord, I mightn't suggest a time signature of 6/8 -- pleasing both to th'senses and soul");
        }

        return (numerator*4/denominator) * subdivisions;
    }

    public static int calculateMeasureDuration(int resolution, TimeSignature timeSig) {
        return calculateMeasureDuration(resolution, timeSig.numerator(), timeSig.denominator());
    }

    // convenience
    public static int calculateMeasureDuration(ScorePartwise.Part.Measure measure) {

        List<Attributes> aList = measure.getNoteOrBackupOrForward()
                .stream()
                .filter(Attributes.class::isInstance)
                .map(Attributes.class::cast)
                .toList();

        assert aList.size() == 1;
        Attributes a = aList.getFirst();
        assert a != null;
        assert a.getDivisions() != null;
        assert a.getTime() != null;
        assert 1 < a.getTime().get(0).getTimeSignature().size();

        int subdivisions = a.getDivisions().intValueExact();
        int numerator = Integer.parseInt(a.getTime().get(0).getTimeSignature().get(0).getValue());
        int denominator = Integer.parseInt(a.getTime().get(0).getTimeSignature().get(1).getValue());

        return calculateMeasureDuration(subdivisions, numerator, denominator);
    }




}
