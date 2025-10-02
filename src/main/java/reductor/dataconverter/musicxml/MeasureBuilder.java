package reductor.dataconverter.musicxml;

import org.audiveris.proxymusic.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reductor.core.Measure;

import java.lang.String;
import java.math.BigDecimal;
import java.math.BigInteger;

import static reductor.dataconverter.musicxml.ConversionToMusicXml.factory;


public class MeasureBuilder {

    private static final Logger logger = LoggerFactory.getLogger(MeasureBuilder.class);

    private Measure prevPieceMeasure;

    MeasureBuilder() {
        prevPieceMeasure = null;
    }

    void setLastMeasure(Measure val) {
        this.prevPieceMeasure = val;
    }

    public ScorePartwise.Part.Measure build(Measure pieceMeasure, long tpq) {

        // Create empty measure.
        ScorePartwise.Part.Measure measure = factory.createScorePartwisePartMeasure();

        // Assign measure number.
        String measureNumString = buildMeasureNumber(pieceMeasure);
        if (pieceMeasure.isPickup()) {
            logger.debug("set an implicit for {}", pieceMeasure);
            measure.setImplicit(YesNo.YES);
        }
        measure.setNumber(measureNumString);

        // Attributes (same level)
        Attributes attributes = buildAttributes(measure, pieceMeasure);
        if (attributes != null) {
            logger.debug("attributes changed at {}", pieceMeasure);
            measure.getNoteOrBackupOrForward().add(attributes);
        }

        // Direction (same level)
        Direction direction = buildDirection(pieceMeasure);
        if (direction != null) {
            logger.debug("tempo changed at {}", pieceMeasure);
            measure.getNoteOrBackupOrForward().add(direction);
        }

        if (prevPieceMeasure == null) {
            logger.debug("handling first measure: {}", pieceMeasure);
            additionalHandlingForFirstMeasure(attributes, pieceMeasure, tpq);
        }

        return measure;
    }

    // These things will be set once PER PIECE and will never be allowed to change throughout the piece
    private void additionalHandlingForFirstMeasure(Attributes attributes, Measure pieceMeasure, long tpq) {

        // Divisions (we are just doing straight-across MIDI resolution --> MusicXML divisions; i.e. ticks-per-quarter
        attributes.setDivisions( new BigDecimal(tpq) );

        // Staves (should always be 2 for piano pieces)
        attributes.setStaves(new BigInteger("2"));

        // Clefs (this is rough-cutting, but too complex for now to allow this to be changed dynamically)
        attributes.getClef().add( buildTrebleClef() );
        attributes.getClef().add( buildBassClef() );
    }


    /////////////////////////////////////////////////////////////////////////////////////////////////////


    private Attributes buildAttributes(ScorePartwise.Part.Measure measure, Measure pieceMeasure) {

        Attributes attributes = factory.createAttributes();

        // If this remains false by the end of the function, the Attributes object returned will be null.
        boolean attributesChanged = false;

        // Key
        Key key = buildKey(pieceMeasure);
        if (key != null) {
            attributes.getKey().add(key); // attach
            attributesChanged = true;
        }

        // Time
        Time time = buildTimeSignature(pieceMeasure);
        if (time != null) {
            attributes.getTime().add(time); // attach
            attributesChanged = true;
        }

        return attributesChanged ? attributes : null;
    }

    // This is where tempo gets set (for playback as well as on-the-page).
    private Direction buildDirection(Measure pieceMeasure) {

        if (prevPieceMeasure != null
                && pieceMeasure.getTempo().getBpm() == prevPieceMeasure.getTempo().getBpm()) {
            return null;
        }

        Sound sound = factory.createSound(); // create
        int bpm = pieceMeasure.getTempo().getBpm(); // calculate
        sound.setTempo(new BigDecimal(bpm)); // set

        Direction direction = factory.createDirection(); // create
        direction.setSound(sound); // set

        return direction;
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////


    private Clef buildTrebleClef() {
        Clef trebleClef = factory.createClef();
        trebleClef.setSign(ClefSign.G);
        trebleClef.setLine(new BigInteger("2"));
        trebleClef.setNumber(new BigInteger("1")); // number attr in clef tag refers to staff
        return trebleClef;
    }

    private Clef buildBassClef() {
        Clef bassClef = factory.createClef();
        bassClef.setSign(ClefSign.F);
        bassClef.setLine(new BigInteger("4"));
        bassClef.setNumber(new BigInteger("2")); // staff 2
        return bassClef;
    }


    private String buildMeasureNumber(Measure pieceMeasure) {
        return String.valueOf(pieceMeasure.getMeasureNumber());
    }

    private Time buildTimeSignature(Measure pieceMeasure) {

        if (prevPieceMeasure != null
                && pieceMeasure.getTimeSignature().compareTo(prevPieceMeasure.getTimeSignature()) == 0) {
            return null;
        }

        Time time = factory.createTime();

        String numerator = String.valueOf(pieceMeasure.getTimeSignature().getNumerator());
        var timeBeats = factory.createTimeBeats(numerator);
        time.getTimeSignature().add(timeBeats);

        String denominator = String.valueOf(pieceMeasure.getTimeSignature().getDenominator());
        var timeBeatType = factory.createTimeBeatType(denominator);
        time.getTimeSignature().add(timeBeatType);

        return time;
    }

    private Key buildKey(Measure pieceMeasure) {

        // ProxyMusix (and most notation programs in general) don't care about mode. So we can't use compareTo() here.
        if (prevPieceMeasure != null
                && pieceMeasure.getKeySignature().getAccidentals()
                == prevPieceMeasure.getKeySignature().getAccidentals()) {
            return null;
        }

        Key key = factory.createKey();

        String accidentalsVal = String.valueOf(pieceMeasure.getKeySignature().getAccidentals());
        key.setFifths(new BigInteger(accidentalsVal));

        String modeVal = pieceMeasure.getKeySignature().getMode() == 0 ? "major" : "minor";
        key.setMode(modeVal);

        return key;
    }


}