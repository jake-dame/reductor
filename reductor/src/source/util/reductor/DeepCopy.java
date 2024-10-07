package reductor;

import javax.sound.midi.*;
import java.util.ArrayList;


/**
 * The {@link javax.sound.midi} library includes no method to make a deep copy of any of
 * its major component objects. Perhaps there is a reason for this. I implement anyway.
 */
class DeepCopy {


    /**
     * Makes a deep copy of a {@link javax.sound.midi.Sequence}.
     *
     * @param sequenceIn The {@link javax.sound.midi.Sequence} to copy.
     * @return A deep copy of the input.
     */
    static Sequence copySequence(Sequence sequenceIn) {

        Sequence sequenceOut;
        try {
            sequenceOut = new Sequence(
                    sequenceIn.getDivisionType(),
                    sequenceIn.getResolution(),
                    sequenceIn.getTracks().length
            );
        } catch (InvalidMidiDataException e) {
            throw new RuntimeException(e);
        }

        assert sequenceIn.getTracks().length == sequenceOut.getTracks().length;

        for (int t = 0; t < sequenceIn.getTracks().length; t++) {

            Track trackIn = sequenceIn.getTracks()[t];
            Track trackOut = sequenceOut.getTracks()[t];

            for (int e = 0; e < trackIn.size(); e++){

                MidiEvent eventIn = trackIn.get(e);
                MidiEvent eventOut = copyEvent(eventIn);
                trackOut.add(eventOut);

            }

            assert trackIn.size() == trackOut.size();
        }

        return sequenceOut;
    }


    /**
     * Makes a deep copy of a {@link javax.sound.midi.MidiEvent}.
     *
     * @param eventIn The {@link javax.sound.midi.MidiEvent} to copy.
     * @return A deep copy of the input.
     */
    static MidiEvent copyEvent(MidiEvent eventIn) {
        MidiMessage messageOut = copyMessage(eventIn.getMessage());
        return new MidiEvent(messageOut, eventIn.getTick());
    }


    /**
     * Makes a deep copy of a {@link javax.sound.midi.MidiMessage}.
     *
     * @param messageIn The {@link javax.sound.midi.MidiMessage} to copy.
     * @return A deep copy of the input.
     */
    static MidiMessage copyMessage(MidiMessage messageIn) {

        MidiMessage messageOut;

        try {

            switch (messageIn) {
                case ShortMessage shortMessage -> {
                    int command = shortMessage.getCommand();
                    int channel = shortMessage.getChannel();
                    int data1 = shortMessage.getData1();
                    int data2 = shortMessage.getData2();
                    messageOut = new ShortMessage(command, channel, data1, data2);
                }
                case MetaMessage metaMessage -> {
                    int type = metaMessage.getType();
                    byte[] data = metaMessage.getData().clone();
                    int length = data.length;
                    messageOut = new MetaMessage(type, data, length);
                }
                case SysexMessage sysexMessage -> {
                    int status = sysexMessage.getStatus();
                    byte[] data = sysexMessage.getData().clone();
                    int length = data.length;
                    messageOut = new SysexMessage(status, data, length);
                }
                default -> {
                    throw new InvalidMidiDataException("Unknown message type: " + messageIn.getStatus());
                }
            }

        } catch (InvalidMidiDataException e) {
            throw new RuntimeException(e);
        }

        return messageOut;
    }


    /**
     * Makes a deep copy of a list of {@link javax.sound.midi.MidiEvent}s.
     *
     * @param eventsIn The list of {@link javax.sound.midi.MidiEvent} to copy.
     * @return A deep copy of the input.
     */
    static ArrayList<MidiEvent> copyEvents(ArrayList<MidiEvent> eventsIn) {

        ArrayList<MidiEvent> eventsOut = new ArrayList<>();

        for (MidiEvent event : eventsIn) {
            MidiEvent copy = copyEvent(event);
            eventsOut.add(copy);
        }

        return eventsOut;
    }


}
