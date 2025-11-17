package reductor.musicxml.builder;


import org.audiveris.proxymusic.*;

import java.lang.String;

import static reductor.dev.Defaults.*;


/*
:~~~~~~~~~ SPEC SUMMARY ~~~~~~~~~:

    1.) .... <identification> (0 or 1)
    2.) .... <part-link> (0+)
    3.) <part-name> (1)
    4.) .... <part-name-display> (0 or 1)
    5.) <part-abbreviation> (0 or 1)
    6.) .... <part-abbreviation-display> (0 or 1)
    7.) .... <group> (0+)
    8.) <score-instrument> (0+)
        a. <instrument-name> (1)
        b. <instrument-abbreviation> (0 or 1)
        c. <instrument-sound> (0 or 1)
        d. .... <solo> OR .... <ensemble> (0 or 1)
        e. .... <virtual-instrument> (0 or 1)
    9.) .... <player> (0+)
    10.) <midi-device> (0 or 1) + <midi-instrument> (0 or 1) (group: 0+)

        a.) <midi-device>
        b.) <midi-instrument>
            i.) <midi-channel> (0 or 1)
            ii.) .... <midi-name> (0 or 1)
            iii.) .... <midi-bank> (0 or 1)
            iv.) <midi-program> (0 or 1)
            v.) .... <midi-unpitched> (0 or 1)
            vi.) <volume> (0 or 1)
            vii.) <pan> (0 or 1)
            viii.) .... <elevation> (0 or 1)


*/

/*
:~~~~~~~~~ ADDITIONALLY ~~~~~~~~~:

*/

public class ScorePartBuilder {


    private final ScorePart scorePart;
    private ScorePartBuilder() { this.scorePart = FACTORY.createScorePart(); }
    public static ScorePartBuilder builder() { return new ScorePartBuilder(); }
    public ScorePart build() { return this.scorePart; }

    public ScorePartBuilder id(String v) {
        this.scorePart.setId(v);
        return this;
    }

    public ScorePartBuilder partName(String v) {
        PartName partName = FACTORY.createPartName();
        partName.setValue(v);
        this.scorePart.setPartName(partName);
        return this;
    }

    public ScorePartBuilder partNameAbbreviation(String v) {
        PartName partNameAbbreviation = FACTORY.createPartName();
        partNameAbbreviation.setValue(v);
        this.scorePart.setPartAbbreviation(partNameAbbreviation);
        return this;
    }

    // TODO: flesh this out
    public ScorePartBuilder scoreInstrument(ScoreInstrument scoreInstrument) {
        this.scorePart.getScoreInstrument().add(scoreInstrument);
        return this;
    }

    // TODO: flesh this out
    public ScorePartBuilder midiDevice(MidiDevice midiDevice) {
        this.scorePart.getMidiDeviceAndMidiInstrument().add(midiDevice);
        return this;
    }

    // TODO: flesh this out
    public ScorePartBuilder midiInstrument(MidiInstrument midiInstrument) {
        this.scorePart.getMidiDeviceAndMidiInstrument().add(midiInstrument);
        return this;
    }


}
