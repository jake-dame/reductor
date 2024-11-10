package reductor;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.Sequence;
import java.io.IOException;

import static reductor.Files.*;


public class Main {

    //for (File file : Files.getFiles() ) {
    //    System.out.println(file.getName());
    //    Piece piece = new Piece(file.getPath());
    //    Reduction red = new Reduction(piece);
    //}

    public static void main(String[] args) {
        // What if I have an interval tree where the max element
        // is [0,1000], and I make a query window with [1001,2000] or something
        try {

            Piece piece = new Piece(BACH_KYRIE_ELEISON);
            Reduction red = new Reduction(piece);
            Util.write(Notes.toSequencePlusAddbacks(red.chords, piece.getAddBacks()), "thisone");

        } catch (InvalidMidiDataException | IOException e) {
            e.printStackTrace();
        }

    }


}