package reductor;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import java.io.File;
import java.io.IOException;

import static reductor.Files.*;


public class Main {

    //for (File file : Files.getFiles() ) {
    //    System.out.println(file.getName());
    //    Piece piece = new Piece(file.getPath());
    //    Reduction red = new Reduction(piece);
    //}


    public static void main(String[] args) {

        try {

            Piece piece = new Piece(OVERLAPPING_TEST_2);
            Util.write(piece.getReconstitution(), "overlap2");

        } catch (InvalidMidiDataException | IOException e) {
            e.printStackTrace();
        }

    }


}