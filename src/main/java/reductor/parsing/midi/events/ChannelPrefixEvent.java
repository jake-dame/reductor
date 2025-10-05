package reductor.parsing.midi.events;


import javax.sound.midi.MidiEvent;


public class ChannelPrefixEvent extends MetaEvent {


    ChannelPrefixEvent(MidiEvent event) {
        super(event);
    }


    @Override
    String dataString() {return "";} // TODO


}
