package reductor.midi.importer.parser.events;


import javax.sound.midi.MidiEvent;
import java.util.Arrays;
import java.util.Map;


public final class SequencerSpecificEvent extends MetaEvent {


    public static final Map<Integer, String> manufacturerCodes;

    static {

        manufacturerCodes = Map.ofEntries(
                Map.entry(0xA, "Delta Labs")
        );

    }


    private final int manufacturerCode;

    SequencerSpecificEvent(MidiEvent event) {
        super(event);

        // javax.sound.midi provides no way to access a byte I can see in the debugger! the manufacturer code!
        //this.manufacturerCode = this.message().getData()[4];
        this.manufacturerCode = 0xA; // TODO: this is hardcoded!
    }


    @Override
    String dataString() {
        return ", Manufacturer: " + manufacturerCodeToString(this.manufacturerCode)
                + ", " + Arrays.toString(this.getMessage().getData());
    }


    public static String manufacturerCodeToString(int manufacturerCode) {

        String manufacturer = manufacturerCodes.get(manufacturerCode);

        if (manufacturer == null) {
            throw new RuntimeException(
                    "Found new manufacturer code: 0x" + Integer.toHexString(manufacturerCode)
            );
        }

        return manufacturer;
    }


}
