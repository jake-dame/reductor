package reductor;

import javax.sound.midi.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import static reductor.Constants.*;
import static reductor.DeepCopy.copySequence;


public class Piece {


    static int RESOLUTION;


    // This is needed for I/O
    File file;

    // These are for debugging
    MidiFileFormat fileFormat;
    Integer fileType;

    // Sequence
    Sequence sequence;

    // Composite lists
    ArrayList<Event<?>> allEvents;
    ArrayList<MetaEvent> allMetaEvents;
    ArrayList<ChannelEvent> allChannelEvents;

    // Meta events
    ArrayList<TextEvent> textEvents;
    ArrayList<TrackNameEvent> trackNameEvents;
    ArrayList<SetTempoEvent> setTempoEvents;
    ArrayList<KeySignatureEvent> keySignatureEvents;
    ArrayList<TimeSignatureEvent> timeSignatureEvents;
    ArrayList<PortChangeEvent> portChangeEvents;
    ArrayList<EndOfTrackEvent> endOfTrackEvents;

    // Channel events
    ArrayList<NoteOnEvent> noteOnEvents;
    ArrayList<NoteOffEvent> noteOffEvents;
    ArrayList<ControlChangeEvent> controlChangeEvents;
    ArrayList<ProgramChangeEvent> programChangeEvents;


    // List of Notes
    ArrayList<Note> notes;

    IntervalTree<Note> tree;


    Piece(String filepath) {

        this.file = new File(filepath);

        try {
            this.sequence = MidiSystem.getSequence(this.file);
            this.fileFormat = MidiSystem.getMidiFileFormat(this.file);
        } catch (InvalidMidiDataException | IOException e) {
            throw new RuntimeException(e);
        }

        this.fileType = fileFormat.getType();

        commonConstruction();

    }


    Piece(Sequence sequence, String name) {

        this.file = assignFile(name);
        this.fileFormat = null;
        this.fileType = null;

        this.sequence = copySequence(sequence);

        commonConstruction();

    }


    private File assignFile(String fileName) {

        if(fileName.contains(".")
                || fileName.contains(" ")
                || fileName.contains("/")
        ) {
            throw new IllegalArgumentException(
                    "name should not include '.'");
        }

        return new File(fileName + ".mid");

    }


    private void commonConstruction() {

        if (this.sequence.getDivisionType() != Sequence.PPQ) {
            throw new RuntimeException("This program does not currently support SMPTE timing");
        }

        // All
        this.allEvents = new ArrayList<>();

        // Channel
        this.allChannelEvents = new ArrayList<>();
        this.noteOnEvents = new ArrayList<>();
        this.noteOffEvents = new ArrayList<>();
        this.controlChangeEvents = new ArrayList<>();
        this.programChangeEvents = new ArrayList<>();

        // Meta
        this.allMetaEvents = new ArrayList<>();
        this.textEvents = new ArrayList<>();
        this.trackNameEvents = new ArrayList<>();
        this.setTempoEvents = new ArrayList<>();
        this.keySignatureEvents = new ArrayList<>();
        this.timeSignatureEvents = new ArrayList<>();
        this.portChangeEvents = new ArrayList<>();
        this.endOfTrackEvents = new ArrayList<>();

        this.notes = new ArrayList<>();

        EventSorter sorter = new EventSorter();
        sorter.sortEvents();

        this.tree = new IntervalTree<>(this.notes);
        assert tree.numElements == this.notes.size();

        // development
        checkDataStrings();
        RESOLUTION = sequence.getResolution();
        // development

    }


    public void play() {

        Util.play(sequence);

    }


    public File write() {

        return Util.write(sequence, file.getName());

    }


    Sequence getSequence() {

        return copySequence(sequence);

    }

    /// Returns the vanilla name (not in filename form)
    public String name() {

        return file.getName().split("\\.")[0];

    }


    public int resolution() {

        return sequence.getResolution();

    }


    public long lengthInTicks() {

        return sequence.getTickLength();

    }


    public long lengthInMicroseconds() {

        return sequence.getMicrosecondLength();

    }


    public float divisionType() {

        return sequence.getDivisionType();

    }


    /// Allows the user to scale the tempo up/down
    public void scaleTempo(float scale) {

        for (SetTempoEvent event : this.setTempoEvents) {
            int newBpm = (int) (event.bpm * scale);
            event.setData(newBpm);
        }

    }


    static ArrayList<MidiEvent> notesToMidiEvents(ArrayList<Note> notes) {

        final ArrayList<MidiEvent> outList = new ArrayList<>();

        final int medianVelocity = 64;

        try {

            for (Note note : notes) {

                ShortMessage onMessage = new ShortMessage(NOTE_ON, note.pitch(), medianVelocity);
                MidiEvent noteOnEvent = new MidiEvent(onMessage, note.start());
                outList.add(noteOnEvent);

                ShortMessage offMessage = new ShortMessage(NOTE_OFF, note.pitch(), 0);
                MidiEvent noteOffEvent = new MidiEvent(offMessage, note.stop());
                outList.add(noteOffEvent);

            }

        } catch (InvalidMidiDataException e) {
            throw new RuntimeException(e);
        }

        return outList;
    }


