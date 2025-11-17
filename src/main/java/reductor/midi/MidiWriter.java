package reductor.midi;


import reductor.app.Paths;

import javax.sound.midi.*;
import java.io.IOException;
import java.nio.file.Path;


public class MidiWriter {

    private MidiWriter() {}

    /**
     * Given a {@link javax.sound.midi.Sequence}, writes out a MIDI file.
     *
     * @param sequence The {@link javax.sound.midi.Sequence} object to serialize
     * @param baseName A base name to give the file
     * @return The Path of the written out file
     */
    public static Path write(Sequence sequence, String baseName) {

        // javax.sound will throw an exception anyway, but without telling you why, so throw here
        if (sequence.getTracks().length == 0) {
            throw new RuntimeException("must add at least one track to sequence");
        }

        // SMF File Type: 0 -> one seq, one trk; 1 -> one seq, two or more trks; 2 -> two or more seq
        int midiFileType = (sequence.getTracks().length == 1) ? 0 : 1;

        Path outPath;
        final String extension = ".mid";
        try {
            outPath = Paths.getOutPath(baseName, extension);
            MidiSystem.write(sequence, midiFileType, outPath.toFile());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return outPath;
    }


}
