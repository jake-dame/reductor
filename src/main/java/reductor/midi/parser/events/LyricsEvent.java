package reductor.midi.importer.parser.events;


import javax.sound.midi.MidiEvent;


public final class LyricsEvent extends MetaEvent {


    private final String lyrics;


    LyricsEvent(MidiEvent event) {
        super(event);
        this.lyrics = new String(this.getMessage().getData());
    }


    public String getLyrics() {return this.lyrics;}


    @Override
    String dataString() {return lyrics;}


}
