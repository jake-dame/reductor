package reductor.midi;

import javax.sound.midi.MidiEvent;


public final class EndOfTrackEvent extends MetaEvent {

    EndOfTrackEvent(MidiEvent event) {
        super(event);
    }


    @Override
    String dataString() { return ""; }

}
