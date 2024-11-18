package reductor;

import java.io.File;
import java.util.ArrayList;


public class Files {

    /// Project-specific directories
    public static final String PROJECT_DIR = "/Users/u0858882/Desktop/Capstone/reductor/";
    public static final String MIDI_FILES_IN_DIR = "/Users/u0858882/Desktop/Capstone/reductor/midis/in/";
    public static final String MIDI_FILES_OUT_DIR = "/Users/u0858882/Desktop/Capstone/reductor/midis/out/";

    /// Lists
    static ArrayList<File> OK_FILES = new ArrayList<>();
    static ArrayList<File> TEST_FILES = new ArrayList<>();
    static ArrayList<File> NO_TIME_SIG_FILES = new ArrayList<>();
    static ArrayList<File> INVALID_FILES = new ArrayList<>();


    /// Piece files downloaded from ClassicalArchives.org or imslp.org
    public static final String BEETHOVEN_MOONLIGHT = addFile("beethoven_moonlight.mid", OK_FILES);
    public static final String BACH_INV_1 = addFile("bach_inv_1.mid", OK_FILES);
    public static final String BACH_PREL_1 = addFile("bach_prel_1.mid", OK_FILES);
    public static final String BACH_BEFIEHL_DU_DEINE_WEGE = addFile("bach_befiehl_du_deine_wege.mid", OK_FILES);
    public static final String BACH_BRANDENBURG_6_I = addFile("bach_brandenburg_6_i.mid", OK_FILES);
    public static final String BACH_BRANDENBURG_6_III = addFile("bach_brandenburg_6_iii.mid", OK_FILES);
    public static final String BACH_BRANDENBURG_3_I = addFile("bach_brandenburg_3_i.mid", OK_FILES);
    public static final String BACH_DOUBLE_I = addFile("bach_double_i.mid", OK_FILES);
    public static final String BACH_ST_JOHN_ARIA = addFile("bach_st_john_aria.mid", OK_FILES);
    public static final String BACH_ST_MATTHEW_OVERTURE = addFile("bach_st_matthew_overture.mid", INVALID_FILES);
    //public static final String BACH_ST_JOHN_OVERTURE = addFile("bach_st_john_overture.mid", OK_FILES);
    public static final String BACH_QUI_SEDES_AD_DEXTERAM_PATRIS = addFile("mass_in_b_minor/qui_sedes_ad_dexteram_patris.mid", OK_FILES);
    public static final String BACH_KYRIE_ELEISON = addFile("mass_in_b_minor/kyrie_eleison.mid", OK_FILES);
    public static final String BACH_ET_RESURREXIT = addFile("mass_in_b_minor/et_resurrexit.mid", OK_FILES);
    public static final String BACH_ET_INCARNATUS_EST = addFile("mass_in_b_minor/et_incarnatus_est.mid", OK_FILES);
    public static final String BACH_DOMINE_DEUS = addFile("mass_in_b_minor/domine_deus.mid", OK_FILES);
    public static final String BEETHOVEN_5_I = addFile("beethoven_5_i.mid", OK_FILES);
    public static final String BEETHOVEN_5_II = addFile("beethoven_5_ii.mid", OK_FILES);
    public static final String BEETHOVEN_5_III_IV = addFile("beethoven_5_iii_iv.mid", OK_FILES);
    public static final String BEETHOVEN_5_IV = addFile("beethoven_5_iv.mid", OK_FILES);
    public static final String BIZET_PEARL_FISHERS_DUET = addFile("bizet_pearl_fishers_duet.mid", OK_FILES);
    public static final String BYRD_AVE_VERUM = addFile("byrd_avc.mid", NO_TIME_SIG_FILES);
    //public static final String BRAHMS_CLAR_QUINTET_I = addFile("brahms_clar_quintet_i.mid", OK_FILES); // dbl note off
    public static final String DVORAK_CZECH_FINALE = addFile("dvorak_czech_finale.mid", OK_FILES);
    public static final String DVORAK_CZECH_ROMANZE = addFile("dvorak_czech_romanze.mid", OK_FILES);
    public static final String DVORAK_QUAR_10_III = addFile("dvorak_quar_10_iii.mid", OK_FILES);
    public static final String DVORAK_SERENADE_I = addFile("dvorak_serenade_i.mid", OK_FILES);
    public static final String DVORAK_SERENADE_V = addFile("dvorak_serenade_v.mid", OK_FILES);
    public static final String DVORAK_SERENADE_VALSE = addFile("dvorak_serenade_valse.mid", OK_FILES);
    public static final String DVORAK_SYMPHONY_8_I = addFile("dvorak_symphony_8_i.mid", OK_FILES);
    public static final String DVORAK_SYMPHONY_8_III = addFile("dvorak_symphony_8_iii.mid", OK_FILES);
    public static final String DVORAK_WIND_SERENADE_I = addFile("dvorak_wind_serenade_i.mid", OK_FILES);
    public static final String HANDEL_SHEBA = addFile("handel_sheba.mid", OK_FILES);
    //public static final String HANDEL_ZADOK = addFile("handel_zadok.mid", OK_FILES);
    public static final String LISZT_SYMPHONY_5 = addFile("liszt_symphony_5.mid", OK_FILES);
    public static final String MAHLER_6_I = addFile("mahler_6_i.mid", OK_FILES);
    public static final String MOZART_DIE_ENTFUHRUNG = addFile("mozart_die_entfuhrung.mid", OK_FILES);
    //public static final String MOZART_LUCIO_SILLA = addFile("mozart_lucio_silla.mid", OK_FILES); // dbl note off
    public static final String MOZART_COSI_OVERTURE = addFile("mozart_cosi_overture.mid", OK_FILES);
    public static final String MOZART_MASS_KYRIE = addFile("mozart_mass_kyrie.mid", NO_TIME_SIG_FILES);
    public static final String MOZART_SOAVE_SIL_VENTO = addFile("mozart_soave_sil_vento.mid", OK_FILES);
    public static final String MOZART_SONATA_IN_C = addFile("mozart_sonata_in_c.mid", OK_FILES);
    public static final String MOZART_SYMPHONY_25_I = addFile("mozart_symphony_25_i.mid", OK_FILES);
    public static final String MOZART_SYMPHONY_29_I = addFile("mozart_symphony_29_i.mid", OK_FILES);
    public static final String MOZART_SYMPHONY_40 = addFile("mozart_symphony_40.mid", OK_FILES);
    public static final String ROSSINI_LA_SCALA_DI_SETA = addFile("rossini_la_scala_di_seta.mid", OK_FILES);
    public static final String ROSSINI_PETITE_MESSE = addFile("rossini_petite_messe.mid", OK_FILES);
    public static final String ROSSINI_CENERENTOLA_OVERTURE = addFile("rossini_cenerentola_overture.mid", NO_TIME_SIG_FILES);
    public static final String ROSSINI_SEMIRAMIDE = addFile("rossini_semiramide.mid", OK_FILES);
    public static final String ROSSINI_STABAT_MATER = addFile("rossini_stabat_mater.mid", OK_FILES);
    public static final String SCHUBERT_DEATH_I = addFile("schubert_death_i.mid", OK_FILES);
    public static final String SCHUBERT_DEATH_II = addFile("schubert_death_ii.mid", OK_FILES);
    public static final String TCHAIKOVSKY_4_I = addFile("tchaikovsky_4_i.mid", OK_FILES);
    public static final String TCHAIKOVSKY_5_I = addFile("tchaikovsky_5_i.mid", OK_FILES);
    public static final String TCHAIKOVSKY_4_II = addFile("tchaikovsky_4_ii.mid", OK_FILES);
    public static final String TCHAIKOVSKY_5_III = addFile("tchaikovsky_5_iii.mid", OK_FILES);