    static Sequence singleTrack(Sequence seqIn) {

        Sequence out;
        try {
            out = new Sequence(seqIn.getDivisionType(), seqIn.getResolution());
        } catch (InvalidMidiDataException e) {
            throw new RuntimeException(e);
        }

        Track outTrack = out.createTrack();

        for (Track track : seqIn.getTracks()) {
            for (int i = 0; i < track.size(); i++) {

                MidiEvent event = track.get(i);

                if (event.getMessage() instanceof ShortMessage message && message.getCommand() == PROGRAM_CHANGE) {

                    try {
                        message.setMessage(message.getCommand(), message.getChannel(), 0x0, message.getData2());
                    } catch (InvalidMidiDataException e) {
                        throw new RuntimeException(e);
                    }

                }

                outTrack.add(event);
            }
        }

        return out;

    }


    // debug
    public void checkDataStrings() {

        for (Event<?> event : this.allEvents) {
            String str = event.toString();
        }

    }


    // debug
    Sequence getReconstitution() {

        ArrayList<MidiEvent> midiEvents = new ArrayList<>();

        midiEvents.addAll( notesToMidiEvents(this.notes) );

        midiEvents.addAll( getEvents(setTempoEvents) );
        midiEvents.addAll( getEvents(keySignatureEvents) );
        midiEvents.addAll( getEvents(timeSignatureEvents) );
        midiEvents.addAll( getEvents(textEvents) );
        midiEvents.addAll( getEvents(trackNameEvents) );

        midiEvents.addAll( getEvents(programChangeEvents) );
        midiEvents.addAll( getEvents(controlChangeEvents) );

        Sequence sequence = Util.makeSequenceFromMidiEvents(RESOLUTION, midiEvents);
        return sequence;
        //return new Piece(sequence, this.name() + "_reconstitution");

    }

    // debug
    private ArrayList<MidiEvent> getEvents(ArrayList<? extends Event<?>> inList) {

        ArrayList<MidiEvent> outList = new ArrayList<>();

        for (Event<?> event : inList) {
            outList.add( event.event );
        }

        return outList;

    }





























    private class EventSorter {

        ArrayList<NoteOnEvent> unpairedNoteOns;

        int currentTrackIndex;
        TrackNameEvent currentTrackName;
        KeySignatureEvent currentKeySignature;
        TimeSignatureEvent currentTimeSignature;

        long lastTick;
        long currentTick;


        EventSorter() {

            unpairedNoteOns = new ArrayList<>();
            currentTrackName = null;
            currentKeySignature = null;
            currentTimeSignature = null;

        }


        private void sortEvents() {

            Track[] tracks = Piece.this.sequence.getTracks();

            for (int trackIndex = 0; trackIndex < tracks.length; trackIndex++) {
                Track track = tracks[trackIndex];

                // Reset at 0 for every track
                lastTick = 0;

                this.currentTrackIndex = trackIndex;

                for (int eventIndex = 0; eventIndex < track.size(); eventIndex++) {
                    MidiEvent event = track.get(eventIndex);

                    currentTick = event.getTick();
                    assert currentTick >= lastTick;

                    Event<?> newEvent;

                    switch (event.getMessage()) {
                        case ShortMessage _ -> {
                            newEvent = sortChannelEvent(event);
                            if (newEvent != null) {
                                Piece.this.allChannelEvents.add((ChannelEvent) newEvent);
                            }
                        }
                        case MetaMessage _ -> {
                            newEvent = sortMetaEvent(event);
                            if (newEvent != null) {
                                Piece.this.allMetaEvents.add((MetaEvent) newEvent);
                            }
                        }
                        default -> { throw new RuntimeException("Event with sysex or unknown message type: " + event.getMessage()); }
                    }

                    if (newEvent != null) {
                        Piece.this.allEvents.add(newEvent);
                    }

                    lastTick = currentTick;

                }

            }

            assert unpairedNoteOns.isEmpty();
            assert allChannelEvents.size() + allMetaEvents.size() == allEvents.size();
            assert noteOnEvents.size() == noteOffEvents.size();

            if (timeSignatureEvents.isEmpty()) {
                System.err.println("THIS MIDI FILE HAS NO TIME SIGNATURE EVENTS");
            }

        }


