package reductor;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiEvent;
import java.util.Objects;


public class TimeSignatureEvent extends MetaEvent {

    private final int upperNumeral;
    private final int lowerNumeral;

    private int resolution;

    TimeSignatureEvent(MidiEvent event) {
        super(event);
        byte[] data = this.getMessage().getData();
        this.upperNumeral = data[0] & 0xFF;
        int lowerNumeralExponent = data[1] & 0xFF;
        this.lowerNumeral = (int) Math.pow(2, lowerNumeralExponent);
        int clockTicksPerTick = data[2] & 0xFF; // don't delete
        int thirtySecondsPerBeat = data[3] & 0xFF; // don't delete
    }

    @Override
    String dataString() {
        return upperNumeral + "/" + lowerNumeral;
    }

    void setData(int upperNumeral, int lowerNumeral) throws InvalidMidiDataException {
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
        //this.exponent = exponent;
        byte[] oldData = getMessage().getData();
        byte clockTicksPerTick = oldData[2];
        byte thirtySecondsPerBeat = oldData[3];
        byte[] newData = new byte[]{(byte) upperNumeral, (byte) exponent, clockTicksPerTick, thirtySecondsPerBeat};
        getMessage().setMessage(this.getMessage().getType(), newData, newData.length);
    }

    public int getResolution() {
        return this.resolution;
    }

    public void setResolution(int val) {
        this.resolution = val;
    }

    public int getUpperNumeral() {
        return upperNumeral;
    }

    public int getLowerNumeral() {
        return lowerNumeral;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TimeSignatureEvent that)) {
            return false;
        }
        return upperNumeral == that.upperNumeral && lowerNumeral == that.lowerNumeral;
    }

    @Override
    public int hashCode() {
        return Objects.hash(upperNumeral, lowerNumeral);
    }


}
