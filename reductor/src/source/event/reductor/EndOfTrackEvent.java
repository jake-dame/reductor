package reductor;

import javax.sound.midi.MidiEvent;


public class EndOfTrackEvent extends MetaEvent {


    EndOfTrackEvent(MidiEvent event, int trackIndex) {

        super(event, trackIndex);
    }


    @Override
    String dataString() {

        return "eot";

    }


}