        private MetaEvent sortMetaEvent(MidiEvent event) {

            MetaMessage message = (MetaMessage) event.getMessage();
            int metaMessageType = message.getType();

            MetaEvent newEvent;
            switch (metaMessageType) {
                case TEXT -> {
                    newEvent = new TextEvent(event, this.currentTrackIndex);
                    textEvents.add((TextEvent) newEvent);
                }
                case TRACK_NAME -> {
                    this.currentTrackName = new TrackNameEvent(event, this.currentTrackIndex);

                    newEvent = new TrackNameEvent(event, this.currentTrackIndex);
                    trackNameEvents.add((TrackNameEvent) newEvent);
                }
                case SET_TEMPO -> {
                    newEvent = new SetTempoEvent(event, this.currentTrackIndex);
                    setTempoEvents.add((SetTempoEvent) newEvent);
                }
                case TIME_SIGNATURE -> {
                    this.currentTimeSignature = new TimeSignatureEvent(event, this.currentTrackIndex);

                    newEvent = new TimeSignatureEvent(event, this.currentTrackIndex);
                    timeSignatureEvents.add((TimeSignatureEvent) newEvent);
                }
                case KEY_SIGNATURE -> {
                    this.currentKeySignature = new KeySignatureEvent(event, this.currentTrackIndex);

                    newEvent = new KeySignatureEvent(event, this.currentTrackIndex);
                    keySignatureEvents.add((KeySignatureEvent) newEvent);
                }
                case PORT_PREFIX -> {
                    newEvent = new PortChangeEvent(event, this.currentTrackIndex);
                    Piece.this.portChangeEvents.add((PortChangeEvent) newEvent);
                }
                case END_OF_TRACK -> {
                    newEvent = new EndOfTrackEvent(event, this.currentTrackIndex);
                    Piece.this.endOfTrackEvents.add((EndOfTrackEvent) newEvent);
                }
                default -> {
                    throw new RuntimeException("UNKNOWN META MESSAGE TYPE: 0x" + Integer.toHexString(metaMessageType));
                }
            }

            return newEvent;

        }


        private ChannelEvent sortChannelEvent(MidiEvent event) {

            ShortMessage message = (ShortMessage) event.getMessage();
            int channelMessageType = sanitizeNoteMessage(message).getCommand();

            ChannelEvent newEvent;
            switch (channelMessageType) {
                case NOTE_ON -> {
                    newEvent = new NoteOnEvent(event, this.currentTrackIndex);
                    this.unpairedNoteOns.add((NoteOnEvent) newEvent);
                    noteOnEvents.add((NoteOnEvent) newEvent);
                }
                case NOTE_OFF -> {
                    newEvent = new NoteOffEvent(event, this.currentTrackIndex);
                    findNoteOn((NoteOffEvent) newEvent);
                    Piece.this.noteOffEvents.add((NoteOffEvent) newEvent);
                }
                case CONTROL_CHANGE -> {
                    newEvent = new ControlChangeEvent(event, this.currentTrackIndex);
                    controlChangeEvents.add((ControlChangeEvent) newEvent);
                }
                case PROGRAM_CHANGE -> {
                    newEvent = new ProgramChangeEvent(event, this.currentTrackIndex);
                    programChangeEvents.add((ProgramChangeEvent) newEvent);
                }
                default -> {
                    throw new RuntimeException("UNKNOWN SHORT MESSAGE TYPE: 0x" + Integer.toHexString(channelMessageType));
                }
            }

            return newEvent;

        }


        private ShortMessage sanitizeNoteMessage(ShortMessage message) {

            if (message.getCommand() == NOTE_ON  &&  message.getData2() == 0) {
                try {
                    message.setMessage(NOTE_OFF, message.getChannel(), message.getData1(), message.getData2());
                } catch (InvalidMidiDataException e) {
                    throw new RuntimeException(e);
                }
            }

            return message;

        }


        private void findNoteOn(NoteOffEvent off) {

            for (int i = this.unpairedNoteOns.size() - 1; i >= 0; i--) {
                NoteOnEvent on = this.unpairedNoteOns.get(i);
                assert !on.paired();

                if (on.pitch() == off.pitch()  &&  on.tick() < off.tick()) {
                    this.unpairedNoteOns.remove(on);

                    NoteEvent.assignPartners(on, off);
                    if (currentTrackName != null) {
                        on.trackName = this.currentTrackName.dataString();
                    }
                    on.key = this.currentKeySignature;
                    on.time = this.currentTimeSignature;

                    Range range = new Range(on.tick(), off.tick());
                    Rhythm rhythm = new Rhythm(RESOLUTION, this.currentTimeSignature);
                    KeyContext keyContext = KeyContext.getKeyContextObject(this.currentKeySignature);
                    Degree degree = null;

                    Note note = new Note(on.pitch, range, rhythm, keyContext, degree);

                    Piece.this.notes.add(note);

                    return;
                }

            }

            throw new RuntimeException("reached end of list and note on not found");

        } // findNoteOn()


    } // EventSorter



}