package reductor.musicxml.exporter;


import org.audiveris.proxymusic.*;

import static reductor.musicxml.exporter.Defaults.FACTORY;


public class Demos {

    public static class DemoScorePartwiseBuilder {
        static void main() {

            // FOR DEMO PURPOSES -- empty components
            ScorePartwise.Part piano = FACTORY.createScorePartwisePart();
            ScorePartwise.Part violin = FACTORY.createScorePartwisePart();
            ScorePartwise.Part cello = FACTORY.createScorePartwisePart();
            // end FOR DEMO PURPOSES

            Encoding encoding = EncodingBuilder.builder()
                    .encoder("", "jd")
                    .software("reductor")
                    .encodingDescription("Using reductor 1.0")
                    .supports(YesNo.YES, "accidental", "", "")
                    .build();

            Identification identification = IdentificationBuilder.builder()
                    .composer("Franz Schubert (creator)")
                    .lyricist("Wilhelm Müller (creator)")
                    .arranger("arranger test value (creator)")
                    .translator("arranger test value (creator)")
                    .subtitle("arranger test value (creator)")
                    .encoding(encoding)
                    .source("""
                            This is just the first two lines, from the Breitkopf edition on IMSLP.
                            This is a good test for all things score header, as it is from
                            a multi-movement work, with a catalog number, lyricist, etc.
                            """
                    )
                    .relation("", """
                                  https://vmirror.imslp.org/files/imglnks/usimg/8/8d/IMSLP60822-PMLP02203-Schubert_Werke_Breitkopf_Serie_XX_Band_9_F.S.878-904.pdf
                                  """
                    )
                    .rights("", "(c) 1827 (identification/rights)")
                    .miscellaneousField("misc-test-type-one", "misc test value 1 (miscellaneous)")
                    .miscellaneousField("misc-test-type-two", "misc test value 2 (miscellaneous)")
                    .build();

            ScorePartwise out = ScorePartwiseBuilder.builder()
                    .workNumber("D.911 (work-number)")
                    .workTitle("Winterreise (work-title)")
                    .movementNumber("Der Wegweiser (movement-number)")
                    .movementTitle("XX. (movement-title)")
                    .identification(identification)
                    .part(piano)
                    .part(violin)
                    .part(cello)
                    .build();

        }
    }

    public static class DemoScorePartBuilder {
        static void main() {

        }
    }

    public static class DemoPartBuilder {
        static void main() {

        }
    }

    public static class DemoMeasureBuilder {
        static void main() {

            ScorePartwise.Part.Measure measure = MeasureBuilder.builder()
                    .attributes()
                        .staves(3)
                        .divisions(480)
                        .clefTreble(1)
                        .clefBass(2)
                        .clefBass(3)
                        .key("C#")
                        .time()
                            .signature(3, 4)
                            .done()
                        .done()
                    .direction()
                        .tempoWords("Allegro")
                        .tempoMetronome("quarter", "80")
                        .playbackSpeed(120)
                        .done()
                    .nonControlling()
                    .build();

        }
    }

    public static class DemoNoteBuilder {
        static void main() {



        }
    }

}

