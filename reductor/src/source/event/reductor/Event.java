package reductor;

import javax.sound.midi.*;

import static reductor.Constants.NOTE_OFF;


/**
 * This is a wrapper class for a {@link javax.sound.midi.MidiEvent}
 *
 * @param <T> The type of MidiMessage the Event holds
 */
public abstract class Event<T extends MidiMessage> {

    private final MidiEvent event;
    private final MessageType type;
    private final T message;
    private int trackIndex;
    private TrackNameEvent trackNameEvent;

    private final Long tick;
    private Long nextEventTick;

    Event(MidiEvent event) {
        this.event = event;
        this.tick = event.getTick();
        this.message = (T) event.getMessage();
        this.type = assignType(this.getMessage());
        this.nextEventTick = null;
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
                case NOTE_OFF -> {
                    yield new NoteOffEvent(event);
                }
                case CONTROL_CHANGE -> new ControlChangeEvent(event);
                case PROGRAM_CHANGE -> new ProgramChangeEvent(event);
                case PITCH_BEND -> new PitchBendEvent(event);
                case CHANNEL_PRESSURE -> new ChannelPressureEvent(event);
                default -> throw new RuntimeException();
            };
        } else if (message instanceof MetaMessage mm) {
            typeCode = mm.getType();
            type = MessageType.getEnumType(typeCode);
            return switch (type) {
                case TEXT -> new TextEvent(event);
                case COPYRIGHT_NOTICE -> new CopyrightNoticeEvent(event);
                case TRACK_NAME -> new TrackNameEvent(event);
                case INSTRUMENT_NAME -> new InstrumentNameEvent(event);
                case PORT_CHANGE -> new PortChangeEvent(event);
                case END_OF_TRACK -> new EndOfTrackEvent(event);
                case SET_TEMPO -> new SetTempoEvent(event);
                case SMPTE_OFFSET -> new SMPTEOffsetEvent(event);
                case TIME_SIGNATURE -> new TimeSignatureEvent(event);
                case KEY_SIGNATURE -> new KeySignatureEvent(event);
                case SEQUENCER_SPECIFIC -> new SequencerSpecificEvent(event);
                case LYRICS -> new LyricsEvent(event);
                case MARKER -> new MarkerEvent(event);
                case CHANNEL_PREFIX -> new ChannelPrefixEvent(event);
                default -> throw new RuntimeException();
            };
        } else {
            //System.out.println("found a sysex message: 0x" + Integer.toHexString(message.getStatus()));
            //throw new RuntimeException("unexpected message type: 0x" + Integer.toHexString(message.getStatus()));
            return null;
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

    public void setNextEventTick(long val) {
        this.nextEventTick = val;
    }

    public final MidiEvent getMidiEvent() {
        return event;
    }

    public final T getMessage() {
        return message;
    }

    public final MessageType getType() {
        return type;
    }

    public final long getTick() {
        return this.tick;
    }

    public final int getTrackIndex() {
        return trackIndex;
    }

    public String getTrackName() {
        return trackNameEvent != null ? trackNameEvent.getName() : "null";
    }

    public final void setTrackIndex(int trackIndex) {
        this.trackIndex = trackIndex;
    }

    public final void setTrackNameEvent(TrackNameEvent trackNameEvent) {
        this.trackNameEvent = trackNameEvent;
    }


}
