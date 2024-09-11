package com.example.reductor;

import javax.sound.midi.*;
import java.io.File;
import java.io.IOException;

public class Main
{
    public static void main(String[] args)
    {
        try
        {
            Sequence seq = new Sequence(Sequence.PPQ, 4);
            Track top = seq.createTrack();
            Track bottom = seq.createTrack();

            ShortMessage onMsg;
            ShortMessage offMsg;

            MidiEvent onEvent;
            MidiEvent offEvent;

            int note = 60;
            for (int i = 0; i < 5; i++)
            {
                onMsg = new ShortMessage();
                onMsg.setMessage(ShortMessage.NOTE_ON, note, 64);
                onEvent = new MidiEvent(onMsg, i*4);
                top.add(onEvent);

                onMsg = new ShortMessage();
                onMsg.setMessage(ShortMessage.NOTE_ON, note+4, 64);
                onEvent = new MidiEvent(onMsg, i*4);
                bottom.add(onEvent);

                offMsg = new ShortMessage();
                offMsg.setMessage(ShortMessage.NOTE_OFF, note, 0);
                offEvent = new MidiEvent(offMsg, i*4+4);
                top.add(offEvent);

                offMsg = new ShortMessage();
                offMsg.setMessage(ShortMessage.NOTE_OFF, note+4, 0);
                offEvent = new MidiEvent(offMsg, i*4+4);
                bottom.add(offEvent);

                note+=2;
            }

//            Sequence seqOut = new Sequence(Sequence.PPQ, 4);
            Track trackOut = seq.createTrack();

            for (Track track : seq.getTracks() )
            {
                for (int i = 0; i < track.size(); i++ )
                {
                    MidiEvent event = track.get(i);
//                    onMsg = new ShortMessage();
//                    onMsg.setMessage(ShortMessage.NOTE_ON, note+8, 64);
//                    event = new MidiEvent(onMsg, i*4);
                    trackOut.add(event);
                }
            }

            File fileOut = new File("custom_OUT.mid");
            MidiSystem.write(seq, 1, fileOut);
        }
        catch (InvalidMidiDataException
               | IOException e)
        {
            throw new RuntimeException(e);
        }

    }










//    public static void printStuff(MidiFileFormat format)
//    {
//        System.out.println( "\nFORMAT: " + format.toString()
//                + "\nLENGTH (bytes): " + format.getByteLength()
//                + "\nDIVISION TYPE: " + format.getDivisionType()
//                + "\nRESOLUTION: " + format.getResolution()
//                + "\nFILE TYPE: " + format.getType()
//                + "\nμsec LENGTH: " + format.getMicrosecondLength()
//                + "\nAUTHOR: " + format.getProperty("author")
//                + "\nTITLE: " + format.getProperty("title")
//                + "\nCOPYRIGHT: " + format.getProperty("copyright")
//                + "\nDATE: " + format.getProperty("date")
//                + "\nCOMMENT: " + format.getProperty("comment")
//        );
//    }

//    public static void main(String[] args)
//    {
//        File fileIn = new File( "Byrd_IN.mid" );
//        File fileOut = new File("Byrd_OUT.mid");
//
//
//        try
//        {
//
//            Sequence seqIn = MidiSystem.getSequence(fileIn);
//            Track[] tracksIn = seqIn.getTracks();
//
//            System.out.println( "resolution: " + seqIn.getResolution() );
//
//            Sequence seqOut = MidiSystem.getSequence(fileOut);
//            Track[] tracksOut = seqIn.getTracks();
//
////            for (Track track : tracksIn)
////            {
////                // for each byte in track
////                    // if byte == NOTE_ON
////                    // MidiEvent event = track.get(i);
////                    // get the event message
////                    // create new message with prev message data byte 2++
////                    // create new event
////                    // tracksOut[0].add
////            }
//
//            for (int i = 1; i < tracksIn[0].size(); i++)
//            {
//                MidiEvent event = tracksIn[0].get(i);
//                MidiMessage message = event.getMessage();
//                byte[] data = message.getMessage();
//                String byteString = Arrays.toString( data );
//                System.out.println(byteString + "\t@ " + event.getTick() );
//            }
//
//            MidiSystem.write(seqOut, MidiSystem.getMidiFileFormat(fileIn).getType(), fileOut);
//        }
//        catch (InvalidMidiDataException | IOException e)
//        {
//            throw new RuntimeException(e);
//        }
//
//    }
}