/*
 ******** Catalog.java ********

 **** SYNOPSIS ****
 * This is a dev file.
 * It is a hodge-podge file picker, but for development (an IDE-time file picker, one might say).
 * It defines the class `Catalog`.
 * Its main feature is the `enum MusicFile`, which is essentially an embedded
       catalog/manifest/registry of MIDI/MusicXML files (assets).
 * Plus some other stuff that support that.
 * And some lazily-created lists that are useful for batch testing.

 **** USE ****
 * 1. Place the file in `reductor/midis/{pieces/, tests/}`
 * 2. Copy-paste the __base name__ of the file (e.g. "beethoven-symphony-5.midi")
 * 3. Add an enum value and fill out the args list accordingly.
 *     - Identifier should match the file base name
 *     - Composer surname is preferred "prefix"

 **** DESIGN NOTES ****
 * This could __certainly__ be re-designed maybe with:
 *     1. With a sidecar manifest, ids, metadata (JSON or CSV)
 *     2. Enums, records, etc.
 *     3. Could script all of this for build-time codegen of the enum you see below
 *     4. etc...
 *
 * But for my workflow, this was by far the simplest:
 *     1. New files are added to the codebase in 1 place (here)
 *     2. IDE dropdowns (just type in "BACH" to see all the BACH files since they are in-code globals)
 *     3. Low potential for breakage or introduction of other bugs from scripting, language stuff, etc.
 *
 * After all, the last thing I want to worry about during a stressful debug or new feature testing cycle is
 *     "how do I add the MIDI file again? There is a bug with reading in the file? etc."
 */

package reductor.dev;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

import static reductor.app.Paths.*;


public class Catalog {

    enum Format { MIDI, MUSICXML }

    enum Category { PIECE, TEST }

    public enum MusicFile {

        // PIECE FILES

