package reductor;

import javax.sound.midi.MidiEvent;
import javax.sound.midi.ShortMessage;


public class ControlChangeEvent extends Event<ShortMessage> {

    int channel;
    int controllerCode;
    int controllerValue;


    ControlChangeEvent(MidiEvent event, int trackIndex) {

        super(event, trackIndex);
        this.channel = this.message.getChannel();
        this.controllerCode = this.message.getData1();
        this.controllerValue = this.message.getData2();

    }


    @Override
    String dataString() {

        return "Channel " + this.channel
                + ", Controller: " + Constants.contollerCodeToString(this.controllerCode)
                + ", " + this.controllerValue;

    }


}
