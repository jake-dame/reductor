package reductor;

import javax.sound.midi.MidiEvent;


public class InstrumentNameEvent extends MetaEvent {


    String instrumentName;


    InstrumentNameEvent(MidiEvent event) {

        super(event);
        this.instrumentName = new String(this.message().getData());

    }


    @Override
    String dataString() {

        return instrumentName;

    }


}