        BACH_BEFIEHL_DU_DEINE_WEGE("bach_befiehl_du_deine_wege.mid", Category.PIECE, Format.MIDI),
        BACH_BEFIEHL_DU_DEINE_WEGE_NEW("bach_befiehl_du_deine_wege_new.mid", Category.PIECE, Format.MIDI),
        BACH_BRANDENBURG_6_I("bach_brandenburg_6_i.mid", Category.PIECE, Format.MIDI),
        BACH_BRANDENBURG_6_III("bach_brandenburg_6_iii.mid", Category.PIECE, Format.MIDI),
        BACH_BRANDENBURG_3_I("bach_brandenburg_3_i.mid", Category.PIECE, Format.MIDI),
        BACH_DOUBLE_I("bach_double_i.mid", Category.PIECE, Format.MIDI),
        BACH_IN_MEINES("bach_in_meines.mid", Category.PIECE, Format.MIDI),
        BACH_INV_1("bach_inv_1.mid", Category.PIECE, Format.MIDI),
        BACH_INV_1_NEW("bach_inv_1_new.mid", Category.PIECE, Format.MIDI),
        BACH_PREL_1("bach_prel_1.mid", Category.PIECE, Format.MIDI),
        BACH_ST_JOHN_ARIA("bach_st_john_aria.mid", Category.PIECE, Format.MIDI),
        BACH_ST_JOHN_OVERTURE("bach_st_john_overture.mid", Category.PIECE, Format.MIDI),
        BACH_QUI_SEDES_AD_DEXTERAM_PATRIS("mass_in_b_minor/qui_sedes_ad_dexteram_patris.mid", Category.PIECE, Format.MIDI),
        BACH_KYRIE_ELEISON("mass_in_b_minor/kyrie_eleison.mid", Category.PIECE, Format.MIDI),
        BACH_ET_RESURREXIT("mass_in_b_minor/et_resurrexit.mid", Category.PIECE, Format.MIDI),
        BACH_ET_INCARNATUS_EST("mass_in_b_minor/et_incarnatus_est.mid", Category.PIECE, Format.MIDI),
        BACH_DOMINE_DEUS("mass_in_b_minor/domine_deus.mid", Category.PIECE, Format.MIDI),
        BEETHOVEN_5_I("beethoven_5_i.mid", Category.PIECE, Format.MIDI),
        BEETHOVEN_5_II("beethoven_5_ii.mid", Category.PIECE, Format.MIDI),
        BEETHOVEN_5_III_IV("beethoven_5_iii_iv.mid", Category.PIECE, Format.MIDI),
        BEETHOVEN_5_IV("beethoven_5_iv.mid", Category.PIECE, Format.MIDI),
        BEETHOVEN_MOONLIGHT("beethoven_moonlight.mid", Category.PIECE, Format.MIDI),
        BIZET_PEARL_FISHERS_DUET("bizet_pearl_fishers_duet.mid", Category.PIECE, Format.MIDI),
        BRAHMS_CLAR_QUINTET_I("brahms_clar_quintet_i.mid", Category.PIECE, Format.MIDI), // dbl note off
        DVORAK_CZECH_FINALE("dvorak_czech_finale.mid", Category.PIECE, Format.MIDI),
        DVORAK_CZECH_ROMANZE("dvorak_czech_romanze.mid", Category.PIECE, Format.MIDI),
        DVORAK_QUAR_10_III("dvorak_quar_10_iii.mid", Category.PIECE, Format.MIDI),
        DVORAK_SERENADE_I("dvorak_serenade_i.mid", Category.PIECE, Format.MIDI),
        DVORAK_SERENADE_V("dvorak_serenade_v.mid", Category.PIECE, Format.MIDI),
        DVORAK_SERENADE_VALSE("dvorak_serenade_valse.mid", Category.PIECE, Format.MIDI),
        DVORAK_SYMPHONY_8_I("dvorak_symphony_8_i.mid", Category.PIECE, Format.MIDI),
        DVORAK_SYMPHONY_8_III("dvorak_symphony_8_iii.mid", Category.PIECE, Format.MIDI),
        DVORAK_WIND_SERENADE_I("dvorak_wind_serenade_i.mid", Category.PIECE, Format.MIDI),
        HANDEL_SHEBA("handel_sheba.mid", Category.PIECE, Format.MIDI),
        LISZT_SYMPHONY_5("liszt_symphony_5.mid", Category.PIECE, Format.MIDI),
        MAHLER_6_I("mahler_6_i.mid", Category.PIECE, Format.MIDI),
        MOZART_DIE_ENTFUHRUNG("mozart_die_entfuhrung.mid", Category.PIECE, Format.MIDI),
        MOZART_LUCIO_SILLA("mozart_lucio_silla.mid", Category.PIECE, Format.MIDI), // dbl note off
        MOZART_COSI_OVERTURE("mozart_cosi_overture.mid", Category.PIECE, Format.MIDI),
        MOZART_SOAVE_SIL_VENTO("mozart_soave_sil_vento.mid", Category.PIECE, Format.MIDI),
        MOZART_SONATA_IN_C("mozart_sonata_in_c.mid", Category.PIECE, Format.MIDI),
        MOZART_SYMPHONY_25_I("mozart_symphony_25_i.mid", Category.PIECE, Format.MIDI),
        MOZART_SYMPHONY_29_I("mozart_symphony_29_i.mid", Category.PIECE, Format.MIDI),
        MOZART_SYMPHONY_40_I("mozart_symphony_40_i.mid", Category.PIECE, Format.MIDI),
        MOZART_SYMPHONY_40_I_NEW("mozart_symphony_40_i_new.mid", Category.PIECE, Format.MIDI),
        ROSSINI_LA_SCALA_DI_SETA("rossini_la_scala_di_seta.mid", Category.PIECE, Format.MIDI),
        ROSSINI_PETITE_MESSE("rossini_petite_messe.mid", Category.PIECE, Format.MIDI),
        ROSSINI_SEMIRAMIDE("rossini_semiramide.mid", Category.PIECE, Format.MIDI),
        ROSSINI_STABAT_MATER("rossini_stabat_mater.mid", Category.PIECE, Format.MIDI),
        SCHUBERT_DEATH_I("schubert_death_i.mid", Category.PIECE, Format.MIDI),
        SCHUBERT_DEATH_II("schubert_death_ii.mid", Category.PIECE, Format.MIDI),
        TCHAIKOVSKY_4_I("tchaikovsky_4_i.mid", Category.PIECE, Format.MIDI),
        TCHAIKOVSKY_5_I("tchaikovsky_5_i.mid", Category.PIECE, Format.MIDI),
        TCHAIKOVSKY_4_II("tchaikovsky_4_ii.mid", Category.PIECE, Format.MIDI),
        TCHAIKOVSKY_5_III("tchaikovsky_5_iii.mid", Category.PIECE, Format.MIDI),


        // TEST FILES

        /*
          test-generic.png
         */
        TEST_GENERIC("test-generic.mid", Category.TEST, Format.MIDI),

        /// C3 (Voice 1), G3 (Voice 2), E4 (Voice 3), C5 (Voice 4). Half note "chord"; 2/4. 1 measure.
        VOICE_TO_CHANNEL_MAPPING("voice_to_channel_mapping.mid", Category.TEST, Format.MIDI),

        /// SATB large intervals, duration test
        TEST_1("Test1.mid", Category.TEST, Format.MIDI),

        /// zadok pattern, 2-measureList, overlapping voices
        TEST_2("Test2.mid", Category.TEST, Format.MIDI),

        /// don't delete quite yet, enharmonic spelling test
        TEST_PITCH("TestPitch.mid", Category.TEST, Format.MIDI),

        /// piano: 2 C5 tied wholes, 4 C5 halfs USING 2-VOICE FEATURE (OVERLAPPING NOTES)
        TEST_3("Test3.mid", Category.TEST, Format.MIDI),

