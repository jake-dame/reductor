package reductor.musicxml.builder;


import org.audiveris.proxymusic.*;

import java.lang.String;
import java.math.BigDecimal;

import static reductor.dev.Defaults.FACTORY;


/*
 Look into this later, has a lot of notations we may want to preserve.
 Where all measure (as opposed to note-specific) text, performance directions, etc.
 <direction-type> has an enormous range of options.
 Look into "metronome" and see if you can set to staff/part, and if musescore will
 Otherwise, all we really care about for now is the <direction>/<sound>
 Additionally, <direction-type> is how you set words? According to MuseScore.

 These snippets are extracted from a 1-measure musescore doc where THREE tempos were
 added to that one and the same measure:
 ```xml
     <!-- Clicked and dragged "Lento" from the palette -->
     <direction placement="above" system="only-top">
        <direction-type>
            <words default-x="-37.68" relative-y="20" font-weight="bold"
            font-size="12">Lento</words>
        </direction-type>
        <sound tempo="52.5"/>
    </direction>

    <!-- Clicked and dragged "<8th note symbol> = 80" -->
    <direction placement="above" system="only-top">
        <direction-type>
            <metronome parentheses="no" default-x="-37.68" default-y="47.84"
            relative-y="20">
                <beat-unit>eighth</beat-unit>
                <per-minute>80</per-minute>
            </metronome>
        </direction-type>
        <sound tempo="40"/>
    </direction>

    <!--
    Clicked and dragged "<8th note symbol> = 80" __AND THEN__ typed Lento in front of
    it inside that same text box
    -->
    <direction placement="above" system="only-top">
        <direction-type>
            <words default-x="-37.68" default-y="22.94" relative-y="20"
            font-weight="bold" font-size="12">Lento </words>
        </direction-type>
        <direction-type>
            <metronome parentheses="no" default-x="-37.68" default-y="22.94"
            relative-y="20">
                <beat-unit>eighth</beat-unit>
                <per-minute>80</per-minute>
            </metronome>
        </direction-type>
        <sound tempo="40"/>
    </direction>
 ```
*/

/*
<direction-type> (1+)
<offset> (0 or 1)
<footnote> (0 or 1)
<level> (0 or 1)
<voice> (0 or 1)
<staff> (0 or 1)
<sound> (0 or 1)
<listening> (0 or 1)
*/

public class DirectionBuilder {


    private final Direction direction;
    private DirectionBuilder() { this.direction = FACTORY.createDirection(); }
    public static DirectionBuilder builder() { return new DirectionBuilder(); }
    public Direction build() { return this.direction; }


    /*
    This is finicky both in terms of ProxyMusic and MusicXML spec itself.
        - I am very surprised MusicXML doesn't have a more straight-forward way to do this like
        it does with so many other things, including things that are comparable in terms of
        position in the hierarchy, re-use, id-relation, etc. Seems like this should be reworked
        in 5.0
        - ProxyMusic reflects this unclear design, and does not have any object or class called
        Words for the <words> element.
        - Instead, you set the value of a FormattedTextId object. Nope, not FormattedText like in
         other places. FormattedTextId.

    MuseScore won't even display a raw <direction>/<words> as a tempo.
        - If you click and drag or manually add a Tempo to a measure in the MuseScore GUI, and
        export the musicxml, it looks like this:
            ```xml
              <direction placement="above" system="only-top">
                <direction-type>
                  <words relative-y="20" font-weight="bold" font-size="12">Allegro</words>
                  </direction-type>
                <sound tempo="144"/>
                </direction>
             ```
         - If you open MuseScore with a musicxml file that just has
            ```xml
            <direction-type>
              <words>Allegro</words>
            </direction-type>
            ```

         it will just look like small, normal, random text. Even if you re-export it, MuseScore
         only adds position info (default-x and -y), not sizing, bolding, etc.

         Where to go from here? Cater exactly to MuseScore, and this API outputs
         <direction>/<words> with actual
             page layout and position information, something it doesn't do anywhere else in this
             program. That would make MuseScore happy, but would not be portable to other
             notation software I assume. So, for now, we will just settle for little non-tempo
             looking tempos at the tops of scores.
    */
    public DirectionBuilder tempoWords(String v) {
        FormattedTextId words = FACTORY.createFormattedTextId();
        words.setValue(v);
        return this.directionType(words);
    }

    /*
     This can get more much more complex and would need to be a builder. For now, to
     avoid that, we are keeping it simple.

     Calling info:
         - noteType == <beat-unit>
         - perMinute can include words and text, and seems to just be a text literal.
         I don't know how MuseScore parses this, but best to stick to int strings like
         "80", "124", etc.
    */
    public DirectionBuilder tempoMetronome(String noteType, String perMinute) {

        PerMinute pm = FACTORY.createPerMinute();
        pm.setValue(perMinute);

        Metronome metronome = FACTORY.createMetronome();
        metronome.setBeatUnit(noteType);
        metronome.setPerMinute(pm);

        return this.directionType(metronome);
    }

    public DirectionBuilder directionType(Object o) {

        DirectionType dt = FACTORY.createDirectionType();

        switch (o) {
            case Metronome m -> dt.setMetronome(m);
            case FormattedTextId words -> dt.getWordsOrSymbol().add(words);
            default -> throw new RuntimeException("only supports <words> and <metronome>");
        }

        this.direction.getDirectionType().add(dt);
        return this;
    }

    // This is the "invisible" or greyed-out tempo direction in musescore. It defaults to to
    // quarter note as the beat type.
    public DirectionBuilder playbackSpeed(int bpm) {
        Sound sound = FACTORY.createSound();
        sound.setTempo(BigDecimal.valueOf(bpm));
        this.direction.setSound(sound);
        return this;
    }


}
