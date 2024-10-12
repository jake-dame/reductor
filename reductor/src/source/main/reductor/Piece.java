package reductor;

import javax.sound.midi.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import static reductor.DeepCopy.copySequence;


public class Piece {


    // This is needed for I/O
    File file;

    // These are probably unused but are remaining for now
    MidiFileFormat fileFormat;
    Integer fileType;

    // Sequence
    Sequence sequence;
    int resolution;

    // Raw midi events
    ArrayList<MidiEvent> events;
    ArrayList<MidiEvent> metaMidiEvents;
    ArrayList<MidiEvent> channelEvents;

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

    // Note value constants specific to this midi file
    // These are used to control the granularity of query windows
    // They are different for every Midi file and are based on the sequence's resolution
    int NOTE_WHOLE;
    int NOTE_HALF;
    int NOTE_QUARTER;
    int NOTE_8TH;
    int NOTE_16TH;
    int NOTE_32ND;
    int NOTE_64TH;
    int NOTE_128TH;


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
        this.events = new ArrayList<>();

        // Channel
        this.channelEvents = new ArrayList<>();
        this.noteOnEvents = new ArrayList<>();
        this.noteOffEvents = new ArrayList<>();
        this.controlChangeEvents = new ArrayList<>();
        this.programChangeEvents = new ArrayList<>();

        // Meta
        this.metaMidiEvents = new ArrayList<>();
        this.textEvents = new ArrayList<>();
        this.trackNameEvents = new ArrayList<>();
        this.setTempoEvents = new ArrayList<>();
        this.keySignatureEvents = new ArrayList<>();
        this.timeSignatureEvents = new ArrayList<>();

        sortEventsIntoLists();

        assignRhythmValues();

        this.notes = Conversion.eventsToNotes(this.channelEvents);

