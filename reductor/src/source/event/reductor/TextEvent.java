package reductor;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiEvent;


public class TextEvent extends MetaEvent {

    TextEvent(MidiEvent event) {
        super(event);
    }

    @Override
    String dataString() {
        return new String(this.getMessage().getData());
    }

    void setData(String text) {
        byte[] newData = text.getBytes();
        try {
            this.getMessage().setMessage(this.getMessage().getType(), newData, newData.length);
        } catch (InvalidMidiDataException e) {
            throw new RuntimeException(e);
        }
    }


}
