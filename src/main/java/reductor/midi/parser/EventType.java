package reductor.midi.parser;


import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.ShortMessage;


public enum EventType {

    // Meta messages (14 total)
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

    // Channel/Voice messages (7 total)
    NOTE_OFF(0x80),
    NOTE_ON(0x90),
    POLY_TOUCH(0xA0),
    CONTROL_CHANGE(0xB0),
    PROGRAM_CHANGE(0xC0),
    CHANNEL_PRESSURE(0xD0),
    PITCH_BEND(0xE0);


    private final int statusByte;

    EventType(int statusByte) { this.statusByte = statusByte; }


    // TODO: this is brittle -- delete unless you add every single type in the MIDI spec
    public boolean isMeta() { return this.ordinal() < 15; }
    public boolean isChannel() { return !isMeta(); }

    public int getStatusByte() { return this.statusByte; }

    /** Given a {@link javax.sound.midi.MidiEvent}, returns the enum constant/object
     * corresponding to a MIDI message. */
    public static EventType getValue(MidiEvent midiEvent) {

        /* The javax midi library uses two differently named methods to
           retrieve the status byte/typecode of a midi message. This means you can't just start
           directly with the .values() loop using something like .getTheStatusByte().
           This also serves double-duty of catching any sysex messages that for some reason
           might exist in a SMF (rare, if ever). Even if it ever happens, they can be safely
           ignored. */
        final int statusByte = switch (midiEvent.getMessage()) {
            case MetaMessage mm -> mm.getType();
            case ShortMessage sm -> sm.getCommand();
            default -> throw new RuntimeException("found sysex message");
        };

        for (EventType val : EventType.values()) {
            if (val.statusByte == statusByte) { return val; }
        }

        throw new RuntimeException("New message type that is not currently in enum: 0x%s"
                        .formatted(Integer.toHexString(statusByte))
        );

    }

    @Override public String toString() {
        // Do not use `this` sans `.name()`
        return this.name() + " (0x%s)"
                .formatted(Integer.toHexString(this.statusByte));
    }


}
