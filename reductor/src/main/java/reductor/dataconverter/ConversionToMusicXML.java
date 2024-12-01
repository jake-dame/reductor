package reductor.dataconverter;

import org.audiveris.proxymusic.*;
import org.audiveris.proxymusic.ScorePartwise.Part;
import org.audiveris.proxymusic.util.Marshalling;
import reductor.Application;
import reductor.piece.Measure;
import reductor.piece.Piece;
import reductor.piece.Note;


import javax.sound.midi.InvalidMidiDataException;
import java.io.*;
import java.lang.String;
import java.math.BigDecimal;
import java.math.BigInteger;

import static reductor.Files.*;


public class ConversionToMusicXML {

    public static void main(String[] args) throws InvalidMidiDataException, UnpairedNoteException, Marshalling.MarshallingException, IOException {

        Piece piece = Application.getPiece(CHOPIN_PREL_c);
        Measure m1 = Application.getPiece(CHOPIN_PREL_c).getMeasures().getFirst();

        ConversionToMusicXML converter = new ConversionToMusicXML();
        converter.getMusicXMLFromPiece(piece);
        converter.write();

    }

    public void write() throws IOException, Marshalling.MarshallingException {

        String outName = "test";
        File xmlFile = new File(MIDI_FILES_OUT_DIR + outName + ".musicxml"); // .xml used in his test
        OutputStream stream = new FileOutputStream(xmlFile);

        Marshalling.marshal(scorePartwise, stream, true, 2);

        stream.close();
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                                                //
    //                                                                                                                //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    ObjectFactory factory;
    ScorePartwise scorePartwise;
    PartList partList;
    ScorePart scorePart;
    Part part;

    Piece piece;


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                                                //
    //                                                                                                                //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    public void getMusicXMLFromPiece(Piece piece) {

        this.piece = piece;

        // You need this to get any object in the whole proxymusic library. So any function using that library should
        // have this (unless, perhaps, it is possible to make a static one at the top of this class?)
        factory = new ObjectFactory();

        // This is the root/parent element of everything in a musicXML document (it has a sister-version called score-timewise that is rarely used)
        scorePartwise = factory.createScorePartwise();

        // Everything related to score header elements gets created/assigned here (limited to part-list for now)
        // This will essentially be the same for every file we output (they are all solo piano pieces)
        buildScoreHeader();

        // We have all the setup done, and we have the single part that we need to add all the measures to.
        // At this point, the task is to construct all the measures and add them to this part below.
        part = factory.createScorePartwisePart();
        scorePartwise.getPart().add(part); // This returns a list. No idea why it's named singular.
        part.setId(scorePart); // This is how he does it in the test file. Just gonna go with it.

        //for (Measure measure : piece.getMeasures()) {
        //    buildMeasure(measure);
        //}

    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                                                //
    //                                                                                                                //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    public void buildScoreHeader() {
        this.partList = buildPartList();
        this.scorePart = buildScorePart();
    }

    public PartList buildPartList() {

        /* This is exactly what the part-list in MuseScore looks like for Chopin Prel e */

        /*
        <part-list>
        <score-part id="P1">
          <part-name>Piano</part-name>
          <part-abbreviation>Pno.</part-abbreviation>
          <score-instrument id="P1-I1">
            <instrument-name>Piano</instrument-name>
            <instrument-sound>keyboard.piano</instrument-sound>
          </score-instrument>
          <midi-device id="P1-I1" port="1"></midi-device>
          <midi-instrument id="P1-I1">
            <midi-channel>1</midi-channel>
            <midi-program>1</midi-program>
            <volume>78.7402</volume>
            <pan>0</pan>
            </midi-instrument>
          </score-part>
        </part-list>
        */

        PartList partList = factory.createPartList(); // get from factory
        scorePartwise.setPartList(partList); // attach to parent element (root in this case, and for all score header elements

        return partList; // return a reference to the object
    }

