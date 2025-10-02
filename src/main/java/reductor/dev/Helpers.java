package reductor.dev;


import javax.sound.midi.*;
import java.io.File;
import java.io.IOException;


public class Helpers {


    /** For use in conjunction with {@link Helpers#openWith}. */
    public static final String MUSESCORE = "MuseScore 4.app";

    /** For use in conjunction with {@link Helpers#openWith}. */
    public static final String GARAGEBAND = "GarageBand.app";


    /**
     * Opens a valid MIDI file with a particular app.
     *
     * @param file The MIDI file to open
     * @param app The name of the application like "appName.app"
     */
    static void openWith(File file, String app) throws IOException {

        if (!app.equals(GARAGEBAND)  &&  !app.equals(MUSESCORE)) {
            throw new RuntimeException("use one of: GARAGEBAND, MUSESCORE");
        }

        // openWith() is only written for macOS right now
        String osName = System.getProperty("os.name").toLowerCase();
        if (!osName.contains("mac")) {
            System.err.println("openWith() is only supported on macOS. Skipping.");
            return;
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
     * Helper for {@link #openWith}. Checks if a file has the correct
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


}
