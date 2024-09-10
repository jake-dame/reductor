package com.example.reductor;

import javax.sound.midi.*;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public class Main
{
    public static void printStuff(MidiFileFormat format)
    {
        System.out.println( "\nFORMAT: " + format.toString()
                + "\nLENGTH (bytes): " + format.getByteLength()
                + "\nDIVISION TYPE: " + format.getDivisionType()
                + "\nRESOLUTION: " + format.getResolution()
                + "\nFILE TYPE: " + format.getType()
                + "\nμsec LENGTH: " + format.getMicrosecondLength()
                + "\nAUTHOR: " + format.getProperty("author")
                + "\nTITLE: " + format.getProperty("title")
                + "\nCOPYRIGHT: " + format.getProperty("copyright")
                + "\nDATE: " + format.getProperty("date")
                + "\nCOMMENT: " + format.getProperty("comment")
        );
    }

    public static void main(String[] args)
    {
        // oracle doc said use an InputStream but didn't say to NOT use a File
        // and this seems to work for now...
        File fileIn = new File("Byrd_IN.mid");
        File fileOut = new File("Byrd_OUT.mid");
        try
        {
            // just seeing what kind of data MidiFileFormat objects have
            // I also needed to know what "fileType" for the write() later
            // Still don't know what it means but apparently it is "1"
            MidiFileFormat format = MidiSystem.getMidiFileFormat(fileIn);
            printStuff(format);

            // Sequences are "made of" tracks
            Sequence sequence = MidiSystem.getSequence(fileIn);

            // 4 tracks: 0 and 5 are garbage; 1-4 are SATB
            Track[] tracks = sequence.getTracks();
            System.out.println("num tracks: " + tracks.length);

            Track soprano = tracks[1];
            System.out.println( "soprano size: " + soprano.size() );
            System.out.println( "soprano ticks: " + soprano.ticks() );

            for (int i = 1; i < 11; i++)
            {
                MidiEvent event = soprano.get(i);
                MidiMessage message = event.getMessage();
                byte[] data = message.getMessage();
                String byteString = Arrays.toString( data );
                System.out.println(byteString + "\t@ " + event.getTick() );
            }

            // NOTE ON; make c#, will be very ugly sounding; mf
            ShortMessage novelMessage = new ShortMessage(0x90, 0x61, 0x7f);
            MidiEvent novelEvent = new MidiEvent( novelMessage, 0 );

//            sequence.deleteTrack( soprano );
            Track newTrack = sequence.createTrack();
            newTrack.add(novelEvent);

            MidiSystem.write(sequence, 1, fileOut);
        }
        catch (InvalidMidiDataException | IOException e)
        {
            throw new RuntimeException(e);
        }

    }
}