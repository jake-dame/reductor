package reductor.dev;


import java.nio.file.Path;


public class Catalog {


    private static final Path PIECES = Path.of("assets", "sourced-midis");
    private static final Path TESTS = Path.of("assets", "tests");


    private Catalog() {}


    public static final Path BACH_BEFIEHL_DU_DEINE_WEGE = PIECES.resolve("bach_befiehl_du_deine_wege.mid");
    public static final Path BACH_BEFIEHL_DU_DEINE_WEGE_NEW = PIECES.resolve("bach_befiehl_du_deine_wege_new.mid");
    public static final Path BACH_BRANDENBURG_6_I = PIECES.resolve("bach_brandenburg_6_i.mid");
    public static final Path BACH_BRANDENBURG_6_III = PIECES.resolve("bach_brandenburg_6_iii.mid");
    public static final Path BACH_BRANDENBURG_3_I = PIECES.resolve("bach_brandenburg_3_i.mid");
    public static final Path BACH_DOUBLE_I = PIECES.resolve("bach_double_i.mid");
    public static final Path BACH_IN_MEINES = PIECES.resolve("bach_in_meines.mid");
    public static final Path BACH_INV_1 = PIECES.resolve("bach_inv_1.mid");
    public static final Path BACH_INV_1_NEW = PIECES.resolve("bach_inv_1_new.mid");
    public static final Path BACH_PREL_1 = PIECES.resolve("bach_prel_1.mid");
    public static final Path BACH_ST_JOHN_ARIA = PIECES.resolve("bach_st_john_aria.mid");
    public static final Path BACH_ST_JOHN_OVERTURE = PIECES.resolve("bach_st_john_overture.mid");
    public static final Path BACH_QUI_SEDES_AD_DEXTERAM_PATRIS = PIECES.resolve(
            "mass_in_b_minor/qui_sedes_ad_dexteram_patris.mid"
    );
    public static final Path BACH_KYRIE_ELEISON = PIECES.resolve("mass_in_b_minor/kyrie_eleison.mid");
    public static final Path BACH_ET_RESURREXIT = PIECES.resolve("mass_in_b_minor/et_resurrexit.mid");
    public static final Path BACH_ET_INCARNATUS_EST = PIECES.resolve("mass_in_b_minor/et_incarnatus_est.mid");
    public static final Path BACH_DOMINE_DEUS = PIECES.resolve("mass_in_b_minor/domine_deus.mid");
    public static final Path BEETHOVEN_5_I = PIECES.resolve("beethoven_5_i.mid");
    public static final Path BEETHOVEN_5_II = PIECES.resolve("beethoven_5_ii.mid");
    public static final Path BEETHOVEN_5_III_IV = PIECES.resolve("beethoven_5_iii_iv.mid");
    public static final Path BEETHOVEN_5_IV = PIECES.resolve("beethoven_5_iv.mid");
    public static final Path BEETHOVEN_MOONLIGHT = PIECES.resolve("beethoven_moonlight.mid");
    public static final Path BIZET_PEARL_FISHERS_DUET = PIECES.resolve("bizet_pearl_fishers_duet.mid");
    public static final Path BRAHMS_CLAR_QUINTET_I = PIECES.resolve("brahms_clar_quintet_i.mid"); // dbl note off
    public static final Path DVORAK_CZECH_FINALE = PIECES.resolve("dvorak_czech_finale.mid");
    public static final Path DVORAK_CZECH_ROMANZE = PIECES.resolve("dvorak_czech_romanze.mid");
    public static final Path DVORAK_QUAR_10_III = PIECES.resolve("dvorak_quar_10_iii.mid");
    public static final Path DVORAK_SERENADE_I = PIECES.resolve("dvorak_serenade_i.mid");
    public static final Path DVORAK_SERENADE_V = PIECES.resolve("dvorak_serenade_v.mid");
    public static final Path DVORAK_SERENADE_VALSE = PIECES.resolve("dvorak_serenade_valse.mid");
    public static final Path DVORAK_SYMPHONY_8_I = PIECES.resolve("dvorak_symphony_8_i.mid");
    public static final Path DVORAK_SYMPHONY_8_III = PIECES.resolve("dvorak_symphony_8_iii.mid");
    public static final Path DVORAK_WIND_SERENADE_I = PIECES.resolve("dvorak_wind_serenade_i.mid");
    public static final Path HANDEL_SHEBA = PIECES.resolve("handel_sheba.mid");
    public static final Path LISZT_SYMPHONY_5 = PIECES.resolve("liszt_symphony_5.mid");
    public static final Path MAHLER_6_I = PIECES.resolve("mahler_6_i.mid");
    public static final Path MOZART_DIE_ENTFUHRUNG = PIECES.resolve("mozart_die_entfuhrung.mid");
    public static final Path MOZART_LUCIO_SILLA = PIECES.resolve("mozart_lucio_silla.mid"); // dbl note off
    public static final Path MOZART_COSI_OVERTURE = PIECES.resolve("mozart_cosi_overture.mid");
    public static final Path MOZART_SOAVE_SIL_VENTO = PIECES.resolve("mozart_soave_sil_vento.mid");
    public static final Path MOZART_SONATA_IN_C = PIECES.resolve("mozart_sonata_in_c.mid");
    public static final Path MOZART_SYMPHONY_25_I = PIECES.resolve("mozart_symphony_25_i.mid");
    public static final Path MOZART_SYMPHONY_29_I = PIECES.resolve("mozart_symphony_29_i.mid");
    public static final Path MOZART_SYMPHONY_40_I = PIECES.resolve("mozart_symphony_40_i.mid");
    public static final Path MOZART_SYMPHONY_40_I_NEW = PIECES.resolve("mozart_symphony_40_i_new.mid");
    public static final Path ROSSINI_LA_SCALA_DI_SETA = PIECES.resolve("rossini_la_scala_di_seta.mid");
    public static final Path ROSSINI_PETITE_MESSE = PIECES.resolve("rossini_petite_messe.mid");
    public static final Path ROSSINI_SEMIRAMIDE = PIECES.resolve("rossini_semiramide.mid");
    public static final Path ROSSINI_STABAT_MATER = PIECES.resolve("rossini_stabat_mater.mid");
    public static final Path SCHUBERT_DEATH_I = PIECES.resolve("schubert_death_i.mid");
    public static final Path SCHUBERT_DEATH_II = PIECES.resolve("schubert_death_ii.mid");
    public static final Path TCHAIKOVSKY_4_I = PIECES.resolve("tchaikovsky_4_i.mid");
    public static final Path TCHAIKOVSKY_5_I = PIECES.resolve("tchaikovsky_5_i.mid");
    public static final Path TCHAIKOVSKY_4_II = PIECES.resolve("tchaikovsky_4_ii.mid");
    public static final Path TCHAIKOVSKY_5_III = PIECES.resolve("tchaikovsky_5_iii.mid");

