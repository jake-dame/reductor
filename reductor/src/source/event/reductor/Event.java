package reductor;

import javax.sound.midi.*;

import static reductor.Constants.NOTE_OFF;


/**
 * This is a wrapper class for a Java MidiEvent
 *
 * @param <T> The type of MidiMessage the Event holds
 */
public abstract class Event<T extends MidiMessage> {


    private final MidiEvent event;
    private final MessageType type;
    private final long tick;
    private final T message;

    private int trackIndex;
    private TrackNameEvent trackNameEvent;


    private Event(MidiEvent event) {
        this.event = event;
        this.tick = event.getTick();
        this.message = (T) event.getMessage();
        this.type = assignType(this.message());
    }

    public static Event<?> createEvent(MidiEvent event) throws InvalidMidiDataException {

        MidiMessage message = event.getMessage();
        int typeCode;
        MessageType type;

        if (message instanceof ShortMessage sm) {
            typeCode = sm.getCommand();
            type = MessageType.getEnumType(typeCode);
            return switch (type) {
                case NOTE_ON -> {
                    int velocity = sm.getData2();
                    if (velocity == 0) {
                        sm.setMessage(NOTE_OFF, sm.getChannel(), sm.getData1(), sm.getData2());
                        yield new NoteOffEvent(event);
                    } else {
                        yield new NoteOnEvent(event);
                    }
                }
                case NOTE_OFF -> new NoteOffEvent(event);
                case CONTROL_CHANGE -> new ControlChangeEvent(event);
                case PROGRAM_CHANGE -> new ProgramChangeEvent(event);
                default -> throw new RuntimeException();
            };
        } else if (message instanceof MetaMessage mm) {
            typeCode = mm.getType();
            type = MessageType.getEnumType(typeCode);
            return switch (type) {
                case TEXT -> new TextEvent(event);
                case TRACK_NAME -> new TrackNameEvent(event);
                case PORT_CHANGE -> new PortChangeEvent(event);
                case END_OF_TRACK -> new EndOfTrackEvent(event);
                case SET_TEMPO -> new SetTempoEvent(event);
                case TIME_SIGNATURE -> new TimeSignatureEvent(event);
                case KEY_SIGNATURE -> new KeySignatureEvent(event);
                default -> throw new RuntimeException();
            };
        } else {
            throw new RuntimeException("unexpected message type: 0x" + Integer.toHexString(message.getStatus()));
        }

    }


    private MessageType assignType(MidiMessage message) {

        int typeCode;

        if (message instanceof ShortMessage sm) {
            typeCode = sm.getCommand();
        } else if (message instanceof MetaMessage mm) {
            typeCode = mm.getType();
        } else {
            throw new RuntimeException("encountered a sysex or invalid message type");
        }

        return MessageType.getEnumType(typeCode);

    }


    abstract String dataString();


    @Override
    public final String toString() {
        return String.format("(Tr%d): %s (%d)",
                this.trackIndex,
                dataString(),
                this.tick
        );
    }

    public final MidiEvent event() {
        return event;
    }

    public final T message() {
        return message;
    }

    public final MessageType type() {
        return type;
    }

    public final long tick() {
        return this.tick;
    }

    public final int trackIndex() {
        return trackIndex;
    }

    public final String trackName() {
        return trackNameEvent.trackName();
    }

    public final void setTrackIndex(int trackIndex) {
        this.trackIndex = trackIndex;
    }

    public final void setTrackNameEvent(TrackNameEvent trackNameEvent) {
        this.trackNameEvent = trackNameEvent;
    }


}
