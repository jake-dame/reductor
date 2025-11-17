package reductor.midi.parser.events;


import reductor.midi.MidiUtil;

import javax.sound.midi.MidiEvent;


public final class ProgramChangeEvent extends ChannelEvent {



    private final int instrumentCode;

    ProgramChangeEvent(MidiEvent event) {
        super(event);
        this.instrumentCode = this.getMessage().getData1();
    }


    public int getInstrumentCode() {return this.instrumentCode;}


    @Override
    String dataString() {
        return super.dataString() + instrumentCodeToString(this.instrumentCode);
    }


    public static String instrumentCodeToString(int instrumentCode) {

        String instrument = MidiUtil.instruments.get(instrumentCode);

        if (instrument == null) {
            throw new RuntimeException(
                    "Found new instrument code: 0x" + Integer.toHexString(instrumentCode)
            );
        }

        return instrument;
    }


}
