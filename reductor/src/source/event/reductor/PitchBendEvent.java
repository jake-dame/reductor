package reductor;

import javax.sound.midi.MidiEvent;


public class PitchBendEvent extends ChannelEvent {

    PitchBendEvent(MidiEvent event) {
        super(event);
    }

}
