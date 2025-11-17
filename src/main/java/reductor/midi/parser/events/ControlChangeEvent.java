package reductor.midi.importer.parser.events;


import javax.sound.midi.MidiEvent;
import java.util.Map;


public final class ControlChangeEvent extends ChannelEvent {


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
                Map.entry(0x47, "sound controller 2 (default is timbre / harmonic intensity / filter resonance)"),
                Map.entry(0x48, "sound controller 3 (default is release time)"),
                Map.entry(0x49, "sound controller 4 (default is attack time)"),
                Map.entry(0x4A, "sound controller 5 (default is brightness or cutoff frequency)"),
                Map.entry(0x5B, "effect 1 depth"),
                Map.entry(0x5D, "effect 3 depth"),
                Map.entry(0x5E, "effect 4 depth (formerly celeste depth)"),
                Map.entry(0x62, "non-registered parameter (coarse)"),
                Map.entry(0x63, "non-registered parameter (fine)"),
                Map.entry(0x64, "registered parameter (coarse)"),
                Map.entry(0x65, "registered parameter (fine)"),
                Map.entry(0x78, "all sound off"),
                Map.entry(0x79, "all controllers off")
        );

    }


    private final int controllerCode;
    private final int controllerValue;


    ControlChangeEvent(MidiEvent event) {
        super(event);
        this.controllerCode = this.getMessage().getData1();
        this.controllerValue = this.getMessage().getData2();
    }

    public int getControllerCode() {return controllerCode;}

    public int getControllerValue() {return controllerValue;}

    @Override
    String dataString() {
        return super.dataString()
                + ", Controller: " + contollerCodeToString(this.controllerCode)
                + ", " + this.controllerValue;
    }


    public static String contollerCodeToString(int controllerCode) {

        String instrument = controllers.get(controllerCode);

        if (instrument == null) {
            throw new RuntimeException(
                    "Found new controller code: 0x" + Integer.toHexString(controllerCode)
            );
        }

        return instrument;
    }


}
