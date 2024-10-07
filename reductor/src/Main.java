package reductor;

import javax.sound.midi.MidiEvent;
import javax.sound.midi.Sequence;
import java.util.ArrayList;

import static reductor.Files.LEVEL_1_TEST;
import static reductor.ReductorUtil.*;


public class Main {


    public static void main(String[] args) {

        Reductor red = new Reductor(LEVEL_1_TEST);
        ArrayList<Chord> chords = red.levelOne();

        ArrayList<MidiEvent> events = new ArrayList<>();
        for (Chord chord : chords) {
            events.addAll( chord.getMidiEvents());
        }
        Sequence seq = makeSequenceFromMidiEvents(red.resolution, new ArrayList[]{ events });

        //openWithGarageBand( write(seq, "test.mid") );

    }


}