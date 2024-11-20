package reductor;

import javax.sound.midi.MidiEvent;
import java.util.Arrays;


public final class LyricsEvent extends MetaEvent {

    private final String lyrics;


    LyricsEvent(MidiEvent event) {
        super(event);
        this.lyrics = new String(this.getMessage().getData());
    }


    public String getLyrics() { return this.lyrics; }


    @Override
    String dataString() { return lyrics; }

}
