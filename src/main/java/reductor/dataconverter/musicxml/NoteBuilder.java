package reductor.dataconverter.musicxml;

import org.audiveris.proxymusic.*;
import reductor.piece.Hand;
import reductor.piece.Note;

import java.lang.Double;
import java.lang.String;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Map;

import static reductor.dataconverter.musicxml.ConversionToMusicXML.factory;
import static reductor.piece.RhythmBases.*;


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


    public static org.audiveris.proxymusic.Note buildNote(Note pieceNote) {

        org.audiveris.proxymusic.Note note = factory.createNote();

        // Pitch
        Pitch pitch = buildPitch(pieceNote);
        note.setPitch(pitch);

        // Duration
        BigDecimal duration = new BigDecimal(pieceNote.getRange().duration());
        note.setDuration(duration);

        // NoteType
        NoteType type = buildNoteType(pieceNote);
        note.setType(type);

        // Staff
        String staff = switch(pieceNote.hand()) {
            case Hand.RIGHT -> "1";
            case Hand.LEFT -> "2";
            default -> {
                if (60 <= pieceNote.pitch()) { yield "1"; }
                else { yield "2"; }
            }
        };
        note.setStaff(new BigInteger(staff));

        // Dot
        if (pieceNote.getRhy().isDotted()) {
            note.setPrintDot(YesNo.YES);
            note.getDot().add(new EmptyPlacement());
        }

        // TimeModification
        if (!pieceNote.getRhy().isRegular()) {
            note.setTimeModification( buildTimeModification(pieceNote) );
        }

        return note;
    }

    private static Pitch buildPitch(Note pieceNote) {

        Pitch pitch = factory.createPitch();

        // e.g. "C#" (false boolean means return pitch string without register numeral)
        String piecePitchString = reductor.piece.Pitch.toStr(pieceNote.pitch(), false);

        // e.g. "C"
        String semitone = piecePitchString.substring(0, 1);
        Step step = Step.fromValue(semitone);
        pitch.setStep(step);

        // Alter
        if (1 < piecePitchString.length()) {

            String alter = "0";
            String acc;

            // e.g. "C#, Cb, or Cx"
            if (piecePitchString.length() == 2) {
                acc = piecePitchString.substring(1, 2);
                if (acc.equals("#")) { alter = "1"; }
                if (acc.equals("b")) { alter = "-1"; }
                if (acc.equals("x")) { alter = "2"; }
            }

            // e.g. "Cbb"
            if (piecePitchString.length() == 3) {
                acc = piecePitchString.substring(1, 3);
                if (acc.equals("bb")) { alter = "-2"; }
            }

            // TODO: setAccidental. Need to create a map.

            pitch.setAlter(new BigDecimal(alter));
        }

        // Register
        int register = reductor.piece.Pitch.getRegister(pieceNote.pitch());
        pitch.setOctave(register);

        return pitch;
    }

    private static NoteType buildNoteType(Note pieceNote) {

        NoteType type = factory.createNoteType();

        String noteTypeString = baseMap.get(pieceNote.getRhy().base());
        type.setValue(noteTypeString);

        return type;
    }

    private static TimeModification buildTimeModification(Note pieceNote) {

        TimeModification timeModification = factory.createTimeModification();

        BigInteger actualNotes = BigInteger.valueOf((long) pieceNote.getRhy().divisor());
        timeModification.setActualNotes(actualNotes);

        BigInteger normalNotes = BigInteger.valueOf((long) pieceNote.getRhy().multiplier());
        timeModification.setNormalNotes(normalNotes);

        return timeModification;
    }

}