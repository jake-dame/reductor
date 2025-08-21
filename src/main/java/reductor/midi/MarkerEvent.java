package reductor.midi;

import javax.sound.midi.MidiEvent;


public final class MarkerEvent extends MetaEvent {

    MarkerEvent(MidiEvent event) {
        super(event);
    }

    @Override
    String dataString() { return ""; } // TODO

}
