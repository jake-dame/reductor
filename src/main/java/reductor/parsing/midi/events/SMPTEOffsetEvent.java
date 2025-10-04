package reductor.parsing.midi.events;

import javax.sound.midi.MidiEvent;


public final class SMPTEOffsetEvent extends MetaEvent {

    SMPTEOffsetEvent(MidiEvent event) {
        super(event);
    }

    @Override
    String dataString() { return ""; } // TODO

}
