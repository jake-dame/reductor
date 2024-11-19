package reductor;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiEvent;


public class SetTempoEvent extends MetaEvent {

    SetTempoEvent(MidiEvent event) {
        super(event);
    }

    @Override
    String dataString() {
        return this.getBPM() + " bpm";
    }

    public int getBPM() {
        return Conversion.convertMicrosecondsToBPM(this.getMessage().getData());
    }

}
