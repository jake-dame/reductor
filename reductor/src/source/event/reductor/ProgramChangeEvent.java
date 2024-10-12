package reductor;

import javax.sound.midi.MidiEvent;
import javax.sound.midi.ShortMessage;


public class ProgramChangeEvent extends Event<ShortMessage> {


    int channel;
    int instrument;


    ProgramChangeEvent(MidiEvent event, int trackIndex) {

        super(event, trackIndex);
        this.channel = this.message.getChannel();
        this.instrument = this.message.getData1();

    }


    @Override
    String dataString() {

        return "Channel " + this.channel + ", " + Constants.instrumentCodeToString(this.instrument);

    }


}
