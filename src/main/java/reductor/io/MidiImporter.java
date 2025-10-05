package reductor.io;


import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import java.io.IOException;
import java.nio.file.Path;


public class MidiImporter {


    private MidiImporter() {}


    public static Sequence readInMidiFile(Path path) {

        Sequence sequence;

        try {
            sequence = MidiSystem.getSequence(path.toFile());
            validateJavaSoundSequence(sequence, path);
        } catch (InvalidMidiDataException | IOException e) {
            throw new RuntimeException(e);
        }

        return sequence;
    }

    private static void validateJavaSoundSequence(Sequence sequence, Path path)
            throws InvalidMidiDataException, IOException {

        if (sequence.getDivisionType() != Sequence.PPQ) {
            System.err.println("this program does not support SMPTE timing");
            throw new RuntimeException();
        }

        int fileType = MidiSystem.getMidiFileFormat(path.toFile()).getType();

        // 0 == single track, 1 == multiple tracks, 2 == multiple sequences (rare)
        if (fileType != 0 && fileType != 1) {
            throw new IllegalArgumentException(
                    "this program does not support midi files with multiple sequences"
            );
        }

    }


}
