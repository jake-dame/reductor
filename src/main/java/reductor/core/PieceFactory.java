package reductor.core;

import reductor.core.midi.ConversionFromMidi;
import reductor.core.midi.UnpairedNoteException;
import reductor.parsing.midi.MidiContainer;
import reductor.parsing.musicxml.MusicXmlContainer;

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

    //public void getPiece(MusicXmlContainer mc) {
    //
    //}


}
