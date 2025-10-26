package reductor.musicxml.exporter;


import org.audiveris.proxymusic.*;
import org.audiveris.proxymusic.ScorePartwise.Part;
import org.audiveris.proxymusic.ScorePartwise.Part.Measure;

import java.lang.String;
import java.math.BigDecimal;
import java.math.BigInteger;


public class Defaults {

    // MISCELLANEOUS
    public static final Object PLACEHOLDER_OBJ = new Object();
    public static final org.audiveris.proxymusic.ObjectFactory FACTORY = new ObjectFactory();

    // SCOREPARTWISE DEFAULTS
    /* Movement title and number may seem backwards, but due to the way MuseScore sources metadata
    for <credit> population, we want 'XX.' as the title, and 'Der Wegweiser' as the subtitle. */
    public static final String DEF_WORK_TITLE = "Winterreise";
    public static final String DEF_WORK_NUMBER = "D.911";
    public static final String DEF_MOVEMENT_NUMBER = "Der Wegweiser";
    public static final String DEF_MOVEMENT_TITLE = "XX.";

    // SCOREPART DEFAULTS
    public static final String DEF_SCOREPART_ID = "P1";
    public static final String DEF_SCOREPART_PART_NAME = "Piano";
    public static final String DEF_SCOREPART_PART_ABBREVIATION = "Pno.";

    // SCOREPART --> SCORE INSTRUMENT
    public static final String DEF_SCORE_INSTRUMENT_ID = "P1-I1";
    public static final String DEF_SCORE_INSTRUMENT_NAME = "Piano";
    public static final String DEF_SCORE_INSTRUMENT_SOUND = "keyboard.piano";

    // SCOREPART --> MIDI DEVICE
    public static final int DEF_MIDI_DEVICE_PORT = 1;
    public static final Integer DEF_MIDI_INSTRUMENT_PROGRAM = 1;

    // SCOREPART --> MIDI INSTRUMENT
    public static final int DEF_MIDI_INSTRUMENT_CHANNEL = 1;
    public static final int DEF_MIDI_PORT = 1;


    // PART DEFAULTS
    public static final String DEF_PART_ID = DEF_SCOREPART_ID;

    /*
    Based on what MuseScore outputs.
    */
    public static final BigDecimal DEF_MIDIINSTRUMENT_VOLUME = new BigDecimal("78.7402");
    public static final BigDecimal DEF_MIDIINSTRUMENT_PAN = new BigDecimal("0");

    public static final Clef DEF_TREBLE_CLEF;
    public static final Clef DEF_BASS_CLEF;
    public static final Clef DEF_ALTO_CLEF;
    public static final Clef DEF_TENOR_CLEF;
    static {
        DEF_TREBLE_CLEF = FACTORY.createClef();
        DEF_TREBLE_CLEF.setSign(ClefSign.G);
        DEF_TREBLE_CLEF.setLine(new BigInteger("2"));

        DEF_BASS_CLEF = FACTORY.createClef();
        DEF_BASS_CLEF.setSign(ClefSign.F);
        DEF_BASS_CLEF.setLine(new BigInteger("4"));

        DEF_ALTO_CLEF = FACTORY.createClef();
        DEF_ALTO_CLEF.setSign(ClefSign.C);
        DEF_ALTO_CLEF.setLine(new BigInteger("3"));

        DEF_TENOR_CLEF = FACTORY.createClef();
        DEF_TENOR_CLEF.setSign(ClefSign.C);
        DEF_TENOR_CLEF.setLine(new BigInteger("4"));
    }

