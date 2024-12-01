package reductor.midi;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.Sequence;
import javax.sound.midi.Track;
import java.util.ArrayList;


public class Events {

    Sequence sequence;

    // Composite lists
    final ArrayList<Event<?>> allEvents = new ArrayList<>();
    final ArrayList<MetaEvent> allMetaEvents = new ArrayList<>();
    final ArrayList<ChannelEvent> allChannelEvents = new ArrayList<>();

    // Meta events
    final ArrayList<TrackNameEvent> trackNameEvents = new ArrayList<>();
    final ArrayList<PortChangeEvent> portChangeEvents = new ArrayList<>();
    final ArrayList<EndOfTrackEvent> endOfTrackEvents = new ArrayList<>();
    public final ArrayList<SetTempoEvent> setTempoEvents = new ArrayList<>();
    public final ArrayList<KeySignatureEvent> keySignatureEvents = new ArrayList<>();
    public final ArrayList<TimeSignatureEvent> timeSignatureEvents = new ArrayList<>();

    // Channel events
    public final ArrayList<NoteOnEvent> noteOnEvents = new ArrayList<>();
    public final ArrayList<NoteOffEvent> noteOffEvents = new ArrayList<>();
    final ArrayList<ControlChangeEvent> controlChangeEvents = new ArrayList<>();
    final ArrayList<ProgramChangeEvent> programChangeEvents = new ArrayList<>();

    public Events(Sequence sequence) throws InvalidMidiDataException {
        this.sequence = sequence;
        EventSorter eventSorter = new EventSorter();
        eventSorter.sortEvents();
    }

    private class EventSorter {

        TrackNameEvent currTrackNameEvent;

        EventSorter() {
            currTrackNameEvent = null;
        }

        private void sortEvents() throws InvalidMidiDataException {

            Track[] tracks = Events.this.sequence.getTracks();

            for (int trackIndex = 0; trackIndex < tracks.length; trackIndex++) {

                Track track = tracks[trackIndex];

                for (int eventIndex = 0; eventIndex < track.size(); eventIndex++) {

                    Event<?> event = Event.createEvent(track.get(eventIndex));

                    if (event instanceof ChannelEvent channelEvent) {
                        Events.this.allChannelEvents.add(channelEvent);
                        handleChannelEvents(channelEvent);
                    } else if (event instanceof MetaEvent metaEvent) {
                        Events.this.allMetaEvents.add(metaEvent);
                        handleMetaEvents(metaEvent);
                    } else {
                        continue; // Handle sysex events (they are ignored, for now)
                    }

                    if (event.getType() == EventType.TRACK_NAME) {
                        this.currTrackNameEvent = (TrackNameEvent) event;
                    }

                    event.setTrackIndex(trackIndex);
                    String trackName = this.currTrackNameEvent != null
                            ? this.currTrackNameEvent.getTrackNameAsString()
                            : "";
                    event.setTrackName(trackName);
                    Events.this.allEvents.add(event);
                }
            }
        }

        private void handleChannelEvents(ChannelEvent event) {
            EventType type = event.getType();
            switch (type) {
                case NOTE_ON -> Events.this.noteOnEvents.add((NoteOnEvent) event);
                case NOTE_OFF -> Events.this.noteOffEvents.add((NoteOffEvent) event);
                case CONTROL_CHANGE -> Events.this.controlChangeEvents.add((ControlChangeEvent) event);
                case PROGRAM_CHANGE -> Events.this.programChangeEvents.add((ProgramChangeEvent) event);
                case PITCH_BEND -> {}
                case CHANNEL_PRESSURE -> {}
                default -> throw new RuntimeException("no case for ChannelEvent type: " + type);
            }
        }

        private void handleMetaEvents(MetaEvent event) {
            EventType type = event.getType();
            switch (type) {
                case TRACK_NAME -> Events.this.trackNameEvents.add((TrackNameEvent) event);
                case PORT_CHANGE -> Events.this.portChangeEvents.add((PortChangeEvent) event);
                case END_OF_TRACK -> Events.this.endOfTrackEvents.add((EndOfTrackEvent) event);
                case SET_TEMPO -> Events.this.setTempoEvents.add((SetTempoEvent) event);
                case TIME_SIGNATURE -> Events.this.timeSignatureEvents.add((TimeSignatureEvent) event);
                case KEY_SIGNATURE -> Events.this.keySignatureEvents.add((KeySignatureEvent) event);
                case TEXT -> {}
                case SEQUENCER_SPECIFIC -> {}
                case SMPTE_OFFSET -> {}
                case COPYRIGHT_NOTICE -> {}
                case INSTRUMENT_NAME -> {}
                case LYRICS -> {}
                case MARKER -> {}
                case CHANNEL_PREFIX -> {}
                default -> throw new RuntimeException("no case for MetaEvent type: " + type);
            }
        }

    }

    /// This returns an ArrayList of all midiEvents in the original Sequence except note events.
    public ArrayList<MidiEvent> getAddBacks() {

        ArrayList<Event<?>> events = new ArrayList<>();

        events.addAll(this.allMetaEvents);
        events.addAll(this.programChangeEvents);
        events.addAll(this.controlChangeEvents);

        // Grab the original MidiEvent from each Event object
        ArrayList<MidiEvent> midiEvents = new ArrayList<>();
        for (Event<?> event : events) {
            midiEvents.add(event.getMidiEvent());
        }

        return midiEvents;
    }

}