package reductor.core;

import reductor.midi.importer.Importer;
import reductor.midi.importer.UnpairedNoteException;
import reductor.midi.reader.Reader;
import reductor.midi.parser.MidiContainer;

import javax.sound.midi.InvalidMidiDataException;
import java.nio.file.Path;
import java.util.ArrayList;


public class PieceFactory {


    private PieceFactory() { }


    public static Piece getPiece(MidiContainer mc) throws UnpairedNoteException {

        Piece.TPQ = mc.getResolution();

        ArrayList<Note> notes = Importer.toNotes(mc.getNoteOnEvents(),
                mc.getNoteOffEvents());
        ArrayList<TimeSignature> timeSigs = Importer.assignRanges(mc.getTimeSignatureEvents(),
                mc.getSequenceLengthInTicks(), TimeSignature.class);
        ArrayList<KeySignature> keySigs = Importer.assignRanges(mc.getKeySignatureEvents(),
                mc.getSequenceLengthInTicks(), KeySignature.class);
        ArrayList<Tempo> tempos = Importer.assignRanges(mc.getSetTempoEvents(),
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
            var sequence = Reader.readInMidiFile(filePath);
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
