package com.example.reductor;

import javax.sound.midi.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

/*
 This is mostly just a wrapper class catered to this project's needs
 Also holds the aggregate() function
*/
public class MidiFile {

    // This is just to label I/O in an organized way
    private final String name_m;

    private final Sequence sequence_m;
    private final Track[] tracks_m;

    /*
     metaEvents: events that hold MetaMessages
     noteEvents: events that hold ShortMessages (NOTE ON/OFF only)
     otherShortEvents: events that hold ShortMessages (everything else)
     sysexEvents: events that hold SysexMessages (shouldn't ever see these but jic)
    */
    private final ArrayList<MidiEvent> metaEvents_m;
    private final ArrayList<MidiEvent> noteEvents_m;
    private final ArrayList<MidiEvent> otherShortEvents_m;
    private final ArrayList<MidiEvent> sysexEvents_m;

    // Mostly for debugging short midi files, seeing header, etc.
    private final byte[] data;

    MidiFile(String filePath) throws IOException, InvalidMidiDataException {

        name_m = filePath.substring(filePath.lastIndexOf('/') + 1).split("\\.")[0];

        data = Files.readAllBytes( Path.of(filePath) );

        sequence_m = MidiSystem.getSequence( new File(filePath) );
        tracks_m = sequence_m.getTracks();

        metaEvents_m = new ArrayList<>();
        noteEvents_m = new ArrayList<>();
        otherShortEvents_m = new ArrayList<>();
        sysexEvents_m = new ArrayList<>();

        sortEventsIntoLists();
    }

    private void sortEventsIntoLists() {

        for (Track track : tracks_m) {
            for (int j = 0; j < track.size(); j++) {
                MidiEvent event = track.get(j);

                // There is only one type of event object in the java midi library, so we
                // distinguish by the event's message type instead.
                switch (event.getMessage()) {
                    case ShortMessage shortMessage -> {
                        int cmd = shortMessage.getCommand();
                        if (cmd == ShortMessage.NOTE_ON || cmd == ShortMessage.NOTE_OFF) {
                            noteEvents_m.add(event);
                        } else {
                            otherShortEvents_m.add(event);
                        }
                    }
                    case MetaMessage ignored -> metaEvents_m.add(event);
                    case SysexMessage ignored -> sysexEvents_m.add(event);
                    default -> throw new IllegalArgumentException(
                            "unknown event type: " + event.getMessage());
                }

            }
        }

    }

    /*
     Add all events from a multiple-track MidiFile Sequence to a single-track
     Along the way:
     - Change all messages to be directed at Channel 1 (0), if applicable
     - Change all "NOTE ON + velocity 0" events to proper NOTE OFF events
     - Remove all obsolete track info; controller change messages

     After writing a new file, returns the MidiFile object based on that file.
    */
    public static MidiFile aggregate(MidiFile midiFile) throws InvalidMidiDataException, IOException {

        Sequence seqIn = midiFile.getSequence();
        Track[] tracks = seqIn.getTracks();
        Sequence seqOut = new Sequence(seqIn.getDivisionType(), seqIn.getResolution());
        Track trackOut = seqOut.createTrack();

        for (int i = 0; i < tracks.length; i++) {
            for (int j = 1; j < tracks[i].size(); j++) {

                MidiEvent newEvent = tracks[i].get(j);
                MidiMessage msg = newEvent.getMessage();

                // Avoid adding meta info needed by tracks that no longer exist
                if (i > 0 && msg instanceof MetaMessage) {
                    continue;
                }

                if (msg instanceof ShortMessage shortMessage) {
                    // Avoid adding control change stuff to aggregate
                    if (shortMessage.getCommand() == 0xB0) {
                        continue;
                    } else {
                        sanitizeShortMessage(shortMessage);
                    }
                }

                trackOut.add(newEvent);
            }
        }

        File newFile = new File("midis/" + midiFile.getName() + "_aggregate.mid");
        MidiSystem.write(seqOut, 0, newFile);
        return new MidiFile(newFile.getPath());
    }

    // Change all message commands to channel 1 (0) + some additional cleaning
    private static void sanitizeShortMessage(ShortMessage shortMessage) throws InvalidMidiDataException {

        int cmd = shortMessage.getCommand();
        int pitch = shortMessage.getData1();
        int velocity = shortMessage.getData2();

        if (cmd == 0x80 || (cmd == 0x90 && velocity == 0)) {
            // Set "NOTE ON, velocity = 0" scheme to proper NOTE OFF message
            shortMessage.setMessage(0x80, pitch, 0);
        } else if (cmd == 0x90) {
            // Set all ON velocities to 64, for uniformity
            shortMessage.setMessage(0x90, pitch, 64);
        } else if (cmd == 0xC0) {
            // This changes the instrument to "Acoustic Grand Piano" (0 is the mapping for that)
            // (second byte is ignored but required for constructor)
            shortMessage.setMessage(0xC0, 0, -1);
        }

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

    public ArrayList<MidiEvent> getNoteEvents() { return new ArrayList<>(noteEvents_m); }

    public ArrayList<MidiEvent> getMetaEvents() { return new ArrayList<>(metaEvents_m); }

    public ArrayList<MidiEvent> getOtherShortEvents() { return new ArrayList<>(otherShortEvents_m); }

    public ArrayList<MidiEvent> getSysexEvents() { return new ArrayList<>(sysexEvents_m); }

    public String getName() { return name_m; }

    // ========== Non-permanent stuff below ============================

    public Sequence getSequence() { return sequence_m; }

    public void printBytes() {

        int ctr = 0;
        for (byte b : data)
        {
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

}