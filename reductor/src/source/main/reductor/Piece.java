package reductor;

import javax.sound.midi.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

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
    Track[] tracks;

    // debug
    ArrayList<MidiEvent> midiEvents;

    // Composite lists
    ArrayList<Event<?>> allEvents;
    ArrayList<MetaEvent> allMetaEvents;
    ArrayList<ChannelEvent> allChannelEvents;

    // Meta events
    ArrayList<TextEvent> textEvents;
    ArrayList<CopyrightNoticeEvent> copyrightNoticeEvents;
    ArrayList<TrackNameEvent> trackNameEvents;
    ArrayList<InstrumentNameEvent> instrumentNameEvents;
    ArrayList<SetTempoEvent> setTempoEvents;
    ArrayList<SMPTEOffsetEvent> smpteOffsetEvents;
    ArrayList<KeySignatureEvent> keySignatureEvents;
    ArrayList<TimeSignatureEvent> timeSignatureEvents;
    ArrayList<PortChangeEvent> portChangeEvents;
    ArrayList<EndOfTrackEvent> endOfTrackEvents;
    ArrayList<SequencerSpecificEvent> sequencerSpecificEvents;

    // Channel events
    ArrayList<NoteOnEvent> noteOnEvents;
    ArrayList<NoteOffEvent> noteOffEvents;
    ArrayList<ControlChangeEvent> controlChangeEvents;
    ArrayList<ProgramChangeEvent> programChangeEvents;


    // List of Notes
    ArrayList<Note> notes;

    IntervalTree<Note> tree;


    Piece(String filepath) throws InvalidMidiDataException, IOException {

        this.file = new File(filepath);

        this.sequence = MidiSystem.getSequence(this.file);
        if (this.sequence.getDivisionType() != Sequence.PPQ) {
            throw new RuntimeException("This program does not currently support SMPTE timing");
        }

        this.tracks = this.sequence.getTracks();

        this.fileFormat = MidiSystem.getMidiFileFormat(this.file);

        this.fileType = fileFormat.getType();

        this.allEvents = new ArrayList<>();

        this.allChannelEvents = new ArrayList<>();
        this.noteOnEvents = new ArrayList<>();
        this.noteOffEvents = new ArrayList<>();
        this.controlChangeEvents = new ArrayList<>();
        this.programChangeEvents = new ArrayList<>();

        this.allMetaEvents = new ArrayList<>();
        this.textEvents = new ArrayList<>();
        this.copyrightNoticeEvents = new ArrayList<>();
        this.trackNameEvents = new ArrayList<>();
        this.instrumentNameEvents = new ArrayList<>();
        this.setTempoEvents = new ArrayList<>();
        this.smpteOffsetEvents = new ArrayList<>();
        this.keySignatureEvents = new ArrayList<>();
        this.timeSignatureEvents = new ArrayList<>();
        this.portChangeEvents = new ArrayList<>();
        this.endOfTrackEvents = new ArrayList<>();
        this.sequencerSpecificEvents = new ArrayList<>();

        this.notes = new ArrayList<>();

        EventSorter sorter = new EventSorter();
        sorter.sortEvents();

        //region <Hacky>
        ArrayList<Note> temp = new ArrayList<>(this.notes);
        HashSet<Note> set = new HashSet<>(temp);
        this.notes = new ArrayList<>(set);
        //endregion

        this.tree = new IntervalTree<>(this.notes);
        if (this.notes.size() != this.tree.numElements()) {
            throw new RuntimeException("tree elements doesn't match notes");
        }

        //region <development/debug>
        this.midiEvents = new ArrayList<>();
        checkDataStrings();
        RESOLUTION = sequence.getResolution();
        //endregion

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

                // Remove the channel parameters to revert
                ShortMessage onMessage = new ShortMessage(NOTE_ON, note.originalChannel, note.pitch(), medianVelocity);
                MidiEvent noteOnEvent = new MidiEvent(onMessage, note.start());
                outList.add(noteOnEvent);

                ShortMessage offMessage = new ShortMessage(NOTE_OFF, note.originalChannel, note.pitch(), 0);
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
            outList.add( event.event() );
        }

        return outList;

    }


    private class EventSorter {

        ArrayList<NoteOnEvent> unpairedNoteOns;

        TrackNameEvent currTrackName;
        KeySignatureEvent currKeySignature;
        TimeSignatureEvent currTimeSignature;

        EventSorter() {
            unpairedNoteOns = new ArrayList<>();
            currTrackName = null;
            currKeySignature = null;
            currTimeSignature = null;
        }

        private void sortEvents() throws InvalidMidiDataException {

            for (int trackIndex = 0; trackIndex < Piece.this.tracks.length; trackIndex++) {

                Track track = Piece.this.tracks[trackIndex];

                for (int eventIndex = 0; eventIndex < track.size(); eventIndex++) {

                    Event<?> newEvent = Event.createEvent( track.get(eventIndex) );

                    newEvent.setTrackIndex(trackIndex);
                    newEvent.setTrackNameEvent(this.currTrackName != null ? this.currTrackName : null);

                    Piece.this.allEvents.add(newEvent);

                    MessageType type = newEvent.type();

                    if (newEvent instanceof ChannelEvent) {

                        Piece.this.allChannelEvents.add((ChannelEvent) newEvent);

                        switch(type) {
                            case NOTE_ON, NOTE_OFF -> handleNoteEvents((NoteEvent) newEvent);
                            case CONTROL_CHANGE -> Piece.this.controlChangeEvents.add((ControlChangeEvent) newEvent);
                            case PROGRAM_CHANGE -> Piece.this.programChangeEvents.add((ProgramChangeEvent) newEvent);
                        }

                    } else if (newEvent instanceof MetaEvent) {

                        Piece.this.allMetaEvents.add((MetaEvent) newEvent);

                        switch(type) {
                            case TEXT -> Piece.this.textEvents.add((TextEvent) newEvent);
                            case TRACK_NAME -> {
                                Piece.this.trackNameEvents.add((TrackNameEvent) newEvent);
                                this.currTrackName = (TrackNameEvent) newEvent;
                            }
                            case PORT_CHANGE -> Piece.this.portChangeEvents.add((PortChangeEvent) newEvent);
                            case END_OF_TRACK -> Piece.this.endOfTrackEvents.add((EndOfTrackEvent) newEvent);
                            case SET_TEMPO -> Piece.this.setTempoEvents.add((SetTempoEvent) newEvent);
                            case TIME_SIGNATURE -> {
                                Piece.this.timeSignatureEvents.add((TimeSignatureEvent) newEvent);
                                this.currTimeSignature = (TimeSignatureEvent) newEvent;
                            }
                            case KEY_SIGNATURE -> {
                                Piece.this.keySignatureEvents.add((KeySignatureEvent) newEvent);
                                this.currKeySignature = (KeySignatureEvent) newEvent;
                            }
                        }

                    }

                }

            }

        }

        private void handleNoteEvents(NoteEvent event) {

            if (event instanceof NoteOnEvent on) {
                Piece.this.noteOnEvents.add(on);
                this.unpairedNoteOns.add(on);
            } else if (event instanceof NoteOffEvent off) {
                Piece.this.noteOffEvents.add(off);
                this.findNoteOn(off);
            } else {
                throw new RuntimeException();
            }

        }

        private void findNoteOn(NoteOffEvent off) {

            for (int i = this.unpairedNoteOns.size() - 1; i >= 0; i--) {
                NoteOnEvent on = this.unpairedNoteOns.get(i);
                assert !on.paired();

                if (on.pitch() == off.pitch()  &&  on.tick() < off.tick()) {
                    this.unpairedNoteOns.remove(on);

                    NoteEvent.assignPartners(on, off);
                    on.setKeySignature(this.currKeySignature);
                    on.setTimeSignature(this.currTimeSignature);

                    createNote(on, off);

                    return;
                }

            }

            throw new RuntimeException("reached end of list and NOTE ON not found");

        }

        private void createNote(NoteOnEvent on, NoteOffEvent off) {
            Range range = new Range(on.tick(), off.tick());
            Note note = new Note(on.pitch(), range);
            Piece.this.notes.add(note);
        }

    }


    //private class EventSorter {
    //
    //    ArrayList<NoteOnEvent> unpairedNoteOns;
    //
    //    TrackNameEvent currentTrackName;
    //    KeySignatureEvent currentKeySignature;
    //    TimeSignatureEvent currentTimeSignature;
    //
    //    int currentTrackIndex;
    //
    //    long lastTick;
    //    long currentTick;
    //
    //
    //    EventSorter() {
    //
    //        unpairedNoteOns = new ArrayList<>();
    //        currentTrackName = null;
    //        currentKeySignature = null;
    //        currentTimeSignature = null;
    //
    //    }
    //
    //
    //    private void sortEvents() {
    //
    //        for (int trackIndex = 0; trackIndex < Piece.this.tracks.length; trackIndex++) {
    //
    //            Track track = Piece.this.tracks[trackIndex];
    //
    //            // Reset at 0 for every track
    //            lastTick = 0;
    //
    //            this.currentTrackIndex = trackIndex;
    //
    //            for (int eventIndex = 0; eventIndex < track.size(); eventIndex++) {
    //
    //                MidiEvent event = track.get(eventIndex);
    //
    //                currentTick = event.getTick();
    //                assert currentTick >= lastTick;
    //                Piece.this.midiEvents.add(event);
    //
    //                Event<?> newEvent;
    //
    //                MidiMessage message = event.getMessage();
    //
    //                switch (message) {
    //                    case ShortMessage _ -> {
    //                        newEvent = sortChannelEvent(event);
    //                        if (newEvent != null) {
    //                            Piece.this.allChannelEvents.add((ChannelEvent) newEvent);
    //                        }
    //                    }
    //                    case MetaMessage _ -> {
    //                        newEvent = sortMetaEvent(event);
    //                        if (newEvent != null) {
    //                            Piece.this.allMetaEvents.add((MetaEvent) newEvent);
    //                        }
    //                    }
    //                    default -> {
    //                        newEvent = null;// todo
    //                        //throw new RuntimeException("Event with sysex or unknown message type: " + event.getMessage());
    //                    }
    //                }
    //
    //                if (newEvent != null) {
    //                    Piece.this.allEvents.add(newEvent);
    //                    newEvent.setTrackNameEvent(this.currentTrackName);
    //                    newEvent.setTrackIndex(this.currentTrackIndex);
    //                }
    //
    //
    //                lastTick = currentTick;
    //
    //            }
    //
    //        }
    //
    //        assert unpairedNoteOns.isEmpty();
    //        assert allChannelEvents.size() + allMetaEvents.size() == allEvents.size();
    //        assert noteOnEvents.size() == noteOffEvents.size();
    //
    //        if (timeSignatureEvents.isEmpty()) {
    //            System.err.println("THIS MIDI FILE HAS NO TIME SIGNATURE EVENTS");
    //        }
    //
    //        if (keySignatureEvents.isEmpty()) {
    //            System.err.println("THIS MIDI FILE HAS NO KEY SIGNATURE EVENTS");
    //        }
    //
    //
    //    }
    //
    //
    //    private MetaEvent sortMetaEvent(MidiEvent event) {
    //
    //        MetaMessage message = (MetaMessage) event.getMessage();
    //        int metaMessageType = message.getType();
    //        MessageType type = MessageType.getEnumType(metaMessageType);
    //
    //        MetaEvent newEvent;
    //        switch (type) {
    //            case TEXT -> {
    //                newEvent = new TextEvent(event);
    //                Piece.this.textEvents.add((TextEvent) newEvent);
    //            }
    //            case COPYRIGHT_NOTICE -> {
    //                newEvent = new CopyrightNoticeEvent(event);
    //                Piece.this.copyrightNoticeEvents.add((CopyrightNoticeEvent) newEvent);
    //            }
    //            case TRACK_NAME -> {
    //                this.currentTrackName = new TrackNameEvent(event);
    //
    //                newEvent = new TrackNameEvent(event);
    //                Piece.this.trackNameEvents.add((TrackNameEvent) newEvent);
    //            }
    //            case INSTRUMENT_NAME -> {
    //                newEvent = new InstrumentNameEvent(event);
    //                Piece.this.instrumentNameEvents.add((InstrumentNameEvent) newEvent);
    //            }
    //            case SET_TEMPO -> {
    //                newEvent = new SetTempoEvent(event);
    //                Piece.this.setTempoEvents.add((SetTempoEvent) newEvent);
    //            }
    //            case TIME_SIGNATURE -> {
    //                this.currentTimeSignature = new TimeSignatureEvent(event);
    //
    //                newEvent = new TimeSignatureEvent(event);
    //                Piece.this.timeSignatureEvents.add((TimeSignatureEvent) newEvent);
    //            }
    //            case KEY_SIGNATURE -> {
    //                this.currentKeySignature = new KeySignatureEvent(event);
    //
    //                newEvent = new KeySignatureEvent(event);
    //                Piece.this.keySignatureEvents.add((KeySignatureEvent) newEvent);
    //            }
    //            case PORT_CHANGE -> {
    //                newEvent = new PortChangeEvent(event);
    //                Piece.this.portChangeEvents.add((PortChangeEvent) newEvent);
    //            }
    //            case END_OF_TRACK -> {
    //                newEvent = new EndOfTrackEvent(event);
    //                Piece.this.endOfTrackEvents.add((EndOfTrackEvent) newEvent);
    //            }
    //            case SEQUENCER_SPECIFIC -> {
    //                newEvent = new SequencerSpecificEvent(event);
    //                Piece.this.sequencerSpecificEvents.add((SequencerSpecificEvent) newEvent);
    //            }
    //            case SMPTE_OFFSET -> {
    //                newEvent = new SMPTEOffsetEvent(event);
    //                Piece.this.smpteOffsetEvents.add((SMPTEOffsetEvent) newEvent);
    //            }
    //            default -> {
    //                throw new RuntimeException("UNKNOWN META MESSAGE TYPE: 0x" + Integer.toHexString(metaMessageType));
    //            }
    //        }
    //
    //        return newEvent;
    //
    //    }
    //
    //
    //    private ChannelEvent sortChannelEvent(MidiEvent event) {
    //
    //        ShortMessage message = (ShortMessage) event.getMessage();
    //        int channelMessageType = sanitizeAnyNoteMessages(message).getCommand();
    //        MessageType type = MessageType.getEnumType(channelMessageType);
    //
    //        ChannelEvent newEvent;
    //        switch (type) {
    //            case NOTE_ON -> {
    //                newEvent = new NoteOnEvent(event);
    //                this.unpairedNoteOns.add((NoteOnEvent) newEvent);
    //                noteOnEvents.add((NoteOnEvent) newEvent);
    //            }
    //            case NOTE_OFF -> {
    //                newEvent = new NoteOffEvent(event);
    //                Piece.this.noteOffEvents.add((NoteOffEvent) newEvent);
    //                findNoteOn((NoteOffEvent) newEvent);
    //            }
    //            case CONTROL_CHANGE -> {
    //                newEvent = new ControlChangeEvent(event);
    //                controlChangeEvents.add((ControlChangeEvent) newEvent);
    //            }
    //            case PROGRAM_CHANGE -> {
    //                newEvent = new ProgramChangeEvent(event);
    //                programChangeEvents.add((ProgramChangeEvent) newEvent);
    //            }
    //            default -> {
    //                throw new RuntimeException("UNKNOWN SHORT MESSAGE TYPE: 0x" + Integer.toHexString(channelMessageType));
    //            }
    //        }
    //
    //        return newEvent;
    //
    //    }
    //
    //
    //    private ShortMessage sanitizeAnyNoteMessages(ShortMessage message) {
    //
    //        if (message.getCommand() == NOTE_ON  &&  message.getData2() == 0) {
    //            try {
    //                message.setMessage(NOTE_OFF, message.getChannel(), message.getData1(), message.getData2());
    //            } catch (InvalidMidiDataException e) {
    //                throw new RuntimeException(e);
    //            }
    //        }
    //
    //        return message;
    //
    //    }
    //
    //
    //    private void findNoteOn(NoteOffEvent off) {
    //
    //        for (int i = this.unpairedNoteOns.size() - 1; i >= 0; i--) {
    //            NoteOnEvent on = this.unpairedNoteOns.get(i);
    //            assert !on.paired();
    //
    //            if (on.pitch() == off.pitch()  &&  on.tick() < off.tick()) { // TODO <= || <
    //
    //                // TODO: you could make this some sort of special case?:
    //                //      Track 0
    //                //      C5 @ 0/0 : (C5 @ 0/0) : C5 @ 0/0 : (C5 @ 960/0) : C5 @ 960/0 : (C5 @ 1919/0) :
    //                //      ^^^^^^^^^^^^^^^^^^^^
    //
    //                this.unpairedNoteOns.remove(on);
    //
    //                NoteEvent.assignPartners(on, off);
    //                on.setKeySignature(this.currentKeySignature);
    //                on.setTimeSignature(this.currentTimeSignature);
    //
    //                createNote(on, off);
    //
    //                return;
    //            }
    //
    //        }
    //
    //        throw new RuntimeException("reached end of list and note on not found");
    //
    //    } // findNoteOn()
    //
    //    private void createNote(NoteOnEvent on, NoteOffEvent off) {
    //
    //        Range range = new Range(on.tick(), off.tick());
    //        // DON'T DELETE
    //        //Duration duration = new Duration(RESOLUTION, this.currentTimeSignature);
    //        //KeyContext keyContext = KeyContext.getKeyContextObject(this.currentKeySignature);
    //        //Degree degree = null;
    //
    //        Note note = new Note(on.pitch(), range);
    //        note.setChannel(on.channel());
    //
    //        Piece.this.notes.add(note);
    //
    //    }
    //
    //
    //} // EventSorter


}