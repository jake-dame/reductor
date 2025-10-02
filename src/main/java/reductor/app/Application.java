package reductor.app;

/*
******** Application.java ********

**** SYNOPSIS ****
* This is a utility file.
* It is a driver for the main runtime flow of the program.
* It defines a static method (and an overload of it) -- `getPiece()`,
      which takes a file and pops out an object of type `Piece`.

**** USE ****
* --

**** DESIGN NOTES ****
* --

* */

import reductor.core.KeySignature;
import reductor.core.Tempo;
import reductor.core.TimeSignature;
import reductor.dataconverter.midi.ConversionFromMidi;
import reductor.dataconverter.midi.UnpairedNoteException;
import reductor.dev.Catalog;
import reductor.parsing.midi.MidiContainer;
import reductor.core.*;

import javax.sound.midi.InvalidMidiDataException;
import java.util.ArrayList;


/**
 * This is in the `dev` package only because it is primitive and is more for aiding development
 * than it is designed to be core part of the program.
 */
public class Application {

    public static Piece getPiece(String filepath) throws InvalidMidiDataException, UnpairedNoteException {

        // 1. From the file path above...
        // 2. ...create a MidiContainer object which has:
        //     - The original sequence (jic)
        //     - The File object (useful at various places)
        //     - An EventContainer object -- a "list of lists" of various midi events
        MidiContainer midiContainer = MidiContainer.createMidiFile(filepath);

        // Failed assertion here means the passed file had: 1.) SMPTE timing detected 2.) Invalid division type
        assert midiContainer != null; // TODO: try-catch should be implemented here

        // 3. Assigns the global TPQ value (very important)
        Piece.TPQ = midiContainer.getResolution();

        // 4. Derives data that will be used to construct a `Piece`
        ArrayList<Note> notes = ConversionFromMidi.toNotes(midiContainer.getNoteOnEvents(),
                midiContainer.getNoteOffEvents());
        ArrayList<TimeSignature> timeSigs = ConversionFromMidi.assignRanges(midiContainer.getTimeSignatureEvents(),
                midiContainer.getSequenceLengthInTicks(), TimeSignature.class);
        ArrayList<KeySignature> keySigs = ConversionFromMidi.assignRanges(midiContainer.getKeySignatureEvents(),
                midiContainer.getSequenceLengthInTicks(), KeySignature.class);
        ArrayList<Tempo> tempos = ConversionFromMidi.assignRanges(midiContainer.getSetTempoEvents(),
                midiContainer.getSequenceLengthInTicks(), Tempo.class);

        // 5. Construct and return the `Piece` object
        return new Piece(notes, timeSigs, keySigs, tempos, midiContainer.getResolution(), midiContainer.getName());
    }

    public static Piece getPiece(Catalog.MusicFile musicFile) throws InvalidMidiDataException, UnpairedNoteException {
        return getPiece(musicFile.getPath());
    }


}