package com.example.reductor;

import java.util.Arrays;

public class Main
{
    private static final String MIDI_NAME = "simple";
    private static final String MIDI_PATH = "midis/" + MIDI_NAME + ".mid";

    public static void main(String[] args)
    {
        try
        {
            MIDI midi = new MIDI(MIDI_PATH);
            //midi.printStuff();

            for (byte b : midi.rawBytes) System.out.print(Integer.toHexString(b) + " ");
            System.out.println('\n');

            //for (String[] arr : midi.noteEvents) System.out.println(Arrays.toString(arr));

            //for (String[] arr : midi.otherEvents) System.out.println(Arrays.toString(arr));

            //midi.play();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

}