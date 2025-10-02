package reductor.parsing.midi;

import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.ShortMessage;

// This is a wrapper and hook class that provides useful functionality
//    concerning a MIDI message's type (i.e. meta, channel, status bytes, etc.)
// It also provides actual typing for MIDI messages types!
public enum EventType {

    // The javax midi library uses two differently named methods to
    // retrieve the status byte/typecode of a midi message... not sure why. Same data.

    // Meta message types (MetaMessage#getType())
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

    // Channel message commands (ShortMessage#getCommand())
    NOTE_OFF(0x80),
    NOTE_ON(0x90),
    POLY_TOUCH(0xA0),
    CONTROL_CHANGE(0xB0),
    PROGRAM_CHANGE(0xC0),
    CHANNEL_PRESSURE(0xD0),
    PITCH_BEND(0xE0);


    private final int statusByte;


    EventType(int statusByte) {
        this.statusByte = statusByte;
    }


    public int getStatusByte() { return this.statusByte; }

    public boolean isMeta() { return this.ordinal() < 14; }
    public boolean isChannel() { return !isMeta(); }

    public static EventType getEnumType(MidiEvent midiEvent) {

        // Make sure it is a meta or channel message -- nothing else
        int statusByte = switch (midiEvent.getMessage()) {
            case MetaMessage mm -> mm.getType();
            case ShortMessage sm -> sm.getCommand();
            default -> throw new RuntimeException("unsupported message (sysex) in SMF");
        };

        // Loop through the enum values to find the match
        for (EventType enumType : EventType.values()) {
            if (enumType.statusByte == statusByte) {
                return enumType;
            }
        } // if here, there is no match -- exception will be thrown

        throw new RuntimeException(
                "New message type that is not currently in enum: 0x" + Integer.toHexString(statusByte)
        );

    }

    @Override
    public String toString() {
        // Do NOT use `this` here!
        return this.name() + " (0x" + Integer.toHexString(this.statusByte) + ")";
    }


}
