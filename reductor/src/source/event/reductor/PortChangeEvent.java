package reductor;

import javax.sound.midi.MidiEvent;


public class PortChangeEvent extends MetaEvent{


    PortChangeEvent(MidiEvent event, int trackIndex) {

        super(event, trackIndex);
    }


    @Override
    String dataString() {

        return "Port: " + (this.message.getData()[0] & 0xff);

    }


}
