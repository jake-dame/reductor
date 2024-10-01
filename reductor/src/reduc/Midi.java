package reduc;

import javax.sound.midi.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import static javax.sound.midi.ShortMessage.*;

// TODO: I don't know how to make this class's constructors not be so duplicate codey

/*
    Purpose: Represent, but not manipulate, a Java/Midi Sequence

    This is essentially a wrapper class for a Java Sequence that, while providing info
    already mostly provided by the vanilla Sequence class, provides extras and handles
    access more specific to this application's needs.
*/
public class Midi {

    private final File file;

    final MidiFileFormat fileFormat;
    final Integer fileType;

    private final Sequence sequence;
    private final Track[] tracks;

    private final int resolution;
    final int NOTE_WHOLE;
    final int NOTE_HALF;
    final int NOTE_QUARTER;
    final int NOTE_8TH;
    final int NOTE_16TH;
    final int NOTE_32ND;
    final int NOTE_64TH;
    final int NOTE_128TH;

    private int bpm;

    private final ArrayList<MidiEvent> textEvents;
    private final ArrayList<MidiEvent> trackNameEvents;
    private final ArrayList<MidiEvent> setTempoEvents;
    private final ArrayList<MidiEvent> keySignatureEvents;
    private final ArrayList<MidiEvent> timeSignatureEvents;

    private final ArrayList<MidiEvent> allEvents;
    private final ArrayList<MidiEvent> metaEvents;
    private final ArrayList<MidiEvent> noteEvents;
    private final ArrayList<MidiEvent> otherShortEvents;
    private final ArrayList<MidiEvent> sysexEvents;

    private final ArrayList<Note> notes;

    Midi(String filepath) {

        this.file = new File(filepath);

        try {
            this.sequence = MidiSystem.getSequence(this.file);
            if (this.sequence.getDivisionType() != Sequence.PPQ) {
                throw new RuntimeException("This program does not currently support SMPTE timing");
            }
            this.fileFormat = MidiSystem.getMidiFileFormat(this.file);
            this.fileType = fileFormat.getType();
        }
        catch (InvalidMidiDataException | IOException e) {
            throw new RuntimeException(e);
        }

        this.tracks = sequence.getTracks();

        this.resolution = sequence.getResolution();
        NOTE_WHOLE = resolution * 4;
        NOTE_HALF = resolution * 2;
        NOTE_QUARTER = resolution;
        NOTE_8TH = resolution / 2;
        NOTE_16TH = resolution / 4;
        NOTE_32ND = resolution / 8;
        NOTE_64TH = resolution / 16;
        NOTE_128TH = resolution / 32;

        this.setTempoEvents = new ArrayList<>();
        this.keySignatureEvents = new ArrayList<>();
        this.timeSignatureEvents = new ArrayList<>();
        this.trackNameEvents = new ArrayList<>();
        this.textEvents = new ArrayList<>();

        this.allEvents = new ArrayList<>();
        this.metaEvents = new ArrayList<>();
        this.noteEvents = new ArrayList<>();
        this.otherShortEvents = new ArrayList<>();
        this.sysexEvents = new ArrayList<>();
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
        } else {
            this.file = new File(name + ".mid");
        }

        this.sequence = sequence; // TODO: find workaround
        if (this.sequence.getDivisionType() != Sequence.PPQ) {
            throw new RuntimeException("This program does not currently support SMPTE timing");
        }
        this.fileFormat = null;
        this.fileType = null;

        this.tracks = sequence.getTracks();
        this.resolution = sequence.getResolution();
        NOTE_WHOLE = resolution * 4; // TODO: quarter == 120 in bach inv, meaning integer division on bottom two
        NOTE_HALF = resolution * 2;
        NOTE_QUARTER = resolution;
        NOTE_8TH = resolution / 2;
        NOTE_16TH = resolution / 4;
        NOTE_32ND = resolution / 8;
        NOTE_64TH = resolution / 16;
        NOTE_128TH = resolution / 32;

        this.setTempoEvents = new ArrayList<>();
        this.keySignatureEvents = new ArrayList<>();
        this.timeSignatureEvents = new ArrayList<>();
        this.trackNameEvents = new ArrayList<>();
        this.textEvents = new ArrayList<>();

