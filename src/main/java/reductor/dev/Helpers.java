package reductor.dev;


import javax.sound.midi.*;
import java.io.IOException;
import java.nio.file.Path;


public class Helpers {


    private static final String MUSESCORE = "MuseScore 4.app";
    private static final String GARAGEBAND = "GarageBand.app";


    private Helpers() {}


    public static void openWithGarageBand(Path path) {
        openWith(path, GARAGEBAND);
    }

    public static void openWithMuseScore(Path path) {
        openWith(path, MUSESCORE);
    }

    private static void openWith(Path path, String app) {

        ProcessBuilder pb;

        String osName = System.getProperty("os.name").toLowerCase();
        if (osName.contains("mac")) {
            pb = new ProcessBuilder("open", "-a", "/Applications/" + app, path.toString());
        } else if (osName.contains("win")) {
            pb = new ProcessBuilder("cmd", "/c", "start", "\"\"", path.toString());
        } else if (osName.contains("nux")) {
            pb = new ProcessBuilder("xdg-open", path.toString());
        } else {
            System.err.println("no openwith for you >:[");
            return;
        }

        try {
            // inheritIO makes it so console messages aren't shot into the void (i.e. subprocess)
            pb.inheritIO().start();
        } catch (IOException e) {
            System.err.println("couldn't open MuseScore 4 or GarageBand. Check app location/installation");
        }

    }

    public static void play(Path path) throws InvalidMidiDataException, IOException {
        Sequence seq = MidiSystem.getSequence(path.toFile());
        play(seq);
    }

    /**
     * Invokes the {@link javax.sound.midi.Sequencer}'s playback functionality.
     * <p>
     * If you are on macOS, this will be Gervill.
     *
     * @param sequence The {@link javax.sound.midi.Sequence} to play
     */
    @SuppressWarnings("BusyWait")
    public static void play(Sequence sequence) throws InvalidMidiDataException {

        try (Sequencer sequencer = MidiSystem.getSequencer()) {

            sequencer.open();
            sequencer.setSequence(sequence);
            sequencer.start();

            while (sequencer.isRunning()) {
                try {
                    // nothing in Sequencer blocks, ignore warning
                    Thread.sleep(100);
                } catch (InterruptedException ignored) {}
            }

        } catch (MidiUnavailableException e) {
            throw new RuntimeException(e);
        }

    }

    public static void checkMidiFileExtension(Path path) {
        String fileName = path.getFileName().toString();
        int dot = fileName.lastIndexOf('.');
        if (dot == -1 || !fileName.substring(dot + 1).equals("mid")) {
            throw new RuntimeException("out file should have '.mid' extension");
        }
    }


}
