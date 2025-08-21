package reductor.midi;

import reductor.dataconverter.midi.ConversionFromMidi;

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
        return ConversionFromMidi.convertMicrosecondsToBPM(this.getMessage().getData());
    }

}