    /// C3 (Voice 1), G3 (Voice 2), E4 (Voice 3), C5 (Voice 4). Half note "chord"; 2/4. 1 measure.
    public static final String VOICE_TO_CHANNEL_MAPPING = addFile("voice_to_channel_mapping.mid", TEST_FILES);

    /// SATB large intervals, duration test
    public static final String TEST_1 = addFile("Test1.mid", TEST_FILES);

    /// zadok pattern, 2-measureList, overlapping voices
    public static final String TEST_2 = addFile("Test2.mid", TEST_FILES);

    /// don't delete quite yet, enharmonic spelling test
    public static final String TEST_PITCH = addFile("TestPitch.mid", TEST_FILES);

    /// piano: 2 C5 tied wholes, 4 C5 halfs USING 2-VOICE FEATURE
    public static final String TEST_3 = addFile("Test3.mid", TEST_FILES); // overlapping noteList

    /// same as Test3, not using 2-voice feature
    public static final String TEST_3_TRY_2 = addFile("Test3Try2.mid", TEST_FILES); // overlapping noteList

    /// two c wholes tied in RH, ascending quarters C3...B3, D4
    public static final String TEST_3_TRY_3 = addFile("Test3Try3.mid", TEST_FILES);

    /// soprano two tied c wholes, alto 2 untied c wholes
    public static final String TEST_3_TRY_4 = addFile("Test3Try4.mid", TEST_FILES);

