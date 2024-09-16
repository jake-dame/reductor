package com.example.reductor;

import javax.sound.midi.*;
import java.io.*;
import java.util.Arrays;

public class MIDI {

    private final String filePath;

    private final String name;

    private final Sequence seqIn;
    private final Sequence seqAg;

    MIDI(String filePath) throws InvalidMidiDataException, IOException {

        this.filePath = filePath;

        this.name = filePath.substring(filePath.lastIndexOf("/") + 1).split("\\.")[0];

        this.seqIn = MidiSystem.getSequence( new File(filePath) );

        this.seqAg = aggregate();
    }

    public void printPerformance() throws InterruptedException
    {
        Track[] tracks = seqIn.getTracks();

        long lastTickValue = 0;
        for (Track track : tracks)
        {
            for (int i = 0; i < track.size(); i++)
            {
                // Verbose but disambiguates

                // Event == timestamped MidiMessage
                MidiEvent currentEvent = track.get(i);
                // MidiMessage is an object
                MidiMessage msgObj = currentEvent.getMessage();
                // Thus a getter is needed to get the actual data
                byte[] bytes = msgObj.getMessage();

                // Do some print- and debug-friendly processing
                // Or set me back 30 steps because I did it wrong
                String[] decodedBytes = decodeMIDIMessage(bytes);

                // These are in milliseconds, I believe, and don't totally know how
                // to work with these, yet
                long noteDuration = currentEvent.getTick() - lastTickValue;

                System.out.println(i + ": " + Arrays.toString(decodedBytes) + ", " + currentEvent.getTick());

                // Simulate how this would unfold in real-time by sleeping for note duration
                // This applies to note offs, too, though, so this is hacky solution to avoid more code
                if ( i % 2 == 0) {
                    Thread.sleep(noteDuration);
                }
            }
        }

    }

    private String[] decodeMIDIMessage(byte[] bytes)
    {
        String[] strings;

        // This is a NOTE_ON or NOTE_OFF message
        if (bytes.length == 3) {
            strings = convertHexToEnglish(bytes);
        // This is a message I don't care about but will still print jic
        } else {
            strings = new String[bytes.length];
            for (int i = 0; i < bytes.length; i++) {
                strings[i] = Integer.toString(bytes[i] & 0xFF);
            }
        }

        return strings;
    }

    private String[] convertHexToEnglish(byte[] bytes)
    {
        String[] ret = new String[3];

        int status = (int) bytes[0] & 0xFF;
        int pitch = bytes[1];
        int velocity = bytes[2];

        // Label status bytes
        if (status >= 128 && status <= 143) {
            ret[0] = "_";
        } else if (status >= 144 && status <= 159) {
            ret[0] = "ON";
        } else {
            //ret[0] = "n/a: "+Integer.toBinaryString(status);
            ret[0] = "_"; // until I figure out why these are all CONTROL_CHANGE messages instead of NOTE_OFFs
        }

        // Label pitch bytes
        switch (pitch % 12) {
            case 0: ret[1] = "C"; break;
            case 1: ret[1] = "C#"; break;
            case 2: ret[1] = "D"; break;
            case 3: ret[1] = "D#"; break;
            case 4: ret[1] = "E"; break;
            case 5: ret[1] = "F"; break;
            case 6: ret[1] = "F#"; break;
            case 7: ret[1] = "G"; break;
            case 8: ret[1] = "G#"; break;
            case 9: ret[1] = "A"; break;
            case 10: ret[1] = "A#"; break;
            case 11: ret[1] = "B"; break;
            default: ret[1] = "unknown: " + pitch;
        }

        // Label velocity bytes
        ret[2] = velocity > 63 ? "f" : "p";

        return ret;
    }

    public void write() throws IOException {
        MidiSystem.write(seqAg, 1, new File("midis/" + this.name + "_OUT.mid"));
    }

    // Given a sequence with one or more tracks, outputs a single-track sequence.
    public Sequence aggregate() throws InvalidMidiDataException {

        Track[] tracksIn = seqIn.getTracks();

        // Should probably just always match the input sequence's timing resolution
        Sequence seqOut = new Sequence(Sequence.PPQ, seqIn.getResolution());
        Track trackOut = seqOut.createTrack();

        // Get every event from every track and add it to the single output track
        for (Track track : tracksIn) {
            for (int i = 1; i < track.size(); i++) { // .size() is # of events
                MidiEvent event = track.get(i);
                trackOut.add(event);
            }
        }

        return seqOut;
    }

    public void play() throws MidiUnavailableException, InvalidMidiDataException {

        // This connects to the default synth, too
        Sequencer seqr = MidiSystem.getSequencer();

        seqr.setSequence(seqAg);

        seqr.open();
        seqr.start();

        while (true) {
            if (seqr.getTickPosition() >= seqr.getTickLength()) {
                seqr.close();
                return;
            }
        }

    }

    public void printStuff() throws InvalidMidiDataException, IOException {

        MidiFileFormat format = MidiSystem.getMidiFileFormat( new File(filePath));

        System.out.println("================SCORE OBJECT===============");
        System.out.println("NAME OF WORK: " + this.name);
        System.out.println(
                        "TIMING RESOLUTION: " + format.getResolution()
                        + "\nFILE FORMAT: " + format
                        + "\nLENGTH (bytes): " + format.getByteLength()
                        + "\nDIVISION TYPE: " + format.getDivisionType()
                        + "\nFILE TYPE: " + format.getType()
                        + "\nμsec LENGTH: " + format.getMicrosecondLength());


        MidiDevice.Info[] info = MidiSystem.getMidiDeviceInfo();
        System.out.println("================DEVICES AVAILABLE===============");
        for (var device : info) {
            System.out.println(device.toString());
        }

        System.out.println("================FILE TYPES SUPPORTED FOR WRITE===============");
        for (int fileType : MidiSystem.getMidiFileTypes()) {
            System.out.println(fileType);
        }
    }

}
