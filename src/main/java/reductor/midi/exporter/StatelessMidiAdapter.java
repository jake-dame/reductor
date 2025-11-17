package reductor.midi.exporter;

import reductor.core.*;
import reductor.midi.builder.SequenceBuilder;

import javax.sound.midi.*;

import static reductor.midi.builder.SequenceBuilder.ACOUSTIC_GRAND_PIANO;

public class StatelessMidiAdapter {

    private final int channel = 0;

    private StatelessMidiAdapter() {
    }

    public static Sequence toSequence(Piece piece) {
        return SequenceBuilder.builder(piece.getResolution())
                .track(tb -> {

                    tb.instrumentName(0, "Piano")
                            .trackName(0, "Piano")
                            .programChange(0, 0, ACOUSTIC_GRAND_PIANO);

                    for (Tempo tempo : piece.getTempos()) {
                        tb.tempo(tempo.getRange().getLow(), tempo.getBpm());
                    }

                    for (TimeSignature ts : piece.getTimeSignatures()) {
                        tb.timeSignature(ts.getRange().getLow(), ts.numerator(), ts.denominator());
                    }

                    for (KeySignature ks : piece.getKeySignatures()) {
                        tb.keySignature(ks.getRange().getLow(), ks.accidentals(), ks.mode());
                    }

                    for (Note note : piece.getNotes()) {
                        tb.noteOn(note.start(), 0, note.getPitch().value(), 64);
                        tb.noteOff(note.stop(), 0, note.getPitch().value(), 0);
                    }

                })
                .build();
    }

}
