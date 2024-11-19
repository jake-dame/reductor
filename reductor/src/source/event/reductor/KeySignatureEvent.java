package reductor;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiEvent;


public class KeySignatureEvent extends MetaEvent {

    int accidentals;
    int mode;
    MessageType type;

    KeySignatureEvent(MidiEvent event) {
        super(event);
        this.accidentals = this.getMessage().getData()[0];
        this.mode = this.getMessage().getData()[1] & 0xFF;
        this.type = MessageType.KEY_SIGNATURE;
    }

    @Override
    String dataString() { return KeySignature.toString(this.mode, this.accidentals); }

}
