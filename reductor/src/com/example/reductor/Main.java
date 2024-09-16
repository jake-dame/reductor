package com.example.reductor;

public class Main {

    private static final String NAME = "simple_quiet";

    private static final String PATH = "midis/" + NAME + ".mid";

    public static void main(String[] args) {

        try {

            MIDI midi = new MIDI(PATH);
            midi.printStuff();
            midi.printPerformance();
            //midi.play();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

}