package reduc;

import javax.sound.midi.*;

public class Main {

    private static final String MINUET_SB = "midis/minuet_SB.mid";
    private static final String MINUET_PIANO = "midis/minuet_piano.mid";
    private static final String MOZART = "midis/mozart_550.mid";

    public static void main(String[] args) {

        try {

            Reductor red = new Reductor(MINUET_SB);

            Sequence seq = red.getSequence();

            for(Track track : seq.getTracks()) {
                for (int i = 0; i < track.size(); i++) {
                    MidiEvent event = track.get(i);
                    System.out.println(event.getTick());
                }
            }

            //MidiUtility.printSequence(seq);
            MidiUtility.play(seq);

        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }

}