    /** A piano scorepart. */
    private static ScorePart defaultScorePart() {

        ScorePart scorePart = FACTORY.createScorePart();

        PartName partName = FACTORY.createPartName();
        partName.setValue(DEF_SCOREPART_PART_NAME);

        PartName partNameAbbrev = FACTORY.createPartName();
        partNameAbbrev.setValue(DEF_SCOREPART_PART_ABBREVIATION);

        ScoreInstrument scoreInstrument = FACTORY.createScoreInstrument();
        scoreInstrument.setId(DEF_SCORE_INSTRUMENT_ID);

        scoreInstrument.setInstrumentName(DEF_SCORE_INSTRUMENT_NAME);
        scoreInstrument.setInstrumentSound(DEF_SCORE_INSTRUMENT_SOUND);

        MidiDevice midiDevice = FACTORY.createMidiDevice();
        midiDevice.setId(scoreInstrument);
        midiDevice.setPort(DEF_MIDI_DEVICE_PORT);
        scorePart.getMidiDeviceAndMidiInstrument().add(midiDevice);

        MidiInstrument midiInstrument = FACTORY.createMidiInstrument();
        midiInstrument.setId(scoreInstrument);
        midiInstrument.setMidiChannel(DEF_MIDI_INSTRUMENT_CHANNEL);
        midiInstrument.setMidiProgram(DEF_MIDI_PORT);
        midiInstrument.setVolume(DEF_MIDIINSTRUMENT_VOLUME);
        midiInstrument.setPan(DEF_MIDIINSTRUMENT_PAN);


        scorePart.getScoreInstrument().add(scoreInstrument);

        scorePart.setId(DEF_SCOREPART_ID);

        scorePart.setPartAbbreviation(partNameAbbrev);
        scorePart.setPartName(partName);

        scorePart.getMidiDeviceAndMidiInstrument().add(midiInstrument);

        return scorePart;
    }

    /**
     * A piano part containing one default measure.
     *
     * @see #defaultScorePart()
     * @see #defaultMeasure()
     * @see #defaultNote()
     * */
    public static Part defaultPart() {
        ScorePart scorePart = defaultScorePart();
        Part part = FACTORY.createScorePartwisePart();
        part.setId(scorePart);
        part.getMeasure().add(defaultMeasure());
        return null;
    }

    /**
     * A 3/4 measure in D Major containing a single default note.
     * @see #defaultNote()
     */
    private static Measure defaultMeasure() {
        Part.Measure measure = FACTORY.createScorePartwisePartMeasure();
        measure.setNumber("1");

        Attributes attributes = FACTORY.createAttributes();
        attributes.setDivisions(new BigDecimal("480"));

        Key key = FACTORY.createKey();
        key.setFifths(new BigInteger("2"));
        key.setMode("major");
        //key.setNumber(new BigInteger("2"));
        attributes.getKey().add(key);

        Time time = FACTORY.createTime();
        time.getTimeSignature().add(FACTORY.createTimeBeats("3"));
        time.getTimeSignature().add(FACTORY.createTimeBeatType("4"));
        //time.setNumber(new BigInteger("1"));
        attributes.getTime().add(time);

        //attributes.getClef().add(MeasureBuilder.buildTrebleClef());
        //attributes.getClef().add(MeasureBuilder.buildBassClef());
        //
        attributes.setStaves(new BigInteger("2"));

        measure.getNoteOrBackupOrForward().add(attributes);

        Direction direction = FACTORY.createDirection();
        Sound sound = FACTORY.createSound();
        sound.setTempo(new BigDecimal("120"));
        direction.setSound(sound);
        measure.getNoteOrBackupOrForward().add(direction);

        measure.getNoteOrBackupOrForward().add(defaultNote());

        return measure;
    }

    public static final Step DEF_PITCH_STEP = Step.valueOf("F");
    public static final BigDecimal DEF_PITCH_ALTER = new BigDecimal("1");
    public static final int DEF_PITCH_OCTAVE = 5;
    public static final String DEF_NOTE_DISPLAY_TYPE = "quarter";
    public static final BigDecimal DEF_NOTE_DURATION = new BigDecimal("480");
    public static final BigInteger DEF_NOTE_STAFF = new BigInteger("1");

    /** An F#5 quarter note. */
    private static Note defaultNote() {

        /* Pitch */
        Pitch pitch = FACTORY.createPitch();
        pitch.setStep(DEF_PITCH_STEP);
        pitch.setAlter(DEF_PITCH_ALTER);
        pitch.setOctave(DEF_PITCH_OCTAVE);

        /* Type */
        NoteType noteType = FACTORY.createNoteType();
        noteType.setValue(DEF_NOTE_DISPLAY_TYPE);

        /* Note */
        Note note = FACTORY.createNote();
        note.setPitch(pitch);
        note.setDuration(DEF_NOTE_DURATION);
        note.setStaff(DEF_NOTE_STAFF);

        return note;
    }


}
