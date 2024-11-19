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

    public int getFileType() throws InvalidMidiDataException, IOException {
        return MidiSystem.getMidiFileFormat(this.file).getType();
    }

    /* BLACK-BOX INTERFACE STUFF*/

    // "I don't care what you do with this data after the fact, I've done my job"

    // This is the _idea_ behind the final black box interface but obviously for now the design is a mess

    // This is everything a Piece could possibly need from MIDI data for reduction operations
    // I'm not sure how to include addBacks in the final design, but those are note technically reduction-related
    // (but they do still make the final product sound, look, and perform better)

    public ArrayList<NoteOnEvent> getNoteEvents() {
        return this.events.noteOnEvents;
    }

    public ArrayList<TimeSignatureEvent> getTimeSignatureEvents() {
        return this.events.timeSignatureEvents;
    }

    // Not _absolutely_ necessary since this is mostly an afterthought in MIDI protocol
    public ArrayList<KeySignatureEvent> getKeySignatureEvents() {
        return this.events.keySignatureEvents;
    }

    public int getResolution() {
        return this.sequence.getResolution();
    }

    // getDivisionType could be needed later but because this program doesn't really plan to support anything other
    // than PPQ, it won't even allow SMPTE files to come through

}
