package reductor.dev;


import reductor.core.KeySignature;
import reductor.midi.MidiUtil;
import reductor.midi.validator.EventType;
import reductor.util.PitchUtil;

import javax.sound.midi.*;
import java.util.List;
import java.util.function.Predicate;


public class MidiPrinter {

    public static Predicate<MidiEvent> notesOnly = MidiPrinter::isNoteEvent;
    public static Predicate<MidiEvent> allEvents = e -> true;

    private MidiPrinter(){}


    public static void printSequence(Sequence sequence, Predicate<MidiEvent> filter) {

        System.out.println("resolution: " + sequence.getResolution());
        System.out.println("division type: " + sequence.getDivisionType());
        System.out.println("tick length: " + sequence.getTickLength());
        System.out.println("microsecond length: " + sequence.getMicrosecondLength());

        int trackNumber = 0;
        for (Track track : sequence.getTracks()) {

            System.out.printf("""
                               ============================== TRACK %s ==============================
                               """.formatted(trackNumber++)
            );

            for (int i = 0; i < track.size(); i++) {
                MidiEvent event = track.get(i);

                if (!filter.test(event)) { continue; }

                String sTick = String.valueOf(event.getTick());
                String sEvent = getEventString(event);

                if (isNoteOffEvent(event)) {
                    System.out.printf("\t %-5s  %s%n", sTick, sEvent);
                } else {
                    System.out.printf("%-5s       %s%n", sTick, sEvent);
                }
            }
        }

    }
    // convenience
    public static void printSequence(Sequence sequence) {
        MidiPrinter.printSequence(sequence, allEvents);
    }

    public static void printEvents(List<MidiEvent> events) {
        for (MidiEvent e : events) { System.out.println(getEventString(e)); }
    }

    public static String getEventString(MidiEvent event) {
        if (event.getMessage() instanceof ShortMessage sm) {
            EventType enumType = EventType.getValue(event);
            return switch (enumType) {
                case NOTE_OFF -> {
                    String sPitch = PitchUtil.parseMidiValue(sm.getData1(), true);
                    String sVelocity = String.valueOf(sm.getData2());
                    yield "-- ch%d %s".formatted(sm.getChannel(), sPitch);
                }
                case NOTE_ON -> {
                    int velocity = sm.getData2();
                    String sPitch = PitchUtil.parseMidiValue(sm.getData1(), true);
                    String sVelocity = String.valueOf(velocity);
                    yield (velocity != 0) ? "ON ch%d %s".formatted(sm.getChannel(), sPitch)
                                          : "-- ch%d %s".formatted(sm.getChannel(), sPitch);
                }
                case PROGRAM_CHANGE -> {
                    int instrumentCode = sm.getData1();
                    String instrument = MidiUtil.instruments.get(instrumentCode);
                    if (instrument == null) {
                        //yield "unknown";
                        throw new RuntimeException("Found new instrument code: 0x" + Integer.toHexString(instrumentCode));
                    }
                    yield "%s ch%d %s".formatted(enumType.name(), sm.getChannel(), instrument);
                }
                case POLY_TOUCH, CHANNEL_PRESSURE, PITCH_BEND, CONTROL_CHANGE -> "%s ch%d".formatted(enumType.name(), sm.getChannel());
                default -> throw new RuntimeException("unknown voice message: %s".formatted(sm));
            };
        } else if (event.getMessage() instanceof MetaMessage mm) {
            EventType enumType = EventType.getValue(event);
            return switch (enumType) {
                case TEXT, COPYRIGHT_NOTICE, TRACK_NAME, INSTRUMENT_NAME, LYRICS, MARKER, CUE_POINT -> "%s %s".formatted(enumType.name(), new String(mm.getData()));
                case CHANNEL_PREFIX, PORT_CHANGE, END_OF_TRACK, SMPTE_OFFSET, SEQUENCER_SPECIFIC -> enumType.name();
                case SET_TEMPO -> "%s %d bpm".formatted(enumType.name(), MidiUtil.convertMicrosecondsToBPM(mm.getData()));
                case TIME_SIGNATURE -> {
                    byte[] data = mm.getData();
                    int upperNumeral = data[0] & 0xFF;
                    int lowerNumeralExponent = data[1] & 0xFF;
                    int lowerNumeral = (int) Math.pow(2, lowerNumeralExponent);
                    long clockTicksPerTick = data[2] & 0xFF;
                    long thirtySecondNotesPerBeat = data[3] & 0xFF;
                    yield "%s %d/%d".formatted(enumType.name(), upperNumeral, lowerNumeral);
                }
                case KEY_SIGNATURE -> {
                    int accidentals = mm.getData()[0];
                    int mode = mm.getData()[1] & 0xFF;
                    String sKey = KeySignature.toString(accidentals, mode);
                    yield "%s %s".formatted(enumType.name(), sKey);
                }
                default -> throw new RuntimeException("unknown meta event: %s".formatted(mm));
            };
        } else {
            throw new RuntimeException("found sysex message: %s".formatted(event.getMessage()));
        }

    }

    public static boolean isNoteEvent(MidiEvent event) {
        return isNoteOnEvent(event) || isNoteOffEvent(event);
    }
    public static boolean isNoteOnEvent(MidiEvent event) {
        return event.getMessage() instanceof ShortMessage msg
                && msg.getCommand() == EventType.NOTE_ON.code();
    }
    public static boolean isNoteOffEvent(MidiEvent event) {
        return event.getMessage() instanceof ShortMessage msg &&
                (msg.getCommand() == EventType.NOTE_OFF.code()
                        || (isNoteOnEvent(event) && msg.getData2() == 0));
    }


}
