package reductor.parsing.midi.events;


import reductor.core.Util;
import reductor.core.midi.ConversionFromMidi;

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
        return Util.convertMicrosecondsToBPM(this.getMessage().getData());
    }


}
