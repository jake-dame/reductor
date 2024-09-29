package reduc;

import javax.sound.midi.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;

/*
    A helper class for development, not much else.
*/
class MidiUtility {

    private final static HashMap<Integer, String> mapPitches;
    private final static HashMap<Integer, String> mapMajor;
    private final static HashMap<Integer, String> mapMinor;

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

    static String getNote(int val) { return mapPitches.get(val); }

    static String getNote(byte val) { return mapPitches.get(val & 0xFF); }

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
                    MidiUtility.getNote(pitch)
            );
        }
    }

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
                        dataString = res + " (usecs per quarter note) ";
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
                        dataString = MidiUtility.getKeySignature(data);
                        break;

                    default: throw new IllegalArgumentException("Unknown metamessage: " + type);
                }
            }

            System.out.println(type_label + ": " + dataString);
        }
    }

    static void play(Sequence sequence) throws MidiUnavailableException, InvalidMidiDataException {

        Sequencer sequencer = MidiSystem.getSequencer();
        sequencer.setSequence(sequence);
        sequencer.open();
        sequencer.start();

        while (true) {
            if (!sequencer.isRunning()) {
                sequencer.close();
                return;
            }
        }

    }

    static void write(Sequence sequence, String name) throws IOException {

        int fileType = 0;

        if (sequence.getTracks().length > 1) {
            fileType = 1;
        }

        MidiSystem.write(sequence, fileType, new File("midis/" + name + "_OUT.mid") );
    }

    // this is not a pretty printout, use w caution
    static void printBytes(String filePath) throws IOException {

        byte[] data = Files.readAllBytes( Path.of(filePath) );

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

    static void printSequence(Sequence seq){
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
                System.out.println("");
            }
        }
        System.out.println("\n===================================================");
    }

}