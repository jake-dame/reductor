package com.example.reductor;

import javax.sound.midi.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

// Sort of a wrapper and quasi-utility class that will change over time
public class MidiFile {

    // This is mostly just to organize label I/O in an organized way
    private final String name_m;

    // This is helpful for seeing file header and other stuff, which java.midi library throws away
    private final byte[] data;

    private final Sequence sequence_m;
    private final Track[] tracks_m;

    final ArrayList<MidiEvent> metaEvents_m;
    final ArrayList<MidiEvent> noteEvents_m;

    // ShortMessages can be any message whose data bytes is <= 2. These are non-note ShortMessages
    // Mostly control change and program change messages; they will be largely ignored
    final ArrayList<MidiEvent> otherShortEvents_m;

    // Shouldn't ever get these, but jic
    final ArrayList<MidiEvent> sysexEvents_m;

    MidiFile(String filePath) throws IOException, InvalidMidiDataException {

        Path filePath_m = Path.of(filePath);
        name_m = filePath.substring(filePath.lastIndexOf('/') + 1).split("\\.")[0];

        data = Files.readAllBytes(filePath_m);

        sequence_m = MidiSystem.getSequence( new File(filePath) );
        tracks_m = sequence_m.getTracks();

        metaEvents_m = new ArrayList<>();
        noteEvents_m = new ArrayList<>();
        otherShortEvents_m = new ArrayList<>();
        sysexEvents_m = new ArrayList<>();

        sortEventsIntoLists();
    }

    private void sortEventsIntoLists() {

        for (int i = 0; i < sequence_m.getTracks().length; i++) {
            Track track = tracks_m[i];

            for (int j = 0; j < track.size(); j++) {
                MidiEvent event = track.get(j);

                // There is only one type of event object in the java midi library, so we
                // distinguish by the event's message type instead.
                switch (event.getMessage()) {

                    case ShortMessage shortMessage -> {

                        // Sort ShortMessages by "note events" and "non-note events"
                        int status = shortMessage.getStatus();
                        if (status >= 0x80 && status <= 0x9F) {
                            noteEvents_m.add(event);
                        } else {
                            otherShortEvents_m.add(event);
                        }

                    }

                    case MetaMessage ignored -> metaEvents_m.add(event);

                    case SysexMessage ignored -> sysexEvents_m.add(event);

                    default -> throw new IllegalArgumentException("Unknown event type: " + event.getMessage());

                }
            }
        }

    }

    // NoteTree will need this
    public ArrayList<MidiEvent> getNoteEvents() {
        return new ArrayList<>(noteEvents_m);
    }

    public String getName() {
        return name_m;
    }


    // Add all events from a multiple-track MidiFile Sequence to a single-track
    // Along the way:
    // - Change all messages to be directed at Channel 1 (0), if applicable
    // - Change all "NOTE ON + velocity 0" events to proper NOTE OFF events
    // - Remove all obsolete track info; controller change messages
    //
    // After writing a new file, returns the MidiFile object based on that file.
    public static MidiFile aggregate(MidiFile midiFile) throws InvalidMidiDataException, IOException {

        Sequence sequence = midiFile.getSequence();
        Track[] tracks = sequence.getTracks();

        Sequence newSequence = new Sequence(sequence.getDivisionType(), sequence.getResolution());
        Track newTrack = newSequence.createTrack();

        for (int i = 0; i < tracks.length; i++) {
            Track track = tracks[i];

            for (int j = 1; j < track.size(); j++) {
                MidiEvent newEvent = track.get(j);
                MidiMessage msg = newEvent.getMessage();

                if (msg instanceof ShortMessage shortMessage) {
                    int cmd = shortMessage.getCommand();

                    int data1 = shortMessage.getData1();
                    int data2 = shortMessage.getData2();

                    if (cmd >= 0x90 && cmd <= 0x9F) {
                        if (data2 > 0) {
                            shortMessage.setMessage(0x90, data1, 64);
                        }
                        else {
                            shortMessage.setMessage(0x80, data1, 0);
                        }
                    }
                    else if (cmd >= 0x80 && cmd <= 0x8F) {
                        shortMessage.setMessage(0x80, data1, 0);
                    }
                    else if (cmd >= 0xC0 && cmd <= 0xCF) {
                        // second byte is ignored but required for constructor
                        shortMessage.setMessage(0xC0, 0, -1);
                    }
                    // Avoid adding control change stuff to aggregate
                    else if (cmd >= 0xB0 && cmd <= 0xBF) {
                        continue;
                    }
                }

                // Avoid adding meta info from tracks that no longer exist
                if (i != 0 && msg instanceof MetaMessage) {
                    continue;
                }

                newTrack.add(newEvent);
            }
        }

        File newFile = new File("midis/" + midiFile.getName() + "_aggregate.mid");

        // 0 is Standard Midi File (SMF) type for "single track" sequence
        MidiSystem.write(newSequence, 0, newFile);

        return new MidiFile(newFile.getPath());
    }

