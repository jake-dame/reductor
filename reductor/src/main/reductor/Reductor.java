package reductor;

import reductor.IntervalTree.Interval;

import javax.sound.midi.*;
import java.util.ArrayList;

import static javax.sound.midi.ShortMessage.*;

/** Contains and controls all aspects of the Reductor operations (sanitization, aggregation, reduction) */
public class Reductor {

    final int resolution;

    /** The original MIDI data (unaltered) */
    final Midi original;

    /** The in-MIDI data, but all on one track and (lightly) sanitized */
    final Midi aggregate;

    /** Interval tree that stores NOTE events */
    final IntervalTree tree;

    Reductor(String filePath) {

        original = new Midi(filePath);
        aggregate = aggregate();

        resolution = original.getResolution();

        tree = new IntervalTree();

        var notes = Note.midiEventsToNotes(aggregate.getNoteEvents());

        tree.addAll(notes);

    }

    Reductor(Midi midi) {

        original = midi;
        aggregate = aggregate();

        resolution = original.getResolution();

        tree = new IntervalTree();

        var notes = Note.midiEventsToNotes(aggregate.getNoteEvents());

        tree.addAll(notes);

    }

    /*
        This function accomplishes:
            - adding all events from all tracks from a sequence to a new, single-track sequence
                - this disallows duplicate events and sorts events by increasing tick value
            - preserves crucial meta messages (set tempo, time signature, key signature), while
              throwing away track-/instrument-specific meta messages
            - "sanitizes" note events so that they are in a predictable/uniform form for later manipulation
     */
    private Midi aggregate() {

        Sequence newSequence;
        try {

            Sequence sequenceIn = original.getSequence();

            newSequence = new Sequence(
                    sequenceIn.getDivisionType(), sequenceIn.getResolution(), 1
            );

            for (int t = 0; t < sequenceIn.getTracks().length; t++) {

                Track track = sequenceIn.getTracks()[t];

                for (int e = 0; e < track.size(); e++) {

                    MidiEvent event = track.get(e);
                    MidiMessage msg = track.get(e).getMessage();

                    // Avoid adding meta events needed by tracks that no longer exist
                    if (t > 0 && msg instanceof MetaMessage) continue;

                    // Avoid adding short events for tracks that no longer exist
                    if (msg instanceof ShortMessage shortMessage) {

                        if (t > 0 && (shortMessage.getCommand() == CONTROL_CHANGE
                                || shortMessage.getCommand() == PROGRAM_CHANGE)) {
                            continue;
                        }
                        else {
                            sanitizeShortMessage(shortMessage);
                        }

                    }

                    Track newTrack = newSequence.getTracks()[0];

                    // This adds in increasing tick order and throws away duplicates
                    newTrack.add(event);
                }
            }

        } catch (InvalidMidiDataException e) {
            throw new RuntimeException(e);
        }

        return new Midi(newSequence, "AGG_" + original.getName());
    }

    /*
        This function accomplishes:
            - Change channel for passed note to channel 1 (0x0)
            - Change scheme from any {NOTE ON, v=0} to proper NOTE OFF
            - Set program change message to reflect piano
     */
    private void sanitizeShortMessage(ShortMessage shortMessage)
            throws InvalidMidiDataException {

        final int cmd = shortMessage.getCommand();
        final int pitch = shortMessage.getData1();
        final int velocity = shortMessage.getData2();
        final int channel = 0;

        if (cmd == NOTE_OFF) {
            shortMessage.setMessage(cmd, channel, pitch, velocity);
        } else if (cmd == NOTE_ON && velocity == 0) {
            shortMessage.setMessage(NOTE_OFF, channel, pitch, 0);
        } else if (cmd == NOTE_ON) {
            shortMessage.setMessage(cmd, channel, pitch, velocity);
        } else if (cmd == PROGRAM_CHANGE) {
            final int acousticGrandPiano = 0;
            shortMessage.setMessage(cmd, channel, acousticGrandPiano, -1);
        }

    }

    // REDUCTION ********************************************************************************************************/

    ArrayList<Chord> levelOne() {

        if(this.aggregate.getResolution() != 480) {
            throw new RuntimeException("non-480 resolution");
        }

        ArrayList<Chord> chords = new ArrayList<>();
        int windowSize = this.aggregate.NOTE_128TH;
        long length = this.aggregate.getLengthInTicks();
        System.out.println("LENGTH: " + length + "ticks");
        for (long windowMin = 0, windowMax = windowSize; windowMax < length; windowMin += windowSize, windowMax += windowSize) {
            Interval window = new Interval(windowMin, windowMax);
            System.out.println("min: " + windowMin + ", max: " + windowMax);
            Chord chord = treeQueryToChord( this.tree.query(window) );
            chords.add(chord);
        }

        for(Chord chord : chords) {
            System.out.println(chord);
        }

        return chords;
    }

    static Chord treeQueryToChord(ArrayList<IntervalTree.Interval> intervals) {

        ArrayList<Note> notes = new ArrayList<>();

        for (IntervalTree.Interval interval : intervals) {
            notes.add(interval.note);
        }

        return new Chord(notes);
    }

    // GETTERS ********************************************************************************************************/

    public Midi getOriginal() { return original; }

    public Midi getAggregate() { return aggregate; }

}
