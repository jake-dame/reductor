package reductor;

import javax.sound.midi.*;
import java.io.File;
import java.io.IOException;

import static reductor.Files.*;


// option + return creates a local variable for "Result is ignored" stuff
// cmd + l now selects line
// cmd + shift + t now re-opens closed tabs


public class Main {

    // TODO:
    // An interface with toMidiEvent() that converts your object back into a MidiEvent (stop storing both)

    // TODO:
    // Make another class called Bucket (putting my foot down this is what I want to call it) for MAJOR beat
    // division. Chords could even inherit from Bucket and be perfectly columnar. When querying for chords, we want
    // to check that all the notes in the "column" follow 1 criterion: their start tick does not exceed the start
    // tick of the SMALLEST note (duration) in the column. This will allow us to get away from the whole query for
    // chords by granularity thing.

    // TODO:
    // Yes, take note creation out of Event sorting. In fact, it should be the last thing you do.
    // Make either the maps for tempo, time sig, etc., and then make notes. Can either make notes have members
    // or indices of these, or just have getters that check DS of the former list each time


    public static void main(String[] args) {

        try {

            /* DON'T DELETE */
            //==================================================
            //for (File file : Files.getOkFiles() ) {
            //    System.out.println("====== About to create " + file.getName() + "======");
            //    Piece piece = Piece.createPiece(file.getPath());
            //    Query red = new Query(piece);
            //}
            //==================================================

            /* DON'T DELETE */
            //==================================================
            //Sequence seq = MidiSystem.getSequence(new File(MOZART_SYMPHONY_40));
            //Track[] tracks = seq.getTracks();;
            //for (Track track : tracks) {
            //    for (int i = 0; i < track.size(); i++) {
            //        MidiEvent event = track.get(i);
            //        MidiMessage message = event.getMessage();
            //
            //        // ======== DO SOMETHING BELOW: =========
            //
            //    }
            //}
            //==================================================

            Piece piece = Piece.createPiece(MEASURE_SIZE_TEST);
            System.out.println();

        } catch (InvalidMidiDataException e) {
            e.printStackTrace();
        }
    }


}