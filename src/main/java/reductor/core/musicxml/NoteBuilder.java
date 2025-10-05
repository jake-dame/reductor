package reductor.core.musicxml;


import org.audiveris.proxymusic.*;
import reductor.core.Hand;
import reductor.core.Measure;
import reductor.core.Note;

import java.lang.Double;
import java.lang.String;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Map;

import static reductor.core.Bases.*;
import static reductor.core.musicxml.ScorePartwiseBuilder.factory;

/**
 * Builds a MusicXML Note.
 * <p>
 * Quick reference for MusicXML terminology:
 * <ul>
 *     <li>
 *         Step: the letter name (sans accidental) of a note in {@code ['A','G']}
 *     </li>
 *     <li>
 *         Alter: how a pitch should be altered; disparate from accidental symbol to render
 *     </li>
 *     <li>
 *         Octave: i.e. register
 *     </li>
 *     <li>
 *         Duration: how many subdivisions this note should get; dependent on
 *         {@code <divisions>} in {@code part/measure/attributes/divisions}
 *     </li>
 *     <li>
 *         Voice: multi-voice-supporting element
 *     </li>
 *     <li>
 *         Type: the type of note to render
 *     </li>
 *     <li>
 *         Dot: for files produced by MuseScore, you will see no print-dot attr, but a {@code <dot>}
 *             element with x/y coordinates. ProxyMusic output is attr. + empty dot element..
 *     </li>
 *     <li>
 *         Accidental: the type of accidental to render
 *     </li>
 *     <li>
 *         Stem: stem direction; can be "up", "down", "none", or "double"; see: {@link org.audiveris.proxymusic.Stem}
 *     </li>
 *     <li>Staff: for piano scores, staff 1 is the top staff, and 2 is the bottom</li>
 * </ul>
 * <p>
 * Example:
 * <pre>
 * {@code
 *      <note default-x="138.72" default-y="-30">
 *         <pitch>
 *           <step>G</step>
 *           <alter>1</alter>
 *           <octave>4</octave>
 *           </pitch>
 *         <duration>3</duration>
 *         <voice>1</voice>
 *         <type>quarter</type>
 *         <dot default-x="156.71" default-y="-25"/>
 *         <accidental>sharp</accidental>
 *         <stem>up</stem>
 *         <staff>1</staff>
 *         </note>
 * }
 * </pre>
 */
public class NoteBuilder {


    private static final Map<Double, String> baseMap;


    static {
        baseMap = Map.ofEntries(
                Map.entry(WHOLE, "whole"),
                Map.entry(HALF, "half"),
                Map.entry(QUARTER, "quarter"),
                Map.entry(EIGHTH, "eighth"),
                Map.entry(SIXTEENTH, "16th"),
                Map.entry(THIRTY_SECOND, "32nd"),
                Map.entry(SIXTY_FOURTH, "64th"),
                Map.entry(ONE_TWENTY_EIGHTH, "128th")
        );
    }


    private NoteBuilder() {}


    public static org.audiveris.proxymusic.Note buildNote(Note pieceNote) {

        org.audiveris.proxymusic.Note xmlNote = factory.createNote();

        Pitch pitch = buildPitch(pieceNote);
        xmlNote.setPitch(pitch);

        BigDecimal duration = buildDuration(pieceNote);
        xmlNote.setDuration(duration);

        NoteType type = buildNoteType(pieceNote);
        xmlNote.setType(type);

        BigInteger staff = buildStaff(pieceNote);
        xmlNote.setStaff(staff);

        // Moving the below two tasks into their own functions, while somewhat pointless,
        //     was done for testability. If a change is made to domain classes,
        //     it is extremely important that no change between the package API's and
        //     the integrity of the conversion is changed.
        // Additionally, the conditionals for whether the function needs to be called at all
        //     was moved into the function itself, so that testing for returning null can occur.

        // This is how ProxyMusic illustrates adding dots.
        EmptyPlacement ep = buildDot(pieceNote);
        if (ep != null) {
            xmlNote.setPrintDot(YesNo.YES);
            xmlNote.getDot().add(ep);
        }

        TimeModification tm = buildTimeModification(pieceNote);
        if (tm != null)  { xmlNote.setTimeModification(tm); }

        return xmlNote;
    }

    private static Pitch buildPitch(Note pieceNote) {

        Pitch pitch = factory.createPitch();

        // e.g. "C#"
        String pitchStr = reductor.core.Pitch.toStr(pieceNote.pitch(), false);

        // e.g. "C"
        String pitchLetter = pitchStr.substring(0, 1);
        Step step = Step.fromValue(pitchLetter);
        pitch.setStep(step);

        if (1 < pitchStr.length()) {

            String alter = "0";
            String acc;

            // e.g. "C#", "Cb", or "C*"
            if (pitchStr.length() == 2) {
                acc = pitchStr.substring(1, 2);
                if (acc.equals("#")) {alter = "1";}
                if (acc.equals("b")) {alter = "-1";}
                if (acc.equals("x")) {alter = "2";}
            }

            // e.g. "Cbb"
            if (pitchStr.length() == 3) {
                acc = pitchStr.substring(1, 3);
                if (acc.equals("bb")) {alter = "-2";}
            }

            pitch.setAlter(new BigDecimal(alter));
        }

        int octave = reductor.core.Pitch.getRegister(pieceNote.pitch());
        pitch.setOctave(octave);

        return pitch;
    }

    private static BigDecimal buildDuration(Note pieceNote) {
        long duration = pieceNote.getRange().duration();
        return new BigDecimal(duration);
    }

    private static NoteType buildNoteType(Note pieceNote) {

        NoteType type = factory.createNoteType();

        String noteTypeString = baseMap.get(pieceNote.getRhy().base());
        type.setValue(noteTypeString);

        return type;
    }

    private static BigInteger buildStaff(Note pieceNote) {
        return switch (pieceNote.hand()) {
            case Hand.RIGHT -> new BigInteger("1");
            case Hand.LEFT -> new BigInteger("2");
            default -> {
                if (60 <= pieceNote.pitch()) {
                    yield new BigInteger("1");
                } else {
                    yield new BigInteger("2");
                }
            }
        };
    }

    private static EmptyPlacement buildDot(Note pieceNote) {
        if (!pieceNote.getRhy().isDotted()) { return null; }
        return new EmptyPlacement();
    }

    private static TimeModification buildTimeModification(Note pieceNote) {

        if (pieceNote.getRhy().isRegular()) { return null; }

        TimeModification timeModification = factory.createTimeModification();

        BigInteger actualNotes = BigInteger.valueOf((long) pieceNote.getRhy().divisor());
        timeModification.setActualNotes(actualNotes);

        BigInteger normalNotes = BigInteger.valueOf((long) pieceNote.getRhy().multiplier());
        timeModification.setNormalNotes(normalNotes);

        return timeModification;
    }


}
