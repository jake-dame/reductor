package reduc;

import javax.sound.midi.*;

import static javax.sound.midi.ShortMessage.*;

public class Reductor {

    private final Midi midiIn;

    private final Midi midiAg;

    Reductor(String filePath)
            throws InvalidMidiDataException {

        midiIn = new Midi(filePath);

        Sequence seqIn = midiIn.getSequence();
        Sequence aggregate = aggregate(seqIn);
        midiAg = new Midi(midiIn.getName() + "_agg", aggregate);
    }

    private Sequence aggregate(Sequence sequenceIn)
            throws InvalidMidiDataException {

        Sequence newSequence = new Sequence(sequenceIn.getDivisionType(), sequenceIn.getResolution(), 1);

        for (int tr = 0; tr < sequenceIn.getTracks().length; tr++) {
            Track track = sequenceIn.getTracks()[tr];
            for (int ev = 0; ev < track.size(); ev++) {
                MidiEvent event = track.get(ev);

                MidiMessage msg = track.get(ev).getMessage();

                // Avoid adding meta info needed by tracks that no longer exist
                if (tr > 0 && msg instanceof MetaMessage) {
                    continue;
                }

                if (msg instanceof ShortMessage shortMessage) {
                    // Avoid adding control change stuff to aggregate
                    if (shortMessage.getCommand() == CONTROL_CHANGE) {
                        continue;
                    } else if (shortMessage.getCommand() == PROGRAM_CHANGE && tr > 0) {
                        continue;
                    } else {
                        sanitizeShortMessage(shortMessage);
                    }
                }

                // this adds in increasing tick order
                // it also prevents duplicates
                newSequence.getTracks()[0].add(event);
            }
        }

        return newSequence;
    }

    private void sanitizeShortMessage(ShortMessage shortMessage)
            throws InvalidMidiDataException {

        int cmd = shortMessage.getCommand();
        int pitch = shortMessage.getData1();
        int velocity = shortMessage.getData2();
        int newChannel = 0;

        if (cmd == NOTE_OFF) {
            shortMessage.setMessage(cmd, newChannel, pitch, velocity);
        } else if (cmd == NOTE_ON && velocity == 0) {
            shortMessage.setMessage(NOTE_OFF, newChannel, pitch, 0);
        } else if (cmd == NOTE_ON) {
            shortMessage.setMessage(cmd, newChannel, pitch, velocity);
        } else if (cmd == PROGRAM_CHANGE) {
            final int acousticGrandPiano = 0;
            shortMessage.setMessage(cmd, newChannel, acousticGrandPiano, -1);
        }

    }

    public Sequence getSequence() {
        return midiIn.getSequence();
    }

    public Sequence getAggregate() { return midiAg.getSequence(); }

}
