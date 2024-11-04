package reductor;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiEvent;


public class TextEvent extends MetaEvent {


    TextEvent(MidiEvent event) {

        super(event);

    }


    @Override
    String dataString() {

        return new String(this.message().getData());

    }


    void setData(String text) {

        byte[] newData = text.getBytes();

        try {
            this.message().setMessage(this.message().getType(), newData, newData.length);
        } catch (InvalidMidiDataException e) {
            throw new RuntimeException(e);
        }

    }


}
