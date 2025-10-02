package reductor.dataconverter.musicxml;

import org.audiveris.proxymusic.*;
import reductor.core.KeySignature;
import reductor.core.Measure;
import reductor.core.Note;
import reductor.core.Range;

import java.lang.String;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;

import static reductor.dataconverter.musicxml.ConversionToMusicXml.factory;
import static reductor.core.KeySignature.getTonic;


public class PartBuilder {

    ScorePartwise.Part part;
    ArrayList<reductor.core.Measure> pieceMeasures;

    PartBuilder(ScorePartwise.Part part, ArrayList<reductor.core.Measure> pieceMeasures) {
        this.part = part;
        this.pieceMeasures = pieceMeasures;
    }

    // dev
    KeySignature currKey;
    // end

    void build(long tpq) {

        MeasureBuilder measureBuilder = new MeasureBuilder();

        for (Measure pieceMeasure : pieceMeasures) {

            // dev
            currKey = pieceMeasure.getKeySignature();
            // end

            // Build non-note aspects of measure (check for attribute changes, assign measure number, etc.)
            ScorePartwise.Part.Measure measure = measureBuilder.build(pieceMeasure, tpq);

            // Populate the ScorePartwise.Part.Measure with notes from the Piece.Measure.
            populateMeasure(measure, pieceMeasure);

            // Attach measure to Part.
            part.getMeasure().add(measure);


            // This is important for the measure builder to know when to assign new attributes to a measure
            measureBuilder.setLastMeasure(pieceMeasure);

        }

    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////

    Note prevNote;

    long measureStart;
    long measureEnd;

    private void populateMeasure(ScorePartwise.Part.Measure measure, Measure pieceMeasure) {

        measureStart = pieceMeasure.getRange().low();
        measureEnd = pieceMeasure.getRange().high();

        ArrayList<Note> rhNotes = pieceMeasure.getRhNotes();
        ArrayList<Note> lhNotes = pieceMeasure.getLhNotes();

        if (!rhNotes.isEmpty()) {

            placeNotesInMeasure(rhNotes, measure);

            long backupAmt = prevNote.stop() + 1 - measureStart;
            if (backupAmt != 0) {
                Backup backup = factory.createBackup();
                backup.setDuration(new BigDecimal(backupAmt));
                measure.getNoteOrBackupOrForward().add(backup);
            }

        }

        if (!lhNotes.isEmpty()) {
            placeNotesInMeasure(lhNotes, measure);
        }

    }

    // Places the notes for a single staff (i.e. right hand, or left hand). Will be called twice.
    private void placeNotesInMeasure(ArrayList<Note> pieceNotes, ScorePartwise.Part.Measure measure) {

        // This gets things in the order I want. No touch.
        pieceNotes.sort(Comparator.comparing(Note::start)
                .thenComparing(Note::pitch)
                .thenComparing(Note::stop)
        );

        prevNote = null;
        int currVoice = 1;
        boolean chordBuilding;
        for (Note pieceNote : pieceNotes) {

            // This trims notes that extend before/after barlines.
            // If they don't need to be trimmed, just a copy is returned.
            // This is where the tieForward/tieBack booleans are set so that the xmlNote knows
            //     whether or not to add tie/tied elements.
            Note currNote = trim(pieceNote);

            // Get the xmlNote with tie/tied elements handled, and staff assignments handled as well.
            var xmlNote = getXmlNote(currNote);

            // Block for very first note.
            if (prevNote == null) {

                xmlNote.setVoice("1");
                measure.getNoteOrBackupOrForward().add(xmlNote);

                // Initialize the previous note.
                prevNote = currNote;

                // We don't need to add chords, voices greater than 1, or forwards/backups for the first note.
                continue;
            }

            /*
             * For the remaining notes on the staff:
             */

            // Check for chords.
            if (currNote.start() == prevNote.start()  &&  currNote.stop() == prevNote.stop()) {
                xmlNote.setChord(new Empty());
                chordBuilding = true;
            } else {
                chordBuilding = false;
            }

            // Check for overlapping notes --> handle voices assignments.
            if (currNote.start() == prevNote.start()  &&  currNote.stop() != prevNote.stop()) {
                if (!chordBuilding) {
                    currVoice++;
                }
            }

            // Check for forwards/backups.
            if(prevNote.start() < currNote.start()) {

                // This is also where we will reset the voice counter since we have moved on
                //     and are therefore no longer overlapping
                currVoice = 1;

                long forwardAmt = pieceNote.start() - (this.prevNote.stop() + 1);
                if (0 < forwardAmt) { measure.getNoteOrBackupOrForward().add( buildForward(forwardAmt) ); }

            } else if(currNote.start() < prevNote.start() && !chordBuilding) {

                long backupAmt = this.prevNote.stop() + 1;
                if (backupAmt != 0) { measure.getNoteOrBackupOrForward().add( buildBackup(backupAmt) ); }

            }

            //checkForRespelling(currNote, xmlNote);

            // Now that applicable forwards/backups have been added, we can add the actual note.
            xmlNote.setVoice(String.valueOf(currVoice));
            measure.getNoteOrBackupOrForward().add(xmlNote);

            // Update last note.
            this.prevNote = currNote;
        }

    }

    private void checkForRespelling(Note note, org.audiveris.proxymusic.Note xmlNote) {

        int tonic = getTonic(currKey);
        boolean isTritone = note.pitch() - tonic == 6;

        if ((currKey.isFlat()  ||  note.pitch() < prevNote.pitch())  &&  !isTritone) {

            Step step = xmlNote.getPitch().getStep();

            Step newStep = step.ordinal() == 0
                    ? Step.values()[ Step.values().length - 1 ]
                    : Step.values()[ step.ordinal() - 1 ];

            xmlNote.getPitch().setStep(newStep);

        }

    }


    private Forward buildForward(long forwardAmt) {
        Forward forward = factory.createForward();
        forward.setDuration(new BigDecimal(forwardAmt));
        return forward;
    }

    private Backup buildBackup(long backupAmt) {
        Backup backup = factory.createBackup();
        backup.setDuration(new BigDecimal(backupAmt));
        return backup;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////

    boolean tieForward;
    boolean tieBack;

    private Note trim(Note pieceNote) {

        tieForward = measureEnd < pieceNote.stop();
        tieBack = pieceNote.start() < measureStart;

        Note newNote = pieceNote;
        if (tieForward) {
            Range rangeWithEndTrimmed = new Range(pieceNote.getRange().low(), measureEnd);
            newNote = pieceNote.setRange(rangeWithEndTrimmed);
        }

        if (tieBack) {
            Range rangeWithBeginningTrimmed = new Range(measureStart, pieceNote.getRange().high());
            newNote = pieceNote.setRange(rangeWithBeginningTrimmed);
        }

        return newNote;
    }

    private org.audiveris.proxymusic.Note getXmlNote(Note pieceNote) {

        org.audiveris.proxymusic.Note note = NoteBuilder.buildNote(pieceNote);

        if (tieForward) { tieForward(note); }
        if (tieBack) { tieBack(note); }

        return note;
    }

    private void tieForward(org.audiveris.proxymusic.Note note) {

        Tie tie = factory.createTie();
        tie.setType(StartStop.START);
        note.getTie().add(tie);

        Notations notations = factory.createNotations();
        note.getNotations().add(notations);

        Tied tied = factory.createTied();
        tied.setType(TiedType.START);
        notations.getTiedOrSlurOrTuplet().add(tied);
    }

    private void tieBack(org.audiveris.proxymusic.Note note) {

        Tie tie = factory.createTie();
        tie.setType(StartStop.STOP);
        note.getTie().add(tie);

        Notations notations = factory.createNotations();
        note.getNotations().add(notations);

        Tied tied = factory.createTied();
        tied.setType(TiedType.STOP);
        notations.getTiedOrSlurOrTuplet().add(tied);
    }

}