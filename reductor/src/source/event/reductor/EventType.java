package reductor;

import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.ShortMessage;


public enum EventType {

    // Meta message types (MetaMessage.getType())
    TEXT(0x1),
    COPYRIGHT_NOTICE(0x2),
    TRACK_NAME(0x3),
    INSTRUMENT_NAME(0x4),
    LYRICS(0x5),
    MARKER(0x6),
    CHANNEL_PREFIX(0x20),
    PORT_CHANGE(0x21),
    END_OF_TRACK(0x2F),
    SET_TEMPO(0x51),
    SMPTE_OFFSET(0x54),
    TIME_SIGNATURE(0x58),
    KEY_SIGNATURE(0x59),
    SEQUENCER_SPECIFIC(0x7F),

    // Channel message commands (ShortMessage.getCommand())
    NOTE_OFF(0x80),
    NOTE_ON(0x90),
    POLY_TOUCH(0xA0),
    CONTROL_CHANGE(0xB0),
    PROGRAM_CHANGE(0xC0),
    CHANNEL_PRESSURE(0xD0),
    PITCH_BEND(0xE0);


    private final int typeCode;


    EventType(int typeCode) {
        this.typeCode = typeCode;
    }


    public int getTypeCode() { return this.typeCode; }

    public boolean isMeta() { return this.ordinal() < 14; }
    public boolean isChannel() { return !isMeta(); }


    @Override
    public String toString() {
        return this.name() + " (0x" + Integer.toHexString(this.typeCode) + ")"; // todo this vs this.name() (see doc)
    }


    public static EventType getEnumType(MidiEvent midiEvent) {

        Integer value = null;
        if (midiEvent.getMessage() instanceof ShortMessage sm) {
            value = sm.getCommand();
        } else if (midiEvent.getMessage() instanceof MetaMessage mm) {
            value = mm.getType();
        }

        for (EventType enumType : EventType.values()) {
            if (enumType.typeCode == value) { return enumType; }
        }

        throw new RuntimeException(
                "New message type that is not currently in enum: 0x" + Integer.toHexString(value)
        );

    }

}
