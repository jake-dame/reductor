package reductor;

import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;


public abstract class MetaEvent extends Event<MetaMessage> {

    MetaEvent(MidiEvent event) {

        super(event);

    }

}
