package reductor.parsing.midi.events;

import javax.sound.midi.MidiEvent;


public final class MarkerEvent extends MetaEvent {

    MarkerEvent(MidiEvent event) {
        super(event);
    }

    @Override
    String dataString() { return ""; } // TODO

}
