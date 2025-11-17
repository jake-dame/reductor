package reductor.midi;


import javax.sound.midi.*;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;


public class MidiUtil {

    public static final Map<Integer, String> instruments;
    static {

        instruments = Map.ofEntries(
                Map.entry(0x0, "acoustic grand piano"),
                Map.entry(0x6, "harpsichord"),
                Map.entry(0x8, "celesta"),
                Map.entry(0xD, "xylophone"),
                Map.entry(0x13, "church organ"),
                Map.entry(0x14, "reed organ"),
                Map.entry(0xE, "tubular bell"),
                Map.entry(0x20, "acoustic bass"),
                Map.entry(0x28, "violin"),
                Map.entry(0x29, "viola"),
                Map.entry(0x2A, "cello"),
                Map.entry(0x2B, "contrabass"),
                Map.entry(0x2C, "tremolo strings"),
                Map.entry(0x2D, "pizzicato strings"),
                Map.entry(0x2E, "orchestral strings/harp"),
                Map.entry(0x2F, "timpani"),
                Map.entry(0x30, "string ensemble 1"),
                Map.entry(0x31, "string ensemble 2/slow strings"),
                Map.entry(0x32, "synth strings 1"),
                Map.entry(0x34, "choir aahs"),
                Map.entry(0x35, "voice oohs"),
                Map.entry(0x36, "synth choir/voice"),
                Map.entry(0x38, "trumpet"),
                Map.entry(0x39, "trombone"),
                Map.entry(0x3A, "tuba"),
                Map.entry(0x3B, "muted trumpet"),
                Map.entry(0x3C, "french horn"),
                Map.entry(0x4A, "recorder"),
                Map.entry(0x44, "oboe"),
                Map.entry(0x45, "english horn"),
                Map.entry(0x46, "bassoon"),
                Map.entry(0x47, "clarinet"),
                Map.entry(0x48, "piccolo"),
                Map.entry(0x49, "flute"),
                Map.entry(0x4B, "pan flute"),
                Map.entry(0x4F, "ocarina"),
                Map.entry(0x6C, "kalimba")
        );

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
     * @param sequence The {@link Sequence} to play
     */
    @SuppressWarnings("BusyWait")
    public static void play(Sequence sequence) {

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

        } catch (MidiUnavailableException | InvalidMidiDataException e) {
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


    /**
     * Given a value in microseconds (per quarter note), converts to beats-per-minute (bpm),
     * which is what humans use to specify tempo. Easily retrieved with getData() on set tempo
     * messages from (Java) MetaMessage class.
     *
     * @param data The tempo as a number split into three LTR bytes
     * @return The same tempo in beats-per-minute
     */
    public static int convertMicrosecondsToBPM(byte[] data) {

        int byteIndex = 0;
        long microsecondsPerQuarterNote = 0;

        while (byteIndex < data.length) {
            microsecondsPerQuarterNote <<= 8;
            microsecondsPerQuarterNote |= (data[byteIndex] & 0xFF);
            byteIndex++;
        }

        final int microsecondsPerMinute = 60_000_000;

        // This cast is fine because none of the numbers here, if valid MIDI spec,
        //     will never get remotely near INTEGER_MAX.
        return microsecondsPerMinute / (int) microsecondsPerQuarterNote;
    }

    /**
     * Given a beats-per-minute (bpm) value (1 <= x <= 60,000,000),
     * converts to microseconds per quarter note, which is what MIDI spec uses to control tempo.
     * Returns a byte array of length 3, which is how that information is transmitted over the wire.
     *
     * @param bpm The tempo in beats-per-minute
     * @return The same tempo in microseconds per quarter note
     */
    public static byte[] convertBPMToMicroseconds(int bpm) {

        /*
         8,355,711 translates to 0x7F7F7F, or the highest possible value MIDI set tempo message data
         sections (which are always 3 bytes long) can accommodate (data bytes cannot go to 0xFF because
         in MIDI, the only bytes allowed to have a set MSB are status bytes (or the "Reset" SysEx message).

         The higher the microseconds-per-quarter-note, the slower the tempo.

         The reverse formula is bpm = 60,000,000 usecs-per-min / usecs-per-quarter-note

         *Lowest valid is actually 7.18071747575, but int is the safer type here since dealing with division.
        */
        if (bpm < 8 || bpm > 60_000_000) {
            throw new IllegalArgumentException("lowest valid bpm is 8; highest is 60,000,000");
        }

        final int microsecondsPerMinute = 60_000_000;
        final int microsecondsPerQuarterNote = microsecondsPerMinute / bpm;

        byte[] data = new byte[3];
        data[0] = (byte) ((microsecondsPerQuarterNote & 0xFF0000) >> 16);
        data[1] = (byte) ((microsecondsPerQuarterNote & 0x00FF00) >> 8);
        data[2] = (byte) (microsecondsPerQuarterNote & 0x0000FF);

        return data;
    }


}
