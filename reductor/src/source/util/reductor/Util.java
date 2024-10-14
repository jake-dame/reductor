package reductor;

import javax.sound.midi.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;

import static reductor.Files.MIDI_FILES_IN_DIR;


/**
 * General purpose utility class for the {@code reductor} program.
 * <p>
 * Some of this is purely for debugging or specific to my machine.
 * */
public class Util {


    private Util() { }


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
     * Helper for {@link Util#openWithGarageBand}. Checks if a file has the correct
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


    /**
     * Given a number of lists, returns a {@link javax.sound.midi.Sequence} object comprised
     * of those events.
     *
     * @param resolution The resolution for the {@link javax.sound.midi.Sequence} (caution)
     * @param lists 1 or more lists of MidiEvent objects
     * @return A {@link javax.sound.midi.Sequence} object.
     */
    static Sequence makeSequenceFromMidiEvents(int resolution, ArrayList<MidiEvent> list) {

        Sequence out;
        try {
            out = new Sequence(Sequence.PPQ, resolution, 1);
        } catch (InvalidMidiDataException e) {
            throw new RuntimeException(e);
        }

        Track track = out.getTracks()[0];

        for (MidiEvent event : list) {
            track.add( DeepCopy.copyEvent(event) );
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

        if (name.contains(".")) {
            throw new RuntimeException("file name should not contain \'.\'");
        }

        File outFile = new File(name + ".mid");

        assert sequence.getTracks().length > 0;

        int fileType;
        if (sequence.getTracks().length == 1) {
            fileType = 0;
        } else {
            fileType = 1;
        }

        try {
            MidiSystem.write(sequence, fileType, outFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        assert outFile.exists();

        return outFile;

    }


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

            String pitchStr;
            if (command == ShortMessage.NOTE_ON || command == ShortMessage.NOTE_OFF) {
                pitchStr = reductor.Pitch.toStr(message.getData1(), null, true);
            } else {
                pitchStr = "n/a";
            }

            System.out.printf("\t%-5d\t%-5s\t%s \n",
                    event.getTick(),
                    col_tab,
                    pitchStr
            );
        }

    }


    static void printNotesSimple(Sequence sequence) {

        for (Track track : sequence.getTracks()) {
            for (int i = 0; i < track.size(); i++) {
                MidiEvent event = track.get(i);
                MidiMessage message = event.getMessage();

                if (message instanceof ShortMessage shortMessage) {

                    if (shortMessage.getCommand() == ShortMessage.NOTE_ON  &&  shortMessage.getData2() > 0) {
                        // DON'T DELETE
                    } else if (shortMessage.getCommand() == ShortMessage.NOTE_OFF
                            || (shortMessage.getCommand() == ShortMessage.NOTE_ON  &&  shortMessage.getData2() == 0) ) {
                        System.out.print("\t");
                    } else {
                        continue;
                    }

                    System.out.println(event.getTick() + " (" + Pitch.pitchesItoS.get(shortMessage.getData1()) + ")");

                }


            }
        }

    }


    static void printBytes(Sequence seq){
        System.out.println();
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
        System.out.println();
    }


    /// Prints bytes in hex form; each message type is on a new line and note on/off are in columns
    static void printBytesByMessageType(String filePath) throws IOException {

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


}
