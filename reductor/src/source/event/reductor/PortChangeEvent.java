package reductor;

import javax.sound.midi.MidiEvent;


public class PortChangeEvent extends MetaEvent{

    PortChangeEvent(MidiEvent event) {
        super(event);
    }

    @Override
    String dataString() {
        return "Port: " + (this.message().getData()[0] & 0xff);
    }

}