    // Plays this MidiFile object's sequence
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

    // Convert meta events from bytes to English and print in user-friendly way
    public void printMetaEvents() {

        for (MidiEvent event : metaEvents_m) {
            MetaMessage message = (MetaMessage) event.getMessage();
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
                        dataString = data[0] + " (usecs per quarter note) ";
                        break;

                    case 0x58:
                        type_label = "Time signature";
                        stringArr = new String[data.length - 1];
                        stringArr[0] = data[0] + "/" + ((int) (Math.pow(2, data[1]))) + ", ";
                        stringArr[1]= data[2] + " (midi clock ticks / beat), ";
                        stringArr[2] = data[3] + " (32nd notes / beat), ";
                        dataString = String.join("", stringArr);
                        break;

                    case 0x59:
                        type_label = "Key signature";
                        labelKeySignature(data, stringArr);
                        dataString = String.join(" ", stringArr);
                        break;

                    default: throw new IllegalArgumentException("Unknown metamessage: " + type);
                }
            }

            System.out.println(type_label + ": " + dataString);
        }
    }

    // Helper for printMetaEvents()
    private void labelKeySignature (byte[] data, String[] stringArr) {

        byte key = data[0];
        byte mode = data[1];

        final int MAJOR = 0;
        final int MINOR = 1;

        switch(mode) {

            case MAJOR -> {
                stringArr[1] = "Major";
                switch(key) {
                    case -7 -> stringArr[0] = "Cb";
                    case -6 -> stringArr[0] = "Gb";
                    case -5 -> stringArr[0] = "Db";
                    case -4 -> stringArr[0] = "Ab";
                    case -3 -> stringArr[0] = "Eb";
                    case -2 -> stringArr[0] = "Bb";
                    case -1 -> stringArr[0] = "F";
                    case 0 -> stringArr[0] = "C";
                    case 1 -> stringArr[0] = "G";
                    case 2 -> stringArr[0] = "D";
                    case 3 -> stringArr[0] = "A";
                    case 4 -> stringArr[0] = "E";
                    case 5 -> stringArr[0] = "B";
                    case 6 -> stringArr[0] = "F#";
                    case 7 -> stringArr[0] = "C#";
                    default -> stringArr[0] = "?";
                }
            }

            case MINOR -> {
                stringArr[1] = "minor";
                switch(key) {
                    case -7 -> stringArr[0] = "Ab";
                    case -6 -> stringArr[0] = "Eb";
                    case -5 -> stringArr[0] = "Bb";
                    case -4 -> stringArr[0] = "F";
                    case -3 -> stringArr[0] = "C";
                    case -2 -> stringArr[0] = "G";
                    case -1 -> stringArr[0] = "D";
                    case 0 -> stringArr[0] = "A";
                    case 1 -> stringArr[0] = "E";
                    case 2 -> stringArr[0] = "B";
                    case 3 -> stringArr[0] = "F#";
                    case 4 -> stringArr[0] = "C#";
                    case 5 -> stringArr[0] = "G#";
                    case 6 -> stringArr[0] = "D#";
                    case 7 -> stringArr[0] = "A#";
                    default -> stringArr[0] = "?";
                }
            }

            default -> stringArr[1] = "?";
        }
    }

    // Convert note events from bytes to English and print in user-friendly way
    public void printNoteEvents() {

        int currChannel = -1;
        for (MidiEvent event : noteEvents_m) {
            ShortMessage message = (ShortMessage) event.getMessage();
            int channel = message.getChannel();
            int command = message.getCommand();
            int pitch = message.getData1();
            int velocity = message.getData2();

            // Print NOTE ON's in one column and NOTE OFF's in another
            String command_str = "";
            if (command >= 0x90 && command <= 0x9F) {
                if (velocity > 0) {
                    command_str = "";
                } else {
                    command_str = "\t";
                }
            } else if (command >= 0x80 && command <= 0x8F) {
                command_str = "\t";
            }

            // Assign alphanumeric (pitch + register) for note values
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

            // Only print channel header once per Track
            if (currChannel != channel) {
                currChannel = channel;
                System.out.println("===CHANNEL " + currChannel + "===");
            }

            System.out.printf("\t%-5d\t%-5s\t%s\n", event.getTick(), command_str, pitch_str);
        }
    }

    // debugging stuff, not permanent
    public void printBytes() {
        int ctr = 0;
        for (byte b : data) {

            int num = b & 0xFF;

            if (num == 0xFF) {
                System.out.print("\n\n");
                ctr = 0;
            }

            if (num >= 0x80 && num <= 0x9F) {
                System.out.print("\n");
                ctr = 0;
            }

            System.out.print(Integer.toHexString(num) + " ");

            ctr++;
            if (ctr > 50) {
                System.out.print('\n');
                ctr = 0;
            }
        }
    }

    // this isn't permanent
    public Sequence getSequence() {
        return sequence_m;
    }

}