package reductor;

import javax.sound.midi.MidiEvent;
import javax.sound.midi.ShortMessage;


public abstract class ChannelEvent extends Event<ShortMessage> {

    private final int channel;

    ChannelEvent(MidiEvent event) {
        super(event);
        this.channel = this.getMessage().getChannel();
    }

    @Override
    String dataString() {
        return "(Ch" + this.channel + ") ";
    }

    public int getChannel() {
        return this.channel;
    }


}