    public ScorePart buildScorePart() {

        ScorePart scorePart = factory.createScorePart();
            scorePart.setId("P1");
            /*
             This is just a getter for the List<Object>; .add() is List.add
             Instead of making a bunch of wrapper functions for list operations, he just
             requires you to get the reference to the list and then do whatever you want.

             This seems to be the pattern with certain things. So if you can't find a setter, search
             for a getter that returns a List<> and just add it.
            */
            partList.getPartGroupOrScorePart().add(scorePart); // attach

        PartName partName = factory.createPartName();
            partName.setValue("Piano");
            scorePart.setPartName(partName); // attach

        /*
         There is no PartNameAbbreviation object or anything, but ScorePart has
         ScorePart.setPartNameAbbreviation(PartName partName) which takes a vanilla PartName object, so
         I'm assuming this is how you do that
        */
        PartName partNameAbbrev = factory.createPartName();
            partNameAbbrev.setValue("Pno.");
            scorePart.setPartAbbreviation(partNameAbbrev); // attach

        ScoreInstrument scoreInstrument = factory.createScoreInstrument();
            scoreInstrument.setId("P1-I1");
            scoreInstrument.setInstrumentName("Piano");
            scoreInstrument.setInstrumentSound("keyboard.piano");
            scorePart.getScoreInstrument().add(scoreInstrument); // attach

        /*
          Sometimes you give something a String id, sometimes you give it an object id. Just check the signature, and
          give it the object that has the matching id in the musescore file using eyeballs.
         */
        MidiDevice midiDevice = factory.createMidiDevice();
            midiDevice.setId(scoreInstrument);
            midiDevice.setPort(1);
            scorePart.getMidiDeviceAndMidiInstrument().add(midiDevice); // attach

        MidiInstrument midiInstrument = factory.createMidiInstrument();
            midiInstrument.setId(scoreInstrument);
            midiInstrument.setMidiChannel(1); // hmmm
            midiInstrument.setMidiProgram(1); // hmmm
            midiInstrument.setVolume(new BigDecimal("78.7402"));
            midiInstrument.setPan(new BigDecimal("0"));
            scorePart.getMidiDeviceAndMidiInstrument().add(midiInstrument); // attach

        return scorePart;
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                                                //
    //                                                                                                                //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    public void extractMeasure(Measure pieceMeasure) {

        ScorePartwise.Part.Measure measure = factory.createScorePartwisePartMeasure();
        part.getMeasure().add(measure); // attach

        var measureNumString = extractMeasureNumber(pieceMeasure);
        measure.setNumber(measureNumString); // attach

        Attributes attributes = extractAttributes(measure, pieceMeasure);
        measure.getNoteOrBackupOrForward().add(attributes); // attach

        for (Note pieceNote : pieceMeasure.getNotes()) {
            org.audiveris.proxymusic.Note note = extractNote(pieceNote);
            measure.getNoteOrBackupOrForward().add(note); // attach
        }

    }

    private Attributes extractAttributes(Part.Measure measure, Measure pieceMeasure) {

        Attributes attributes = factory.createAttributes();

        /*
        Attributes for measure one; should be pretty straightforward to get from TimeSignature, KeySignature
        The divisions, however, will be a big deal

        <attributes>
        <divisions>24</divisions>
        <key>
          <fifths>1</fifths>
          </key>
        <time>
          <beats>4</beats>
          <beat-type>4</beat-type>
          </time>
        <staves>2</staves> // TODO
        <clef number="1">
          <sign>G</sign>
          <line>2</line>
          </clef>
        <clef number="2">
          <sign>F</sign>
          <line>4</line>
          </clef>
        </attributes>
        */

        // Divisions
        BigDecimal divisions = extractDivisions(pieceMeasure);
        attributes.setDivisions(divisions);

        // Key
        Key key = extractKeySignature(pieceMeasure);
        attributes.getKey().add(key); // attach

        // Time
        Time time = extractTimeSignature(pieceMeasure);
        attributes.getTime().add(time); // attach

        // Clefs
        Clef trebleClef = extractTrebleClef();
        attributes.getClef().add(trebleClef); // attach

        Clef bassClef = extractBassClef();
        attributes.getClef().add(bassClef); // attach

        // Tempo
        Direction direction = extractTempo(pieceMeasure);
        measure.getNoteOrBackupOrForward().add(direction); // attach

        return attributes;
    }

