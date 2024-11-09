package reductor;

import javax.sound.midi.MidiEvent;


public class EndOfTrackEvent extends MetaEvent {

    EndOfTrackEvent(MidiEvent event) {
        super(event);
    }

    @Override
    String dataString() {
        return "eot";
    }


}
