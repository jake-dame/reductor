package reductor.midi.exporter;

import reductor.core.KeySignature;
import reductor.core.Tempo;
import reductor.core.TimeSignature;
import reductor.core.Noted;
import reductor.midi.parser.EventType;
import reductor.core.*;

import javax.sound.midi.*;
import java.util.ArrayList;
import java.util.List;


/**
 * Defines the necessary functions to convert a valid MIDI files from a `Piece` object.
 */
public class Exporter {

    private Exporter() { }

    public static Sequence getSequence(Piece piece) throws InvalidMidiDataException {
        return getSequence(piece.getNotes(),
                piece.getTimeSignatures(),
                piece.getKeySignatures(),
                piece.getTempos()
        );
    }

    public static Sequence getSequence(
            ArrayList<Note> notes,
            ArrayList<TimeSignature> timeSigs,
            ArrayList<KeySignature> keySigs,
            ArrayList<Tempo> tempos
    ) throws InvalidMidiDataException {

        final int RESOLUTION = 480;
        final int CHANNEL = 0;
        final String TRACK_NAME = "Piano";
        final String INSTRUMENT_NAME = "Piano";

        final Sequence seq = new Sequence(Sequence.PPQ, RESOLUTION);
        final Track track = seq.createTrack();

        track.add(toTrackNameEvent(TRACK_NAME));
        track.add(toInstrumentNameEvent(INSTRUMENT_NAME));
        track.add(toProgramChangeEvent(CHANNEL));

        for (TimeSignature timeSig : timeSigs) { track.add(toTimeSignatureEvent(timeSig)); }
        for (KeySignature keySig : keySigs) { track.add(toKeySignatureEvent(keySig)); }
        for (Tempo tempo : tempos) { track.add(toTempoEvent(tempo)); }

        for (MidiEvent event : toNoteEvents(notes, CHANNEL)) { track.add(event); }

        return seq;
    }

    public static <T extends Noted> ArrayList<MidiEvent> toNoteEvents(List<T> notedElems, int channel) throws InvalidMidiDataException {

        final ArrayList<MidiEvent> out = new ArrayList<>();
        final int MEDIAN_VELOCITY = 64;

        for (T elem : notedElems) {
            ArrayList<Note> notes = elem.getNotes();

            for (Note note : notes) {
                ShortMessage onMessage = new ShortMessage(ShortMessage.NOTE_ON, channel,
                        note.pitch(), MEDIAN_VELOCITY);
                MidiEvent noteOnEvent = new MidiEvent(onMessage, note.start());
                out.add(noteOnEvent);

                ShortMessage offMessage = new ShortMessage(ShortMessage.NOTE_OFF, channel,
                        note.pitch(), 0);
                MidiEvent noteOffEvent = new MidiEvent(offMessage, note.stop());
                out.add(noteOffEvent);
            }

        }

        return out;
    }

    public static MidiEvent toKeySignatureEvent(KeySignature keySignature) throws InvalidMidiDataException {
        byte[] bytes = new byte[]{(byte) keySignature.accidentals(), (byte) keySignature.mode()};

        MetaMessage message = new MetaMessage(
                EventType.KEY_SIGNATURE.getStatusByte(),
                bytes,
                bytes.length
        );

        return new MidiEvent(message, keySignature.getRange().low());
    }

    public static MidiEvent toTempoEvent(Tempo tempo) throws InvalidMidiDataException {

        byte[] bytes = Util.convertBPMToMicroseconds(tempo.getBpm());

        MetaMessage message = new MetaMessage(
                EventType.SET_TEMPO.getStatusByte(),
                bytes,
                bytes.length
        );

        return new MidiEvent(message, tempo.getRange().low());
    }


    public static MidiEvent toTimeSignatureEvent(TimeSignature timeSignature) throws InvalidMidiDataException {

        int upperNumeral = timeSignature.numerator();
        int lowerNumeral = timeSignature.denominator();

        if (upperNumeral > 128 || upperNumeral < 1) {
            throw new IllegalArgumentException("invalid upperNumeral: " + upperNumeral);
        }

        if (lowerNumeral > 128 || lowerNumeral < 1) {
            throw new IllegalArgumentException("invalid lowerNumeral: " + lowerNumeral);
        }

        int exponent = 0;
        int lowerNumeralCopy = lowerNumeral;
        while (lowerNumeralCopy >= 2) {
            lowerNumeralCopy /= 2;
            exponent++;
        }

        byte clockTicksPerTick = (byte) (24 * (4 / lowerNumeral)); // TODO: double-check this
        byte thirtySecondsPerBeat = 8;

        byte[] bytes = new byte[]{
                (byte) upperNumeral,
                (byte) exponent,
                clockTicksPerTick,
                (byte) (32 / lowerNumeral)
        };

        MetaMessage message = new MetaMessage(
                EventType.TIME_SIGNATURE.getStatusByte(),
                bytes,
                bytes.length
        );

        return new MidiEvent(message, timeSignature.getRange().low());
    }

    public static MidiEvent toProgramChangeEvent(int channel) throws InvalidMidiDataException {

        final int ACOUSTIC_GRAND_PIANO = 0x0;

        ShortMessage message = new ShortMessage(
                ShortMessage.PROGRAM_CHANGE,
                channel,
                ACOUSTIC_GRAND_PIANO,
                0  // Data2 for program change messages is ignored as it is n/a; the method needs it, though

        );

        return new MidiEvent(message, 0);
    }

    public static MidiEvent toTrackNameEvent(String trackName) throws InvalidMidiDataException {

        byte[] bytes = trackName.getBytes();

        MetaMessage message = new MetaMessage(
                EventType.TRACK_NAME.getStatusByte(),
                bytes,
                bytes.length
        );

        return new MidiEvent(message, 0);
    }

    public static MidiEvent toInstrumentNameEvent(String instrumentName) throws InvalidMidiDataException {

        byte[] bytes = instrumentName.getBytes();

        MetaMessage message = new MetaMessage(
                EventType.TRACK_NAME.getStatusByte(),
                bytes,
                bytes.length
        );

        return new MidiEvent(message, 0);
    }

}
