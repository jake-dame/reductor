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
    public static final String TEST_1 = addFile("Test1.mid"); // SATB large intervals, rhythm test
    public static final String TEST_2 = addFile("Test2.mid"); // zadok pattern, 2-measures, overlapping voices
    public static final String TEST_PITCH = addFile("TestPitch.mid"); // don't delete quite yet, enharmonic spelling test

    public static final String TEST_3 = addFile("Test3.mid"); // piano: 2 C5 tied wholes, 4 C5 halfs USING 2-VOICE FEATURE
    public static final String TEST_3_TRY_2 = addFile("Test3Try2.mid"); // same as Test3, not using 2-voice feature
    public static final String TEST_3_TRY_3 = addFile("Test3Try3.mid"); // two c wholes tied in RH, ascending quarters C3...B3, D4
    public static final String TEST_3_TRY_4 = addFile("Test3Try4.mid"); // soprano two tied c wholes, alto 2 untied c wholes

    public static final String TEST_4 = addFile("Test4.mid"); // 20 instruments, 20 tracks, lots of C5s and a couple chords (1 measure, mostly wholes)
    public static final String TEST_4_TRY_2 = addFile("Test4Try2.mid"); // same as below, but minus 20 empty tracks
    public static final String TEST_4_TRY_3 = addFile("Test4Try3.mid"); // 20 empty tracks + piccolo (4 c5 quarters) + soprano (c5 whole)


    /// This just makes it easier to add new files in one step and reduces text in this file.
    static String addFile(String name) {
        String filePath = MIDI_FILES_IN_DIR + name;
        FILES.add( new File(filePath) );
        return filePath;
    }


}

// TODO: make paths not absolute paths but still make openWithGarageBand() and write() happy