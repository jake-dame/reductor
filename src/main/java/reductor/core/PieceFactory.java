package reductor.core;

import reductor.core.midi.ConversionFromMidi;
import reductor.core.midi.UnpairedNoteException;
import reductor.io.MidiImporter;
import reductor.parsing.midi.MidiContainer;

import javax.sound.midi.InvalidMidiDataException;
import java.nio.file.Path;
import java.util.ArrayList;


public class PieceFactory {


    private PieceFactory() { }


    public static Piece getPiece(MidiContainer mc) throws UnpairedNoteException {

        Piece.TPQ = mc.getResolution();

        ArrayList<Note> notes = ConversionFromMidi.toNotes(mc.getNoteOnEvents(),
                mc.getNoteOffEvents());
        ArrayList<TimeSignature> timeSigs = ConversionFromMidi.assignRanges(mc.getTimeSignatureEvents(),
                mc.getSequenceLengthInTicks(), TimeSignature.class);
        ArrayList<KeySignature> keySigs = ConversionFromMidi.assignRanges(mc.getKeySignatureEvents(),
                mc.getSequenceLengthInTicks(), KeySignature.class);
        ArrayList<Tempo> tempos = ConversionFromMidi.assignRanges(mc.getSetTempoEvents(),
                mc.getSequenceLengthInTicks(), Tempo.class);

        return new Piece(
                notes,
                timeSigs,
                keySigs,
                tempos,
                mc.getResolution()
        );

    }

    static Piece getTester(Path filePath) {

        Piece piece = null;
        try {
            var sequence = MidiImporter.readInMidiFile(filePath);
            var mc = new MidiContainer(sequence);
            piece = PieceFactory.getPiece(mc);
        } catch(InvalidMidiDataException | UnpairedNoteException e) {
            System.err.println(e.getMessage());
        }

        return piece;
    }

    //public void getPiece(MusicXmlContainer mc) {
    //
    //}


}
