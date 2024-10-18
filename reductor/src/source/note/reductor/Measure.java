package reductor;

public class Measure implements Ranged {


    Range range;


    Measure() {

    }


    @Override
    public Range range() {

        return new Range(this.range);

    }


}
