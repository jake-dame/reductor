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


        // dev
        ArrayList<NoteOnEvent> troublemakers = new ArrayList<>();
        for (NoteOnEvent on : midiFile.events.noteOnEvents) {
            if (179710 < on.getTick()  &&  on.getTick() < 179714) {
                troublemakers.add(on);
            }
        }
        System.out.println();

        //dev



        this.context = Context.createContext(midiFile.sequence.getResolution(), midiFile.sequence.getTickLength());

        ArrayList<Note> notes = Conversion.toNotes(midiFile.events.noteOnEvents, midiFile.events.noteOffEvents);
        ArrayList<TimeSignature> timeSigs = Conversion.assignRanges(midiFile.events.timeSignatureEvents, TimeSignature.class);
        ArrayList<KeySignature> keySigs = Conversion.assignRanges(midiFile.events.keySignatureEvents, KeySignature.class);
        ArrayList<Tempo> tempos = Conversion.assignRanges(midiFile.events.setTempoEvents, Tempo.class);

        return new Piece(notes, timeSigs, keySigs, tempos);
    }

}