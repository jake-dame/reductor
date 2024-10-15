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
    ArrayList<MetaEvent> metaEvents;
    ArrayList<ChannelEvent> channelEvents;

    // Channel events
    ArrayList<NoteOnEvent> noteOnEvents;
    ArrayList<NoteOffEvent> noteOffEvents;
    ArrayList<ControlChangeEvent> controlChangeEvents;
    ArrayList<ProgramChangeEvent> programChangeEvents;

    // Meta events
    ArrayList<TextEvent> textEvents;
    ArrayList<TrackNameEvent> trackNameEvents;
    ArrayList<SetTempoEvent> setTempoEvents;
    ArrayList<KeySignatureEvent> keySignatureEvents;
    ArrayList<TimeSignatureEvent> timeSignatureEvents;

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
        this.channelEvents = new ArrayList<>();
        this.noteOnEvents = new ArrayList<>();
        this.noteOffEvents = new ArrayList<>();
        this.controlChangeEvents = new ArrayList<>();
        this.programChangeEvents = new ArrayList<>();

        // Meta
        this.metaEvents = new ArrayList<>();
        this.textEvents = new ArrayList<>();
        this.trackNameEvents = new ArrayList<>();
        this.setTempoEvents = new ArrayList<>();
        this.keySignatureEvents = new ArrayList<>();
        this.timeSignatureEvents = new ArrayList<>();

        this.notes = new ArrayList<>();

        sortEvents();

        this.tree = new IntervalTree(this.notes);

        checkDataStrings(); // debug

    }


    private void sortEvents() {

        Track[] tracks = this.sequence.getTracks();

        // TODO could be implemented using a map or chaining hash table?
        ArrayList<NoteOnEvent> unpaired = new ArrayList<>();

        for (int trackIndex = 0; trackIndex < tracks.length; trackIndex++) {
            Track track = tracks[trackIndex];
            //debug
            long lastTick = 0;
            long currentTick;
            //debug

            for (int eventIndex = 0; eventIndex < track.size(); eventIndex++) {
                MidiEvent event = track.get(eventIndex);

                // debug
                currentTick = event.getTick();
                assert currentTick >= lastTick;
                // debug

                Event newEvent;

                switch (event.getMessage()) {
                    case ShortMessage _ -> {
                        newEvent = sortChannelEvent(event, trackIndex, unpaired);
                        if (newEvent != null) {
                            this.channelEvents.add((ChannelEvent) newEvent);
                        }
                    }
                    case MetaMessage _ -> {
                        newEvent = sortMetaEvent(event, trackIndex);
                        if (newEvent != null) {
                            this.metaEvents.add((MetaEvent) newEvent);
                        }
                    }
                    default -> { throw new RuntimeException("Event with sysex or unknown message type: " + event.getMessage()); }
                }

                if (newEvent != null) {
                    this.allEvents.add(newEvent);
                }

                // debug
                lastTick = currentTick;
                // debug

            }

        }

        assert unpaired.isEmpty();

    }


    private ChannelEvent sortChannelEvent(MidiEvent event, int trackIndex, ArrayList<NoteOnEvent> unpaired) {

        ShortMessage message = (ShortMessage) event.getMessage();
        int channelMessageType = message.getCommand();

        if (channelMessageType == NOTE_ON  &&  message.getData2() == 0) {
            try {
                message.setMessage(NOTE_OFF, message.getChannel(), message.getData1(), message.getData2());
            } catch (InvalidMidiDataException e) {
                throw new RuntimeException(e);
            }
        }

        ChannelEvent newEvent;

        switch (channelMessageType) {
            case NOTE_ON -> {
                newEvent = new NoteOnEvent(event, trackIndex);
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
            default -> throw new IllegalArgumentException("UNKNOWN SHORT MESSAGE TYPE: 0x" + Integer.toHexString(channelMessageType));
        }

        return newEvent;

    }


    private void findNoteOn(NoteOffEvent off, ArrayList<NoteOnEvent> unpaired) {

        for (int i = unpaired.size() - 1; i >= 0; i--) {
            NoteOnEvent on = unpaired.get(i);
            assert !on.paired();

            if (on.pitch() == off.pitch()  &&  on.tick() < off.tick()) {
                NoteEvent.assignPartners(on, off);
                this.notes.add(new Note(on.pitch(), new Range(on.tick(), off.tick())));
                return;
            }

        }

        throw new RuntimeException("reached end of list and note on not found");

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
                newEvent = new TrackNameEvent(event, trackIndex);
                this.trackNameEvents.add((TrackNameEvent) newEvent);
            }
            case SET_TEMPO -> {
                newEvent = new SetTempoEvent(event, trackIndex);
                this.setTempoEvents.add((SetTempoEvent) newEvent);
            }
            case TIME_SIGNATURE -> {
                newEvent = new TimeSignatureEvent(event, trackIndex);
                this.timeSignatureEvents.add((TimeSignatureEvent) newEvent);
            }
            case KEY_SIGNATURE -> {
                newEvent = new KeySignatureEvent(event, trackIndex);
                this.keySignatureEvents.add((KeySignatureEvent) newEvent);
            }
            case PORT_PREFIX, END_OF_TRACK -> {
                return null;
            }
            default -> {
                throw new IllegalArgumentException("UNKNOWN META MESSAGE TYPE: 0x" + Integer.toHexString(metaMessageType));
            }
        }

        return newEvent;

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