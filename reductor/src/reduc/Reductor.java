package reduc;

import javax.sound.midi.*;

import static javax.sound.midi.ShortMessage.*;

public class Reductor {

    private final Midi midiIn;

    private final Midi midiAg;

    Reductor(String filePath) throws InvalidMidiDataException {

        midiIn = new Midi(filePath);

        Sequence seqIn = midiIn.getSequence();
        Sequence aggregate = aggregate(seqIn);
        midiAg = new Midi(midiIn.getName() + "_agg", aggregate);
    }

    private Sequence aggregate(Sequence sequence) throws InvalidMidiDataException {

        Track[] tracksIn = sequence.getTracks();

        Sequence seqOut = new Sequence(
                sequence.getDivisionType(), sequence.getResolution());

        Track trackOut = seqOut.createTrack();

        for (int trackIndex = 0; trackIndex < tracksIn.length; trackIndex++) {

            Track track = tracksIn[trackIndex];

            for (int eventIndex = 0; eventIndex < track.size(); eventIndex++) {

                MidiEvent event = track.get(eventIndex);
                MidiMessage msg = event.getMessage();

                // Avoid adding meta info needed by tracks that no longer exist
                if (trackIndex > 0 && msg instanceof MetaMessage) {
                    continue;
                }

                if (msg instanceof ShortMessage shortMessage) {
                    // Avoid adding control change stuff to aggregate
                    if (shortMessage.getCommand() == CONTROL_CHANGE) {
                        continue;
                    } else if (shortMessage.getCommand() == PROGRAM_CHANGE && trackIndex > 0) {
                        continue;
                    } else {
                        sanitizeShortMessage(shortMessage);
                    }
                }

                trackOut.add(event);
            }
        }

        return seqOut;
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

    public Sequence getAggregate() {
        return midiAg.getSequence();
    }


}
