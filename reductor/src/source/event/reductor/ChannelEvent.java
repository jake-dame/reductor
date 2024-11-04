package reductor;

import javax.sound.midi.MidiEvent;
import javax.sound.midi.ShortMessage;


public abstract class ChannelEvent extends Event<ShortMessage> {


    private final int channel;


    ChannelEvent(MidiEvent event) {

        super(event);
        this.channel = this.message().getChannel();

    }


    @Override
    String dataString() {

        return "(Ch" + this.channel + ") ";

    }


    public int channel() {

        return this.channel;

    }


}
