package reductor.parsing.midi;

import reductor.parsing.midi.events.*;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.Sequence;
import javax.sound.midi.Track;
import java.util.ArrayList;


public class MidiContainer {

    private final int resolution;
    private final long lengthInTicks;

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

    public MidiContainer(Sequence sequence) throws InvalidMidiDataException {
        this.resolution = sequence.getResolution();
        this.lengthInTicks = sequence.getTickLength();
        MidiContainer.EventSorter eventSorter = new MidiContainer.EventSorter();
        eventSorter.sortEvents(sequence.getTracks());
    }

    public int getResolution() { return this.resolution; }
    public long getSequenceLengthInTicks() { return this.lengthInTicks; }

    public ArrayList<NoteOnEvent> getNoteOnEvents() { return this.noteOnEvents; }
    public ArrayList<NoteOffEvent> getNoteOffEvents() { return this.noteOffEvents; }
    public ArrayList<TimeSignatureEvent> getTimeSignatureEvents() { return this.timeSignatureEvents; }
    public ArrayList<KeySignatureEvent> getKeySignatureEvents() { return this.keySignatureEvents; }
    public ArrayList<SetTempoEvent> getSetTempoEvents() { return this.setTempoEvents; }


    // Inner class meant as just a dispatcher for MidiContainer class
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
        private void sortEvents(Track[] tracks) throws InvalidMidiDataException {

            for (int trackIndex = 0; trackIndex < tracks.length; trackIndex++) {

                Track track = tracks[trackIndex];

                for (int eventIndex = 0; eventIndex < track.size(); eventIndex++) {

                    // 1. Create the Event object
                    Event<?> event = Event.createEvent(track.get(eventIndex));

                    // 2. Sort it based on its type
                    if (event instanceof ChannelEvent channelEvent) {
                        MidiContainer.this.allChannelEvents.add(channelEvent);
                        handleChannelEvents(channelEvent);
                    } else if (event instanceof MetaEvent metaEvent) {
                        MidiContainer.this.allMetaEvents.add(metaEvent);
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
                    MidiContainer.this.allEvents.add(event);
                }
            }
        }

        private void handleChannelEvents(ChannelEvent event) {
            EventType type = event.getType();
            switch (type) {
                case NOTE_ON -> MidiContainer.this.noteOnEvents.add((NoteOnEvent) event);
                case NOTE_OFF -> MidiContainer.this.noteOffEvents.add((NoteOffEvent) event);
                case CONTROL_CHANGE -> MidiContainer.this.controlChangeEvents.add((ControlChangeEvent) event);
                case PROGRAM_CHANGE -> MidiContainer.this.programChangeEvents.add((ProgramChangeEvent) event);
                case PITCH_BEND -> {}
                case CHANNEL_PRESSURE -> {}
                default -> throw new RuntimeException("no case for ChannelEvent type: " + type);
            }
        }

        private void handleMetaEvents(MetaEvent event) {
            EventType type = event.getType();
            switch (type) {
                case TRACK_NAME -> MidiContainer.this.trackNameEvents.add((TrackNameEvent) event);
                case PORT_CHANGE -> MidiContainer.this.portChangeEvents.add((PortChangeEvent) event);
                case END_OF_TRACK -> MidiContainer.this.endOfTrackEvents.add((EndOfTrackEvent) event);
                case SET_TEMPO -> MidiContainer.this.setTempoEvents.add((SetTempoEvent) event);
                case TIME_SIGNATURE -> MidiContainer.this.timeSignatureEvents.add((TimeSignatureEvent) event);
                case KEY_SIGNATURE -> MidiContainer.this.keySignatureEvents.add((KeySignatureEvent) event);
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