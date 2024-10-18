package reductor;

import javax.sound.midi.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import static reductor.Constants.*;
import static reductor.DeepCopy.copySequence;

@SuppressWarnings("rawtypes")
public class Piece {


    // This is needed for I/O
    File file;

    // These are for debugging
    MidiFileFormat fileFormat;
    Integer fileType;

    // Sequence
    Sequence sequence;
    int resolution;

    // Composite lists
    ArrayList<Event> allEvents;
    ArrayList<MetaEvent> allMetaEvents;
    ArrayList<ChannelEvent> allChannelEvents;

    // Meta events
    ArrayList<TextEvent> textEvents;
    ArrayList<TrackNameEvent> trackNameEvents;
    ArrayList<SetTempoEvent> setTempoEvents;
    ArrayList<KeySignatureEvent> keySignatureEvents;
    ArrayList<TimeSignatureEvent> timeSignatureEvents;

    // Channel events
    ArrayList<NoteOnEvent> noteOnEvents;
    ArrayList<NoteOffEvent> noteOffEvents;
    ArrayList<ControlChangeEvent> controlChangeEvents;
    ArrayList<ProgramChangeEvent> programChangeEvents;


    // List of Notes
    ArrayList<Note> notes;

    // Note tree
    IntervalTree tree;


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

        this.notes = new ArrayList<>();

        this.resolution = sequence.getResolution();

        sortEvents();

        this.tree = new IntervalTree(this.notes);

