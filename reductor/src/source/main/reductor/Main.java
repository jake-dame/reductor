package reductor;

// DON'T DELETE
import java.util.Comparator;

import static reductor.Files.*;


public class Main {


    public static void main(String[] args) {

        Piece piece = new Piece(TEST_4);

        piece.noteOnEvents.sort( Comparator.comparingInt(NoteOnEvent::channel));

        for (NoteOnEvent on : piece.noteOnEvents) {
            System.out.println(
                    "{Track : "+on.trackIndex+", Name : "+on.trackName+", Channel : "+on.channel+", Pitch : "+on.pitch+"}"
            );
        }

        piece.play();

    }


}