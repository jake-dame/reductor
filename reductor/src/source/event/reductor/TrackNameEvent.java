package reductor;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import java.nio.charset.StandardCharsets;

public class TrackNameEvent extends Event<MetaMessage> {


    TrackNameEvent(MidiEvent event, int trackIndex) {

        super(event, trackIndex);

    }


    @Override
    String dataString() {

        return new String(this.message.getData(), StandardCharsets.UTF_8);

    }


    void setData(String trackName) {

        byte[] newData = trackName.getBytes();

        try {
            message.setMessage(this.message.getType(), newData, newData.length);
        } catch (InvalidMidiDataException e) {
            throw new RuntimeException(e);
        }

    }


}
