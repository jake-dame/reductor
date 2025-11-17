package reductor.midi.parser.events;


import reductor.midi.MidiUtil;

import javax.sound.midi.MidiEvent;


public final class SetTempoEvent extends MetaEvent {


    SetTempoEvent(MidiEvent event) {
        super(event);
    }

    @Override
    String dataString() {
        return this.getBPM() + " bpm";
    }

    public int getBPM() {
        return MidiUtil.convertMicrosecondsToBPM(this.getMessage().getData());
    }


}
