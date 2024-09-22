package reduc;

import javax.sound.midi.Sequence;

public class Main {

    private static final String MINUET_SB = "midis/minuet_SB.mid";
    private static final String MINUET_PIANO = "midis/minuet_piano.mid";
    private static final String MOZART = "midis/mozart_550.mid";

    public static void main(String[] args) {

        try {

            Reductor red = new Reductor(MINUET_SB);

            Sequence seq = red.getAggregate();
            MidiUtility.write(seq, "test");

            MidiUtility.printSequence(seq);
            MidiUtility.play(seq);

        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }

}