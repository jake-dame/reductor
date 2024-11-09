package reductor;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiEvent;


public class TrackNameEvent extends MetaEvent {

    private final String trackName;

    TrackNameEvent(MidiEvent event) {
        super(event);
        trackName = new String(this.getMessage().getData());
    }

    @Override
    String dataString() {
        return this.getTrackName();
    }

    public String getName() {
        return this.trackName;
    }

    void setTrackName(String trackName) throws InvalidMidiDataException {
        byte[] newData = trackName.getBytes();
        getMessage().setMessage(this.getMessage().getType(), newData, newData.length);
    }


}
