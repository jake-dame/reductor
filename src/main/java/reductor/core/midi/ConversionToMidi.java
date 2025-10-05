package reductor.core.midi;

import reductor.core.KeySignature;
import reductor.core.Tempo;
import reductor.core.TimeSignature;
import reductor.core.Noted;
import reductor.parsing.midi.EventType;
import reductor.core.*;

import javax.sound.midi.*;
import java.util.ArrayList;
import java.util.List;


/**
 * Defines the necessary functions to convert a valid MIDI files from a `Piece` object.
 */
public class ConversionToMidi {

    private ConversionToMidi() { }

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
                ShortMessage onMessage = new ShortMessage(ShortMessage.NOTE_ON, channel, note.pitch(), MEDIAN_VELOCITY);
                MidiEvent noteOnEvent = new MidiEvent(onMessage, note.start());
                out.add(noteOnEvent);

                ShortMessage offMessage = new ShortMessage(ShortMessage.NOTE_OFF, channel, note.pitch(), 0);
                MidiEvent noteOffEvent = new MidiEvent(offMessage, note.stop());
                out.add(noteOffEvent);
            }

        }

        return out;
    }

    public static MidiEvent toKeySignatureEvent(KeySignature keySignature) throws InvalidMidiDataException {
        byte[] bytes = new byte[]{(byte) keySignature.getAccidentals(), (byte) keySignature.getMode()};

        MetaMessage message = new MetaMessage(
                EventType.KEY_SIGNATURE.getStatusByte(),
                bytes,
                bytes.length
        );

        return new MidiEvent(message, keySignature.getRange().low());
    }

    public static MidiEvent toTempoEvent(Tempo tempo) throws InvalidMidiDataException {

        byte[] bytes = convertBPMToMicroseconds(tempo.getBpm());

        MetaMessage message = new MetaMessage(
                EventType.SET_TEMPO.getStatusByte(),
                bytes,
                bytes.length
        );

        return new MidiEvent(message, tempo.getRange().low());
    }

    /**
     * Given a beats-per-minute (bpm) value (1 <= x <= 60,000,000),
     * converts to microseconds per quarter note, which is what MIDI spec uses to control tempo.
     * Returns a byte array of length 3, which is how that information is transmitted over the wire.
     *
     * @param bpm The tempo in beats-per-minute
     * @return The same tempo in microseconds per quarter note
     */
    public static byte[] convertBPMToMicroseconds(int bpm) {

        /*
         8,355,711 translates to 0x7F7F7F, or the highest possible value MIDI set tempo message data
         sections (which are always 3 bytes long) can accommodate (data bytes cannot go to 0xFF because
         in MIDI, the only bytes allowed to have a set MSB are status bytes (or the "Reset" SysEx message).

         The higher the microseconds-per-quarter-note, the slower the tempo.

         The reverse formula is bpm = 60,000,000 usecs-per-min / usecs-per-quarter-note

         *Lowest valid is actually 7.18071747575, but int is the safer type here since dealing with division.
        */
        if (bpm < 8 || bpm > 60_000_000) {
            throw new IllegalArgumentException("lowest valid bpm is 8; highest is 60,000,000");
        }

        final int microsecondsPerMinute = 60_000_000;
        final int microsecondsPerQuarterNote = microsecondsPerMinute / bpm;

        byte[] data = new byte[3];
        data[0] = (byte) ((microsecondsPerQuarterNote & 0xFF0000) >> 16);
        data[1] = (byte) ((microsecondsPerQuarterNote & 0x00FF00) >> 8);
        data[2] = (byte) (microsecondsPerQuarterNote & 0x0000FF);

        return data;
    }

    public static MidiEvent toTimeSignatureEvent(TimeSignature timeSignature) throws InvalidMidiDataException {

        int upperNumeral = timeSignature.getNumerator();
        int lowerNumeral = timeSignature.getDenominator();

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
