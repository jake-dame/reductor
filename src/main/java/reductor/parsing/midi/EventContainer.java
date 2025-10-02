package reductor.parsing.midi;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.Sequence;
import javax.sound.midi.Track;
import java.util.ArrayList;


// Purpose: Construct, in a loop, many Event objects AND collect them into useful lists
public class EventContainer {

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

    public EventContainer(Sequence sequence) throws InvalidMidiDataException {
        this.sequence = sequence;
        EventSorter eventSorter = new EventSorter();
        eventSorter.sortEvents();
    }

    // Inner class meant as just a dispatcher for EventContainer class
    private class EventSorter {

        // this is tracked throughout the creation loop to give
        //    this metadata attributed to each Event object. It can be helpful.
        TrackNameEvent currTrackNameEvent;

        EventSorter() {
            currTrackNameEvent = null;
        }

        // The dispatching function that dispatches out to:
        //     - handleChannelEvents
        //     - handleMetaEvents
        private void sortEvents() throws InvalidMidiDataException {

            Track[] tracks = EventContainer.this.sequence.getTracks();

            for (int trackIndex = 0; trackIndex < tracks.length; trackIndex++) {

                Track track = tracks[trackIndex];

                for (int eventIndex = 0; eventIndex < track.size(); eventIndex++) {

                    // 1. Create the Event object
                    Event<?> event = Event.createEvent(track.get(eventIndex));

                    // 2. Sort it based on its type
                    if (event instanceof ChannelEvent channelEvent) {
                        EventContainer.this.allChannelEvents.add(channelEvent);
                        handleChannelEvents(channelEvent);
                    } else if (event instanceof MetaEvent metaEvent) {
                        EventContainer.this.allMetaEvents.add(metaEvent);
                        handleMetaEvents(metaEvent);
                    } else {
                        continue; // Handle sysex events eventually (they are ignored, for now)
                    }

                    if (event.getType() == EventType.TRACK_NAME) {
                        this.currTrackNameEvent = (TrackNameEvent) event;
                    }

                    event.setTrackIndex(trackIndex);
                    String trackName = this.currTrackNameEvent != null
                            ? this.currTrackNameEvent.getTrackNameAsString()
                            : "";
                    event.setTrackName(trackName);
                    EventContainer.this.allEvents.add(event);
                }
            }
        }

        private void handleChannelEvents(ChannelEvent event) {
            EventType type = event.getType();
            switch (type) {
                case NOTE_ON -> EventContainer.this.noteOnEvents.add((NoteOnEvent) event);
                case NOTE_OFF -> EventContainer.this.noteOffEvents.add((NoteOffEvent) event);
                case CONTROL_CHANGE -> EventContainer.this.controlChangeEvents.add((ControlChangeEvent) event);
                case PROGRAM_CHANGE -> EventContainer.this.programChangeEvents.add((ProgramChangeEvent) event);
                case PITCH_BEND -> {}
                case CHANNEL_PRESSURE -> {}
                default -> throw new RuntimeException("no case for ChannelEvent type: " + type);
            }
        }

        private void handleMetaEvents(MetaEvent event) {
            EventType type = event.getType();
            switch (type) {
                case TRACK_NAME -> EventContainer.this.trackNameEvents.add((TrackNameEvent) event);
                case PORT_CHANGE -> EventContainer.this.portChangeEvents.add((PortChangeEvent) event);
                case END_OF_TRACK -> EventContainer.this.endOfTrackEvents.add((EndOfTrackEvent) event);
                case SET_TEMPO -> EventContainer.this.setTempoEvents.add((SetTempoEvent) event);
                case TIME_SIGNATURE -> EventContainer.this.timeSignatureEvents.add((TimeSignatureEvent) event);
                case KEY_SIGNATURE -> EventContainer.this.keySignatureEvents.add((KeySignatureEvent) event);
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