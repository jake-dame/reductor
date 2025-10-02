package reductor;

import reductor.dataconverter.midi.ConversionToMidi;
import reductor.dataconverter.midi.UnpairedNoteException;
import reductor.app.Application;

import javax.sound.midi.InvalidMidiDataException;

import static reductor.dev.Catalog.MusicFile.*;
import static reductor.dev.Helpers.play;


public class Main {

    static void main()
            throws InvalidMidiDataException,
            UnpairedNoteException {

        var piece = Application.getPiece(BACH_BEFIEHL_DU_DEINE_WEGE_NEW);
        play(ConversionToMidi.toSequence(piece));

    }

}