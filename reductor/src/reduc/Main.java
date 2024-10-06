package reduc;

import javax.sound.midi.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;

import static reduc.Files.BEETHOVEN_5_IV;
import static reduc.Files.BEETHOVEN_MOONLIGHT;
import static reduc.ReductorUtil.play;

public class Main {

    public static void main(String[] args) throws InvalidMidiDataException, IOException {

        Midi midi = new Midi(BEETHOVEN_MOONLIGHT);

        Sequence seqIn = MidiSystem.getSequence(new File(BEETHOVEN_5_IV));
        play(seqIn);
        Sequence copy = ReductorUtil.copySequence(seqIn);
        //ReductorUtil.play(copy);

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