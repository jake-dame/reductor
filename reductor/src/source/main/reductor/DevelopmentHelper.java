package reductor;

import javax.sound.midi.InvalidMidiDataException;
import java.util.ArrayList;
import java.util.Comparator;


public class DevelopmentHelper {

    private DevelopmentHelper() { }

    public static Piece getPiece(String filepath) throws InvalidMidiDataException {

        MidiFile midiFile = MidiFile.createMidiFile(filepath);

        // debug
        long len = midiFile.sequence.getTickLength();
        midiFile.events.noteOnEvents.sort(Comparator.comparingLong(NoteOnEvent::getTick));
        midiFile.events.noteOffEvents.sort(Comparator.comparingLong(NoteOffEvent::getTick));
        var firstNoteOn = midiFile.events.noteOnEvents.getFirst();
        var firstNoteOff = midiFile.events.noteOffEvents.getFirst();
        var lastNoteOn = midiFile.events.noteOnEvents.getLast();
        var lastNoteOff = midiFile.events.noteOffEvents.getLast();
        // debug

        ArrayList<Note> notes = Conversion.toNotes(midiFile.getNoteEvents());
        ArrayList<TimeSignature> timeSigs = Conversion.toTimeSigs(midiFile.getTimeSignatureEvents());

        Piece piece = new Piece(notes, timeSigs);

        // dev

        assert piece.noteList.size() == midiFile.events.noteOnEvents.size();
        assert piece.noteList.size() == midiFile.events.noteOffEvents.size();

        // by low endpoint
        assert piece.tree.getMin().low() == firstNoteOn.getTick();
        assert piece.tree.getMax().low() == lastNoteOn.getTick();

        // by tick
        assert piece.tree.getFirstTick() == firstNoteOn.getTick();
        assert piece.tree.getLastTick() == lastNoteOff.getTick();

        //assert piece.range.high() == midiFile.sequence.getTickLength();

        // dev


        return piece;
    }

}
