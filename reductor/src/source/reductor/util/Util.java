package reductor.util;

import javax.sound.midi.*;
import java.io.File;
import java.io.IOException;

import static reductor.Files.MIDI_FILES_OUT_DIR;


/**
 * General purpose utility class for the {@code reductor} program.
 * <p>
 * Some of this is purely for debugging or specific to my machine.
 */
public class Util {

    /// Use in conjunction with {@link Util#openWith}
    public static final String MUSESCORE = "MuseScore 4.app";
    /// Use in conjunction with {@link Util#openWith}
    public static final String GARAGEBAND = "GarageBand.app";


    private Util() { }


    /**
     * Opens a valid MIDI file with a particular app.
     *
     * @param file The MIDI file to open
     * @param app The name of the application like "appName.app"
     */
    static void openWith(File file, String app) throws IOException {

        if (!app.equals(GARAGEBAND)  &&  !app.equals(MUSESCORE)) {
            throw new RuntimeException("selected app not supported");
        }

        checkMidiFileExtension(file);

        ProcessBuilder processBuilder = new ProcessBuilder(
                "open",
                "-a",
                "/Applications/" + app,
                file.getAbsolutePath()
        );

        processBuilder.start();
    }

    /**
     * Helper for {@link Util#openWith}. Checks if a file has the correct
     * ".mid" extension.
     *
     * @param file The file to check.
     */
    private static void checkMidiFileExtension(File file) {
        String outFileName = file.getName();
        int periodIndex = outFileName.lastIndexOf('.');
        if (periodIndex == -1
                || !outFileName.substring(periodIndex + 1).equals("mid")) {
            throw new RuntimeException("out file should have '.mid' extension");
        }
    }

    /**
     * Invokes the {@link javax.sound.midi.Sequencer}'s playback functionality.
     *
     * @param sequence The {@link javax.sound.midi.Sequence} to play
     */
    public static void play(Sequence sequence) throws InvalidMidiDataException {

        Sequencer sequencer;
        try {
            sequencer = MidiSystem.getSequencer();
            sequencer.setSequence(sequence);
            sequencer.open();
            sequencer.start();
        } catch (MidiUnavailableException e) {
            throw new RuntimeException(e);
        }

        while (true) {
            if (!sequencer.isRunning()) {
                sequencer.close();
                return;
            }
        }

    }

    /**
     * Given a {@link javax.sound.midi.Sequence}, writes out a valid ".mid" file to
     * this project's out directory.
     *
     * @param sequence The {@link javax.sound.midi.Sequence} object to write out
     * @param name     A name to give the file
     * @return The File object pertaining to the new file
     */
    public static File write(Sequence sequence, String name) {

        if (name.contains(".")) {
            throw new RuntimeException("file name should not contain '.'");
        }

        File outFile = new File(MIDI_FILES_OUT_DIR + name + ".mid");

        // Append a unique int to out file; if "my_file.mid" exists, new will be "my_file_1.mid"
        int counter = 1;
        while (outFile.exists()) {
            outFile = new File(MIDI_FILES_OUT_DIR + name + "_" + counter + ".mid");
            counter++;
        }

        //// Assuming this program will never write a 2 file type (multiple sequences)
        //int fileType = sequence.getTracks().length == 1 ? 0 : 1;
        int fileType = 0; // TODO: double-check

        try {
            MidiSystem.write(sequence, fileType, outFile);
            if (!outFile.exists()) { throw new IOException("write out failed"); }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return outFile;
    }


}