    public static final Path TEST_1 = TESTS.resolve("test-1.mid");
    public static final Path VOICE_TO_CHANNEL_MAPPING = TESTS.resolve("voice_to_channel_mapping.mid");
    public static final Path OLD_TEST_1 = TESTS.resolve("Test1.mid");
    public static final Path TEST_2 = TESTS.resolve("Test2.mid");
    public static final Path TEST_PITCH = TESTS.resolve("TestPitch.mid");
    public static final Path TEST_3 = TESTS.resolve("Test3.mid");
    public static final Path TEST_3_TRY_2 = TESTS.resolve("Test3Try2.mid");
    public static final Path TEST_3_TRY_3 = TESTS.resolve("Test3Try3.mid");
    public static final Path TEST_3_TRY_4 = TESTS.resolve("Test3Try4.mid");
    public static final Path TEST_4 = TESTS.resolve("Test4.mid");
    public static final Path TEST_4_TRY_2 = TESTS.resolve("Test4Try2.mid");
    public static final Path TEST_4_TRY_3 = TESTS.resolve("Test4Try3.mid");
    public static final Path OVERLAPPING_TEST = TESTS.resolve("overlapping_test.mid");
    public static final Path COLUMN_TEST_1 = TESTS.resolve("column_test_1.mid");
    public static final Path COLUMN_TEST_2 = TESTS.resolve("column_test_2.mid");
    public static final Path PSEUDO_ANACRUSIS_TEST = TESTS.resolve("pseudo_anacrusis_test.mid");
    public static final Path ORNAMENT_TEST = TESTS.resolve("ornament_test.mid");
    public static final Path MEASURE_SIZE_TEST = TESTS.resolve("measure_size_test.mid");
    public static final Path OVERLAPPING_TEST_2 = TESTS.resolve("overlapping_test_2.mid");
    public static final Path OVERLAPPING_TEST_3 = TESTS.resolve("overlapping_test_3.mid");
    public static final Path OVERLAPPING_TEST_4 = TESTS.resolve("overlapping_test_4.mid");
    public static final Path OVERLAPPING_TEST_5 = TESTS.resolve("overlapping_test_5.mid");
    public static final Path OVERLAPPING_TEST_6 = TESTS.resolve("overlapping_test_6.mid");
    public static final Path OVERLAPPING_TEST_7 = TESTS.resolve("overlapping_test_7.mid");
    public static final Path OVERLAPPING_ASCENDING_SCALE = TESTS.resolve("overlapping_ascending_scale.mid");
    public static final Path BEETHOVEN_7_FUGUE = TESTS.resolve("beethoven_7_fugue_full.mid");
    public static final Path PICKUP_MEASURE_TEST = TESTS.resolve("pickup_measure_test.mid");
    /*
     * Reading the actual bytes in the SMF from the .mid file that MuseScore produced showed that even with
     * well-separated notes (like 8th → 8th rest → 8th → 8th rest), the note off event is still sent at -1 the real
     * duration (so note on at 0, note off at 239).
     */
    public static final Path TICKS_TEST = PIECES.resolve("ticks_test.mid");
    public static final Path BEETHOVEN_7_FUGUE_PIANO = PIECES.resolve("beethoven_7_fugue_piano.mid");
    public static final Path CHOPIN_PREL_e = PIECES.resolve("chopin_prel_e.mid");
    public static final Path CHOPIN_PREL_c = PIECES.resolve("chopin_prel_c.mid");


}
