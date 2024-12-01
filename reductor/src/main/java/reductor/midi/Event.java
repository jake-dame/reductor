package reductor.midi;

import javax.sound.midi.*;


/**
 * This is a wrapper class for a {@link javax.sound.midi.MidiEvent}
 *
 * @param <T> The type of MidiMessage the Event holds
 */
public abstract class Event<T extends MidiMessage> {

    private final MidiEvent event;
    private final EventType type;
    private final T message;
    private int trackIndex;
    private String trackName;


    private final Long tick;


    Event(MidiEvent event) {
        this.event = event;
        this.tick = event.getTick();
        this.message = (T) event.getMessage();
        this.type = EventType.getEnumType(event);
    }


    public static Event<?> createEvent(MidiEvent event) throws InvalidMidiDataException {
        MidiMessage message = event.getMessage();
        int typeCode;
        EventType type;
        if (message instanceof ShortMessage sm) {
            type = EventType.getEnumType(event);
            return switch (type) {
                case NOTE_ON -> {
                    int velocity = sm.getData2();
                    if (velocity == 0) {
                        sm.setMessage(EventType.NOTE_OFF.getTypeCode(), sm.getChannel(), sm.getData1(), sm.getData2());
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
            type = EventType.getEnumType(event);
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

    abstract String dataString();

    @Override
    public final String toString() {
        return String.format("{track %d (%s), tick %d -> %s",
                this.trackIndex,
                this.getTrackName(),
                this.tick,
                dataString()
        );
    }

    public final MidiEvent getMidiEvent() { return event; }
    public final T getMessage() { return message; }
    public final EventType getType() { return type; }
    public final long getTick() { return this.tick; }

    public String getTrackName() { return this.trackName != null ? trackName : ""; }
    public final void setTrackName(String trackName) { this.trackName = trackName; }

    public final int getTrackIndex() { return trackIndex; }
    public final void setTrackIndex(int trackIndex) { this.trackIndex = trackIndex; }

}