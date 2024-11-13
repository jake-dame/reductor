package reductor;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.Sequence;
import javax.sound.midi.Track;
import java.util.ArrayList;
import java.util.Comparator;


public class Events {

    Sequence sequence;

    // Composite lists
    final ArrayList<MidiEvent> allMidiEvents;
    final ArrayList<Event<?>> allEvents;
    final ArrayList<MetaEvent> allMetaEvents;
    final ArrayList<ChannelEvent> allChannelEvents;

    // Meta events
    final ArrayList<TrackNameEvent> trackNameEvents;
    final ArrayList<PortChangeEvent> portChangeEvents;
    final ArrayList<EndOfTrackEvent> endOfTrackEvents;
    final ArrayList<SetTempoEvent> setTempoEvents;
    final ArrayList<KeySignatureEvent> keySignatureEvents;
    final ArrayList<TimeSignatureEvent> timeSignatureEvents;

    // Channel events
    final ArrayList<NoteOnEvent> noteOnEvents;
    final ArrayList<NoteOffEvent> noteOffEvents;
    final ArrayList<ControlChangeEvent> controlChangeEvents;
    final ArrayList<ProgramChangeEvent> programChangeEvents;

    Events(Sequence sequence) throws InvalidMidiDataException {

        this.sequence = sequence;

        this.allMidiEvents = new ArrayList<>();
        this.allEvents = new ArrayList<>();

        this.allMetaEvents = new ArrayList<>();
        this.trackNameEvents = new ArrayList<>();
        this.portChangeEvents = new ArrayList<>();
        this.endOfTrackEvents = new ArrayList<>();
        this.setTempoEvents = new ArrayList<>();
        this.keySignatureEvents = new ArrayList<>();
        this.timeSignatureEvents = new ArrayList<>();

        this.allChannelEvents = new ArrayList<>();
        this.noteOnEvents = new ArrayList<>();
        this.noteOffEvents = new ArrayList<>();
        this.controlChangeEvents = new ArrayList<>();
        this.programChangeEvents = new ArrayList<>();

        EventSorter eventSorter = new EventSorter();
        eventSorter.sortEvents();
    }

    /// This returns an ArrayList of all midiEvents in the original Sequence except NOTE events.
    public ArrayList<MidiEvent> getAddBacks() {

        ArrayList<Event<?>> events = new ArrayList<>();

        // Add back all meta events
        events.addAll(this.allMetaEvents);

        // Add back all channel events that are NOT note events
        events.addAll(this.programChangeEvents);
        events.addAll(this.controlChangeEvents);

        // Grab the original MidiEvent from each Event object
        ArrayList<MidiEvent> midiEvents = new ArrayList<>();
        for (Event<?> event : events) {
            midiEvents.add(event.getMidiEvent());
        }

        return midiEvents;

    }

    ArrayList<TimeSignatureEvent> getTimeSignatureEvents() {
        timeSignatureEvents.sort(Comparator.comparingLong(TimeSignatureEvent::getTick));
        return timeSignatureEvents;
    }



    private class EventSorter {

        ArrayList<NoteOnEvent> unpairedNoteOns;
        TrackNameEvent currTrackNameEvent;

        EventSorter() {
            unpairedNoteOns = new ArrayList<>();
            currTrackNameEvent = null;
        }

        private void sortEvents() throws InvalidMidiDataException {

            Track[] tracks = Events.this.sequence.getTracks();

            for (int trackIndex = 0; trackIndex < tracks.length; trackIndex++) {

                Track track = tracks[trackIndex];

                for (int eventIndex = 0; eventIndex < track.size(); eventIndex++) {

                    Event<?> newEvent = Event.createEvent(track.get(eventIndex));

                    if (newEvent instanceof ChannelEvent channelEvent) {
                        Events.this.allChannelEvents.add(channelEvent);
                        handleChannelEvents(channelEvent);
                    }
                    else if (newEvent instanceof MetaEvent metaEvent) {
                        Events.this.allMetaEvents.add(metaEvent);
                        handleMetaEvents(metaEvent);
                    }
                    else {
                        // Handle sysex events (they are ignored, for now)
                        continue;
                    }

                    if (newEvent.getType() == MessageType.TRACK_NAME) {
                        this.currTrackNameEvent = (TrackNameEvent) newEvent;
                    }

                    newEvent.setTrackIndex(trackIndex);
                    newEvent.setTrackNameEvent(this.currTrackNameEvent);

                    Events.this.allEvents.add(newEvent);

                }

            }

            if (!unpairedNoteOns.isEmpty()) {
                /*
                 If this line has been reached, there is a "stuck" NOTE ON event.
                 Haven't encountered it yet.
                */
                handleUnpairedNoteOns();
            }

        }

        private void handleChannelEvents(ChannelEvent event) {
            MessageType type = event.getType();
            switch (type) {
                case NOTE_ON, NOTE_OFF -> handleNoteEvents((NoteEvent) event);
                case CONTROL_CHANGE -> Events.this.controlChangeEvents.add((ControlChangeEvent) event);
                case PROGRAM_CHANGE -> Events.this.programChangeEvents.add((ProgramChangeEvent) event);
                case PITCH_BEND -> {} // todo
                case CHANNEL_PRESSURE -> {} // todo
                default -> throw new RuntimeException("no case for ChannelEvent type: " + type);
            }
        }

        private void handleMetaEvents(MetaEvent event) {
            MessageType type = event.getType();
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

        private void handleNoteEvents(NoteEvent event) {

            if (event instanceof NoteOnEvent on) {
                // When NOTE ONs are encountered, just add them to the waiting list to be paired
                Events.this.noteOnEvents.add(on);
                this.unpairedNoteOns.add(on);
            } else if (event instanceof NoteOffEvent off) {
                // When NOTE OFFs are encountered, it is time to pair them with a previous NOTE ON
                Events.this.noteOffEvents.add(off);
                this.findNoteOn(off);
            } else {
                // This is a sanity check, but is impossible to occur
                throw new RuntimeException();
            }

        }

        /*
         Search through the waiting list (unpaired NOTE ON events) and find a match for the
         passed NOTE OFF event.
        */
        private void findNoteOn(NoteOffEvent off) {

            // Iterate through the waiting list of unpaired NOTE ON events
            for (int i = this.unpairedNoteOns.size() - 1; i >= 0; i--) {

                NoteOnEvent on = this.unpairedNoteOns.get(i);

                // If a matching pitch is found, do the operations necessary for recording that data correctly.
                if (on.getPitch() == off.getPitch() && on.getTick() < off.getTick()) {

                    NoteEvent.assignPartners(on, off);

                    this.unpairedNoteOns.remove(on);

                    return;

                }

            }

            /*
            If this line has been reached, there was no match found.
            This can sometimes happen in MIDI data produced by various notation software.
            They are usually harmless, if redundant, NOTE OFF events corresponding to no NOTE ON.
            */
            handleUnpairedNoteOffs(off);

        }


        /*
        The next two functions are stubs, but are here because the strategy kept changing. Now,
        for future's sake, these decisions can be made here and not risk screwing up the other
        functions (which happened during development more than one time).
        This allows for freedom to throw an exception, log, add to a list and handle with some sort
        of default partner, etc.
        */

        void handleUnpairedNoteOns() { }
        void handleUnpairedNoteOffs(NoteOffEvent off) { }


    }


}
