package reductor.parsing.midi;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;

import static java.nio.file.Files.createFile;

// Purpose: Construct a MidiContainer object; provide public getters for
//     its many lists (actually wrappers for private access to its EventContainer object members
public class MidiContainer {

    private final File file;

    private final Sequence sequence;

    private final EventContainer eventContainer;

    private MidiContainer(Sequence sequence, File file, Integer fileType) throws InvalidMidiDataException {
        this.sequence = sequence;
        this.file = file;
        this.eventContainer = new EventContainer(this.sequence);
    }

    // Factory (1 of 2) for private constructor
    public static MidiContainer createMidiFile(String filepath) throws InvalidMidiDataException {

        File file = new File(filepath);

        Sequence sequence;
        int fileType;

        // This is java midi library stuff extraction and validation
        try {
            sequence = MidiSystem.getSequence(file);
            fileType = MidiSystem.getMidiFileFormat(file).getType();
        } catch (InvalidMidiDataException | IOException e) {
            System.err.println(e.getMessage());
            return null;
        }

        // In-house validation
        // Division type should always be 0.0f for this program.
        if (sequence.getDivisionType() != Sequence.PPQ) {
            System.err.println("this program does not support SMPTE timing");
            return null;
        }

        return new MidiContainer(sequence, file, fileType);

    }

    // Factory (2 of 2) for private constructor
    public static MidiContainer createMidiFile(Sequence sequence, String name) throws InvalidMidiDataException {

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
            return new MidiContainer(sequence, file, null);
        } else {
            System.err.println("passed Sequence was null");
        }

        return null;

    }

    // Wrapper method calls javax
    // 0 == single track; 1 == multiple tracks; 2 == multiple sequences (rare)
    public int getFileType() throws InvalidMidiDataException, IOException {
        return MidiSystem.getMidiFileFormat(this.file).getType();
    }


    /* ==========
       PUBLIC API
     * ========== */


    public int getResolution() { return this.sequence.getResolution(); }
    public long getSequenceLengthInTicks() { return this.sequence.getTickLength(); }
    public String getName() { return this.file.getName(); }

    public ArrayList<NoteOnEvent> getNoteOnEvents() { return this.eventContainer.noteOnEvents; }
    public ArrayList<NoteOffEvent> getNoteOffEvents() { return this.eventContainer.noteOffEvents; }

    public ArrayList<TimeSignatureEvent> getTimeSignatureEvents() { return this.eventContainer.timeSignatureEvents; }
    public ArrayList<KeySignatureEvent> getKeySignatureEvents() { return this.eventContainer.keySignatureEvents; }
    public ArrayList<SetTempoEvent> getSetTempoEvents() { return this.eventContainer.setTempoEvents; }


}
