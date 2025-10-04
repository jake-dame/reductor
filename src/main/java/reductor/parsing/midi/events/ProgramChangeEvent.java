package reductor.parsing.midi.events;

import javax.sound.midi.MidiEvent;
import java.util.Map;


public final class ProgramChangeEvent extends ChannelEvent {

    public static final Map<Integer, String> instruments;

    static {

        instruments = Map.ofEntries(
                Map.entry(0x0, "acoustic grand piano"),
                Map.entry(0x6, "harpsichord"),
                Map.entry(0x8, "celesta"),
                Map.entry(0xD, "xylophone"),
                Map.entry(0x13, "church organ"),
                Map.entry(0x14, "reed organ"),
                Map.entry(0xE, "tubular bell"),
                Map.entry(0x20, "acoustic bass"),
                Map.entry(0x28, "violin"),
                Map.entry(0x29, "viola"),
                Map.entry(0x2A, "cello"),
                Map.entry(0x2B, "contrabass"),
                Map.entry(0x2C, "tremolo strings"),
                Map.entry(0x2D, "pizzicato strings"),
                Map.entry(0x2E, "orchestral strings/harp"),
                Map.entry(0x2F, "timpani"),
                Map.entry(0x30, "string ensemble 1"),
                Map.entry(0x31, "string ensemble 2/slow strings"),
                Map.entry(0x32, "synth strings 1"),
                Map.entry(0x34, "choir aahs"),
                Map.entry(0x35, "voice oohs"),
                Map.entry(0x36, "synth choir/voice"),
                Map.entry(0x38, "trumpet"),
                Map.entry(0x39, "trombone"),
                Map.entry(0x3A, "tuba"),
                Map.entry(0x3B, "muted trumpet"),
                Map.entry(0x3C, "french horn"),
                Map.entry(0x4A, "recorder"),
                Map.entry(0x44, "oboe"),
                Map.entry(0x45, "english horn"),
                Map.entry(0x46, "bassoon"),
                Map.entry(0x47, "clarinet"),
                Map.entry(0x48, "piccolo"),
                Map.entry(0x49, "flute"),
                Map.entry(0x4B, "pan flute"),
                Map.entry(0x4F, "ocarina"),
                Map.entry(0x6C, "kalimba")
        );

    }

    private final int instrumentCode;

    ProgramChangeEvent(MidiEvent event) {
        super(event);
        this.instrumentCode = this.getMessage().getData1();
    }


    public int getInstrumentCode() { return this.instrumentCode; }


    @Override
    String dataString() {
        return super.dataString() + instrumentCodeToString(this.instrumentCode);
    }


    public static String instrumentCodeToString(int instrumentCode) {

        String instrument = instruments.get(instrumentCode);

        if (instrument == null) {
            throw new RuntimeException(
                    "Found new instrument code: 0x" + Integer.toHexString(instrumentCode)
            );
        }

        return instrument;
    }

}
