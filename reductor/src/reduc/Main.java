package reduc;

public class Main {

    static final String MOZART_40 = "midis/mozart_40_i.mid";
    static final String BEETHOVEN_MOONLIGHT = "midis/beethoven_moonlight.mid";
    static final String BEETHOVEN_5_IV = "midis/beethoven_5_iv.mid"; // at 69.2 clarinets have erroneous g's
    static final String BACH_INV_1 = "midis/bach_inv_1.mid";
    static final String BACH_PREL_1 = "midis/bach_prel_1.mid";

    static final String MINUET_SB = "midis/minuet_SB.mid";
    static final String MINUET_PIANO = "midis/minuet_piano.mid";

    static final String LEVEL_1_TEST = "midis/level_1_test.mid";


    public static void main(String[] args) {

        Reductor reductor = new Reductor(MOZART_40, 1);
        Midi original = reductor.getOriginal();
        Midi aggregate = reductor.getAggregate();
        Midi reduction = reductor.getReduction();

        Midi midi = reduction;

        System.out.println(reduction.getName());

        // Bach inv: set tempo message: [0xFF, 0x59, 0x3, 0xD, 0x14, 0x37] --> 0xD1437 --> 857_143 --> 75 bpm
        //int bpm = (int) (midi.getTempo() * 1.5);
        //midi.setTempo(bpm);
        //ReductorUtil.openWithGarageBand(midi.writeOut());
    }

}