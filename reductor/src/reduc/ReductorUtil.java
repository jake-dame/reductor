package reduc;

import javax.sound.midi.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;

/** Utility class. Some of this will stay, some of this will be development-specific and go */
public class ReductorUtil {

    public static final String MIDIS_DIR = "/Users/u0858882/Desktop/Capstone/reductor/midis/";
    public static final String PROJECT_DIR = "/Users/u0858882/Desktop/Capstone/reductor/";

    public static final int MESSAGE_TYPE_TEXT = 0x01;
    public static final int MESSAGE_TYPE_TRACK_NAME = 0x03;
    public static final int MESSAGE_TYPE_CHANNEL_PREFIX = 0x20;
    public static final int MESSAGE_TYPE_END_OF_TRACK = 0x2F;
    public static final int MESSAGE_TYPE_SET_TEMPO = 0x51;
    public static final int MESSAGE_TYPE_TIME_SIGNATURE = 0x58;
    public static final int MESSAGE_TYPE_KEY_SIGNATURE = 0x59;

    public static final int MESSAGE_TYPE_NOTE_OFF = 0x80;
    public static final int MESSAGE_TYPE_NOTE_ON = 0x90;

    private static final HashMap<Integer, String> mapPitches;
    private static final HashMap<Integer, String> mapMajor;
    private static final HashMap<Integer, String> mapMinor;

    static {
        mapPitches = new HashMap<>();
        for (int num = 0; num < 128; num++) {
            String space = "";
            String pitch = switch (num % 12) {
                case 0 -> "C" + space + (num/12 - 1);
                case 1 -> "C#" + space + (num/12 - 1);
                case 2 -> "D" + space + (num/12 - 1);
                case 3 -> "D#" + space + (num/12 - 1);
                case 4 -> "E" + space + (num/12 - 1);
                case 5 -> "F" + space + (num/12 - 1);
                case 6 -> "F#" + space + (num/12 - 1);
                case 7 -> "G" + space + (num/12 - 1);
                case 8 -> "G#" + space + (num/12 - 1);
                case 9 -> "A" + space + (num/12 - 1);
                case 10 -> "A#" + space + (num/12 - 1);
                case 11 -> "B" + space + (num/12 - 1);
                default -> "";
            };
            mapPitches.put(num, pitch);
        }

        mapMajor = new HashMap<>();
        for (int num = -7; num < 8; num++) {
            String keySig = switch (num) {
                case -7 -> "Cb";
                case -6 -> "Gb";
                case -5 -> "Db";
                case -4 -> "Ab";
                case -3 -> "Eb";
                case -2 -> "Bb";
                case -1 -> "F";
                case 0 -> "C";
                case 1 -> "G";
                case 2 -> "D";
                case 3 -> "A";
                case 4 -> "E";
                case 5 -> "B";
                case 6 -> "F#";
                case 7 -> "C#";
                default -> "?";
            };
            mapMajor.put(num, keySig);
        }

        mapMinor = new HashMap<>();
        for (int num = -7; num < 8; num++) {
            String keySig = switch (num) {
                case -7 -> "Ab";
                case -6 -> "Eb";
                case -5 -> "Bb";
                case -4 -> "F";
                case -3 -> "C";
                case -2 -> "G";
                case -1 -> "D";
                case 0 -> "A";
                case 1 -> "E";
                case 2 -> "B";
                case 3 -> "F#";
                case 4 -> "C#";
                case 5 -> "G#";
                case 6 -> "D#";
                case 7 -> "A#";
                default -> "?";
            };
            mapMinor.put(num, keySig);
        }

    }

    public static String getNote(int val) { return mapPitches.get(val); }

    public static String getNote(byte val) { return mapPitches.get(val & 0xFF); }

    static String getKeySignature(byte[] bytes) {

        int accidentalCount = bytes[0];
        int mode = bytes[1];

        String str;
        if (mode == 0) {
            str = mapMajor.get(accidentalCount);
            str += " Major";
        } else {
            str = mapMinor.get(accidentalCount);
            str += " minor";
        }

        return str;
    }

