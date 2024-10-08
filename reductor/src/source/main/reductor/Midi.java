package reductor;

import javax.sound.midi.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import static reductor.DeepCopy.copyEvents;
import static reductor.DeepCopy.copySequence;
import static reductor.ReductorUtil.*;

// TODO: I would like to rename this. MIDInterface doesn't really describe this class.
//  Really it should be named "Sequence" but that is the same as the javax.midi one...

/**
 * Represents a Java/Midi Sequence.
 * <p>
 * This is essentially a wrapper class for a Java {@link javax.sound.midi.Sequence Sequence} that, while providing
 * read-only info already mostly provided by the vanilla {@link javax.sound.midi.Sequence Sequence} class, is objectively
 * cooler (i.e. provides extra features and handles data accesses more specific to this application's needs).
 * </p>
 * */
public class Midi {

    private final File file;

    final MidiFileFormat fileFormat;
    final Integer fileType;

    private final Sequence sequence;
    private final Track[] tracks;

    final int NOTE_WHOLE;
    final int NOTE_HALF;
    final int NOTE_QUARTER;
    final int NOTE_8TH;
    final int NOTE_16TH;
    final int NOTE_32ND;
    final int NOTE_64TH;
    final int NOTE_128TH;

    private final ArrayList<Integer> bpmList = new ArrayList<>();

    // TODO: Adding a measure indexing/count would be a cool feature in the end;
    //  would need to take advantage of timeSignatureEvents. Pickup measures (musicxml vis-a-vis MIDI)?

    private final ArrayList<MidiEvent> textEvents = new ArrayList<>();
    private final ArrayList<MidiEvent> trackNameEvents = new ArrayList<>();
    private final ArrayList<MidiEvent> setTempoEvents = new ArrayList<>();
    private final ArrayList<MidiEvent> keySignatureEvents = new ArrayList<>();
    private final ArrayList<MidiEvent> timeSignatureEvents = new ArrayList<>();

    private final ArrayList<MidiEvent> allEvents = new ArrayList<>();
    private final ArrayList<MidiEvent> metaEvents = new ArrayList<>();
    private final ArrayList<MidiEvent> noteEvents = new ArrayList<>();
    private final ArrayList<MidiEvent> otherShortEvents = new ArrayList<>();
    private final ArrayList<MidiEvent> sysexEvents = new ArrayList<>();

    private final ArrayList<Note> notes;

    Midi(String filepath) {

        this.file = new File(filepath);

        try {
            this.sequence = MidiSystem.getSequence(this.file);

            if (this.sequence.getDivisionType() != Sequence.PPQ) {
                throw new RuntimeException("This program does not currently support SMPTE timing");
            }

            this.fileFormat = MidiSystem.getMidiFileFormat(this.file);
        }
        catch (InvalidMidiDataException | IOException e) {
            throw new RuntimeException(e);
        }

        this.fileType = fileFormat.getType();

        this.tracks = sequence.getTracks();

        int resolution = sequence.getResolution();
        NOTE_WHOLE = resolution * 4;
        NOTE_HALF = resolution * 2;
        NOTE_QUARTER = resolution;
        NOTE_8TH = resolution / 2;
        NOTE_16TH = resolution / 4;
        NOTE_32ND = resolution / 8;
        NOTE_64TH = resolution / 16;
        NOTE_128TH = resolution / 32;

        sortEventsIntoLists();

        // TODO: either sanitize in here or make notes not a part of
        //  the midi (only part of interval tree). If this is called before
        //  the sanitization in Reductor, it will throw exceptions in the conversion function.
        //this.notes = Note.eventsToNotes(noteEvents);
        this.notes = null;
    }

    Midi(Sequence sequence, String name) {

        if(name.contains(".") || name.contains(" ") || name.contains("/")) {
            throw new IllegalArgumentException(
                    "name should just be a unique identifier, not in filename form"
            );
        }
        this.file = new File(name + ".mid");

        this.sequence = copySequence(sequence);
        if (this.sequence.getDivisionType() != Sequence.PPQ) {
            throw new RuntimeException("This program does not currently support SMPTE timing");
        }

        this.fileFormat = null;
        this.fileType = null;

        this.tracks = sequence.getTracks();
        int resolution = sequence.getResolution();
        // TODO: quarter == 120 in bach inv, meaning integer division on bottom two,
        //  so this all needs to be fixed OR research how fractional tick values are
        //  handled in MIDI and allow them here too
        NOTE_WHOLE = resolution * 4;
        NOTE_HALF = resolution * 2;
        NOTE_QUARTER = resolution;
        NOTE_8TH = resolution / 2;
        NOTE_16TH = resolution / 4;
        NOTE_32ND = resolution / 8;
        NOTE_64TH = resolution / 16;
        NOTE_128TH = resolution / 32;

        sortEventsIntoLists();

        this.notes = midiEventsToNotes(getNoteEvents());
    }

