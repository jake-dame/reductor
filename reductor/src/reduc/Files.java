package reduc;

import java.io.File;
import java.util.ArrayList;

public class Files {

    static final ArrayList<File> FILES;

    static final String MOZART_40 = "midis/mozart_40_i.mid";
    static final String BEETHOVEN_MOONLIGHT = "midis/beethoven_moonlight.mid";
    static final String BEETHOVEN_5_IV = "midis/beethoven_5_iv.mid"; // at 69.2 clarinets have erroneous g's
    static final String BACH_INV_1 = "midis/bach_inv_1.mid";
    static final String BACH_PREL_1 = "midis/bach_prel_1.mid";

    static final String MINUET_SB = "midis/minuet_SB.mid";
    static final String MINUET_PIANO = "midis/minuet_piano.mid";

    static final String LEVEL_1_TEST = "midis/level_1_test.mid";

    static {
        FILES = new ArrayList<>();
        FILES.add(new File(MOZART_40));
        FILES.add(new File(BEETHOVEN_MOONLIGHT));
        FILES.add(new File(BEETHOVEN_5_IV));
        FILES.add(new File(BACH_INV_1));
        FILES.add(new File(BACH_PREL_1));
        FILES.add(new File(MINUET_SB));
        FILES.add(new File(MINUET_PIANO));
        FILES.add(new File(LEVEL_1_TEST));
    }

    // this is used mostly for parameterized testing
    public static ArrayList<File> getMidiFileObjects() {
        return Files.FILES;
    }

}
