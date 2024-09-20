package com.example.reductor;

import javax.sound.midi.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;

import static javax.sound.midi.Sequence.*;

public class MidiFile
{
    private final static int MEZZO_FORTE = 64;

    // FILE STUFF
    final Path filePath_m;
    final File fileObject_m;
    final String fileName_m;

    // DATA STUFF
    final byte[] rawBytes_m;

    // SEQUENCE STUFF
    final Sequence sequence_m;
    final Track[] tracks_m;

    // AGGREGATE STUFF
    Sequence aggregate_m;

    // TOOL STUFF
    ArrayList<MidiEvent> metaEvents_m;
    ArrayList<MidiEvent> noteEvents_m;
    ArrayList<MidiEvent> sysexEvents_m;

    MidiFile(String filePath) throws IOException, InvalidMidiDataException {

        filePath_m = Path.of(filePath);
        fileObject_m = new File(filePath_m.toString());
        fileName_m = filePath.substring(filePath.lastIndexOf('/') + 1);

        rawBytes_m = Files.readAllBytes(filePath_m);

        sequence_m = MidiSystem.getSequence(fileObject_m);
        tracks_m = sequence_m.getTracks();

        aggregate_m = aggregate(sequence_m);

        metaEvents_m = new ArrayList<>();
        noteEvents_m = new ArrayList<>();
        sysexEvents_m = new ArrayList<>();

        sortEvents();
    }

    private void sortEvents() {
        for (int i = 0; i < sequence_m.getTracks().length; i++) {
            Track track = tracks_m[i];
            for (int j = 0; j < track.size(); j++) {
                var event = track.get(j);
                // There is only one event object in the library, so we
                // distinguish by the event's message type instead.
                switch (event.getMessage()) {
                    case MetaMessage metaMessage -> metaEvents_m.add(event);
                    case ShortMessage shortMessage -> {
                        int command = shortMessage.getCommand();
                        if (command == ShortMessage.NOTE_ON || command == ShortMessage.NOTE_OFF) {
                            noteEvents_m.add(event);
                        }
                    }
                    case SysexMessage sysexMessage -> sysexEvents_m.add(event);
                    case null, default -> throw new IllegalArgumentException(
                            "Unknown event type: " + event.getMessage()
                    );
                }
            }
        }
    }

