package reductor.parsing.midi.events;

import javax.sound.midi.MidiEvent;
import javax.sound.midi.ShortMessage;


public abstract class ChannelEvent extends Event<ShortMessage> {

    private final int channel;
    //private final ShortMessage message;

    ChannelEvent(MidiEvent event) {
        super(event);
        //this.message = (ShortMessage) event.getMessage();
        this.channel = this.getMessage().getChannel();
    }

    //@SuppressWarnings("unchecked")
    //@Override
    //public ShortMessage getMessage() { return this.message; }

    public final int getChannel() { return this.channel; }


    @Override
    String dataString() { return "Ch. " + this.channel + " -> "; }

}
