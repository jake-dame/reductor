package reductor;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.Sequence;
import java.io.IOException;

import static reductor.Files.*;


public class Main {

    // option + return creates a local variable for "Result is ignored" stuff
    // cmd + l now selects line
    // cmd + shift + t now re-opens closed tabs

    // TODO: Make a time signature class this is getting ridiculous

    // TODO:
    // You know I am starting to think that the duplicate note thing might actually be better implemented in the
    // query function after all with flags. And it wouldn't be all that bad.
    //     + Add every Node you touch to a STACK and set its queried flag to true.
    //     + At the end of the query in IntervalTree, just pop everything out of the stack and set them all back to
    //     false.
    //     + I think this would make the code so much easier to maintain and simpler, AND it wouldn't really be a
    //     performance thing in the end when compared to how you're doing things now.
    //     + jake: double check that the queried flag can stay with Node and not have to be in Note, because that
    //     WOULD be a considerable design drawback (don't want a member in Note that has to do with whether or not it
    //     was queried during a method call).

    // TODO: double-check that the way you writing files out (type 1: single-track) isn't screwing up musescore stuff

    // TODO: make resolution be a member of anything that needs it? This might solve a lot of the passing resolution
    //  around wildly thing:
    //      + TimeSignature
    //      + Rhythm (Note can just have getResolution() { this.rhythm.getResolution() }

    // TODO: an interface with toMidiEvent() that converts your object back into a MidiEvent
    // so that you aren't storing both.
    // One day I see the Events lists as being demolished and that will be a beautiful day

    // TODO: make a decision:
    //     + Make EventSorter loop do even more
    //     + Go with the maps approach for stuff like Key and Time signatures
    //     + Same with note pairing algorithm (probably go back to it's own function)


    /// __DON'T DELETE__
    //for (File file : Files.getFiles() ) {
    //    System.out.println(file.getName());
    //    Piece piece = new Piece(file.getPath());
    //    Reduction red = new Reduction(piece);
    //}

    public static void main(String[] args) {

        try {

            Piece piece = new Piece(MEASURE_SIZE_TEST);
            Reduction red = new Reduction(piece);
            System.out.println();

        } catch (InvalidMidiDataException | IOException e) {
            e.printStackTrace();
        }

    }


}