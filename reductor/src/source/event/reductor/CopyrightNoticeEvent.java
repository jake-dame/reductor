package reductor;

import javax.sound.midi.MidiEvent;


public class CopyrightNoticeEvent extends MetaEvent {

    CopyrightNoticeEvent(MidiEvent event) {
        super(event);
    }

    @Override
    String dataString() {
        return new String(this.getMessage().getData());
    }


}
