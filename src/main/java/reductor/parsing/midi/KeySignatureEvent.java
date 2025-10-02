package reductor.parsing.midi;

import reductor.core.KeySignature;

import javax.sound.midi.MidiEvent;


public final class KeySignatureEvent extends MetaEvent {

    private final int accidentals;
    private final int mode;

    KeySignatureEvent(MidiEvent event) {
        super(event);
        this.accidentals = this.getMessage().getData()[0];
        this.mode = this.getMessage().getData()[1] & 0xFF;
    }

    public int getAccidentals() { return this.accidentals; }
    public int getMode() { return this.mode; }

    @Override
    String dataString() {
        return KeySignature.toString(this.mode, this.accidentals);
    }

}
