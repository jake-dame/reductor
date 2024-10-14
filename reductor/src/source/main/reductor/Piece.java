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

    // These are probably unused but are remaining for now
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
                    "name should just be a unique identifier and not in filename form");
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

        sortEventsIntoLists();

        ArrayList<NoteEvent> noteEvents = new ArrayList<>();
        noteEvents.addAll(noteOnEvents);
        noteEvents.addAll(noteOffEvents);
        this.notes = Conversion.pairAndCreateNotes(noteEvents);

        //pairNoteEvents();
        //
        //createNotes();

        this.tree = new IntervalTree(this.notes);

        checkDataStrings(); // debug

    }


    private void sortEventsIntoLists() {

        Track[] tracks = this.sequence.getTracks();

        for (int trackIndex = 0; trackIndex < tracks.length; trackIndex++) {
            Track track = tracks[trackIndex];

            for (int eventIndex = 0; eventIndex < track.size(); eventIndex++) {
                MidiEvent event = track.get(eventIndex);

                Event newEvent;

                switch (event.getMessage()) {
                    case ShortMessage _ -> {
                        newEvent = sortChannelEvent(event, trackIndex);
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

            }

        }

    }


    private ChannelEvent sortChannelEvent(MidiEvent event, int trackIndex) {

        ShortMessage message = (ShortMessage) event.getMessage();
        int type = message.getCommand();

        ChannelEvent newEvent;

        switch (type) {
            case NOTE_ON -> {

                int velocity = message.getData2();

                if (velocity == 0) {
                    try {
                        message.setMessage(NOTE_OFF, message.getChannel(), message.getData1(), message.getData2());
                    } catch (InvalidMidiDataException e) {
                        throw new RuntimeException(e);
                    }
                    newEvent = new NoteOffEvent(event, trackIndex);
                    this.noteOffEvents.add((NoteOffEvent) newEvent);
                } else {
                    newEvent = new NoteOnEvent(event, trackIndex);
                    this.noteOnEvents.add((NoteOnEvent) newEvent);
                }

            }
            case NOTE_OFF -> {
                newEvent = new NoteOffEvent(event, trackIndex);
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
            default -> throw new IllegalArgumentException("UNKNOWN SHORT MESSAGE TYPE: 0x" + Integer.toHexString(type));
        }

        return newEvent;

    }


    private MetaEvent sortMetaEvent(MidiEvent event, int trackIndex) {

        MetaMessage message = (MetaMessage) event.getMessage();
        int metaMessageType = message.getType();

        MetaEvent newEvent;

        switch (metaMessageType) {
            case Constants.TEXT -> {
                newEvent = new TextEvent(event, trackIndex);
                this.textEvents.add(new TextEvent(event, trackIndex));
            }
            case Constants.TRACK_NAME -> {
                newEvent = new TrackNameEvent(event, trackIndex);
                this.trackNameEvents.add( new TrackNameEvent(event, trackIndex) );
            }
            case Constants.SET_TEMPO -> {
                newEvent = new SetTempoEvent(event, trackIndex);
                this.setTempoEvents.add( new SetTempoEvent(event, trackIndex) );
            }
            case Constants.TIME_SIGNATURE -> {
                newEvent = new TimeSignatureEvent(event, trackIndex);
                this.timeSignatureEvents.add( new TimeSignatureEvent(event, trackIndex) );
            }
            case Constants.KEY_SIGNATURE -> {
                newEvent = new KeySignatureEvent(event, trackIndex);
                this.keySignatureEvents.add( new KeySignatureEvent(event, trackIndex) );
            }
            case Constants.PORT_PREFIX, Constants.END_OF_TRACK -> {
                return null;
            }
            default -> throw new IllegalArgumentException("UNKNOWN META MESSAGE TYPE: 0x" + Integer.toHexString(metaMessageType));
        }

        return newEvent;

    }


    //private void pairNoteEvents() {
    //
    //    ArrayList<NoteEvent> unpaired = new ArrayList<>();
    //
    //    this.noteOnEvents.sort(null);
    //    this.noteOffEvents.sort(null);
    //
    //    for (int i = 0; i < noteOnEvents.size(); i++) {
    //        NoteOnEvent on = noteOnEvents.get(i);
    //
    //        for (int j = i; j < noteOffEvents.size(); j++) {
    //            NoteOffEvent off = noteOffEvents.get(j);
    //
    //            if (on.pitch() == off.pitch() &&  on.tick() < off.tick()) {
    //
    //                if (on.tick() >= off.tick()) {
    //                    System.out.println();
    //                }
    //
    //
    //                on.assignPartner(off);
    //                off.assignPartner(on);
    //                break;
    //            }
    //
    //            if (j == noteOffEvents.size() - 1) {
    //                unpaired.add(on);
    //                unpaired.add(off);
    //            }
    //
    //        }
    //
    //    }
    //
    //    System.out.println();
    //    assert unpaired.isEmpty();
    //
    //}


    //private void createNotes() {
    //
    //    this.notes = new ArrayList<>();
    //
    //    for (NoteOnEvent event : noteOnEvents) {
    //
    //        if (event.tick() >= event.partner().tick()) {
    //            System.out.println();
    //        }
    //
    //        Range range = new Range(event.tick(), event.partner().tick());
    //        this.notes.add( new Note(event.pitch, range) );
    //
    //    }
    //
    //}


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
                outTrack.add(track.get(i));
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