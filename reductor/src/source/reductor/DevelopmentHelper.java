package reductor;

import reductor.dataconverter.Conversion;
import reductor.dataconverter.UnpairedNoteException;
import reductor.midi.MidiFile;
import reductor.midi.NoteOnEvent;
import reductor.piece.*;

import javax.sound.midi.InvalidMidiDataException;
import java.util.ArrayList;


public class DevelopmentHelper {


    public static Piece getPiece(String filepath) throws InvalidMidiDataException, UnpairedNoteException {

        MidiFile midiFile = MidiFile.createMidiFile(filepath);

        //// dev
        //ArrayList<NoteOnEvent> troublemakers = new ArrayList<>();
        //for (NoteOnEvent on : midiFile.getNoteOnEvents()) {
        //    if (179710 < on.getTick()  &&  on.getTick() < 179714) {
        //        troublemakers.add(on);
        //    }
        //}
        //System.out.println();
        ////dev

        Piece.TPQ = midiFile.getResolution();
        System.err.println("Res: " + Piece.TPQ + " for " + midiFile.getName());

        ArrayList<Note> notes = Conversion.toNotes(midiFile.getNoteOnEvents(),
                midiFile.getNoteOffEvents());
        ArrayList<TimeSignature> timeSigs = Conversion.assignRanges(midiFile.getTimeSignatureEvents(),
                midiFile.getSequenceLengthInTicks(), TimeSignature.class);
        ArrayList<KeySignature> keySigs = Conversion.assignRanges(midiFile.getKeySignatureEvents(),
                midiFile.getSequenceLengthInTicks(), KeySignature.class);
        ArrayList<Tempo> tempos = Conversion.assignRanges(midiFile.getSetTempoEvents(),
                midiFile.getSequenceLengthInTicks(), Tempo.class);

        return new Piece(notes, timeSigs, keySigs, tempos, midiFile.getResolution(), midiFile.getName());
    }


}