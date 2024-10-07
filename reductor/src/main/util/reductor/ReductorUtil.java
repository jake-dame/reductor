package reductor;

import javax.sound.midi.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static reductor.Files.MIDI_FILES_IN_DIR;
import static reductor.Pitch.getKeySignature;


/**
 * General purpose utility class for the {@code reductor} program.
 * <p>
 * Some of this is purely for debugging or specific to my machine.
 * */
public class ReductorUtil {


    /**
     * Given a beats-per-minute (bpm) value (1 <= x <= 60,000,000),
     * converts to microseconds per quarter note, which is what MIDI spec uses to control tempo.
     * Returns a byte array of length 3, which is how that information is transmitted over the wire.
     *
     * @param bpm The tempo in beats-per-minute
     * @return The same tempo in microseconds per quarter note
     */
    static byte[] convertBPMToMicroseconds(int bpm) {

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


    /**
     * Given a value in microseconds (per quarter note), converts to beats-per-minute (bpm),
     * which is what humans use to specify tempo. Easily retrieved with getData() on set tempo
     * messages from (Java) MetaMessage class.
     *
     * @param data The tempo as a number split into three LTR bytes
     * @return The same tempo in beats-per-minute
     */
    static int convertMicrosecondsToBPM(byte[] data) {

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
     * Opens a valid MIDI file with GarageBand.
     *
     * @param file The file to open with GarageBand
     */
    static void openWithGarageBand(File file) {

        checkMidiFileExtension(file);

        try {
            ProcessBuilder processBuilder = new ProcessBuilder(
                    "open",
                    "-a",
                    "/Applications/GarageBand.app",
                    file.getAbsolutePath()
            );
            processBuilder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    /**
     * Given a number of lists, returns a {@link javax.sound.midi.Sequence} object comprised
     * of those events.
     *
     * @param resolution The resolution for the {@link javax.sound.midi.Sequence} (caution)
     * @param lists 1 or more lists of MidiEvent objects
     * @return A {@link javax.sound.midi.Sequence} object.
     */
    static Sequence makeSequenceFromMidiEvents(int resolution, ArrayList<MidiEvent>[] lists) {

        Sequence out;
        try {
            out = new Sequence(Sequence.PPQ, resolution, 1);
        } catch (InvalidMidiDataException e) {
            throw new RuntimeException(e);
        }

        Track track = out.getTracks()[0];

        for(ArrayList<MidiEvent> list : lists) {
            for (MidiEvent event : list) {
                track.add(event);
            }
        }

        return out;
    }


    /**
     * A wrapper for the {@link javax.sound.midi.Sequencer}'s {@code play()} function.
     *
     * @param sequence The {@link javax.sound.midi.Sequence} to play
     */
    static void play(Sequence sequence) {

        Sequencer sequencer;
        try {
            sequencer = MidiSystem.getSequencer();
            sequencer.setSequence(sequence);
            sequencer.open();
        } catch (MidiUnavailableException | InvalidMidiDataException e) {
            throw new RuntimeException(e);
        }

        sequencer.start();

        while (true) {
            if ( ! sequencer.isRunning()) {
                sequencer.close();
                return;
            }
        }

    }


    /**
     * Given a {@link javax.sound.midi.Sequence}, writes out a valid {@code .mid} file to
     * this project's out directory. Currently, prepends "OUT_" if the name has no other
     * project-specific prefix.
     *
     * @param sequence The {@link javax.sound.midi.Sequence} object to write out
     * @param name A name to give the file
     * @return The File object pertaining to the new file
     */
    static File write(Sequence sequence, String name) {

        File outFile = new File("");

        // If it already had a valid prefix, make a new one to not have "OUT_" too
        int underscoreIndex = name.indexOf('_');
        if (underscoreIndex != -1) {

            String prefix = name.substring(0, underscoreIndex);
            if (prefix.equals("AGG") || prefix.equals("RED")) {
                outFile = new File(MIDI_FILES_IN_DIR, name);
            }

        } else {
            outFile = new File(MIDI_FILES_IN_DIR, name);
        }

        checkMidiFileExtension(outFile);

        // File type: 0 ==> single track; 1 ==> multiple tracks; 2 ==> multiple songs (not used in this program).
        final int fileType = sequence.getTracks().length < 1 ? 0 : 1;

        try {
            MidiSystem.write(sequence, fileType, outFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (!outFile.exists()) {
            throw new RuntimeException("out file does not exist");
        }


        return outFile;
    }


    /**
     * Used for writing out and opening with Garage Band. Checks if a file has the correct
     * {@code .mid} extension.
     *
     * @param file The file to check.
     */
    private static void checkMidiFileExtension(File file) {

        String outFileName = file.getName();
        int periodIndex = outFileName.lastIndexOf('.');
        if (periodIndex == -1
                || !outFileName.substring(periodIndex + 1).equals("mid") ) {
            throw new RuntimeException("out file does not have '.mid' extension JAKE");
        }

    }


    //region Printing
    /// You must provide note events, this just parses and prints them.
    static void printNoteEvents(ArrayList<MidiEvent> events) {

        int currChannel = -1;
        for (MidiEvent event : events) {

            if ( !(event.getMessage() instanceof ShortMessage message) ) {
                throw new NullPointerException("something other than ShortMessage in list");
            }

            // Only print channel header once per Track
            if (currChannel != message.getChannel()) {
                currChannel = message.getChannel();
                System.out.printf("Channel %d \n", currChannel);
            }

            // Print NOTE ON/OFF in diff columns
            int command = message.getCommand();
            int velocity = message.getData2();
            String col_tab;
            if (command == ShortMessage.NOTE_OFF || (command == ShortMessage.NOTE_ON && velocity == 0) ) {
                col_tab = "\t";
            } else {
                col_tab = "";
            }

            int pitch = message.getData1();
            System.out.printf("\t%-5d\t%-5s\t%s \n",
                    event.getTick(),
                    col_tab,
                    reductor.Pitch.numericalPitchToString(pitch, true)
            );
        }

    }

    /// You must provide meta events, this just parses and prints them.
    static void printMetaEvents(ArrayList<MidiEvent> events) {

        for (MidiEvent event : events) {

            if ( !(event.getMessage() instanceof MetaMessage message) ) {
                throw new NullPointerException("something other than ShortMessage in list");
            }

            int type = message.getType();
            byte[] data = message.getData();

            String type_label;
            String[] stringArr = new String[data.length];
            String dataString = "";

            if (type >= 0x20 && type < 0x2F) {
                type_label = "Channel prefix";
                dataString = String.valueOf(data[0]);
            } else {
                switch (type) {

                    case 0x01:
                        type_label = "Text";
                        for (int i = 0; i < data.length; i++) {
                            stringArr[i] = String.valueOf((char) data[i]);
                        }
                        dataString = String.join("", stringArr);
                        break;

                    case 0x03:
                        type_label = "\nTrack name";
                        for (int i = 0; i < data.length; i++) {
                            stringArr[i] = String.valueOf((char) data[i]);
                        }
                        dataString = String.join("", stringArr);
                        break;

                    case 0x2F:
                        type_label = "End of track";
                        break;

                    case 0x51:
                        type_label = "Set tempo";
                        int res = 0;
                        for (byte b : data) {
                            res <<= 8;
                            res |= b & 0xFF;
                        }
                        dataString = res + " microseconds-per-quarter-note ";
                        break;

                    case 0x58:
                        type_label = "Time signature";
                        stringArr = new String[data.length - 1];
                        stringArr[0] = (data[0] & 0xFF) + "/" + ((int) (Math.pow(2, data[1]))) + ", ";
                        stringArr[1]= (data[2] & 0xFF) + " (midi clock ticks / beat), ";
                        stringArr[2] = (data[3] & 0xFF) + " (32nd notes / beat), ";
                        dataString = String.join("", stringArr);
                        break;

                    case 0x59:
                        type_label = "Key signature";
                        dataString = getKeySignature(data);
                        break;

                    default: throw new IllegalArgumentException("Unknown metamessage: " + type);
                }
            }

            System.out.println(type_label + ": " + dataString);
        }

    }

    /// This just prints a sequence in byte (hex) form
    static void printBytesInALine(Sequence seq){
        System.out.println("\n================printSequence()=======================");
        for (Track track : seq.getTracks()) {
            for (int i = 0; i < track.size(); i++) {
                MidiEvent event = track.get(i);
                MidiMessage msg = event.getMessage();
                byte[] data = msg.getMessage();
                for (int j = 0; j < data.length; j++) {
                    int firstByte = data[0] & 0xFF;
                    if (firstByte == 0x80) {
                        System.out.print("\t");
                    }
                    System.out.print(Integer.toHexString(data[j] & 0xFF) + " ");
                }
                System.out.println();
            }
        }
        System.out.println("\n===================================================");
    }

    /// This is not a pretty printout, use w caution
    static void printBytesSeparatedByMessageType(String filePath) throws IOException {

        byte[] data = java.nio.file.Files.readAllBytes( Path.of(filePath) );

        int lineCtr = 0;
        for (int i = 0; i < data.length; i++) {
            int b = data[i] & 0xFF;

            if (b == 0xFF) {
                System.out.println();
                lineCtr++;
            }

            if(lineCtr > 4) {
                if (b >= 0x90 && b <= 0x9F) {
                    System.out.println();
                }
                if (b >= 0x80 && b <= 0x8F) {
                    System.out.print("\n\t");
                }
            }

            System.out.print(Integer.toHexString(b) + " ");
        }

    }
    //endregion


}