    /// 20 instruments, 20 tracks, lots of C5s and a couple chords (1 measure, mostly wholes)
    public static final String TEST_4 = addFile("Test4.mid", TEST_FILES);

    /// piccolo (4 c5 quarters) + soprano (c5 whole)
    public static final String TEST_4_TRY_2 = addFile("Test4Try2.mid", TEST_FILES);

    /// 20 empty tracks + piccolo (4 c5 quarters) + soprano (c5 whole)
    public static final String TEST_4_TRY_3 = addFile("Test4Try3.mid", TEST_FILES);

    /// Overlapping test (4 measure string quartet in C)
    public static final String OVERLAPPING_TEST = addFile("overlapping_test.mid", TEST_FILES);

    /// Complex test; see .mscz file
    public static final String COLUMN_TEST = addFile("column_test.mid", TEST_FILES);

    /// Complex test; see .mscz file
    public static final String ORNAMENT_TEST = addFile("ornament_test.mid", TEST_FILES);

    public static final String OVERLAPPING_TEST_2 = addFile("overlapping_test_2.mid", TEST_FILES);
    public static final String OVERLAPPING_TEST_3 = addFile("overlapping_test_3.mid", TEST_FILES);
    public static final String OVERLAPPING_TEST_4 = addFile("overlapping_test_4.mid", TEST_FILES);
    public static final String OVERLAPPING_TEST_5 = addFile("overlapping_test_5.mid", TEST_FILES);
    public static final String OVERLAPPING_TEST_6 = addFile("overlapping_test_6.mid", TEST_FILES);
    public static final String OVERLAPPING_TEST_7 = addFile("overlapping_test_7.mid", TEST_FILES);
    public static final String OVERLAPPING_ASCENDING_SCALE = addFile("overlapping_ascending_scale.mid", TEST_FILES);
    public static final String BEETHOVEN_7_FUGUE = addFile("beethoven_7_fugue_full.mid", TEST_FILES);
    public static final String MEASURE_SIZE_TEST = addFile("measure_size_test.mid", TEST_FILES);
    public static final String PICKUP_MEASURE_TEST = addFile("pickup_measure_test.mid", TEST_FILES);
    public static final String BEETHOVEN_7_FUGUE_PIANO = addFile("beethoven_7_fugue_piano.mid", TEST_FILES); // overlapping noteList

    public static final String CHOPIN_PREL_e = addFile("chopin_prel_e.mid", TEST_FILES);
    public static final String CHOPIN_PREL_c = addFile("chopin_prel_c.mid", TEST_FILES);


    private Files() { }


    // This just makes it easier to add new files in one step and reduces text in this file.
    private static String addFile(String name, ArrayList<File> list) {
        String filePath = MIDI_FILES_IN_DIR + name;
        list.add(new File(filePath));
        return filePath;
    }

    static ArrayList<File> getTestFiles() { return TEST_FILES; }
    static ArrayList<File> getOkFiles() { return OK_FILES; }
    static ArrayList<File> getNoTimeSigFiles() { return NO_TIME_SIG_FILES; }
    static ArrayList<File> getInvalidFiles() { return INVALID_FILES; }


}