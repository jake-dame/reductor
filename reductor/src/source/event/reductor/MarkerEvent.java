package reductor;

import javax.sound.midi.MidiEvent;


public class MarkerEvent extends MetaEvent {

    MarkerEvent(MidiEvent event) {
        super(event);
    }

    @Override
    String dataString() {
        return "not implemented";
    }

}
