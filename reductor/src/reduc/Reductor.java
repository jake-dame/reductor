package reduc;

import javax.sound.midi.*;

import static javax.sound.midi.ShortMessage.*;

public class Reductor {

    final Midi midiIn;
    final Midi midiAg;

    /*
        For now, this has two member objects:
            - A Midi object for the original/in- Sequence
            - A Midi object for the aggregated version of the in-sequence
     */
    Reductor(String filePath)
            throws InvalidMidiDataException {

        midiIn = new Midi(filePath);
        midiAg = new Midi(midiIn.getName() + "_agg", aggregate(midiIn.getSequence()) );

    }

    /*
        This function accomplishes:
            - adding all events from all tracks from a sequence to a new, single-track sequence
                - this disallows duplicate events and sorts events by increasing tick value
            - preserves crucial meta messages (set tempo, time signature, key signature), while
              throwing away track-/instrument-specific meta messages
            - "sanitizes" note events so that they are in a predictable/uniform form for later manipulation
     */
    private Sequence aggregate(Sequence sequenceIn)
            throws InvalidMidiDataException {

        Sequence newSequence = new Sequence(
                sequenceIn.getDivisionType(), sequenceIn.getResolution(), 1);

        for (int t = 0; t < sequenceIn.getTracks().length; t++) {

            Track track = sequenceIn.getTracks()[t];

            for (int e = 0; e < track.size(); e++) {

                MidiEvent event = track.get(e);
                MidiMessage msg = track.get(e).getMessage();

                // Avoid adding meta events needed by tracks that no longer exist
                if (t > 0 && msg instanceof MetaMessage )  continue;

                // Avoid adding short events for tracks that no longer exist
                if (msg instanceof ShortMessage shortMessage) {

                    if ( t > 0 && (shortMessage.getCommand() == CONTROL_CHANGE
                                || shortMessage.getCommand() == PROGRAM_CHANGE)) {
                        continue;
                    } else {
                        sanitizeShortMessage(shortMessage);
                    }

                }

                Track newTrack = newSequence.getTracks()[0];

                // This adds in increasing tick order and throws away duplicates
                newTrack.add(event);
            }
        }

        return newSequence;
    }

    /*
        This function accomplishes:
            - Change channel for passed event to channel 1 (0x0)
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

    // not safe
    public Sequence getOriginalSequence() {return midiIn.getSequence(); }
    public Sequence getAggregatedSequence() { return midiAg.getSequence(); }

}
