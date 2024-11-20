package reductor;

import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.ShortMessage;


public abstract class MetaEvent extends Event<MetaMessage> {

    //private final MetaMessage message;


    MetaEvent(MidiEvent event) {
        super(event);
        //this.message = (MetaMessage) event.getMessage();
    }


    //@SuppressWarnings("unchecked")
    //@Override
    //public MetaMessage getMessage() { return this.message; }

}
