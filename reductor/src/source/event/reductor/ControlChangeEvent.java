package reductor;

import javax.sound.midi.MidiEvent;
import java.util.Map;


public class ControlChangeEvent extends ChannelEvent {


    public static final Map<Integer, String> controllers;

    static {

        controllers = Map.ofEntries(
                Map.entry(0x2, "breath controller (coarse)"),
                Map.entry(0x7, "bank select (fine)"),
                Map.entry(0xA, "pan (coarse)"),
                Map.entry(0x5B, "effect 1 depth"),
                Map.entry(0x5D, "effect 3 depth"),
                Map.entry(0x79, "all controllers off")
        );

    }


    int controllerCode;
    int controllerValue;


    ControlChangeEvent(MidiEvent event, int trackIndex) {

        super(event, trackIndex);
        this.controllerCode = this.message.getData1();
        this.controllerValue = this.message.getData2();

    }


    @Override
    String dataString() {

        return super.dataString()
                + ", Controller: " + contollerCodeToString(this.controllerCode)
                + ", " + this.controllerValue;

    }


    String contollerCodeToString(int controllerCode) {

        String instrument = controllers.get(controllerCode);

        if (instrument == null) {
            throw new RuntimeException("Found new controller code: 0x" + Integer.toHexString(controllerCode));
        }

        return instrument;

    }


}
