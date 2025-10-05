package reductor.parsing.midi.events;


import javax.sound.midi.MidiEvent;


public final class CopyrightNoticeEvent extends MetaEvent {


    private final String copyrightNotice;


    CopyrightNoticeEvent(MidiEvent event) {
        super(event);
        this.copyrightNotice = new String(this.getMessage().getData());
    }


    @Override
    String dataString() {return this.copyrightNotice;}


}
