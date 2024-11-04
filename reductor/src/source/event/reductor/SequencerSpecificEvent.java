package reductor;

import javax.sound.midi.MidiEvent;
import java.util.Arrays;
import java.util.Map;


public class SequencerSpecificEvent extends MetaEvent {


    public static final Map<Integer, String> manufacturerCodes;


    static {

        manufacturerCodes = Map.ofEntries(
                Map.entry(0xA, "Delta Labs")
        );

    }


    int manufacturerCode;


    SequencerSpecificEvent(MidiEvent event) {

        super(event);
        //this.manufacturerCode = this.message().getData()[4];
        // javax.sound.midi provides no way to access a byte I can see in the debugger! the manufacturer code!
        this.manufacturerCode = 0xA;
        System.out.println("");

    }


    @Override
    String dataString() {

        return ", Manufacturer: " + manufacturerCodeToString(this.manufacturerCode)
                + ", " + Arrays.toString(this.message().getData());

    }


    String manufacturerCodeToString(int manufacturerCode) {

        String manufacturer = manufacturerCodes.get(manufacturerCode);

        if (manufacturer == null) {
            throw new RuntimeException("Found new manufacturer code: 0x" + Integer.toHexString(manufacturerCode));
        }

        return manufacturer;

    }



}
