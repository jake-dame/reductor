package reduc;

public class Main {

    static final String MINUET_SB = "midis/minuet_SB.mid";
    static final String MINUET_PIANO = "midis/minuet_piano.mid";
    static final String MOZART = "midis/mozart_550.mid";

    public static void main(String[] args) {

        try {
            Reductor reductor = new Reductor(MOZART);
            var s = reductor.getAggregatedSequence();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }

}