    public Clef extractTrebleClef() {
        Clef trebleClef = factory.createClef();
        trebleClef.setSign(ClefSign.G);
        trebleClef.setLine(new BigInteger("2"));
        trebleClef.setNumber(new BigInteger("1")); // TODO hmmm
        return trebleClef;
    }

    public Clef extractBassClef() {
        Clef bassClef = factory.createClef();
        bassClef.setSign(ClefSign.F);
        bassClef.setLine(new BigInteger("4"));
        bassClef.setNumber(new BigInteger("2")); // TODO hmmm
        return bassClef;
    }

    public Direction extractTempo(Measure pieceMeasure) {
                /*

        TEMPO: super low priority, useful for playback only

        This is everything I can find for the below stuff in the Chopin. It belongs in a Measure attributes

        <direction placement="above">
        <direction-type>
          <words default-x="-37.67" relative-y="20" font-weight="bold" font-size="12">Largo</words>
          </direction-type>
        <staff>1</staff>
        <sound tempo="50"/>
        </direction>

        */

        //// Direction / Metronome / Sound
        //Direction direction = factory.createDirection();
        //DirectionType directionType = factory.createDirectionType();
        //direction.getDirectionType().add(directionType);
        //
        //Metronome metronome = factory.createMetronome();
        //metronome.setBeatUnit(directionData.beatUnit);
        //for (int i = 0; i < directionData.dots; i++) {
        //    metronome.getBeatUnitDot().add(factory.createEmpty());
        //}
        //
        //// Doesn't seem to be recognized by Finale or MuseScore
        ////        BeatUnitTied but = factory.createBeatUnitTied();
        ////        but.setBeatUnit("eighth");
        ////        metronome.getBeatUnitTied().add(but);
        //
        //PerMinute perMinute = factory.createPerMinute();
        //perMinute.setValue(directionData.perMinute);
        //metronome.setPerMinute(perMinute);
        //metronome.setParentheses(directionData.parentheses);
        //directionType.setMetronome(metronome);
        //
        //Sound sound = factory.createSound();
        //sound.setTempo(directionData.tempo);
        //direction.setSound(sound);

        return null;
    }

    private BigDecimal extractDivisions(Measure pieceMeasure) {
        return null; // TODO
    }

    private String extractMeasureNumber(Measure pieceMeasure) {
        return String.valueOf(pieceMeasure.getMeasureNumber());
    }

    private Time extractTimeSignature(Measure pieceMeasure) {
        Time time = factory.createTime();
        String numerator = String.valueOf(pieceMeasure.getTimeSignature().getNumerator());
        String denominator = String.valueOf(pieceMeasure.getTimeSignature().getDenominator());
        time.getTimeSignature().add(factory.createTimeBeats(numerator));
        time.getTimeSignature().add(factory.createTimeBeatType(denominator));
        return time;
    }

    private Key extractKeySignature(Measure pieceMeasure) {
        Key key = factory.createKey();
        String stringVal = String.valueOf(pieceMeasure.getKeySignature().getAccidentals());
        key.setFifths(new BigInteger(stringVal));
        return key;
    }

    public org.audiveris.proxymusic.Note extractNote(Note pieceNote) {

        org.audiveris.proxymusic.Note note = factory.createNote();

        // Pitch
        Pitch pitch = extractPitch(pieceNote);
        note.setPitch(pitch); // attach

        // Duration
        BigDecimal duration = extractDuration(pieceNote);
        note.setDuration(duration);

        // Type
        NoteType type = extractNoteType(pieceNote);
        note.setType(type); // attach

        return note;
    }


    public Pitch extractPitch(Note pieceNote) {
        Pitch pitch = factory.createPitch();

        pitch.setStep(Step.C); // TODO

        int register = reductor.piece.Pitch.getRegister(pieceNote.pitch());
        pitch.setOctave(register);

        return pitch;
    }

    public BigDecimal extractDuration(Note pieceNote) {
        return null; // TODO
    }

    public NoteType extractNoteType(Note pieceNote) {
        NoteType type = factory.createNoteType(); // TODO
        type.setValue("whole");
        return type;
    }

}
