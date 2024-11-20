package reductor;

import javax.sound.midi.MidiEvent;


public final class TrackNameEvent extends MetaEvent {

    private final String trackName;


    TrackNameEvent(MidiEvent event) {
        super(event);
        trackName = new String(this.getMessage().getData());
    }


    public String getTrackNameAsString() { return this.trackName; }


    @Override
    String dataString() { return this.trackName; }

}