        this.allEvents = new ArrayList<>();
        this.metaEvents = new ArrayList<>();
        this.noteEvents = new ArrayList<>();
        this.otherShortEvents = new ArrayList<>();
        this.sysexEvents = new ArrayList<>();
        sortEventsIntoLists();

        this.notes = Note.eventsToNotes(getNoteEvents());
    }

    private void sortEventsIntoLists() {

        for (Track track : sequence.getTracks()) {
            for (int eventIndex = 0; eventIndex < track.size(); eventIndex++) {
                MidiEvent event = track.get(eventIndex);
                allEvents.add(event);
                switch (event.getMessage()) {
                    case ShortMessage shortMessage -> {
                        if (shortMessage.getCommand() == NOTE_ON
                            || shortMessage.getCommand() == NOTE_OFF) {
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

        if (metaMessageType >= 0x20 && metaMessageType < 0x2F) { // Channel prefix
            // DO NOT DELETE: channel 1 (0x20 + 0x01) is the only channel I've seen so far?
            if (metaMessageType != 0x21) {
                System.out.println("metaMessageType value: "
                        + Integer.toHexString(metaMessageType)
                );
            }
        }
        else {
            // DO NOT REMOVE THE BRACKET STRUCTURE FROM ANY OF THESE you will probably add more later
            switch (metaMessageType) {
                case ReductorUtil.MESSAGE_TYPE_TEXT -> {
                    this.textEvents.add(event);
                }
                case ReductorUtil.MESSAGE_TYPE_TRACK_NAME -> {
                    this.trackNameEvents.add(event);
                }
                case ReductorUtil.MESSAGE_TYPE_END_OF_TRACK -> {
                    // DO NOT DELETE I won't do anything with this message but the default needs it
                }
                case ReductorUtil.MESSAGE_TYPE_SET_TEMPO -> {
                    this.setTempoEvents.add(event);
                    this.bpm = ReductorUtil.convertMicrosecondsToBPM(event);
                }
                case ReductorUtil.MESSAGE_TYPE_TIME_SIGNATURE -> {
                    this.timeSignatureEvents.add(event);
                }
                case ReductorUtil.MESSAGE_TYPE_KEY_SIGNATURE -> {
                    this.keySignatureEvents.add(event);
                }
                default -> {
                    throw new IllegalArgumentException("Unknown metamessage: " + metaMessageType);
                }
            }
        }
    }

    public void play() { ReductorUtil.play(sequence); }

    public File writeOut() { return ReductorUtil.write(sequence, file.getName()); }

    // * SETTERS ******************************************************************************************************/

    /*
        Sets the tempo for THE ENTIRE SEQUENCE (overwriting interspersed tempo messages).
    */
    public void setTempo(int bpm) {

        final int setTempoMetaMessageType = 0x51;
        byte[] data = ReductorUtil.convertBPMToMicroseconds(bpm);
        final int setTempoMetaMessageLength = 3;

        for (MidiEvent event : this.setTempoEvents) {
            MetaMessage msg  = (MetaMessage) event.getMessage();
            if(msg.getStatus() != ReductorUtil.MESSAGE_TYPE_SET_TEMPO) { // this if can probably be deleted later
                try {
                    msg.setMessage(setTempoMetaMessageType, data, setTempoMetaMessageLength);
                } catch (InvalidMidiDataException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    // * GETTERS ******************************************************************************************************/

    public String getName() { return file.getName().split("\\.")[0]; }

    public int getResolution() { return resolution; }

    public long getLengthInTicks() { return sequence.getTickLength(); }

    public long getLengthInMicroseconds() { return sequence.getMicrosecondLength(); }

    public float getDivisionType() { return sequence.getDivisionType(); }

    /* To set tempo, you can get the bpm here, NOT the actual events. setTempo() takes an int bpm as well. */
    public int getTempo() { return this.bpm; }

    ArrayList<MidiEvent> getAllEvents() { return new ArrayList<>(allEvents); }

    ArrayList<MidiEvent> getNoteEvents() { return new ArrayList<>(noteEvents); }

    ArrayList<MidiEvent> getMetaEvents() { return new ArrayList<>(metaEvents); }

    ArrayList<MidiEvent> getOtherShortEvents() { return new ArrayList<>(otherShortEvents); }

    ArrayList<MidiEvent> getSysexEvents() { return new ArrayList<>(sysexEvents); }

    // NOT SAFE
    Sequence getSequence() { return sequence; }

}