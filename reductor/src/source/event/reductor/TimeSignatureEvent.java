package reductor;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiEvent;
import java.util.Objects;


public class TimeSignatureEvent extends MetaEvent {

    private final int upperNumeral;
    private final int lowerNumeral;
    private final long clockTicksPerTick;
    private final long thirtySecondsPerBeat;

    TimeSignatureEvent(MidiEvent event) {
        super(event);

        byte[] data = this.getMessage().getData();

        this.upperNumeral = data[0] & 0xFF;

        int lowerNumeralExponent = data[1] & 0xFF;
        this.lowerNumeral = (int) Math.pow(2, lowerNumeralExponent);

        this.clockTicksPerTick = data[2] & 0xFF;

        this.thirtySecondsPerBeat = data[3] & 0xFF;
        System.out.println("(from TimeSignatureEvent) clockTicksPerTick: " + clockTicksPerTick);
        System.out.println("(from TimeSignatureEvent) thirtySecondsPerBeat: " + thirtySecondsPerBeat);
    }

    @Override
    String dataString() { return upperNumeral + "/" + lowerNumeral; }

    public int getUpperNumeral() { return upperNumeral; }
    public int getLowerNumeral() { return lowerNumeral; }

}
