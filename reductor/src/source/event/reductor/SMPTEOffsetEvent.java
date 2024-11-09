package reductor;

import javax.sound.midi.MidiEvent;


public class SMPTEOffsetEvent extends MetaEvent {

    SMPTEOffsetEvent(MidiEvent event) {
        super(event);
    }

    @Override
    String dataString() {
        return "";
    }


}