    private void sortEventsIntoLists() {

        // TODO: I could just do Note construction here? But would make this a really heavy loop/set of functions.
        for (Track track : sequence.getTracks()) {
            for (int eventIndex = 0; eventIndex < track.size(); eventIndex++) {
                MidiEvent event = track.get(eventIndex);
                allEvents.add(event);
                switch (event.getMessage()) {
                    case ShortMessage shortMessage -> {
                        if (shortMessage.getCommand() == MessageType.NOTE_ON
                                || shortMessage.getCommand() == MessageType.NOTE_OFF) {
                            noteEvents.add(event);
                        } else {
                            otherShortEvents.add(event);
                        }
                    }
                    case MetaMessage msg -> {
                        metaEvents.add(event);
                        sortMetaMessages(event);
                    }
                    case SysexMessage _ -> {
                        sysexEvents.add(event);
                    }
                    default -> {
                        throw new IllegalArgumentException("unknown event type: " + event.getMessage());
                    }
                }
            }
        }
    }

    private void sortMetaMessages(MidiEvent event) {

        MetaMessage message = (MetaMessage) event.getMessage();

        int metaMessageType = message.getType();
        byte[] data = message.getData();

        // Channel prefix
        if (metaMessageType >= 0x20 && metaMessageType < 0x2F) {
            // DO NOT DELETE: channel 1 (0x20 + 0x01) is the only channel I've seen so far?
            if (metaMessageType != 0x21) {
                System.out.println("metaMessageType value: "
                        + Integer.toHexString(metaMessageType)
                );
            }
        }
        else {
            switch (metaMessageType) {
                case MessageType.TEXT -> {
                    this.textEvents.add(event);
                }
                case MessageType.TRACK_NAME -> {
                    this.trackNameEvents.add(event);
                }
                case MessageType.END_OF_TRACK -> {
                    // DO NOT DELETE
                }
                case MessageType.SET_TEMPO -> {
                    this.setTempoEvents.add(event);
                    byte[] tempoArray = ((MetaMessage) event.getMessage()).getData();
                    this.bpmList.add( convertMicrosecondsToBPM(tempoArray) );
                }
                case MessageType.TIME_SIGNATURE -> {
                    this.timeSignatureEvents.add(event);
                }
                case MessageType.KEY_SIGNATURE -> {
                    this.keySignatureEvents.add(event);
                }
                default -> {
                    throw new IllegalArgumentException("Unknown metamessage: " + metaMessageType);
                }
            }
        }
    }

    public void playSequence() {
        play(sequence);
    }

    public File writeOut() {
        return write(sequence, file.getName());
    }

    //region <Setters>

    /** Allows the user to scale the tempo up/down. */
    public void setTempo(int scale) {

        final int setTempoMetaMessageLength = 3;

        for (MidiEvent event : this.setTempoEvents) {
            MetaMessage msg  = (MetaMessage) event.getMessage();
            int bpm = convertMicrosecondsToBPM(msg.getData());
            byte[] scaledData = convertBPMToMicroseconds(bpm * scale);
            if (msg.getStatus() == MessageType.SET_TEMPO) {
                try {
                    msg.setMessage(MessageType.SET_TEMPO, scaledData, setTempoMetaMessageLength);
                } catch (InvalidMidiDataException e) {
                    throw new RuntimeException(e);
                }
            }
        }

    }

    /**
     * Sets the tempo for the entire sequence, regardless of any set tempo message's position.
     * */
    public void setTempoGlobal(int bpm) {

        byte[] data = convertBPMToMicroseconds(bpm);
        final int setTempoMetaMessageLength = 3;

        for (MidiEvent event : this.setTempoEvents) {
            MetaMessage msg  = (MetaMessage) event.getMessage();
            if (msg.getStatus() == MessageType.SET_TEMPO) {
                try {
                    msg.setMessage(MessageType.SET_TEMPO, data, setTempoMetaMessageLength);
                } catch (InvalidMidiDataException e) {
                    throw new RuntimeException(e);
                }
            }
        }

    }

    //endregion

    //region <Getters>

    Sequence getSequence() {
        return copySequence(sequence);
    }

    /** Returns the vanilla name (not in filename form) */
    public String getName() {
        return file.getName().split("\\.")[0];
    }

    public int getResolution() {
        return sequence.getResolution();
    }

    public long getLengthInTicks() {
        return sequence.getTickLength();
    }

    public long getLengthInMicroseconds() {
        return sequence.getMicrosecondLength();
    }

    public float getDivisionType() {
        return sequence.getDivisionType();
    }

    // for development
    public void getActuallyPrintBpms() {
        for(Integer marking : bpmList) {
            System.out.println("BPM: " + marking);
        }
    }

    ArrayList<MidiEvent> getAllEvents() {
        return copyEvents(allEvents);
    }

    ArrayList<MidiEvent> getNoteEvents() {
        return copyEvents(noteEvents);
    }

    ArrayList<MidiEvent> getMetaEvents() {
        return copyEvents(metaEvents);
    }

    ArrayList<MidiEvent> getOtherShortEvents() {
        return copyEvents(otherShortEvents);
    }

    ArrayList<MidiEvent> getSysexEvents() {
        return copyEvents(sysexEvents);
    }

    ArrayList<MidiEvent> getSetTempoEvents() {
        return copyEvents(setTempoEvents);
    }

    ArrayList<MidiEvent> getTimeSignatureEvents() {
        return copyEvents(timeSignatureEvents);
    }

    ArrayList<MidiEvent> getKeySignatureEvents() {
        return copyEvents(keySignatureEvents);
    }
    
    ArrayList<MidiEvent> getTextEvents() {
        return copyEvents(textEvents);
    }
    
    ArrayList<MidiEvent> getTrackEvents() {
        return copyEvents(trackNameEvents);
    }

    //endregion

}