package reductor;

import reductor.dataconverter.ConversionFromMidi;
import reductor.dataconverter.UnpairedNoteException;
import reductor.midi.MidiFile;
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

        Piece.TPQ = midiFile.getResolution(); // jic

        ArrayList<Note> notes = ConversionFromMidi.toNotes(midiFile.getNoteOnEvents(),
                midiFile.getNoteOffEvents());
        ArrayList<TimeSignature> timeSigs = ConversionFromMidi.assignRanges(midiFile.getTimeSignatureEvents(),
                midiFile.getSequenceLengthInTicks(), TimeSignature.class);
        ArrayList<KeySignature> keySigs = ConversionFromMidi.assignRanges(midiFile.getKeySignatureEvents(),
                midiFile.getSequenceLengthInTicks(), KeySignature.class);
        ArrayList<Tempo> tempos = ConversionFromMidi.assignRanges(midiFile.getSetTempoEvents(),
                midiFile.getSequenceLengthInTicks(), Tempo.class);

        return new Piece(notes, timeSigs, keySigs, tempos, midiFile.getResolution(), midiFile.getName());
    }


}