        checkDataStrings(); // debug

    }


    // todo
    ArrayList<NoteOnEvent> unpaired;
    TrackNameEvent trackNameEvent = null;
    KeySignatureEvent keySignatureEvent = null;
    TimeSignatureEvent timeSignatureEvent = null;
    int currentPort = -1;
    ArrayList<MidiEvent> port_change = new ArrayList<>();
    ArrayList<MidiEvent> eot = new ArrayList<>();
    // todo


    // TODO could be implemented using a map or chaining hash table?
    private void sortEvents() {

        Track[] tracks = this.sequence.getTracks();

        for (int trackIndex = 0; trackIndex < tracks.length; trackIndex++) {
            Track track = tracks[trackIndex];
            // todo
            long lastTick = 0;
            long currentTick;
            // todo

            this.unpaired = new ArrayList<>();

            for (int eventIndex = 0; eventIndex < track.size(); eventIndex++) {
                MidiEvent event = track.get(eventIndex);

                // todo
                currentTick = event.getTick();
                assert currentTick >= lastTick;
                // todo

                Event newEvent;

                switch (event.getMessage()) {
                    case ShortMessage _ -> {
                        newEvent = sortChannelEvent(event, trackIndex, unpaired);
                        if (newEvent != null) {
                            this.allChannelEvents.add((ChannelEvent) newEvent);
                        }
                    }
                    case MetaMessage _ -> {
                        newEvent = sortMetaEvent(event, trackIndex);
                        if (newEvent != null) {
                            this.allMetaEvents.add((MetaEvent) newEvent);
                        }
                    }
                    default -> { throw new RuntimeException("Event with sysex or unknown message type: " + event.getMessage()); }
                }

                if (newEvent != null) {
                    this.allEvents.add(newEvent);
                }

                // todo
                lastTick = currentTick;
                // todo

            }

        }

        assert unpaired.isEmpty();

    }


    private MetaEvent sortMetaEvent(MidiEvent event, int trackIndex) {

        MetaMessage message = (MetaMessage) event.getMessage();
        int metaMessageType = message.getType();

        MetaEvent newEvent;

        switch (metaMessageType) {
            case TEXT -> {
                newEvent = new TextEvent(event, trackIndex);
                this.textEvents.add((TextEvent) newEvent);
            }
            case TRACK_NAME -> {
                this.trackNameEvent = new TrackNameEvent(event, trackIndex); // todo
                newEvent = new TrackNameEvent(event, trackIndex);
                this.trackNameEvents.add((TrackNameEvent) newEvent);
            }
            case SET_TEMPO -> {
                newEvent = new SetTempoEvent(event, trackIndex);
                this.setTempoEvents.add((SetTempoEvent) newEvent);
            }
            case TIME_SIGNATURE -> {
                this.timeSignatureEvent = new TimeSignatureEvent(event, trackIndex); // todo
                newEvent = new TimeSignatureEvent(event, trackIndex);
                this.timeSignatureEvents.add((TimeSignatureEvent) newEvent);
            }
            case KEY_SIGNATURE -> {
                this.keySignatureEvent = new KeySignatureEvent(event, trackIndex); // todo
                newEvent = new KeySignatureEvent(event, trackIndex);
                this.keySignatureEvents.add((KeySignatureEvent) newEvent);
            }
            case PORT_PREFIX -> {
                this.currentPort = message.getData()[0] & 0xff;
                port_change.add(event);
                return null;
            }
            case END_OF_TRACK -> {
                eot.add(event);
                return null;
            }
            default -> {
                throw new IllegalArgumentException("UNKNOWN META MESSAGE TYPE: 0x" + Integer.toHexString(metaMessageType));
            }
        }

        return newEvent;

    }


    private ChannelEvent sortChannelEvent(MidiEvent event, int trackIndex, ArrayList<NoteOnEvent> unpaired) {

        ShortMessage message = (ShortMessage) event.getMessage();

        if (message.getCommand() == NOTE_ON  &&  message.getData2() == 0) {
            try {
                message.setMessage(NOTE_OFF, message.getChannel(), message.getData1(), message.getData2());
            } catch (InvalidMidiDataException e) {
                throw new RuntimeException(e);
            }
        }

        int channelMessageType = message.getCommand();

        ChannelEvent newEvent;

        switch (channelMessageType) {
            case NOTE_ON -> {
                newEvent = new NoteOnEvent(event, trackIndex);
                newEvent.trackName = trackNameEvent.dataString();
                unpaired.add((NoteOnEvent) newEvent);
                this.noteOnEvents.add((NoteOnEvent) newEvent);
            }
            case NOTE_OFF -> {
                newEvent = new NoteOffEvent(event, trackIndex);
                findNoteOn((NoteOffEvent) newEvent, unpaired);
                this.noteOffEvents.add((NoteOffEvent) newEvent);
            }
            case CONTROL_CHANGE -> {
                newEvent = new ControlChangeEvent(event, trackIndex);
                this.controlChangeEvents.add((ControlChangeEvent) newEvent);
            }
            case PROGRAM_CHANGE -> {
                newEvent = new ProgramChangeEvent(event, trackIndex);
                this.programChangeEvents.add((ProgramChangeEvent) newEvent);
            }
            default -> {
                throw new IllegalArgumentException("UNKNOWN SHORT MESSAGE TYPE: 0x" + Integer.toHexString(channelMessageType));
            }
        }

        return newEvent;

    }


    private void findNoteOn(NoteOffEvent off, ArrayList<NoteOnEvent> unpaired) {

        for (int i = unpaired.size() - 1; i >= 0; i--) {
            NoteOnEvent on = unpaired.get(i);
            assert !on.paired();

            if (on.pitch() == off.pitch()  &&  on.tick() < off.tick()) {
                this.notes.add(new Note(on.pitch(), new Range(on.tick(), off.tick())));
                unpaired.remove(on);

                // todo
                on.partner = off;
                off.partner = on;
                on.paired = off.paired = true;
                on.trackName = this.trackNameEvent.dataString();
                on.key = this.keySignatureEvent;
                on.time = this.timeSignatureEvent;
                // todo

                return;
            }

        }

        throw new RuntimeException("reached end of list and note on not found");

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

        // TODO some kind of check to make sure scale doesn't push things out of range
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


    Sequence singleTrack() {

        Sequence in = this.getSequence();

        Sequence out;
        try {
            out = new Sequence(in.getDivisionType(), in.getResolution());
        } catch (InvalidMidiDataException e) {
            throw new RuntimeException(e);
        }

        Track outTrack = out.createTrack();

        for (Track track : in.getTracks()) {
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

        for (Event event : this.allEvents) {
            String str = event.toString();
        }

    }


    // debug
    Piece getReconstitution() {

        ArrayList<MidiEvent> midiEvents = new ArrayList<>();

        midiEvents.addAll( notesToMidiEvents(this.notes) );

        midiEvents.addAll( getEvents(setTempoEvents) );
        midiEvents.addAll( getEvents(keySignatureEvents) );
        midiEvents.addAll( getEvents(timeSignatureEvents) );
        midiEvents.addAll( getEvents(textEvents) );
        midiEvents.addAll( getEvents(trackNameEvents) );

        midiEvents.addAll( getEvents(programChangeEvents) );
        midiEvents.addAll( getEvents(controlChangeEvents) );

        Sequence sequence = Util.makeSequenceFromMidiEvents(this.resolution, midiEvents);

        return new Piece(sequence, this.name() + "_reconstitution");

    }

    // debug
    private ArrayList<MidiEvent> getEvents(ArrayList<? extends Event> inList) {

        ArrayList<MidiEvent> outList = new ArrayList<>();

        for (Event event : inList) {
            outList.add( event.event );
        }

        return outList;

    }


}