    public void printMetaEvents() {

        System.out.print("\n================META MESSAGES===============");
        for (MidiEvent event : metaEvents_m) {

            MetaMessage message = (MetaMessage) event.getMessage();
            if (message == null) {
                throw new IllegalArgumentException("not a meta message");
            }

            int type = message.getType();
            byte[] data = message.getData();

            String type_str = "";
            String[] data_str = new String[data.length];
            String text_str = "";

            if (type >= 0x20 && type < 0x2F) {
                type_str = "[Channel prefix] ";
                text_str = String.valueOf(data[0]);
            } else {
                switch (type) {
                    case 0x01:
                        type_str = "[Text] ";
                        for (int i = 0; i < data.length; i++) {
                            data_str[i] = String.valueOf((char) data[i]);
                        }
                        text_str = String.join("", data_str);
                        break;
                    case 0x03:
                        type_str = "\n[Track name] ";
                        for (int i = 0; i < data.length; i++) {
                            data_str[i] = String.valueOf((char) data[i]);
                        }
                        text_str = String.join("", data_str);
                        break;
                    case 0x2F:
                        type_str = "[End of track] ";
                        break;
                    case 0x51:
                        type_str = "[Set tempo] ";
                        text_str = "(usecs per quarter note) " + data[0];
                        break;
                    case 0x58:
                        type_str = "[Time signature] ";
                        data_str = new String[data.length - 1];
                        data_str[0] = data[0] + "/" + ((int) (Math.pow(2, data[1])));
                        data_str[1]= ", (Midi clock ticks per beat) " + data[2];
                        data_str[2] = ", (32nd notes per beat) " + data[3];
                        text_str = String.join("", data_str);
                        break;
                    case 0x59:
                        type_str = "[Key signature] ";
                        switch(data[1]) {
                            case 0 -> {
                                data_str[1] = "Major";
                                switch(data[0]) {
                                    case -7 -> data_str[0] = "Cb";
                                    case -6 -> data_str[0] = "Gb";
                                    case -5 -> data_str[0] = "Db";
                                    case -4 -> data_str[0] = "Ab";
                                    case -3 -> data_str[0] = "Eb";
                                    case -2 -> data_str[0] = "Bb";
                                    case -1 -> data_str[0] = "F";
                                    case 0 -> data_str[0] = "C";
                                    case 1 -> data_str[0] = "G";
                                    case 2 -> data_str[0] = "D";
                                    case 3 -> data_str[0] = "A";
                                    case 4 -> data_str[0] = "E";
                                    case 5 -> data_str[0] = "B";
                                    case 6 -> data_str[0] = "F#";
                                    case 7 -> data_str[0] = "C#";
                                    default -> data_str[0] = "?";
                                }
                            }
                            case 1 -> {
                                data_str[1] = "minor";
                                switch(data[0]) {
                                    case -7 -> data_str[0] = "Ab";
                                    case -6 -> data_str[0] = "Eb";
                                    case -5 -> data_str[0] = "Bb";
                                    case -4 -> data_str[0] = "F";
                                    case -3 -> data_str[0] = "C";
                                    case -2 -> data_str[0] = "G";
                                    case -1 -> data_str[0] = "D";
                                    case 0 -> data_str[0] = "A";
                                    case 1 -> data_str[0] = "E";
                                    case 2 -> data_str[0] = "B";
                                    case 3 -> data_str[0] = "F#";
                                    case 4 -> data_str[0] = "C#";
                                    case 5 -> data_str[0] = "G#";
                                    case 6 -> data_str[0] = "D#";
                                    case 7 -> data_str[0] = "A#";
                                    default -> data_str[0] = "?";
                                }
                            }
                            default -> data_str[1] = "?";
                        }
                        text_str = String.join(" ", data_str);
                        break;
                    default: throw new IllegalArgumentException("Unknown metamessage: " + type);
                }
            }

            System.out.println(type_str + text_str);
        }
    }

    public void printNoteEvents() throws InvalidMidiDataException {

        // Used to only print channel if it changes
        int currChannel = -1;

        for (MidiEvent event : noteEvents_m) {

            ShortMessage message = (ShortMessage) event.getMessage();
            int status = message.getStatus();
            int channel = message.getChannel();
            int command = message.getCommand();
            int pitch = message.getData1();
            int velocity = message.getData2();

            if (message.getLength() < 2 || message.getLength() > 3) {
                throw new IllegalArgumentException(
                        "Unknown shortmessage: " + Arrays.toString(message.getMessage())
                );
            }

            String command_str;
            if (command >= 0x90 && command <= 0x9F) {
                if (velocity > 0) {
                    command_str = "";
                    ((ShortMessage) event.getMessage()).setMessage(status, pitch, MEZZO_FORTE);
                } else {
                    command_str = "\t";
                    ((ShortMessage) event.getMessage()).setMessage(status, pitch, 0);
                }
            } else if (command >= 0x80 && command <= 0x8F) {
                command_str = "\t";
            } else {
                throw new IllegalArgumentException("Unknown shortmessage: " + Arrays.toString(message.getMessage()));
            }

            String pitch_str = switch (pitch % 12) {
                case 0 -> "C " + (pitch/12 - 1);
                case 1 -> "C#" + (pitch/12 - 1);
                case 2 -> "D " + (pitch/12 - 1);
                case 3 -> "D#" + (pitch/12 - 1);
                case 4 -> "E " + (pitch/12 - 1);
                case 5 -> "F " + (pitch/12 - 1);
                case 6 -> "F#" + (pitch/12 - 1);
                case 7 -> "G " + (pitch/12 - 1);
                case 8 -> "G#" + (pitch/12 - 1);
                case 9 -> "A " + (pitch/12 - 1);
                case 10 -> "A#" + (pitch/12 - 1);
                case 11 -> "B " + (pitch/12 - 1);
                default -> "?";
            };

            if (currChannel != channel) {
                currChannel = channel;
                System.out.println("===CHANNEL " + currChannel + "===");
            }

            System.out.printf("\t%-5d\t%-5s\t%s\n", event.getTick(), command_str, pitch_str);
        }

    }

