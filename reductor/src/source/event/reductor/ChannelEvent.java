package reductor;

import javax.sound.midi.MidiEvent;
import javax.sound.midi.ShortMessage;


public abstract class ChannelEvent extends Event<ShortMessage> {

    int channel;

    ChannelEvent(MidiEvent event, int trackIndex) {

        super(event, trackIndex);
        this.channel = this.message.getChannel();

    }


    @Override
    String dataString() {

        return "CH." + this.channel + ", ";

    }

    public ShortMessage getMessage() {

        return this.message;

    }


    //todo
    public int channel() {

        return this.channel;

    }
    //todo


}
