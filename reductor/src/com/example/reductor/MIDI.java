package com.example.reductor;

import javax.sound.midi.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;

public class MIDI
{
    // FILE STUFF
    final Path filePath;
    final File file;
    final String name;
    final byte[] rawBytes;

    final MidiFileFormat fileFormat;
    final int fileType;

    // SEQUENCE STUFF
    final Sequence sequence;
    final Track[] tracks;
    final int numTracks;
    final int resolution;

    int totalBytes;
    int totalEvents;
    int totalNotesOn;
    int totalNotesOff;

    // AGGREGATE STUFF
    Sequence aggregate;

    // TOOL STUFF
    ArrayList<String[]> allEvents;
    ArrayList<String[]> otherEvents;
    ArrayList<String[]> noteEvents;

    MIDI(String filePath)
    {
        this.filePath = Path.of(filePath); // midis/some_midi.mid
        this.file = new File(this.filePath.toString());

        int fileNameIndex = filePath.lastIndexOf('/') + 1;
        String fileName = filePath.substring(fileNameIndex);
        this.name = fileName.split("\\.")[0];

        try {
            this.rawBytes = Files.readAllBytes(this.filePath);
            this.sequence = MidiSystem.getSequence(this.file);
            this.fileFormat = MidiSystem.getMidiFileFormat(this.file);
        } catch (IOException | InvalidMidiDataException e) {
            throw new RuntimeException(e);
        }

        this.tracks = this.sequence.getTracks();
        this.numTracks = this.tracks.length;
        this.resolution = this.sequence.getResolution();
        this.fileType = this.fileFormat.getType();

        this.allEvents = new ArrayList<>();
        this.noteEvents = new ArrayList<>();
        this.otherEvents = new ArrayList<>();

        analyze();
        aggregate();
    }

    private void analyze()
    {
        for (Track track : this.tracks)
        {
            for (int i = 0; i < track.size(); i++)
            {
                MidiEvent event = track.get(i);
                System.out.println("i: " + i + ", event: " + event);
                var message = event.getMessage();
                System.out.println(message.getClass() + ": " + Arrays.toString(message.getMessage()) + "\n");

                //translateAndAddToList(message);

                //this.totalBytes += message.getLength();
            }
        }

        this.totalEvents = this.totalNotesOn + this.totalNotesOff;
    }

    private void translateAndAddToList(MidiMessage message)
    {
        byte[] data = message.getMessage();

        int status = message.getStatus();

        //if (status < 0x80 || status > 0x9F) {
        if (status < 0x80 || status > 0x9F && status != 176) {
            addOtherEvent(data);
        } else {
            addNoteEvent(data);
        }

    }

    private void addOtherEvent(byte[] data)
    {
        String[] res = new String[data.length];

        for (int j = 0; j < data.length; j++) {
            res[j] = String.valueOf(data[j] & 0xFF);
            //res[j] = String.valueOf(data[j]);
            //res[j] = Integer.toBinaryString(data[j]);
        }

        this.otherEvents.add(res);
    } // 0

    private void addNoteEvent(byte[] data)
    {
        if (data.length != 3) {
            throw new IllegalArgumentException("note event of not length 3");
        }

        String[] res = new String[3];

        int status = (int) data[0] & 0xFF;
        //if (status < 0x90) {
        if (status == 176) {
            res[0] = "off";
            this.totalNotesOff++;
        } else {
            res[0] = "ON";
            this.totalNotesOn++;
        }

        int pitch = (int) data[1] & 0xFF;
        switch (pitch % 12) {
            case 0: res[1] = "C"; break;
            case 1: res[1] = "C#"; break;
            case 2: res[1] = "D"; break;
            case 3: res[1] = "D#"; break;
            case 4: res[1] = "E"; break;
            case 5: res[1] = "F"; break;
            case 6: res[1] = "F#"; break;
            case 7: res[1] = "G"; break;
            case 8: res[1] = "G#"; break;
            case 9: res[1] = "A"; break;
            case 10: res[1] = "A#"; break;
            case 11: res[1] = "B"; break;
            default: res[1] = "unknown: " + pitch;
        }

        int velocity = (int) data[2] & 0xFF;
        //res[2] = velocity > 63 ? "f" : "p";
        res[2] = String.valueOf(velocity);

        this.noteEvents.add(res);
    }

    public void aggregate()
    {
        try {
            this.aggregate = new Sequence(Sequence.PPQ, this.resolution);
        } catch (InvalidMidiDataException e) {
            throw new RuntimeException(e);
        }

        Track singleTrack = this.aggregate.createTrack();

        // Get every event from every track and add it to the single output track
        for (Track track : this.tracks) {
            for (int i = 1; i < track.size(); i++) {
                MidiEvent event = track.get(i);
                singleTrack.add(event);
            }
        }

    }

    public void write() throws IOException
    {
        MidiSystem.write(this.sequence, 1, new File("midis/" + this.name + "_OUT.mid"));
    }

    public void play()
    {
        Sequencer seqr;
        try {
            // This connects to the default synth, too
            seqr = MidiSystem.getSequencer();
            seqr.setSequence(this.sequence);
            seqr.open();
        }
        catch (MidiUnavailableException | InvalidMidiDataException e) {
            throw new RuntimeException(e);
        }

        seqr.start();

        while (true) {
            if (!seqr.isRunning()) {
                seqr.close();
                return;
            }
        }

    }

    public void printStuff() throws InvalidMidiDataException, IOException
    {
        System.out.println("================REDUCTOR===============");
        System.out.println("NAME OF WORK: " + this.name);
        System.out.println("LENGTH IN BYTES (Raw): " + this.rawBytes.length);

        System.out.println("================SEQUENCE===============");
        System.out.println("RESOLUTION: "+ this.sequence.getResolution());
        System.out.println("DIVISION TYPE: "+ this.sequence.getDivisionType());
        //System.out.println("uSEC LENGTH: "+ this.sequence.getMicrosecondLength());
        System.out.println("TICK LENGTH: "+ this.sequence.getTickLength());
        //System.out.println("PATCH LIST: "+ Arrays.toString(this.sequence.getPatchList()));

        MidiFileFormat format = MidiSystem.getMidiFileFormat( new File(this.filePath.toString()) );
        System.out.println("================MIDIFILEFORMAT===============");
        System.out.println("LENGTH IN BYTES (MidiFileFormat): " + format.getByteLength());

        //MidiDevice.Info[] info = MidiSystem.getMidiDeviceInfo();
        //System.out.println("================DEVICES AVAILABLE===============");
        //System.out.println(Arrays.toString(info));
        //
        //System.out.println("================FILE TYPES SUPPORTED FOR WRITE===============");
        //System.out.println( Arrays.toString( MidiSystem.getMidiFileTypes() ) );
    }
}