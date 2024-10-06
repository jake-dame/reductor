package reduc;

import javax.sound.midi.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;

import static reduc.Files.*;
import static reduc.ReductorUtil.*;

public class Main {

    public static void main(String[] args) throws InvalidMidiDataException, IOException {

        ArrayList<MidiEvent> events = new ArrayList<>();

        Reductor red = new Reductor(LEVEL_1_TEST);
        ArrayList<Chord> chords = red.levelOne();

        for (Chord chord : chords) {
            events.addAll(chord.toMidiEvents());
        }
        Sequence seq = eventsToSequence(new ArrayList[]{events}, red.resolution);
        openWithGarageBand( write(seq, "name.mid") );

    }

    //public static void main(String[] args) {
    //
    //    Midi midi = new Midi(LEVEL_1_TEST);
    //
    //    Reductor red = new Reductor(midi);
    //
    //    File file = midi.writeOut();
    //    ReductorUtil.openWithGarageBand(file);
    //
    //    //ReductorUtil.openWithGarageBand( red.getOriginal().writeOut() );
    //
    //    //File outFile = ReductorUtil.write(seq, "allEvents.mid");
    //    //ArrayList<MidiEvent>[] lists = new ArrayList[]{ midi.getAllEvents() };
    //    //var seq = ReductorUtil.makeSequence(lists, midi.getResolution());
    //
    //}

}