package reductor;

import javax.sound.midi.MidiEvent;
import javax.sound.midi.Sequence;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static reductor.Piece.notesToMidiEvents;


/// This is purely a development-specific and debugging class
public class Gamut {


    static void play(Sequence seq) {



    }


    @SafeVarargs
    static void play(List<? extends Ranged>... lists) {



    }


    static void play(ArrayList<MidiEvent> midiEvents) {



    }


    @SafeVarargs
    static void play(ArrayList<? extends Event<?>>... events) {



    }









































    static void open(Sequence seq) {

        File file = Util.write(seq, "idc");
        Util.openWithGarageBand(file);

    }


    @SafeVarargs
    static void open(List<? extends Noted>... lists) {

        ArrayList<Note> outList = new ArrayList<>();

        for (List<? extends Noted> list : lists) {
            for (Noted noted : list) {
                outList.addAll(noted.getNotes());
            }
        }

        open( notesToMidiEvents(outList) );

    }


    static void open(ArrayList<MidiEvent> midiEvents) {

        Sequence seq = Util.makeSequenceFromMidiEvents(Piece.RESOLUTION, midiEvents);

        open(seq);

    }


    @SafeVarargs
    static void open(ArrayList<? extends Event<?>>... events) {



    }


}
