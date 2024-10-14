package reductor;

import javax.sound.midi.MidiEvent;
import java.util.Map;


public class ProgramChangeEvent extends ChannelEvent {


    public static final Map<Integer, String> instruments;

    static {

        instruments = Map.ofEntries(
                Map.entry(0x0, "acoustic grand piano"),
                Map.entry(0x2F, "timpani"),
                Map.entry(0x30, "string ensemble 1"),
                Map.entry(0x34, "choir aahs"),
                Map.entry(0x39, "trombone"),
                Map.entry(0x44, "oboe"),
                Map.entry(0x46, "bassoon"),
                Map.entry(0x47, "clarinet")
        );

    }


    int instrumentCode;


    ProgramChangeEvent(MidiEvent event, int trackIndex) {

        super(event, trackIndex);
        this.instrumentCode = this.message.getData1();

    }


    @Override
    String dataString() {

        return super.dataString()
                + instrumentCodeToString(this.instrumentCode);

    }


    String instrumentCodeToString(int instrumentCode) {

        String instrument = instruments.get(instrumentCode);

        if (instrument == null) {
            throw new RuntimeException("Found new instrument code: 0x" + Integer.toHexString(instrumentCode));
        }

        return instrument;

    }


}