        /// same as Test3, not using 2-voice feature (OVERLAPPING NOTES)
        TEST_3_TRY_2("Test3Try2.mid", Category.TEST, Format.MIDI),

        /// two c wholes tied in RH, ascending quarters C3...B3, D4
        TEST_3_TRY_3("Test3Try3.mid", Category.TEST, Format.MIDI),

        /// soprano two tied c wholes, alto 2 untied c wholes
        TEST_3_TRY_4("Test3Try4.mid", Category.TEST, Format.MIDI),

        /// 20 instruments, 20 tracks, lots of C5s and a couple chords (1 measure, mostly wholes)
        TEST_4("Test4.mid", Category.TEST, Format.MIDI),

        /// piccolo (4 c5 quarters) + soprano (c5 whole)
        TEST_4_TRY_2("Test4Try2.mid", Category.TEST, Format.MIDI),

        /// 20 empty tracks + piccolo (4 c5 quarters) + soprano (c5 whole)
        TEST_4_TRY_3("Test4Try3.mid", Category.TEST, Format.MIDI),

        /// Overlapping test (4 measure string quartet in C)
        OVERLAPPING_TEST("overlapping_test.mid", Category.TEST, Format.MIDI),

        /// Complex test; see .mscz file
        COLUMN_TEST_1("column_test_1.mid", Category.TEST, Format.MIDI),
        COLUMN_TEST_2("column_test_2.mid", Category.TEST, Format.MIDI),

        /*
         Reading the actual bytes in the SMF from the .mid file that MuseScore produced showed that even with
         well-separated notes (like 8th → 8th rest → 8th → 8th rest), the note off event is still sent at -1 the real
         duration (so note on at 0, note off at 239).
        */
        /// In 2/4: C4 (8th), C4 (8th)  |  C4 (qtr), C4 (qtr)  |  C4 (qtr), D4 (qtr)
        TICKS_TEST("ticks_test.mid", Category.TEST, Format.MIDI),

        /// Papageno pickup pattern, 3 measures, 4/4, first measures is "full" with 3 qtr rests and 1 qtr pickup
        PSEUDO_ANACRUSIS_TEST("pseudo_anacrusis_test.mid", Category.TEST, Format.MIDI),

        /// Complex test; see .mscz file
        ORNAMENT_TEST("ornament_test.mid", Category.TEST, Format.MIDI),

        // See documentation in {@link reductor.PieceTest#getMeasureRanges}
        MEASURE_SIZE_TEST("measure_size_test.mid", Category.TEST, Format.MIDI),
        OVERLAPPING_TEST_2("overlapping_test_2.mid", Category.TEST, Format.MIDI),
        OVERLAPPING_TEST_3("overlapping_test_3.mid", Category.TEST, Format.MIDI),
        OVERLAPPING_TEST_4("overlapping_test_4.mid", Category.TEST, Format.MIDI),
        OVERLAPPING_TEST_5("overlapping_test_5.mid", Category.TEST, Format.MIDI),
        OVERLAPPING_TEST_6("overlapping_test_6.mid", Category.TEST, Format.MIDI),
        OVERLAPPING_TEST_7("overlapping_test_7.mid", Category.TEST, Format.MIDI),
        OVERLAPPING_ASCENDING_SCALE("overlapping_ascending_scale.mid", Category.TEST, Format.MIDI),
        BEETHOVEN_7_FUGUE("beethoven_7_fugue_full.mid", Category.TEST, Format.MIDI),
        PICKUP_MEASURE_TEST("pickup_measure_test.mid", Category.TEST, Format.MIDI),

        /// overlapping notelist
        BEETHOVEN_7_FUGUE_PIANO("beethoven_7_fugue_piano.mid", Category.TEST, Format.MIDI),
        CHOPIN_PREL_e("chopin_prel_e.mid", Category.TEST, Format.MIDI),
        CHOPIN_PREL_c("chopin_prel_c.mid", Category.TEST, Format.MIDI),
        ;

        private final String fileName;
        private final Category category;
        private final Format format;

        MusicFile(String fileName, Category category, Format format) {
            this.fileName = fileName;
            this.category = category;
            this.format = format;
        }

        String getFileName() { return fileName; }
        Format getFormat() { return format; }
        Category getCategory() { return category; }

        public String getPath() {
            return this.category == Category.TEST
                    ? Path.of(DIR_MIDI_TESTS + fileName).toString()
                    : Path.of(DIR_MIDI_PIECES + fileName).toString();
        }

    }


    private Catalog() { }


    static List<File> getPieceMidis() { return by(Category.PIECE) ; }
    static List<File> getTestMidis() { return by(Category.TEST) ; }

    private static List<File> by(Category cat) {
        return java.util.Arrays.stream(MusicFile.values())  // iterate thru enum values
                .filter(m -> m.category == cat)             // filter by category
                .map(m -> new File(m.getPath()))            // construct the java.io.file object
                .toList();                                  // immutable list
    }

}