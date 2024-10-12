package reductor;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;


public class TimeSignatureEvent extends Event<MetaMessage> {


    TimeSignatureEvent(MidiEvent event, int trackIndex) {

        super(event, trackIndex);

    }


    @Override
    String dataString() {

        byte[] data = this.message.getData();

        int upperNumeral = data[0] & 0xFF;
        int lowerNumeralExponent = data[1] & 0xFF;
        int clockTicksPerTick = data[2] & 0xFF; // don't delete
        int thirtySecondsPerBeat = data[3] & 0xFF; // don't delete

        return upperNumeral + "/" + (int) Math.pow(2, lowerNumeralExponent);

    }


    void setData(int upperNumeral, int lowerNumeral) {

        if (upperNumeral > 128 || upperNumeral < 1) {
            throw new IllegalArgumentException("invalid upperNumeral: " + upperNumeral);
        }

        if (lowerNumeral > 128 || lowerNumeral < 1) {
            throw new IllegalArgumentException("invalid lowerNumeral: " + lowerNumeral);
        }

        int exponent = 0;
        while (lowerNumeral >= 2) {
            lowerNumeral /= 2;
            exponent++;
        }

        byte[] oldData = message.getData();
        byte clockTicksPerTick = oldData[2];
        byte thirtySecondsPerBeat = oldData[3];

        byte[] newData = new byte[]{(byte) upperNumeral, (byte) exponent, clockTicksPerTick, thirtySecondsPerBeat};

        try {
            message.setMessage(this.message.getType(), newData, newData.length);
        }
        catch (InvalidMidiDataException e) {
            throw new RuntimeException(e);
        }

    }


}
