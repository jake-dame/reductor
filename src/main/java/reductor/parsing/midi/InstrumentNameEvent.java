package reductor.parsing.midi;

import javax.sound.midi.MidiEvent;


public final class InstrumentNameEvent extends MetaEvent {

    String instrumentName;


    InstrumentNameEvent(MidiEvent event) {
        super(event);
        this.instrumentName = new String(this.getMessage().getData());
    }


    public String getInstrumentName() { return this.instrumentName; };


    @Override
    String dataString() { return this.instrumentName; }

}
