package reductor;

import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;


/**
 * This is a wrapper class for a Java MidiEvent
 *
 * @param <T> The type of MidiMessage the Event holds
 */
public abstract class Event<T extends MidiMessage> {


    MidiEvent event;
    T message;
    int trackIndex;
    long tick;


    Event(MidiEvent event, int trackIndex) {

        this.event = event;
        this.tick = event.getTick();
        this.trackIndex = trackIndex;
        this.message = (T) event.getMessage();

    }


    abstract String dataString();


    //@Override
    //public String toString() {
    //
    //    return String.format("Track %d  Tick  %d  :  %s",
    //            this.trackIndex,
    //            this.tick,
    //            dataString()
    //    );
    //
    //}

    @Override
    public String toString() {

        return String.format("(%s) Track %d  Tick  %d  :  %s",
                this.getClass().getSimpleName(),
                this.trackIndex,
                this.tick,
                dataString()
        );

    }


}
