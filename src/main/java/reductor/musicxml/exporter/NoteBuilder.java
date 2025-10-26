package reductor.musicxml.exporter;

import org.audiveris.proxymusic.*;

import java.lang.String;
import java.math.BigDecimal;
import java.util.List;

import static reductor.musicxml.exporter.Defaults.FACTORY;
import static reductor.musicxml.exporter.Defaults.PLACEHOLDER_OBJ;


/*
Exactly one of the following (all are "in this order"):

    1.) GRACE NOTE
        a.) <grace> (Required)
        b.) Exactly one of the following:

            i.) <chord> (Optional) + <pitch>, <unpitched>, OR <rest> (Required)
            ii.) <tie> (0 to 2 times)

               ~ OR ~

            i.) <cue> (Required)
            ii.) <chord> (Optional) + <pitch>, <unpitched>, OR <rest> (Required)

    2.) CUE NOTE
        a.) <cue> (Required)
        b.) <chord> (Optional) + <pitch>, <unpitched>, OR <rest> (Required)
        c.) <duration> (Required)

    3.) NOTE
        a.) <chord> (Optional) + <pitch>, <unpitched>, OR <rest> (Required)
        b.) <duration> (Required)
        c.) <tie> (0 to 2 times)

 And the rest are optional (in this order):

    <instrument> (Zero or more times)
    <footnote>, <level>, AND/OR <voice> (Optional - any combo, but in that order)
    <type> (Optional)
    <dot> (Zero or more times)
    <accidental> (Optional)
    <time-modification> (Optional)
    <stem> (Optional)
    <notehead> (Optional)
    <notehead-text> (Optional)
    <staff> (Optional)
    <beam> (0 to 8 times)
    <notations> (Zero or more times)
    <lyric> (Zero or more times)
    <play> (Optional)
    <listen> (Optional)
*/

/*
This will not(e) handle:
    - <instrument>: complex playback feature
    - <lyric>: a large no
    - <play>: complex playback feature
    - <listen>:  complex playback feature
*/
@SuppressWarnings("FieldCanBeLocal")
public class NoteBuilder {

    public static NoteBuilderBuilder builder() {
        return new NoteBuilderBuilder();
    }

    public static class NoteBuilderBuilder {


        /* Exactly one of the following: */
        private GraceBuilder graceBuilder;
        public static class GraceBuilder {

            // Set <grace> in .build() // 1

            // Set <cue> in .build() // if no <tie>                ↴

            private Empty chord; // 0 or 1

            /* Exactly one of the following: */
            private Pitch pitch;
            private Unpitched unpitched;
            private Rest rest;

            private List<Tie> tie; // 0, 1, or 2 // if no <cue>     up

        }

        private CueBuilder cueBuilder;
        public static class CueBuilder {

            // Set <cue> in .build() // 1

            private Empty chord; // 0 or 1

            /* Exactly one of the following: */
            private Pitch pitch;
            private Unpitched unpitched;
            private Rest rest;

            private BigDecimal duration; // 1

        }

        private NormalNoteBuilder normalNoteBuilder;
        public static class NormalNoteBuilder {

            private Empty chord; // 0 or 1

            /* Exactly one of the following: */
            private Pitch pitch;
            private Unpitched unpitched;
            private Rest rest;

            /* Exactly one of the following: */
            private BigDecimal duration;

            private List<Tie> tie; // 0, 1, or 2

        }

        // In this order; all are 0+ (any combo)
            // private footnote;
            private Level level;
            //private voice;

        private NoteType type; // 0 or 1
        //private dot; // 0+
        private Accidental accidental; // 0 or 1
        private TimeModification timeModification; // 0 or 1
        private Stem stem; // 0 or 1
        private Notehead notehead; // 0 or 1
        private NoteheadText noteheadText; // 0 or 1
        private Integer staff; // 0 or 1
        private Beam beam; // 0, 1, 2, 3, 4, 5, 6, 7, or 8
        private Notations notations; // 0+


        NoteBuilderBuilder() { }


        //public NoteBuilderBuilder footnote() {
        //    var footnote = ;
        //
        //
        //
        //    this.footnote = footnote;
        //    return this;
        //}

        public NoteBuilderBuilder level() {
            Level level = FACTORY.createLevel();



            this.level = level;
            return this;
        }

        //public NoteBuilderBuilder voice() {
        //    var voice = ;
        //
        //
        //
        //    this.voice = voice;
        //    return this;
        //}


        /* https://www.w3.org/2021/06/musicxml40/musicxml-reference/data-types/note-type-value/ */
        public NoteBuilderBuilder type(String v) {
            NoteType type = FACTORY.createNoteType();
            type.setValue(v);
            this.type = type;
            return this;
        }

        public NoteBuilderBuilder accidental() {
            Accidental accidental = FACTORY.createAccidental();



            this.accidental = accidental;
            return this;
        }

        public NoteBuilderBuilder timeModification() {
            TimeModification tm = FACTORY.createTimeModification();



            this.timeModification = tm;
            return this;
        }

        /* https://www.w3.org/2021/06/musicxml40/musicxml-reference/data-types/stem-value/ */
        public NoteBuilderBuilder stem(String v) {
            Stem stem = FACTORY.createStem();
            stem.setValue(StemValue.fromValue(v));
            this.stem = stem;
            return this;
        }


        /* https://www.w3.org/2021/06/musicxml40/musicxml-reference/data-types/notehead-value/ */
        public NoteBuilderBuilder noteHead(String v, YesNo filled, YesNo paren, String smufl) {
            Notehead notehead = FACTORY.createNotehead();
            notehead.setValue(NoteheadValue.fromValue(v));
            notehead.setFilled(filled);
            notehead.setParentheses(paren);
            notehead.setSmufl(smufl); // NMTOKEN
            this.notehead = notehead;
            return this;
        }

        public NoteBuilderBuilder noteHeadText() {
            NoteheadText noteheadText = FACTORY.createNoteheadText();

            // These are both complex. Putting aside for now.

            FormattedText displayText = FACTORY.createFormattedText();
            noteheadText.getDisplayTextOrAccidentalText().add(displayText);

            AccidentalText accidentalText = FACTORY.createAccidentalText();
            noteheadText.getDisplayTextOrAccidentalText().add(accidentalText);

            this.noteheadText = noteheadText;
            return this;
        }

        // this is the positiveInteger XMLScheme type. That's all.
        public NoteBuilderBuilder staff(Integer v) {
            this.staff = v;
            return this;
        }

        /*
        v: https://www.w3.org/2021/06/musicxml40/musicxml-reference/data-types/beam-value/
        level: MusicXML supports 1 (for an eighth note) to 8 (for a 1024th note)
        */
        public NoteBuilderBuilder beam(String v, Integer level) {
            Beam beam = FACTORY.createBeam();
            beam.setValue(BeamValue.fromValue(v));
            beam.setNumber(level);
            this.beam = beam;
            return this;
        }

        public NoteBuilderBuilder notations() {
            Notations notations = FACTORY.createNotations();

            // This is insanely complex and will need a sub-builder if not sub-sub-builders :,)

            this.notations = notations;
            return this;
        }

        // ======================================  BUILD  ======================================= //

        public Note build() {

            Note note = FACTORY.createNote();

            return note;
        }


    }


}
