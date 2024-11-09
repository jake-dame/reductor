package reductor;

import javax.sound.midi.MidiEvent;
import java.util.Arrays;


public class LyricsEvent extends MetaEvent {

    String lyrics;

    LyricsEvent(MidiEvent event) {
        super(event);
        this.lyrics = Arrays.toString(this.getMessage().getData());
    }

    @Override
    String dataString() {
        return lyrics;
    }

}
