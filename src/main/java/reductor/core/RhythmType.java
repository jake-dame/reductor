package reductor.core;


import reductor.app.Application;


public enum RhythmType {

    //WHOLE(4),
    //HALF(2),
    //QUARTER(1),
    //EIGHTH(0.5),
    //SIXTEENTH(0.25),
    //THIRTY_SECOND(0.125),
    //SIXTY_FOURTH(0.0625),
    //ONE_TWENTY_EIGHTH(0.03125);

    //ONE_TWENTY_EIGHTH(0.03125),
    //SIXTY_FOURTH(0.0625),
    //THIRTY_SECOND(0.125),
    //SIXTEENTH(0.25),
    //EIGHTH(0.5),
    //QUARTER(1),
    //HALF(2),
    //WHOLE(4);

    WHOLE(4),

    HALF(2),
    TRIPLET_HALF(WHOLE.multiplier / 3),
    DOTTED_HALF(HALF.multiplier * 1.5),

    QUARTER(1),
    TRIPLET_QUARTER(HALF.multiplier / 3),
    DOTTED_QUARTER(QUARTER.multiplier * 1.5),

    EIGHTH(0.5),
    TRIPLET_EIGHTH(QUARTER.multiplier / 3),
    DOTTED_EIGHTH(EIGHTH.multiplier * 1.5),

    SIXTEENTH(0.25),
    TRIPLET_SIXTEENTH(EIGHTH.multiplier / 3),
    DOTTED_SIXTEENTH(SIXTEENTH.multiplier * 1.5),

    THIRTY_SECOND(0.125),
    TRIPLET_THIRTY_SECOND(SIXTEENTH.multiplier / 3),
    DOTTED_THIRTY_SECOND(THIRTY_SECOND.multiplier * 1.5),

    SIXTY_FOURTH(0.0625),
    TRIPLET_SIXTY_FOURTH(THIRTY_SECOND.multiplier / 3),
    DOTTED_SIXTY_FOURTH(SIXTY_FOURTH.multiplier * 1.5),

    ONE_TWENTY_EIGHTH(0.03125),
    TRIPLET_ONE_TWENTY_EIGHTH(SIXTY_FOURTH.multiplier / 3),
    DOTTED_ONE_TWENTY_EIGHTH(ONE_TWENTY_EIGHTH.multiplier * 1.5);

    private final double multiplier;
    private int duration;

    static private boolean durationsSet;

    RhythmType(double multiplier) {
        this.multiplier = multiplier;
    }

    private static void setDurations() {

        if (!durationsSet) {
            for (RhythmType val : RhythmType.values()) {
                val.duration = (int) (val.multiplier * Application.resolution);
            }

            durationsSet = true;
        }

    }

    public int getDuration() {
        setDurations();
        return this.duration;
    }

    static long getDuration(RhythmType enumVal) {
        setDurations();
        return enumVal.duration;
    }

    static Range getEquivalentRange(RhythmType enumVal) {
        setDurations();
        return new Range(0, enumVal.duration - 1);
    }

    public static RhythmType getEnumType(long duration) {

        setDurations();

        RhythmType[] arr = RhythmType.values();
        int i = 0;
        RhythmType currType = WHOLE;

        while (i < arr.length && duration < currType.duration) {
            currType = arr[i];
            i++;
        }

        return currType;

        /*
         This is tough because values falling in between could have multiple interpretations:
             1. Have to account for dotted (perfectly in between)
             2. Triplet (perfectly divided into thirds between upper and lower value)
                 + Furthermore, this would require look-behind/-ahead to see if neighbors were of
                       similar value (can't even rely on exact, due to staccatos or tie-overs)
             3. Tied noteList could be literally any combination (just select GCF or something)

             And if it still doesn't match any of those categories perfectly, it could be:
                 + closer to upper value could be portato or breath
                 + closer to lower value could be staccato/staccatissimo

             And the biggest problem of all is that many multiple combinations of the above states
                 could have equivalent durations with other states

             How to classify a dotted, staccato, triplet, tied note????

        */

    }


}
