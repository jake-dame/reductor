package reductor;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;

import static java.nio.file.Files.createFile;


public class MidiFile {

    private final File file;

    final Sequence sequence;

    final Events events;

    private MidiFile(Sequence sequence, File file, Integer fileType) throws InvalidMidiDataException {
        this.sequence = sequence;
        this.file = file;
        this.events = new Events(this.sequence);
    }

    public static MidiFile createMidiFile(String filepath) throws InvalidMidiDataException {

        File file = new File(filepath);

        Sequence sequence;
        int fileType;

        try {
            sequence = MidiSystem.getSequence(file);
            fileType = MidiSystem.getMidiFileFormat(file).getType();
        } catch (InvalidMidiDataException | IOException e) {
            System.err.println(e.getMessage());
            return null;
        }

        // Division type should always be 0.0f for this program.
        if (sequence.getDivisionType() != Sequence.PPQ) {
            System.err.println("this program does not support SMPTE timing yet");
            return null;
        }

        String name = file.getName().split("\\.")[0];

        return new MidiFile(sequence, file, fileType);

    }

    public static MidiFile createMidiFile(Sequence sequence, String name) throws InvalidMidiDataException {

        if (!name.contains(".")) {
            System.err.println("name should be plain, not in file form");
            return null;
        }

        File file;

        try {
            file = createFile(Path.of(name + ".mid")).toFile();
        } catch (IOException e) {
            System.err.println(e.getMessage());
            return null;
        }

        if (sequence != null) {
            return new MidiFile(sequence, file, null);
        } else {
            System.err.println("passed Sequence was null");
        }

        return null;

    }

    // for ease in development
    public Sequence getSequence() {
        return this.sequence;
    }

    /// 0 == single track; 1 == multiple tracks; 2 == multiple sequences (rare)
    public int getFileType() throws InvalidMidiDataException, IOException {
        return MidiSystem.getMidiFileFormat(this.file).getType();
    }

}
