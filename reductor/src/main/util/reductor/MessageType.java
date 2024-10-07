package reductor;

/**
 * Constants class for Midi message types.
 */
public class MessageType {

    /// MetaMessage
    public static final int TEXT = 0x01;
    public static final int TRACK_NAME = 0x03;
    public static final int CHANNEL_PREFIX = 0x20;
    public static final int END_OF_TRACK = 0x2F;
    public static final int SET_TEMPO = 0x51;
    public static final int TIME_SIGNATURE = 0x58;
    public static final int KEY_SIGNATURE = 0x59;

    /// ShortMessage
    public static final int NOTE_OFF = 0x80;
    public static final int NOTE_ON = 0x90;

}


// TODO: Convert to enum?