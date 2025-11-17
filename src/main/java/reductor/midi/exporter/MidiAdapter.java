//package reductor.midi.exporter;
//
//import reductor.core.KeySignature;
//import reductor.core.Tempo;
//import reductor.core.TimeSignature;
//import reductor.midi.builder.SequenceBuilder;
//import reductor.core.*;
//
//import javax.sound.midi.*;
//
//import static reductor.midi.builder.SequenceBuilder.ACOUSTIC_GRAND_PIANO;
//
//
//public class MidiAdapter {
//
//    private final SequenceBuilder.TrackBuilder builder;
//    private final int channel = 0;
//
//    private MidiAdapter(int resolution){
//        this.builder = SequenceBuilder.TrackBuilder.builder(resolution);
//    }
//
//    public static Sequence toSequence(Piece piece) {
//        MidiAdapter midiAdapter = new MidiAdapter(piece.getResolution());
//        midiAdapter.adaptPiece(piece);
//        return SequenceBuilder.builder(piece.getResolution())
//                .track(midiAdapter.builder.build())
//                .build();
//    }
//
//    private void adaptPiece(Piece piece) {
//        this.builder.instrumentName(0, "Piano")
//                .trackName(0, "Piano")
//                .programChange(0, channel, ACOUSTIC_GRAND_PIANO);
//        for (Tempo t : piece.getTempos()) { this.adaptTempo(t); }
//        for (TimeSignature t : piece.getTimeSignatures()) { this.adaptTimeSignature(t); }
//        for (KeySignature t : piece.getKeySignatures()) { this.adaptKeySignature(t); }
//        for (Note n : piece.getNotes()) { adaptNote(n); }
//    }
//
//    private void adaptKeySignature(KeySignature keySignature) {
//        long startTick = keySignature.getRange().getLow();
//        int accidentals = keySignature.accidentals();
//        int mode = keySignature.mode();
//        this.builder.keySignature(startTick, accidentals, mode);
//    }
//
//    private void adaptTempo(Tempo tempo) {
//        long startTick = tempo.getRange().getLow();
//        int bpm = tempo.getBpm();
//        this.builder.tempo(tempo.getRange().getLow(), tempo.getBpm());
//    }
//
//    private void adaptTimeSignature(TimeSignature timeSignature) {
//        long startTick = timeSignature.getRange().getLow();
//        int numerator = timeSignature.numerator();
//        int denominator = timeSignature.denominator();
//        this.builder.timeSignature(startTick, numerator, denominator);
//    }
//
//    private void adaptNote(Note note) {
//        this.builder.noteOn(note.start(), 0, note.getPitch().value(), 64);
//        this.builder.noteOff(note.stop(), 0, note.getPitch().value(), 0);
//    }
//
//    //public static <T extends Noted> ArrayList<MidiEvent> toNoteEvents(List<T> notedElems, int channel) throws InvalidMidiDataException {
//    //
//    //    final ArrayList<MidiEvent> out = new ArrayList<>();
//    //    final int MEDIAN_VELOCITY = 64;
//    //
//    //    for (T elem : notedElems) {
//    //        ArrayList<Note> notes = elem.getNotes();
//    //
//    //        for (Note note : notes) {
//    //            ShortMessage onMessage = new ShortMessage(ShortMessage.NOTE_ON, channel,
//    //                    note.pitch(), MEDIAN_VELOCITY);
//    //            MidiEvent noteOnEvent = new MidiEvent(onMessage, note.start());
//    //            out.add(noteOnEvent);
//    //
//    //            ShortMessage offMessage = new ShortMessage(ShortMessage.NOTE_OFF, channel,
//    //                    note.pitch(), 0);
//    //            MidiEvent noteOffEvent = new MidiEvent(offMessage, note.stop());
//    //            out.add(noteOffEvent);
//    //        }
//    //
//    //    }
//    //
//    //    return out;
//    //}
//
//
//}
