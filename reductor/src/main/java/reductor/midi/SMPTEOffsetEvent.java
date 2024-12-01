package reductor.midi;

import javax.sound.midi.MidiEvent;


public final class SMPTEOffsetEvent extends MetaEvent {

    SMPTEOffsetEvent(MidiEvent event) {
        super(event);
    }

    @Override
    String dataString() { return ""; } // TODO

}
