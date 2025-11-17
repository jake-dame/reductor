package reductor.musicxml.exporter.builder;


import org.audiveris.proxymusic.*;
import org.audiveris.proxymusic.ScorePartwise.Part;
import org.audiveris.proxymusic.ScorePartwise.Part.Measure;
import reductor.core.Piece;
import reductor.core.builders.PieceBuilder;
import reductor.midi.MidiReader;
import reductor.midi.importer.MidiAdapter;
import reductor.midi.importer.UnpairedNoteException;
import reductor.midi.importer.parser.MidiContainer;

import javax.sound.midi.InvalidMidiDataException;
import java.lang.String;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.file.Path;


public class Defaults {

    public static final ObjectFactory FACTORY = new ObjectFactory();

    public static Clef trebleClef() {
        Clef c = FACTORY.createClef();
        c.setSign(ClefSign.G);
        c.setLine(new BigInteger("2"));
        return c;
    }

    public static Clef bassClef() {
        Clef c = FACTORY.createClef();
        c.setSign(ClefSign.F);
        c.setLine(new BigInteger("4"));
        return c;
    }

    public static Clef altoClef() {
        Clef c = FACTORY.createClef();
        c.setSign(ClefSign.C);
        c.setLine(new BigInteger("3"));
        return c;
    }

    public static Clef tenorClef() {
        Clef c = FACTORY.createClef();
        c.setSign(ClefSign.C);
        c.setLine(new BigInteger("4"));
        return c;
    }



    /** A piano scorepart. */
    //private static ScorePart defaultScorePart() {
    //
    //    ScorePart scorePart = FACTORY.createScorePart();
    //
    //    PartName partName = FACTORY.createPartName();
    //    partName.setValue(DEF_SCOREPART_PART_NAME);
    //
    //    PartName partNameAbbrev = FACTORY.createPartName();
    //    partNameAbbrev.setValue(DEF_SCOREPART_PART_ABBREVIATION);
    //
    //    ScoreInstrument scoreInstrument = FACTORY.createScoreInstrument();
    //    scoreInstrument.setId(DEF_SCORE_INSTRUMENT_ID);
    //
    //    scoreInstrument.setInstrumentName(DEF_SCORE_INSTRUMENT_NAME);
    //    scoreInstrument.setInstrumentSound(DEF_SCORE_INSTRUMENT_SOUND);
    //
    //    MidiDevice midiDevice = FACTORY.createMidiDevice();
    //    midiDevice.setId(scoreInstrument);
    //    midiDevice.setPort(DEF_MIDI_DEVICE_PORT);
    //    scorePart.getMidiDeviceAndMidiInstrument().add(midiDevice);
    //
    //    MidiInstrument midiInstrument = FACTORY.createMidiInstrument();
    //    midiInstrument.setId(scoreInstrument);
    //    midiInstrument.setMidiChannel(DEF_MIDI_INSTRUMENT_CHANNEL);
    //    midiInstrument.setMidiProgram(DEF_MIDI_PORT);
    //    midiInstrument.setVolume(DEF_MIDIINSTRUMENT_VOLUME);
    //    midiInstrument.setPan(DEF_MIDIINSTRUMENT_PAN);
    //
    //
    //    scorePart.getScoreInstrument().add(scoreInstrument);
    //
    //    scorePart.setId(DEF_SCOREPART_ID);
    //
    //    scorePart.setPartAbbreviation(partNameAbbrev);
    //    scorePart.setPartName(partName);
    //
    //    scorePart.getMidiDeviceAndMidiInstrument().add(midiInstrument);
    //
    //    return scorePart;
    //}

    //public static Part defaultPart() {
    //    ScorePart scorePart = defaultScorePart();
    //    Part part = FACTORY.createScorePartwisePart();
    //    part.setId(scorePart);
    //    part.getMeasure().add(defaultMeasure());
    //    return null;
    //}

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


    public static String id(int id) { return "P"+id; }


    public static Piece getTester(Path filePath) {

        Piece piece = null;
        try {
            var sequence = MidiReader.readInMidiFile(filePath);
            var mc = new MidiContainer(sequence);
            piece = MidiAdapter.toPiece(mc);
        } catch(InvalidMidiDataException | UnpairedNoteException e) {
            System.err.println(e.getMessage());
        }

        return piece;
    }


}
