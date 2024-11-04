package reductor;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiEvent;

public class TrackNameEvent extends MetaEvent {


    TrackNameEvent(MidiEvent event) {

        super(event);

    }


    @Override
    String dataString() {

        return new String(this.message().getData());

    }


    void setData(String trackName) {

        byte[] newData = trackName.getBytes();

        try {
            message().setMessage(this.message().getType(), newData, newData.length);
        } catch (InvalidMidiDataException e) {
            throw new RuntimeException(e);
        }

    }


}
