package reductor;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiEvent;


public class KeySignatureEvent extends MetaEvent {

    int accidentals;
    int mode;
    MessageType type;

    KeySignatureEvent(MidiEvent event) {

        super(event);
        this.accidentals = this.message().getData()[0];
        this.mode = this.message().getData()[1] & 0xFF;
        this.type = MessageType.KEY_SIGNATURE;

    }


    @Override
    String dataString() {

        return KeyContext.getKeySignature(this.message().getData());

    }


    void setData(int numAccidentals, int mode) {

        if (numAccidentals < -7 || numAccidentals > 7) {
            throw new IllegalArgumentException("invalid number of accidentals to set; should be in [-7,7]: " + numAccidentals);
        }

        if (mode < 0 || mode > 1) {
            throw new IllegalArgumentException("invalid mode to set; should be in [0,1]: " + mode);
        }

        byte[] newData = new byte[]{(byte) numAccidentals, (byte) mode};

        try {
            message().setMessage(this.message().getType(), newData, newData.length);
        } catch (InvalidMidiDataException e) {
            throw new RuntimeException(e);
        }

    }


}
