package reductor.midi.importer.parser.events;


import reductor.util.TimeUtil;

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
        return TimeUtil.convertMicrosecondsToBPM(this.getMessage().getData());
    }


}