    public Sequence aggregate(Sequence sequence) throws InvalidMidiDataException {

        Sequence newSequence = new Sequence(PPQ, sequence.getResolution());
        Track newTrack = newSequence.createTrack();

        for (Track track : sequence.getTracks()) {
            for (int i = 1; i < track.size(); i++) {
                MidiEvent event = track.get(i);
                newTrack.add(event);
            }
        }

        return newSequence;
    }

    public void writeOut() throws IOException {
        MidiSystem.write(sequence_m, 1, new File("midis/" + fileName_m + "_OUT.mid"));
    }

    public void play() throws MidiUnavailableException, InvalidMidiDataException {

        Sequencer sequencer = MidiSystem.getSequencer();
        sequencer.setSequence(sequence_m);
        sequencer.open();
        sequencer.start();

        while (true) {
            if (!sequencer.isRunning()) {
                sequencer.close();
                return;
            }
        }

    }

    public void printBytes() {
        int ctr = 0;
        for (byte b : rawBytes_m) {
            int intt = b & 0xFF;
            if (intt == 0xFF
                || intt >> 4 == 0b1001) {
                System.out.print("\n");
                if (intt == 0xFF) {
                    System.out.print("\n");
                }
                ctr = 0;
            }
            System.out.print(Integer.toHexString(intt) + " ");
            ctr++;
            if (ctr > 50) {
                System.out.print('\n');
                ctr = 0;
            }
        }
    }

    public void printFileInfo() throws InvalidMidiDataException, IOException
    {
        System.out.println("\n================printFileInfo()===============");
        System.out.println("FILE NAME: " + fileName_m);
        int fileType = MidiSystem.getMidiFileFormat( new File(filePath_m.toString()) ).getType();
        String fileTypeStr;
        switch (fileType) {
            case 0 -> fileTypeStr = fileType + "(single track)";
            case 1 -> fileTypeStr = fileType + "(multiple track)";
            case 2 -> fileTypeStr = fileType + "(multiple song)";
            default -> fileTypeStr = "?";
        }
        System.out.println("SMF TYPE: " + fileTypeStr);
        System.out.println("NUM TRACKS: " + tracks_m.length);
        for (int i = 0; i < tracks_m.length; i++) {
            System.out.println("\tTrack " + i + " events: " + tracks_m[i].size() );
        }

        if (rawBytes_m.length != MidiSystem.getMidiFileFormat( new File(filePath_m.toString())).getByteLength()) {
            throw new RuntimeException("disparity between raw file and midi library byte count");
        }
        System.out.println("LENGTH IN BYTES: " + rawBytes_m.length);
        System.out.println("LENGTH IN TICKS: "+ sequence_m.getTickLength());
        System.out.println("RESOLUTION: "+ sequence_m.getResolution());

        float divisionType = sequence_m.getDivisionType();
        String divTypeStr = "";
        if (divisionType == PPQ) {
            divTypeStr = "PPQ";
        } else if (divisionType == SMPTE_24) {
            divTypeStr = "SMPTE_24";
        } else if (divisionType == SMPTE_25) {
            divTypeStr = "SMPTE_25";
        } else if (divisionType == SMPTE_30DROP) {
            divTypeStr = "SMPTE_30DROP";
        } else if (divisionType == SMPTE_30) {
            divTypeStr = "SMPTE_30";
        }

        if (divisionType != PPQ) {
            System.out.println("DIVISION TYPE: "+ divTypeStr);
        }

        if ( sequence_m.getPatchList().length > 0) {
            System.out.println("PATCH LIST: "+ Arrays.toString(sequence_m.getPatchList()));
        }

        System.out.println("meta messages: " + metaEvents_m.size());
        System.out.println("short messages: " + noteEvents_m.size());
        if ( !sysexEvents_m.isEmpty() ) {
            System.out.println("sysex messages: " + sysexEvents_m.size());
        }

        //System.out.println("================DEVICES AVAILABLE===============");
        //System.out.println(Arrays.toString(MidiSystem.getMidiDeviceInfo();));
    }
}