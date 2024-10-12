package reductor;

import java.io.File;
import java.util.ArrayList;


public class Files {


    private Files() { }


    /// These are for parameterized tests
    public static final ArrayList<File> FILES = new ArrayList<>();

    static ArrayList<File> getMidiFiles() {
        return FILES;
    }

    /// Project-specific directories
    public static final String PROJECT_DIR = "/Users/u0858882/Desktop/Capstone/reductor/";
    public static final String MIDI_FILES_IN_DIR = "/Users/u0858882/Desktop/Capstone/reductor/midis/in/";
    public static final String MIDI_FILES_OUT_DIR = "/Users/u0858882/Desktop/Capstone/reductor/midis/out/";

    /// Piece files downloaded from ClassicalArchives.org or imslp.org
    public static final String MOZART_40 = addFile("mozart_40_i.mid");
    public static final String BEETHOVEN_MOONLIGHT = addFile("beethoven_moonlight.mid");
    public static final String BEETHOVEN_5_IV = addFile("beethoven_5_iv.mid"); // at 69.2 clarinets have erroneous g's
    public static final String BACH_INV_1 = addFile("bach_inv_1.mid");
    public static final String BACH_PREL_1 = addFile("bach_prel_1.mid");

    /// Test MIDIs (created using MuseScore)
    public static final String MINUET_SOPRANO_BASS = addFile("minuet_SB.mid");
    public static final String MINUET_PIANO = addFile("minuet_piano.mid");
    public static final String TEST_1 = addFile("Test1.mid");
    public static final String TEST_2 = addFile("Test2.mid");
    public static final String TEST_3 = addFile("Test3.mid");


    /// This just makes it easier to add new files in one step and reduces text in this file.
    static String addFile(String name) {
        String filePath = MIDI_FILES_IN_DIR + name;
        FILES.add( new File(filePath) );
        return filePath;
    }


}

// TODO: make paths not absolute paths but still make openWithGarageBand() and write() happy