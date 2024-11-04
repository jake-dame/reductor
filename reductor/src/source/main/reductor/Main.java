package reductor;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.Sequence;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import static reductor.Files.*;


public class Main {


    public static void main(String[] args) {

        try {

            Piece piece = new Piece(BACH_BEFIEHL_DU_DEINE_WEGE);
            Reduction red = new Reduction(piece);

            ArrayList<MidiEvent> sopranoEvents = Piece.notesToMidiEvents(red.soprano.getNotes());
            ArrayList<MidiEvent> bassEvents = Piece.notesToMidiEvents(red.bass.getNotes());

            ArrayList<MidiEvent> allMidiEvents = new ArrayList<>();
            allMidiEvents.addAll(sopranoEvents);
            allMidiEvents.addAll(bassEvents);

            Sequence seq = Util.makeSequenceFromMidiEvents(480, allMidiEvents);

            Util.play(seq);

            File outFile = Util.write(seq, "bach");

        } catch (InvalidMidiDataException | IOException e) {
            e.printStackTrace();
        }

    }


}