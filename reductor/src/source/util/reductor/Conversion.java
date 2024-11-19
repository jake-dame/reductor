package reductor;

import javax.sound.midi.*;
import java.util.*;


public class Conversion {

    public static ArrayList<Note> toNotes(ArrayList<NoteOnEvent> noteOnEvents) {
        ArrayList<Note> out = new ArrayList<>();
        noteOnEvents.forEach( on -> out.add( toNote(on) ) );
        return out;
    }

    public static Note toNote(NoteOnEvent on) {
        Range range = new Range(on.getTick(), on.getPartner().getTick());
        return new Note(on.getPitch(), range, on.getTrackName());
    }

    public  static <T extends Noted> ArrayList<MidiEvent> toMidiEvents(List<T> notedElems) throws InvalidMidiDataException {

        final ArrayList<MidiEvent> out = new ArrayList<>();
        final int MEDIAN_VELOCITY = 64;

        for (T elem : notedElems) {
            ArrayList<Note> notes = elem.getNotes();

            for (Note note : notes) {
                ShortMessage onMessage = new ShortMessage(ShortMessage.NOTE_ON, note.pitch(), MEDIAN_VELOCITY);
                MidiEvent noteOnEvent = new MidiEvent(onMessage, note.start());
                out.add(noteOnEvent);

                ShortMessage offMessage = new ShortMessage(ShortMessage.NOTE_OFF, note.pitch(), 0);
                MidiEvent noteOffEvent = new MidiEvent(offMessage, note.stop());
                out.add(noteOffEvent);
            }
        }

        return out;
    }

    public static Sequence toSequence(int resolution, List<ArrayList<MidiEvent>> midiEventsLists) throws InvalidMidiDataException {
        Sequence out = new Sequence(Sequence.PPQ, resolution);
        Track track = out.createTrack();

        for (ArrayList<MidiEvent> list : midiEventsLists) {
            for (MidiEvent event : list) {
                track.add(event);
            }
        }

        return out;
    }


    /* =============
     * KEY SIGNATURE
     * ===========*/


    /**
     * Returns a {@link reductor.KeySignature} given a valid MIDI key signature event:
     *
     * @param event A {@link javax.sound.midi.MidiEvent}
     *
     * @return A {@link reductor.KeySignature} corresponding to the MIDI data.
     */
    public static KeySignature toKeySignature(MidiEvent event, Range range) {

        byte[] data;
        if (event.getMessage() instanceof MetaMessage mm) {
            if (mm.getType() == EventType.KEY_SIGNATURE.getTypeCode()) {
                data = mm.getData();
            } else {
                throw new RuntimeException("toKeySignature was given an event that is not a key signature event");
            }
        } else {
            throw new RuntimeException("toKeySignature was given an event that is not a key signature event");
        }

        int accidentalCount = data[0];
        int mode = data[1];

        if (accidentalCount < -7 || accidentalCount > 7) {
            throw new IllegalArgumentException("bytes representing accidental counts must be between -7 and 7");
        }

        if (mode == 0  ||  mode == 1) {
            return new KeySignature(accidentalCount, mode, range);
        }

        throw new IllegalArgumentException("bytes representing modes must be 0 (major) or 1 (minor)");
    }

    public static MidiEvent fromKeySignature(KeySignature keySignature) throws InvalidMidiDataException {
        byte[] bytes = new byte[]{(byte) keySignature.getAccidentals(), (byte) keySignature.getMode()};

        MetaMessage message = new MetaMessage(
                EventType.KEY_SIGNATURE.getTypeCode(),
                bytes,
                bytes.length
        );

        return new MidiEvent(message, keySignature.getRange().low());
    }


    /* =====
     * TEMPO
     * ===*/


    /**
     * Returns a {@link reductor.Tempo} given a valid MIDI set tempo event:
     *
     * @param event A {@link javax.sound.midi.MidiEvent}
     *
     * @return A {@link reductor.Tempo} corresponding to the MIDI data.
     */
    public static Tempo toTempo(MidiEvent event, Range range) {

        byte[] data;
        if (event.getMessage() instanceof MetaMessage mm) {
            if (mm.getType() == EventType.SET_TEMPO.getTypeCode()) {
                data = mm.getData();
            } else {
                throw new RuntimeException("toTempo was given an event that is not a set tempo event");
            }
        } else {
            throw new RuntimeException("toTempo was given an event that is not a set tempo event");
        }

        return new Tempo( convertMicrosecondsToBPM(data), range );
    }

