package reductor.demos;


import org.audiveris.proxymusic.*;
import org.audiveris.proxymusic.ScorePartwise.Part;
import org.audiveris.proxymusic.ScorePartwise.Part.Measure;
import org.audiveris.proxymusic.util.Marshalling;
import reductor.musicxml.MusicXmlWriter;
import reductor.musicxml.builder.*;


import java.io.IOException;

import static reductor.dev.Defaults.FACTORY;


public class XmlDemos {

    static void main() {
        var sp = ScorePartwiseBuilder.builder().build();
        var m  = MeasureBuilder.builder().build();
    }

    public static class DemoScorePartwiseBuilder {
        static void main() {

            // FOR DEMO PURPOSES -- empty components
            ScorePartwise.Part tenor = FACTORY.createScorePartwisePart();
            ScorePartwise.Part piano = FACTORY.createScorePartwisePart();
            // end FOR DEMO PURPOSES

            // ============================================================= //
            // Option 1: Create all sub-components separately
            //     + Could re-use something like Encoding in other ScorePartwise-s
            //     + However, it is basically in inverse order, you have to construct
            //     sub-components first

            Encoding encoding = EncodingBuilder.builder()
                    .encoder("John Java")
                    .software("reductor 1.0")
                    .encodingDescription("this was not validated against the XSD")
                    .build();

            Identification identification = IdentificationBuilder.builder()
                    .composer("Franz Schubert")
                    .lyricist("Wilhelm Müller")
                    .encoding(encoding)
                    .source(
                        """
                        This is just the first two lines, from the Breitkopf edition on IMSLP.
                        This is a good test for all things score header, as it is from
                        a multi-movement work, with a catalog number, lyricist, etc.
                        """
                    )
                    .relation(
                        """
                        https://vmirror.imslp.org/files/imglnks/usimg/8/8d/IMSLP60822-PMLP02203-Schubert_Werke_Breitkopf_Serie_XX_Band_9_F.S.878-904.pdf
                        """
                    )
                    .rights("Public Domain")
                    .miscellaneousField("purpose", "demo the ScorePartwiseBuilder")
                    .miscellaneousField("edition-used", "Breitkopf & Härtel")
                    .build();

            ScorePartwise sp1 = ScorePartwiseBuilder.builder()
                    .workNumber("D.911")
                    .workTitle("Winterreise")
                    .movementNumber("Der Wegweiser")
                    .movementTitle("XX.")
                    .identification(identification)
                    .part(tenor)
                    .part(piano)
                    .build();

            // ============================================================= //
            // Option 2: Create sub-components inline
            //     + If something is not really reusable, just do it inline
            //     + Easier to see exactly what will be produced by the Builder

            // TODO Change these back to Consumers
            ScorePartwise sp2 = ScorePartwiseBuilder.builder()
                    .workNumber("D.911")
                    .workTitle("Winterreise")
                    .movementNumber("Der Wegweiser")
                    .movementTitle("XX.")
                    .identification(IdentificationBuilder.builder()
                        .composer("Franz Schubert")
                        .lyricist("Wilhelm Müller")
                        .encoding(
                            EncodingBuilder.builder()
                                .encoder("John Java")
                                .software("reductor 1.0")
                                .encodingDescription("this was not validated against the XSD")
                                .build()
                        )
                        .source(
                            """
                            This is just the first two lines, from the Breitkopf edition on IMSLP.
                            This is a good test for all things score header, as it is from
                            a multi-movement work, with a catalog number, lyricist, etc.
                            """
                        )
                        .relation(
                            """
                            https://vmirror.imslp.org/files/imglnks/usimg/8/8d/IMSLP60822-PMLP02203-Schubert_Werke_Breitkopf_Serie_XX_Band_9_F.S.878-904.pdf
                            """
                        )
                        .rights("Public Domain")
                        .miscellaneousField("purpose", "demo the ScorePartwiseBuilder")
                        .miscellaneousField("edition-used", "Breitkopf & Härtel")
                        .build()
                    )
                    .part(tenor)
                    .part(piano)
                    .build();
        }
    }

    // TODO Parts class
    //public static class DemoScorePartwiseBuilderConveniences {
    //    static void main() {
    //
    //        //
    //
    //        // Returns builder, allowing further customization
    //        ScorePartwise sp1 = ScorePartwiseBuilder.from(
    //                    Parts.of(Defaults.piano(), Defaults.violin(), Defaults.cello())
    //                )
    //                .title("Reductor Trio")
    //                .composer("John Cage")
    //                .build();
    //
    //        // Returns a ready-made. Same as .from() but calls the build() step for you, which is
    //        // pretty lazy, but can be convenient in some use cases (See: Testing ScorePartwise.md)
    //        ScorePartwise sp2 = ScorePartwiseBuilder.of(
    //                Parts.of(Defaults.piano(), Defaults.violin(), Defaults.cello())
    //        );
    //
    //    }
    //}

    public static class DemoMeasureBuilder {
        static void main() {
            // TODO fill this out
            Time time = TimeBuilder.builder().build();

            Direction direction = DirectionBuilder.builder().build();

            Attributes attributes = AttributesBuilder.builder().build();

            Measure measure = MeasureBuilder.builder()
                    .attributes(attributes)
                    .direction(direction)
                    .nonControlling()
                    .build();
        }
    }

    public static class DemoNoteBuilder {
        static void main() throws Marshalling.MarshallingException, IOException {
            // TODO just invoke Defaults.scorePartwiseSinglePart
            //SET-UP this
            ScorePartwise sp = FACTORY.createScorePartwise();
            PartList pl = FACTORY.createPartList();
            sp.setPartList(pl);
            ScorePart spt = FACTORY.createScorePart();
            pl.getPartGroupOrScorePart().add(spt);
            spt.setId("P1");
            Part part = FACTORY.createScorePartwisePart();
            part.setId(spt);
            sp.getPart().add(part);
            Measure measure = FACTORY.createScorePartwisePartMeasure();
            part.getMeasure().add(measure);
            // end SET-UP

            // "Last call wins" approach here, make sure you document it
            Note note = NoteBuilder.builder()
                    .pitch("C5")
                    .tieStart()
                    .duration(480)
                    .type("quarter")
                    .build();

            measure.getNoteOrBackupOrForward().add(note);

            MusicXmlWriter.write(sp, "note-demo");
        }
    }

}

