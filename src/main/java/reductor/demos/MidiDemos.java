package reductor.demos;


import reductor.dev.MidiPrinter;
import reductor.midi.MidiUtil;
import reductor.midi.builder.SequenceBuilder;

import javax.sound.midi.Sequence;
import java.io.IOException;


public class MidiDemos {

    public static class OdeToJoy {

        static void main() {

            Sequence seq = SequenceBuilder.builder(480)
                    .track(tb -> tb.trackName(0, "Violin I,II")
                            .instrumentName(0, "Violin I,II")
                            .text(0, "L. v.Beethoven - Symphony No. 9 in D Minor, iv. Presto (mm.116-19)")
                            .programChange(0, 0, 0)
                            .keySignature(0, "DM")
                            .timeSignature(0, 4, 4)
                            .tempo(0, 135)
                            .note(2, "F#4")
                            .note(1, "G4")
                            .note(1, "A4")
                            .note(1, "A4")
                            .note(1, "G4")
                            .note(1, "F#4")
                            .note(1, "E4")
                            .note(2, "D4")
                            .note(1, "E4")
                            .note(1, "F#4")
                            .note(1, "F#4")
                            .note(1, "E4")
                            .note(2, "E4")
                    ).track(tb -> tb.trackName(0, "Viola")
                            .instrumentName(0, "Viola")
                            .programChange(0, 0, 71)
                            .keySignature(0, "DM")
                            .timeSignature(0, 4, 4)
                            .tempo(0, 135)
                            .note(2, "F#4")
                            .note(1, "G4")
                            .note(1, "F#4")
                            .note(1.5, "D4 ")
                            .note(0.5, "E4")
                            .note(1, "F#4")
                            .note(2, "B3")
                            .note(1, "A3")
                            .note(1, "G3")
                            .note(2, "F#3")
                            .note(1, "A3")
                            .note(1, "D4")
                            .note(1, "C#4")
                    ).track(tb -> tb.trackName(0, "Violincello")
                            .instrumentName(0, "Violincello")
                            .programChange(0, 0, 48)
                            .keySignature(0, "DM")
                            .timeSignature(0, 4, 4)
                            .tempo(0, 135)
                            .note(2, "A3")
                            .note(2, "D3")
                            .note(1.5, "B2")
                            .note(0.5, "C#3")
                            .note(1, "D3")
                            .note(2, "G3")
                            .note(1, "F#3")
                            .note(1, "E3")
                            .note(2, "D3")
                            .note(1, "C#3")
                            .note(1, "B2")
                            .note(1, "A2")
                    ).build();


            MidiPrinter.printSequence(seq);

            MidiUtil.play(seq);
            //
            //MidiWriter.write(seq, "ode-to-joy");
        }
    }

    public static class PieceToMidi {

        static void main() throws IOException {

            Sequence seq = SequenceBuilder.from("assets/pieces/chopin-prelude-c-minor/chopin-prelude-c-minor.mid");

            //MidiDebugger.printRawBytes("assets/pieces/chopin-prelude-c-minor/chopin-prelude-c-minor.mid");
            MidiPrinter.printSequence(seq);


        }
    }


}