    /*
     Given a bpm specification (an integer 1 <= x <= 60,000,000), converts to microseconds per quarter note,
     which is what MIDI spec uses to set tempo.
    */
    static byte[] convertBPMToMicroseconds(int bpm) {

            /*
             8,355,711 translates to 0x7F7F7F, or the highest possible value MIDI set tempo message data
             sections (which are always 3 bytes long) can accommodate (data bytes cannot go to 0xFF because
             in MIDI, the only bytes allowed to have a set MSB are status bytes (or the "Reset" SysEx message).

             The higher the microseconds-per-quarter-note, the slower the tempo.

             The reverse formula is bpm = 60,000,000 usecs-per-min / usecs-per-quarter-note

             Lowest valid is actually 7.18071747575, but int is the safer type here since dealing with division.
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

    static int convertMicrosecondsToBPM(MidiEvent setTempoEvent) {

        MetaMessage msg = (MetaMessage) setTempoEvent.getMessage();

        // this check can probably be deleted later
        if (msg.getType() != ReductorUtil.MESSAGE_TYPE_SET_TEMPO) {
            throw new IllegalArgumentException("this is not a set tempo message");
        }

        byte[] data = msg.getData();

        //int dataByte = 0;
        //long microsecondsPerQuarterNote = 0;
        //while (dataByte < data.length) {
        //    microsecondsPerQuarterNote <<= 8;
        //    microsecondsPerQuarterNote |= (data[dataByte] & 0xFF);
        //    dataByte++;
        //}

        long microsecondsPerQuarterNote = (data[0] & 0xFF);

        microsecondsPerQuarterNote <<= 8;
        microsecondsPerQuarterNote |= (data[1] & 0xFF);

        microsecondsPerQuarterNote <<= 8;
        microsecondsPerQuarterNote |= (data[2] & 0xFF);

        final int microsecondsPerMinute = 60_000_000;

        // This is fine because none of the numbers here, if valid MIDI spec, will never get remotely near INTEGER_MAX.
        return microsecondsPerMinute / (int) microsecondsPerQuarterNote;
    }

    static void openWithGarageBand(File file) {

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

    static Sequence makeSequence(ArrayList<MidiEvent>[] lists, int resolution) {

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

    public static Sequence copySequence(Sequence sequenceIn) {

        Sequence sequenceOut;
        try {
            sequenceOut = new Sequence(
                    sequenceIn.getDivisionType(),
                    sequenceIn.getResolution(),
                    sequenceIn.getTracks().length
            );
        } catch (InvalidMidiDataException e) {
            throw new RuntimeException(e);
        }

        for (int t = 0; t < sequenceIn.getTracks().length; t++) {
            Track trackIn = sequenceIn.getTracks()[t];
            Track trackOut = sequenceOut.getTracks()[t];
            for (int e = 0; e < trackIn.size(); e++){
                MidiEvent eventIn = trackIn.get(e);
                MidiEvent eventOut = copyEvent(eventIn);
                trackOut.add(eventOut);
            }
        }

        return sequenceOut;
    }

    static MidiEvent copyEvent(MidiEvent eventIn) {
        MidiMessage messageOut = copyMessage(eventIn.getMessage());
        return new MidiEvent(messageOut, eventIn.getTick());
    }

    static MidiMessage copyMessage(MidiMessage messageIn) {

        MidiMessage messageOut;

        try {

            switch (messageIn) {
                case ShortMessage shortMessage -> {
                    int command = shortMessage.getCommand();
                    int channel = shortMessage.getChannel();
                    int data1 = shortMessage.getData1();
                    int data2 = shortMessage.getData2();
                    messageOut = new ShortMessage(command, channel, data1, data2);
                }
                case MetaMessage metaMessage -> {
                    int type = metaMessage.getType();
                    byte[] data = metaMessage.getData();
                    int length = data.length;
                    messageOut = new MetaMessage(type, data.clone(), length);
                }
                case SysexMessage sysexMessage -> {
                    int status = sysexMessage.getStatus();
                    byte[] data = sysexMessage.getData();
                    int length = data.length;
                    messageOut = new SysexMessage(status, data.clone(), length);
                }
                default -> {
                    throw new InvalidMidiDataException("unknown message type: " + messageIn.getStatus());
                }
            }

        } catch (InvalidMidiDataException e) {
            throw new RuntimeException(e);
        }

        return messageOut;
    }

    static ArrayList<MidiEvent> copyEvents(ArrayList<MidiEvent> eventsIn) {

        ArrayList<MidiEvent> eventsOut = new ArrayList<>();

        for (MidiEvent event : eventsIn) {
            MidiEvent copy = copyEvent(event);
            eventsOut.add(copy);
        }

        return eventsOut;
    }

    // * EASE FUNCTIONS ***********************************************************************************************/

    protected static void play(Sequence sequence) {

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

    /*
     Returns the File object pertaining to the file that was written out.
    */
    static File write(Sequence sequence, String name) {

        // Attach a prefix if it doesn't already have one to prevent overwriting original midi
        File outFile = new File(MIDIS_DIR, "OUT_" + name);

        int underscoreIndex = name.indexOf('_');
        if (underscoreIndex != -1) {
            String prefix = name.substring(0, underscoreIndex);
            if (prefix.equals("AGG") || prefix.equals("RED")) {
                // If it already had a valid prefix, make a new one to not have "OUT_" too
                outFile = new File(MIDIS_DIR, name);
            }
        }

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

        // Check extension (this is mostly for me during development because I need to be protected from myself)
        String outFileName = outFile.getName();
        int periodIndex = outFileName.lastIndexOf('.');
        if (periodIndex == -1
                || !outFileName.substring(periodIndex + 1).equals("mid") ) {
            throw new RuntimeException("out file does not have '.mid' extension JAKE");
        }

        return outFile;
    }

    // * PRINTING STUFF ***********************************************************************************************/

    /* You must provide note events, this just parses and prints them. */
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
                    ReductorUtil.getNote(pitch)
            );
        }

    }

    /* You must provide meta events, this just parses and prints them. */
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
                        dataString = ReductorUtil.getKeySignature(data);
                        break;

                    default: throw new IllegalArgumentException("Unknown metamessage: " + type);
                }
            }

            System.out.println(type_label + ": " + dataString);
        }

    }

    /* This just prints a sequence in byte (hex) form */
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

    /* This is not a pretty printout, use w caution */
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

}