        this.tree = new IntervalTree(this.notes);

    }


    private void assignRhythmValues() {

        // "Conventional" resolution is 480 ticks per quarter note:
        this.NOTE_WHOLE = -1;   // 1920
        this.NOTE_HALF = -1;    // 960
        this.NOTE_QUARTER = -1; // 480 (resolution of Sequence)
        this.NOTE_8TH = -1;     // 240
        this.NOTE_16TH = -1;    // 120
        this.NOTE_32ND = -1;    // 60
        this.NOTE_64TH = -1;    // 30
        this.NOTE_128TH = -1;   // 15

        this.resolution = this.sequence.getResolution();

        if (this.resolution < 1) {
            throw new RuntimeException("invalid resolution (>1): " + this.resolution);
        }

        if (resolution > Integer.MAX_VALUE / 4) {
            throw new RuntimeException("resolution too high to be represented as a whole note: " + this.resolution);
        }

        this.NOTE_QUARTER = this.resolution;
        this.NOTE_HALF = this.resolution * 2;
        this.NOTE_WHOLE = this.resolution * 4;


        if ((resolution / 2) >= 15) {
            this.NOTE_8TH = this.resolution / 2;
        } else {
            return;
        }

        if ((resolution / 4) >= 15) {
            this.NOTE_16TH = this.resolution / 4;
        } else {
            return;
        }

        if ((resolution / 8) >= 15) {
            this.NOTE_32ND = this.resolution / 8;
        } else {
            return;
        }

        if ((resolution / 16) >= 15) {
            this.NOTE_64TH = this.resolution / 16;
        } else {
            return;
        }

        if ((resolution / 32) >= 15) {
            this.NOTE_128TH = this.resolution / 32;
        }

    }

    private void sortEventsIntoLists() {

        Track[] tracks = this.sequence.getTracks();

        for (int trackIndex = 0; trackIndex < tracks.length; trackIndex++) {
            Track track = tracks[trackIndex];

            for (int eventIndex = 0; eventIndex < track.size(); eventIndex++) {
                MidiEvent event = track.get(eventIndex);

                events.add(event);

                switch (event.getMessage()) {
                    case ShortMessage _ -> { sortChannelEvents(event, trackIndex); }
                    case MetaMessage _ -> { sortMetaEvents(event, trackIndex); }
                    default -> { throw new RuntimeException("Event with sysex or unknown message type: " + event.getMessage()); }
                }

            }

        }

    }

    private void sortChannelEvents(MidiEvent event, int trackIndex) {

        ShortMessage message = (ShortMessage) event.getMessage();
        int shortMessageType = message.getCommand();

        this.channelEvents.add(event);

        switch (shortMessageType) {
            case ShortMessage.NOTE_ON -> {

                if (message.getData2() == 0) {
                    try {
                        message.setMessage(Constants.NOTE_OFF, message.getChannel(), message.getData1(), message.getData2());
                    } catch (InvalidMidiDataException e) {
                        throw new RuntimeException(e);
                    }
                    this.noteOffEvents.add( new NoteOffEvent(event, trackIndex) );
                } else {
                    this.noteOnEvents.add( new NoteOnEvent(event, trackIndex) );
                }

            }
            case ShortMessage.NOTE_OFF -> { this.noteOffEvents.add( new NoteOffEvent(event, trackIndex) ); }
            case ShortMessage.CONTROL_CHANGE -> { this.controlChangeEvents.add( new ControlChangeEvent(event, trackIndex) ); }
            case ShortMessage.PROGRAM_CHANGE -> { this.programChangeEvents.add( new ProgramChangeEvent(event, trackIndex) ); }
            case ShortMessage.PITCH_BEND -> { int num = 42; }
            default -> { throw new IllegalArgumentException("UNKNOWN SHORT MESSAGE TYPE: 0x" + Integer.toHexString(shortMessageType)); }
        }


    }

    private void sortMetaEvents(MidiEvent event, int trackIndex) {

        MetaMessage message = (MetaMessage) event.getMessage();
        int metaMessageType = message.getType();
        byte[] data = message.getData();

        this.metaMidiEvents.add(event);

        switch (metaMessageType) {
            case Constants.TEXT -> { this.textEvents.add(new TextEvent(event, trackIndex)); }
            case Constants.TRACK_NAME -> { this.trackNameEvents.add( new TrackNameEvent(event, trackIndex) ); }
            case Constants.PORT_PREFIX -> { int num = 42; }
            case Constants.END_OF_TRACK -> { int num = 43; }
            case Constants.SET_TEMPO -> { this.setTempoEvents.add( new SetTempoEvent(event, trackIndex) ); }
            case Constants.TIME_SIGNATURE -> { this.timeSignatureEvents.add( new TimeSignatureEvent(event, trackIndex) ); }
            case Constants.KEY_SIGNATURE -> { this.keySignatureEvents.add( new KeySignatureEvent(event, trackIndex) ); }
            default -> { throw new IllegalArgumentException("UNKNOWN META MESSAGE TYPE: 0x" + Integer.toHexString(metaMessageType)); }
        }

    }

    public void play() {
        ReductorUtil.play(sequence);
    }

    public File write() {
        return ReductorUtil.write(sequence, file.getName());
    }


    //region <Getters>


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


    //end region


    /// Allows the user to scale the tempo up/down
    public void scaleTempo(float scale) {

        // TODO some kind of check to make sure scale doesn't push things out of range
        for (SetTempoEvent event : this.setTempoEvents) {
            int newBpm = (int) (event.bpm * scale);
            event.setData(newBpm);
        }

    }


























    ArrayList<Chord> getChords(int windowSize) {

        ArrayList<Chord> chords = new ArrayList<>();

        long length = lengthInTicks();
        System.out.println("LENGTH: " + length + " ticks");

        long windowMin = 0;
        long windowMax = windowSize;

        while (windowMax <= length) {

            Range window = new Range(windowMin, windowMax - 1);

            ArrayList<Note> matches = this.tree.query(window);

            Chord chord = treeQueryToChord(matches, windowMin, windowMax);

            chords.add(chord);
            windowMin += windowSize;
            windowMax += windowSize;

        }

        for(Chord chord : chords) {
            System.out.println(chord + "");
        }

        return chords;
    }

    static Chord treeQueryToChord(ArrayList<Note> matches, long tick, long othertick) {

        ArrayList<Note> notes = new ArrayList<>(matches);

        return new Chord(notes, tick, othertick);
    }



    //Piece getAggregate() {
    //
    //    Sequence sequenceIn = getSequence();
    //
    //    Sequence newSequence;
    //    try {
    //
    //        newSequence = new Sequence(
    //                sequenceIn.getDivisionType(), sequenceIn.getResolution(), 1
    //        );
    //
    //        for (int t = 0; t < sequenceIn.getTracks().length; t++) {
    //
    //            Track track = sequenceIn.getTracks()[t];
    //
    //            for (int e = 0; e < track.size(); e++) {
    //
    //                MidiEvent event = track.get(e);
    //                MidiMessage msg = track.get(e).getMessage();
    //
    //                // Avoid adding meta events needed by tracks that no longer exist
    //                if (t > 0 && msg instanceof MetaMessage) continue;
    //
    //                // Avoid adding short events for tracks that no longer exist
    //                if (msg instanceof ShortMessage shortMessage) {
    //
    //                    if (t > 0 && (shortMessage.getCommand() == CONTROL_CHANGE
    //                            || shortMessage.getCommand() == PROGRAM_CHANGE)) {
    //                        continue;
    //                    }
    //                    else {
    //                        sanitizeShortMessage(shortMessage);
    //                    }
    //
    //                }
    //
    //                Track newTrack = newSequence.getTracks()[0];
    //
    //                // This adds in increasing tick order and throws away duplicates
    //                newTrack.add(event);
    //            }
    //        }
    //
    //    } catch (InvalidMidiDataException e) {
    //        throw new RuntimeException(e);
    //    }
    //
    //    return new Piece(newSequence, "AGG_" + name());
    //}



}