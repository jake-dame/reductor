package reductor;

import java.util.Map;


/**
 * Constants class for Piece message types.
 */
public class Constants {


    /// Meta message types (MetaMessage.getType())
    public static final int TEXT = 0x1;
    public static final int TRACK_NAME = 0x3;
    public static final int CHANNEL_PREFIX = 0x20;
    public static final int PORT_PREFIX = 0x21;
    public static final int END_OF_TRACK = 0x2F;
    public static final int SET_TEMPO = 0x51;
    public static final int TIME_SIGNATURE = 0x58;
    public static final int KEY_SIGNATURE = 0x59;


    /// Channel message command (ShortMessage.getCommand())
    public static final int NOTE_OFF = 0x80;
    public static final int NOTE_ON = 0x90;
    public static final int POLY_TOUCH = 0xA0;
    public static final int CONTROL_CHANGE = 0xB0;
    public static final int PROGRAM_CHANGE = 0xC0;
    public static final int CHANNEL_PRESSURE = 0xD0;
    public static final int PITCH_BEND = 0xE0;


    public static final Map<Integer, String> instruments;
    public static final Map<Integer, String> controllers;


    static {

        instruments = Map.ofEntries(
                Map.entry(0x0, "acoustic grand piano"),
                Map.entry(0x34, "choir aahs")
        );

        controllers = Map.ofEntries(
                Map.entry(0x2, "breath controller (coarse)"),
                Map.entry(0x7, "bank select (fine)"),
                Map.entry(0xA, "pan (coarse)"),
                Map.entry(0x5B, "effect 1 depth"),
                Map.entry(0x5D, "effect 3 depth"),
                Map.entry(0x79, "all controllers off")
        );

    }


    private Constants() { }


    public static String instrumentCodeToString(int instrumentCode) {

        String instrument = instruments.get(instrumentCode);

        if (instrument == null) {
            throw new RuntimeException("Found new instrument code: 0x" + Integer.toHexString(instrumentCode));
        }

        return instrument;

    }

    public static String contollerCodeToString(int controllerCode) {

        String instrument = controllers.get(controllerCode);

        if (instrument == null) {
            throw new RuntimeException("Found new controller code: 0x" + Integer.toHexString(controllerCode));
        }

        return instrument;

    }


}