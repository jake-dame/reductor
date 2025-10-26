package reductor.musicxml.exporter;


import org.audiveris.proxymusic.*;

import java.lang.String;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static reductor.musicxml.exporter.Defaults.*;


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
    9.) .... <player> (0+)
    10.) <midi-device> (0 or 1) + <midi-instrument> (0 or 1) (group: 0+)
*/

/*
:~~~~~~~~~ ADDITIONALLY ~~~~~~~~~:

*/

public class ScorePartBuilder {


    private PartName partName;
    private PartName partNameAbbreviation;
    private final List<ScoreInstrument> scoreInstruments;
    private final List<MidiDevice> midiDevices;
    private final List<MidiInstrument> midiInstruments;


    private ScorePartBuilder() {
        this.partName = FACTORY.createPartName();
        this.partNameAbbreviation = FACTORY.createPartName();
        this.scoreInstruments = new ArrayList<>();
        this.midiDevices = new ArrayList<>();
        this.midiInstruments = new ArrayList<>();
    }


    public static ScorePartBuilder builder() {
        return new ScorePartBuilder();
    }

    public ScorePartBuilder partName(String v) {
        PartName partName = FACTORY.createPartName();
        partName.setValue(v);
        this.partName = partName;
        return this;
    }

    public ScorePartBuilder partNameAbbreviation(String v) {
        PartName partNameAbbreviation = FACTORY.createPartName();
        partNameAbbreviation.setValue(v);
        this.partNameAbbreviation = partNameAbbreviation;
        return this;
    }

    /* This will not handle <solo>, <ensemble>, or <virtual-instrument>, for now. */
    public ScorePartBuilder scoreInstrument(String name, String sound) {
        ScoreInstrument si = FACTORY.createScoreInstrument();
        si.setInstrumentName(name);
        si.setInstrumentSound(sound);
        scoreInstruments.add(si);
        return this;
    }

    public ScorePartBuilder midiDevice(Object id, Integer port) {
        MidiDevice md = FACTORY.createMidiDevice();
        md.setId(id);
        md.setPort(port);
        midiDevices.add(md);
        return this;
    }

    /*
    """
    In this order

        <midi-channel> (Optional)
        <midi-name> (Optional)
        <midi-bank> (Optional)
        <midi-program> (Optional)
        <midi-unpitched> (Optional)
        <volume> (Optional)
        <pan> (Optional)
        <elevation> (Optional)
    """
    */
    /* This handles what MuseScore outputs. It does not output by default anything having to
    do with: <midi-name>, <midi-bank>, <midi-unpitched>, or <elevation>. */
    public ScorePartBuilder midiInstrument(
            Integer channel, Integer program, BigDecimal volume, BigDecimal pan
    ) {
        MidiInstrument mi = FACTORY.createMidiInstrument();
        mi.setMidiChannel(channel);
        mi.setMidiProgram(program);
        mi.setVolume(volume);
        mi.setPan(pan);
        midiInstruments.add(mi);
        return this;
    }

    public ScorePart build() {

        ScorePart scorePart = FACTORY.createScorePart();

        scorePart.setPartName(this.partName);
        scorePart.setPartAbbreviation(this.partNameAbbreviation);
        scorePart.getMidiDeviceAndMidiInstrument().addAll(this.scoreInstruments);
        scorePart.getMidiDeviceAndMidiInstrument().addAll(this.midiDevices);
        scorePart.getMidiDeviceAndMidiInstrument().addAll(this.midiInstruments);

        return scorePart;
    }

    //public ScorePart buildDefault() {
    //    return this.partName(
    //                    DEF_SCOREPART_PART_NAME
    //            )
    //            .partNameAbbreviation(
    //                    DEF_SCOREPART_PART_ABBREVIATION
    //            )
    //            .scoreInstrument(
    //                    DEF_SCORE_INSTRUMENT_NAME,
    //                    DEF_SCORE_INSTRUMENT_SOUND
    //            )
    //            .midiDevice(
    //                    this.scoreInstruments.getFirst(),
    //                    DEF_MIDI_PORT
    //            )
    //            .midiInstrument(
    //                    DEF_MIDI_INSTRUMENT_CHANNEL,
    //                    DEF_MIDI_INSTRUMENT_PROGRAM,
    //                    DEF_MIDIINSTRUMENT_VOLUME,
    //                    DEF_MIDIINSTRUMENT_PAN
    //            )
    //            .build();
    //}


}