    public static MidiEvent fromTempo(Tempo tempo) throws InvalidMidiDataException {

        byte[] bytes = Conversion.convertBPMToMicroseconds(tempo.getBpm());

        MetaMessage message = new MetaMessage(
                EventType.SET_TEMPO.getTypeCode(),
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

    /**
     * Given a value in microseconds (per quarter note), converts to beats-per-minute (bpm),
     * which is what humans use to specify tempo. Easily retrieved with getData() on set tempo
     * messages from (Java) MetaMessage class.
     *
     * @param data The tempo as a number split into three LTR bytes
     * @return The same tempo in beats-per-minute
     */
    public static int convertMicrosecondsToBPM(byte[] data) {

        int byteIndex = 0;
        long microsecondsPerQuarterNote = 0;

        while (byteIndex < data.length) {
            microsecondsPerQuarterNote <<= 8;
            microsecondsPerQuarterNote |= (data[byteIndex] & 0xFF);
            byteIndex++;
        }

        final int microsecondsPerMinute = 60_000_000;

        // This cast is fine because none of the numbers here, if valid MIDI spec,
        //     will never get remotely near INTEGER_MAX.
        return microsecondsPerMinute / (int) microsecondsPerQuarterNote;
    }


    /* ==============
     * TIME SIGNATURE
     * ============*/


    public static TimeSignature toTimeSignature(MidiEvent event, Range range) {

        byte[] data;
        if (event.getMessage() instanceof MetaMessage mm) {
            if (mm.getType() == EventType.TIME_SIGNATURE.getTypeCode()) {
                data = mm.getData();
            } else {
                throw new RuntimeException("toTempo was given an event that is not a set tempo event");
            }
        } else {
            throw new RuntimeException("toTempo was given an event that is not a set tempo event");
        }

        int upperNumeral = data[0] & 0xFF;
        int lowerNumeralExponent = data[1] & 0xFF;
        int lowerNumeral = (int) Math.pow(2, lowerNumeralExponent);

        int clockTicksPerTick = data[2] & 0xFF; // don't delete
        int thirtySecondsPerBeat = data[3] & 0xFF; // don't delete

        return new TimeSignature(
                Context.resolution(),
                upperNumeral,
                lowerNumeral,
                range
        );

    }

    public static MidiEvent fromTimeSignature(TimeSignature timeSignature) throws InvalidMidiDataException {

        int upperNumeral = timeSignature.getUpperNumeral();
        int lowerNumeral = timeSignature.getLowerNumeral();

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

        byte clockTicksPerTick = (byte) (24 * (4 / lowerNumeral)); // TODO: this may not be right
        byte thirtySecondsPerBeat = 8;

        byte[] bytes = new byte[]{
                (byte) upperNumeral,
                (byte) exponent,
                clockTicksPerTick,
                (byte) (32 / lowerNumeral)
        };

        MetaMessage message = new MetaMessage(
                EventType.TIME_SIGNATURE.getTypeCode(),
                bytes,
                bytes.length
        );

        return new MidiEvent(message, timeSignature.getRange().low());
    }


    /* ============
    * ASSIGN RANGES
    * ===========*/


    public static <E extends Event<?>, C extends Ranged> ArrayList<C>
    assignRanges(List<E> midiEvents, Class<C> classToConvertTo) {

        if (midiEvents.isEmpty()) { return new ArrayList<>(); }
        ArrayList<E> eventsCopy = new ArrayList<>(midiEvents);
        eventsCopy.sort(Comparator.comparingLong(Event::getTick));

        ArrayList<C> out = new ArrayList<>();

        int size = eventsCopy.size();
        for (int i = 0; i < size; i++) {

            Event<?> event = eventsCopy.get(i);

            long tick = event.getTick();
            long nextTick;

            nextTick = i == size - 1
                    ? Context.finalTick() + 1
                    : eventsCopy.get(i+1).getTick();

            // This checks for the "same" midi meta event being sent at the same time, just on different tracks
            if (tick == nextTick) {
                continue;
            }

            // Meta events don't get "off" messages, it's just whenever the next event occurs.
            // So really this "should" be tick, nextTick, like [0,480], but that range.high() value won't even be
            // used in the reverse conversion process - the only thing that matters is the internal representation
            // and what Piece needs, which is that standard range.high() is nextTick - 1.
            Range range = new Range(tick, nextTick - 1);

            C newGuy = switch (classToConvertTo.getSimpleName()) {
                case "TimeSignature" -> classToConvertTo.cast(toTimeSignature(event.getMidiEvent(), range));
                case "KeySignature" -> classToConvertTo.cast(toKeySignature(event.getMidiEvent(), range));
                case "Tempo" -> classToConvertTo.cast(toTempo(event.getMidiEvent(), range));
                default -> throw new RuntimeException("invalid class for using assignRanges function: " + classToConvertTo);
            };

            out.add(newGuy);
        }

        return out;
    }


}


//// TODO: make this more like the tempo and key signature ones, and decouple the range assignment. Just have this
////  be object creation as well as checking for invalid data.
//static ArrayList<TimeSignature> toTimeSigs(ArrayList<TimeSignatureEvent> timeSignatureEvents) throws RuntimeException {
//
//    if (timeSignatureEvents.isEmpty()) {
//        throw new RuntimeException("this program only supports MIDI files with time signature data");
//    }
//
//    // ensure these are in ascending order by tick, otherwise the range creation loop won't work
//    timeSignatureEvents.sort(Comparator.comparingLong(TimeSignatureEvent::getTick));
//
//    ArrayList<TimeSignature> out = new ArrayList<>();
//
//    int size = timeSignatureEvents.size();
//    for (int i = 0; i < size; i++) {
//
//        var event = timeSignatureEvents.get(i);
//
//        long tick = event.getTick();
//        long nextTick;
//
//        if (i == size - 1) {
//            nextTick = Context.finalTick() + 1;
//        } else {
//            nextTick = timeSignatureEvents.get(i+1).getTick();
//        }
//
//        Range range = new Range(tick, nextTick - 1);
//
//        TimeSignature timeSig = new TimeSignature(
//                Context.resolution(),
//                event.getUpperNumeral(),
//                event.getLowerNumeral(),
//                range
//        );
//
//        out.add(timeSig);
//    }
//
//    return out;
//
//}

//public static Sequence toSequencePlusAddbacks(List<Column> columns, ArrayList<MidiEvent> addBacks) throws InvalidMidiDataException {
//    ArrayList<MidiEvent> midiEvents = new ArrayList<>(addBacks);
//    for (Column column : columns) {
//        midiEvents.addAll( column.getNotes().toMidiEvents() );
//    }
//    return toSequence(midiEvents);
//}
