package reduc;

import javax.sound.midi.*;

import java.util.ArrayList;

import static javax.sound.midi.ShortMessage.*;

/*
    Purpose: Encapsulate the data (midi-In --> reduction) manipulation stuff. Sanitization, aggregation, and reduction.
    It does not handle file I/O, play, etc. Those are all functions of the three `Midi` members, which all have getters.
*/
public class Reductor {

    final int resolution;

    final Midi original; // midi-In
    final Midi aggregate; // aggregate (stepping stone to reduction)
    final Midi reduction; // reduction

    final int reductionLevel;

    final IntervalTree tree;

    Reductor(String filePath, int reductionLevel) {

        original = new Midi(filePath);
        aggregate = aggregate();
        reduction = reduce();

        if (reductionLevel < 0 || reductionLevel > 3) {
            throw new RuntimeException("valid reduction values are 0-3; 0 returns the aggregate");
        }
        this.reductionLevel = reductionLevel;

        resolution = original.getResolution();

        tree = new IntervalTree();

        var notes = Note.eventsToNotes(aggregate.getNoteEvents());

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

    public Midi getOriginal() { return original; }

    public Midi getAggregate() { return aggregate; }

    public Midi getReduction() { return reduction; }

    /////////////////////////////////// everything below this line is quick and dirty, just to test stuff

    private Midi reduce() {

        ArrayList<ArrayList<Note>> resultOfQueryChunking = new ArrayList<>();

        switch(reductionLevel) {
            case 0 -> { return aggregate; }
            case 1 -> { resultOfQueryChunking = levelOne(); }
            case 2 -> { resultOfQueryChunking = levelTwo(); }
            case 3 -> { resultOfQueryChunking = levelThree(); }
        }

        return convertListOfListsToMidiObject(resultOfQueryChunking);
    }

    private Midi convertListOfListsToMidiObject(ArrayList<ArrayList<Note>> listOfLists) {

        Sequence seq;
        try {
            seq = new Sequence(
                    aggregate.getSequence().getDivisionType(),
                    aggregate.getSequence().getResolution()
            );
        } catch (InvalidMidiDataException e) {
            throw new RuntimeException(e);
        }



        Track track = seq.createTrack();
        ArrayList<MidiEvent> metas = original.getMetaEvents();
        for(MidiEvent e : metas ) {
            track.add(e);
        }

        for (int L = 0; L < listOfLists.size(); L++) {
            ArrayList<Note> list = listOfLists.get(L);
            ArrayList<MidiEvent> midiEvents = Note.notesToEvents(list);


            for (MidiEvent e : midiEvents) {
                track.add(e);
            }

        }

        return new Midi(seq, "RED_" + original.getName());
    }


    private ArrayList<ArrayList<Note>> levelOne() {

        ArrayList<ArrayList<Note>> listOfLists = new ArrayList<>();

        long len = aggregate.getSequence().getTickLength();

        long resolution = aggregate.NOTE_QUARTER; // resolution == quarter note length
        ArrayList<Note> temp;
        for (long chunk = resolution; chunk < len; chunk+=resolution) {

            ArrayList<IntervalTree.Interval> res = tree.query(chunk - resolution, chunk);
            temp = new ArrayList<>();
            for(IntervalTree.Interval I : res) {
                Note note = new Note(I.low, I.high, I.note.pitch);
                temp.add(note);
            }
            listOfLists.add(temp);
        }

        return listOfLists;
    }

    private ArrayList<ArrayList<Note>> levelTwo() { return null; }

    private ArrayList<ArrayList<Note>> levelThree() { return null; }

}
