package reductor.piece;

import static reductor.piece.RhythmBases.*;


public enum Rhy {

    r_0(3000, 1, 1),

    r_1(WHOLE, 1, 1),

    r_2(HALF, 1, 1),
    r_2dot(HALF, 1.5, 1),
    r_2in3(HALF, 2, 3),
    r_2in5(HALF, 2, 5),
    r_2in7(HALF, 2, 7),
    r_2in9(HALF, 2, 9),

    r_4(QUARTER, 1, 1),
    r_4dot(QUARTER, 1.5, 1),
    r_4in3(QUARTER, 2, 3),
    r_4in5(QUARTER, 2, 5),
    r_4in7(QUARTER, 2, 7),
    r_4in9(QUARTER, 2, 9),

    r_8(EIGHTH, 1, 1),
    r_8dot(EIGHTH, 1.5, 1),
    r_8in3(EIGHTH, 2, 3),
    //r_8in5(EIGHTH, 2, 5),
    //r_8in7(EIGHTH, 2, 7),
    //r_8in9(EIGHTH, 2, 9),

    r_16(SIXTEENTH, 1, 1),
    r_16dot(SIXTEENTH, 1.5, 1),
    r_16in3(SIXTEENTH, 2, 3),
    //r_16in5(SIXTEENTH, 2, 5),
    //r_16in7(SIXTEENTH, 2, 7),
    //r_16in9(SIXTEENTH, 2, 9),

    r_32(THIRTY_SECOND, 1, 1),
    r_32dot(THIRTY_SECOND, 1.5, 1),
    r_32in3(THIRTY_SECOND, 2, 3),
    //r_32in5(THIRTY_SECOND, 2, 5),
    //r_32in7(THIRTY_SECOND, 2, 7),
    //r_32in9(THIRTY_SECOND, 2, 9),

    r_64(SIXTY_FOURTH, 1, 1),
    r_64dot(SIXTY_FOURTH, 1.5, 1),
    //r_64in3(SIXTY_FOURTH, 2, 3),
    //r_64in5(SIXTY_FOURTH, 2, 5),
    //r_64in7(SIXTY_FOURTH, 2, 7),
    //r_64in9(SIXTY_FOURTH, 2, 9),

    r_128(ONE_TWENTY_EIGHTH, 1, 1),
    r_128dot(ONE_TWENTY_EIGHTH, 1.5, 1),
    r_128in3(ONE_TWENTY_EIGHTH, 2, 3),
    //r_128in5(ONE_TWENTY_EIGHTH, 2, 5),
    //r_128in7(ONE_TWENTY_EIGHTH, 2, 7),
    //r_128in9(ONE_TWENTY_EIGHTH, 2, 9)
    ;


    public final double base;
    public final double multiplier;
    public final double divisor;

    public final double duration;

    Rhy(double base, double multiplier, double divisor) {
        this.base = base;
        this.multiplier = multiplier;
        this.divisor = divisor;

        this.duration = (this.base * this.multiplier) / this.divisor;
    }

    public double base() { return this.base; }
    public double parentBase() { return this.base * 2; }

    public double divisor() { return this.divisor; }
    public double multiplier() { return this.multiplier; }

    public double duration() { return this.duration; }

    public boolean isDotted() { return this.multiplier == 1.5; }
    public boolean isRegular() { return this.divisor == 1; }

    public boolean isTrip() { return this.divisor == 3; }
    public boolean isQuint() { return this.divisor == 5; }
    public boolean isSept() { return this.divisor == 7; }
    public boolean isNon() { return this.divisor == 9; }
    public boolean isUnholy() { return !isTrip() && !isQuint() && !isSept() && !isNon(); }

    public static Range toRange(Rhy type) {
        return new Range(0, (long) (type.duration - 1));
    }

    public static Range toRange(double start, Rhy type) {
        return new Range((long) start, (long) (start + (long) (type.duration - 1)));
    }


    public static Rhy type(Range range) { return type(range.high() - range.low()); }
    public static Rhy type(double duration) {

        if (WHOLE < duration) { return r_0; }

        for (Rhy val : Rhy.values()) {
            if (val.duration == duration) { return val; }
        }

        return closestMatch(duration);
    }

    public static Rhy closestMatch(double duration) {

        Rhy currVal = r_1;
        var values = Rhy.values();
        for (Rhy val : values) {
            double currSmallest = Math.abs(currVal.duration - duration);
            double thisDiff = Math.abs(val.duration - duration);
            if (thisDiff < currSmallest) { currVal = val;  }
        }

        return currVal;
    }

}