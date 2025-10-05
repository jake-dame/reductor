package reductor.io;


import javax.sound.midi.*;
import java.io.IOException;
import java.nio.file.Path;


public class MidiExporter {


    private static final String EXTENSION = ".mid";

    // For now, this program will only ever produce single-sequence, single-track piano MIDI files.
    private static final int MIDI_FILE_TYPE = 0;


    private MidiExporter() {}


    /**
     * Given a {@link javax.sound.midi.Sequence}, writes out a MIDI file.
     *
     * @param sequence The {@link javax.sound.midi.Sequence} object to serialize
     * @param baseName A base name to give the file
     * @return The Path of the written out file
     */
    public static Path write(Sequence sequence, String baseName) throws IOException {

        Path path = Paths.getOutPath(baseName + "-OUT", EXTENSION);

        MidiSystem.write(sequence, MIDI_FILE_TYPE, path.toFile());

        return path;
    }


}
