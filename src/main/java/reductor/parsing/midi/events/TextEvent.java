package reductor.parsing.midi.events;


import javax.sound.midi.MidiEvent;


public final class TextEvent extends MetaEvent {


    private final String text;


    TextEvent(MidiEvent event) {
        super(event);
        this.text = new String(this.getMessage().getData());
    }


    public String getText() {return this.text;}


    @Override
    String dataString() {return this.text;}


}
