package reductor;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;


public class KeySignatureEvent extends Event<MetaMessage> {


    KeySignatureEvent(MidiEvent event, int trackIndex) {

        super(event, trackIndex);

    }


    @Override
    String dataString() {

        return Pitch.getKeySignature(this.message.getData());

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
            message.setMessage(this.message.getType(), newData, newData.length);
        }
        catch (InvalidMidiDataException e) {
            throw new RuntimeException(e);
        }

    }


}
