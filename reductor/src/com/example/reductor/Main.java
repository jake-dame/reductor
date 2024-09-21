package com.example.reductor;

public class Main {

    private static final String SCALE = "midis/scale.mid";
    private static final String MINUET_SB = "midis/minuet_SB.mid";
    private static final String MINUET_PIANO = "midis/minuet_piano.mid";
    private static final String MINUET_SB_NoRPNs = "midis/minuet_SB_noRPNs.mid";
    private static final String MOZART = "midis/mozart_550.mid";

    public static void main(String[] args) {

        try {

            MidiFile midi = new MidiFile(MOZART);
            var newMidi = MidiFile.aggregate(midi);

            NoteTree tree = new NoteTree(newMidi.getNoteEvents());
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }

}