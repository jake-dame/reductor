package reductor;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiEvent;
import java.util.ArrayList;
import java.util.Comparator;


public class DevelopmentHelper {

    MidiFile midiFile;
    Context context;

    public DevelopmentHelper() { }

    public Piece getPiece(String filepath) throws InvalidMidiDataException, UnpairedNoteException {

        this.midiFile = MidiFile.createMidiFile(filepath);

        // debug
        //long len = midiFile.sequence.getTickLength();
        //midiFile.events.noteOnEvents.sort(Comparator.comparingLong(NoteOnEvent::getTick));
        //midiFile.events.noteOffEvents.sort(Comparator.comparingLong(NoteOffEvent::getTick));
        //var firstNoteOn = midiFile.events.noteOnEvents.getFirst();
        //var firstNoteOff = midiFile.events.noteOffEvents.getFirst();
        //var lastNoteOn = midiFile.events.noteOnEvents.getLast();
        //var lastNoteOff = midiFile.events.noteOffEvents.getLast();

        //System.out.print("\n noteOnEvents: ");
        //for (NoteOnEvent note : midiFile.events.noteOnEvents) {
        //    System.out.print(note.getPartner().getTick() - note.getTick() + ", ");
        //}
        // debug

        this.context = Context.createContext(midiFile.sequence.getResolution(), midiFile.sequence.getTickLength());

        ArrayList<Note> notes = Conversion.toNotes(midiFile.events.noteOnEvents, midiFile.events.noteOffEvents);
        ArrayList<TimeSignature> timeSigs = Conversion.assignRanges(midiFile.events.timeSignatureEvents, TimeSignature.class);
        ArrayList<KeySignature> keySigs = Conversion.assignRanges(midiFile.events.keySignatureEvents, KeySignature.class);
        ArrayList<Tempo> tempos = Conversion.assignRanges(midiFile.events.setTempoEvents, Tempo.class);

        Piece piece = new Piece(notes, timeSigs, keySigs, tempos);

        // dev
        //assert piece.tree.getMin().low() == firstNoteOn.getTick();
        //assert piece.tree.getMax().low() == lastNoteOn.getTick();
        //assert piece.tree.getFirstTick() == firstNoteOn.getTick();
        //assert piece.tree.getLastTick() == lastNoteOff.getTick();
        // dev

        return piece;
    }

}
