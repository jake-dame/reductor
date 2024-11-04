package reductor;

import javax.sound.midi.MidiEvent;
import java.util.Map;


public class ControlChangeEvent extends ChannelEvent {


    public static final Map<Integer, String> controllers;

    static {

        controllers = Map.ofEntries(
                Map.entry(0x0, "bank select (coarse)"),
                Map.entry(0x1, "modulation wheel (coarse)"),
                Map.entry(0x2, "breath controller (coarse)"),
                Map.entry(0x6, "data entry (coarse)"),
                Map.entry(0x7, "bank select (fine)"),
                Map.entry(0xA, "pan (coarse)"),
                Map.entry(0xB, "expression (coarse)"),
                Map.entry(0x20, "bank select (fine)"),
                Map.entry(0x40, "hold (damper, sustain) pedal 1 (on/off)"),
                Map.entry(0x43, "soft pedal (on/off)"),
                Map.entry(0x5B, "effect 1 depth"),
                Map.entry(0x5D, "effect 3 depth"),
                Map.entry(0x62, "non-registered parameter (coarse)"),
                Map.entry(0x63, "non-registered parameter (fine)"),
                Map.entry(0x64, "registered parameter (coarse)"),
                Map.entry(0x65, "registered parameter (fine)"),
                Map.entry(0x79, "all controllers off")
        );

    }


    int controllerCode;
    int controllerValue;


    ControlChangeEvent(MidiEvent event) {

        super(event);
        this.controllerCode = this.message().getData1();
        this.controllerValue = this.message().getData2();

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
