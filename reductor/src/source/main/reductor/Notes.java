package reductor;

import javax.sound.midi.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/// This is a wrapper class for `ArrayList<Note>`. It makes conversion to ArrayList<MidiEvent> and Sequence easier
/// and centralized. Also I got tired of typing ArrayList<Note\>.
public class Notes {

    private final ArrayList<Note> notes;
    private Map<String, List<Note>> mapByInstrument;
    private Map<Integer, List<Note>> mapByChannel;
    private Map<Long, List<Note>> mapByDuration;


    Notes(ArrayList<NoteOnEvent> noteOnEvents) {
        assert noteOnEvents != null;
        this.notes = new ArrayList<>();
        for (NoteOnEvent on : noteOnEvents) {
            this.notes.add(new Note(on));
        }
        mapNotes();
    }

    public void mapNotes() {

        this.mapByInstrument = new HashMap<>();
        this.mapByChannel = new HashMap<>();
        this.mapByDuration = new HashMap<>();

        for (Note note : this.notes) {

            this.mapByInstrument.computeIfAbsent(note.getTrackName(), k -> new ArrayList<>()).add(note);
            this.mapByChannel.computeIfAbsent(note.getOriginalChannel(), k -> new ArrayList<>()).add(note);
            this.mapByDuration.computeIfAbsent(note.getDuration(), k -> new ArrayList<>()).add(note);
        }
    }

    /// This is List to avoid type erasure conflicting method signatures
    Notes(List<Note> notes) {
        this.notes = (ArrayList<Note>) notes;
    }

    /// Empty constructor
    Notes() {
        this.notes = new ArrayList<>();
    }


    public boolean add(Note note) {
        return this.notes.add(note);
    }

    public void add(int index, Note note) {
        this.notes.add(index, note);
    }

    public int size() {
        return this.notes.size();
    }

    public boolean isEmpty() {
        return this.notes.isEmpty();
    }

    public ArrayList<Note> getList() {
        return this.notes;
    }

    public Note getFirst() {
        return this.notes.getFirst();
    }

    public Note getLast() {
        return this.notes.getLast();
    }

    public Note get(int index) {
        return this.notes.get(index);
    }

    public ArrayList<MidiEvent> toMidiEvents() throws InvalidMidiDataException {
        return toMidiEvents(this.notes);
    }

    public Sequence toSequence() throws InvalidMidiDataException {
        return toSequence(this.toMidiEvents());
    }

    public Sequence toSequencePlusAddBacks(ArrayList<MidiEvent> addBacks) throws InvalidMidiDataException {
        ArrayList<MidiEvent> notesPlusAddBacks = new ArrayList<>(this.toMidiEvents());
        notesPlusAddBacks.addAll(addBacks);
        return toSequence(notesPlusAddBacks);
    }


    public static ArrayList<MidiEvent> toMidiEvents(ArrayList<Note> notes) throws InvalidMidiDataException {

        final ArrayList<MidiEvent> outList = new ArrayList<>();
        final int MEDIAN_VELOCITY = 64;

        for (Note note : notes) {
            // todo switch between these
            //final int CHANNEL = note.getAssignedChannel();
            final int CHANNEL = note.getOriginalChannel();
            //final int CHANNEL = 0;
            ShortMessage onMessage = new ShortMessage(ShortMessage.NOTE_ON, CHANNEL, note.getPitch(),
                    MEDIAN_VELOCITY);
            MidiEvent noteOnEvent = new MidiEvent(onMessage, note.start());
            outList.add(noteOnEvent);

            ShortMessage offMessage = new ShortMessage(ShortMessage.NOTE_OFF, CHANNEL, note.getPitch(), 0);
            MidiEvent noteOffEvent = new MidiEvent(offMessage, note.stop());
            outList.add(noteOffEvent);
        }

        return outList;
    }

    public static Sequence toSequence(ArrayList<MidiEvent> midiEvents) throws InvalidMidiDataException {

        Sequence out = new Sequence(Piece.DIVISION_TYPE, Piece.RESOLUTION);
        Track track = out.createTrack();

        for (MidiEvent event : midiEvents) {
            //track.add( DeepCopy.copyEvent(event) ); // todo this was issue can't remember
            track.add(event);
        }

        return out;
    }

    // List vs. ArrayList is bc type erasure
    public static Sequence toSequence(List<Chord> chords) throws InvalidMidiDataException {
        ArrayList<MidiEvent> midiEvents = new ArrayList<>();
        for (Chord chord : chords) {
            midiEvents.addAll( chord.getNotes().toMidiEvents() );
        }
        return toSequence(midiEvents);
    }

    public static Sequence toSequencePlusAddbacks(List<Chord> chords, ArrayList<MidiEvent> addBacks) throws InvalidMidiDataException {
        ArrayList<MidiEvent> midiEvents = new ArrayList<>(addBacks);
        for (Chord chord : chords) {
            midiEvents.addAll( chord.getNotes().toMidiEvents() );
        }
        return toSequence(midiEvents);
    }


}