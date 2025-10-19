package reductor.parsing.musicxml;


import jakarta.xml.bind.JAXBElement;
import org.audiveris.proxymusic.*;
import org.audiveris.proxymusic.ScorePartwise.Part;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reductor.app.Metadata;
import reductor.core.*;
import reductor.core.Note;
import reductor.core.Pitch;
import reductor.io.MusicXmlImporter;

import java.lang.String;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@SuppressWarnings({"ResultOfMethodCallIgnored", "unused"}) // dev only
public class MusicXmlContainer {


    private static final Logger log = LoggerFactory.getLogger(MusicXmlContainer.class);

    private final static Path schubert = Path.of(
            "assets/pieces/schubert-der-wegweiser/schubert-der-wegweiser.musicxml");
    private final static Path test1 = Path.of("assets/tests/test1/test1.musicxml");
    private final static Path test2 = Path.of("assets/tests/test2/test2.musicxml");

    final static Metadata metadata = new Metadata(false);

    static ArrayList<Measure> measures;


    static void main() {
        parse_scorePartwise(schubert);
    }


    // ======================================  ^^ DEV ^^  ======================================= //


    public static void parse_scorePartwise(Path musicXmlFilePath) {
        ScorePartwise sp = MusicXmlImporter.readInMusicXmlFile(musicXmlFilePath);
        parse_scorePartwise(sp);
    }

    private static void parse_scorePartwise(ScorePartwise scorePartwise) {

        /* SCORE HEADER */

        /* Most of this stuff will be retained wholesale for round trip, and just
        * re-added at the export stage. A few data elements might be parsed to provide
        * an output file name, for debugging info, or for validation. */

        parse_work(scorePartwise.getWork());

        // ignore if null (applies to both)
        String movementNumber = scorePartwise.getMovementNumber(); // 20
        String movementTitle = scorePartwise.getMovementTitle(); // Der Wegweiser

        parse_identification(scorePartwise.getIdentification());

        parse_credit(scorePartwise.getCredit());

        /* OTHER SCORE HEADER */

        String version = scorePartwise.getVersion(); // This refers to <score-partwise version="4.0">
        if (Float.parseFloat(version) != 4.0f) {
            log.info("found score-partwise that is not version 4.0");
        }
        metadata.setVersion(version);

        Defaults defaults = scorePartwise.getDefaults();
        if (defaults.getConcertScore() != null) {
            log.info("found concert-score");
        }

        /* PART LIST */

        parse_partlist(scorePartwise.getPartList());

        /* PARTS */

        List<Part> parts = scorePartwise.getPart();

        for (Part p : parts) {
            var idIsAScorePart = p.getId();
            parse_part(p);
        }

    }

    private static void parse_work(Work work) {

        // ignore if null or "Untitled score"
        String workTitle = work.getWorkTitle(); // Winterreise

        // ignore if null
        String workNumber = work.getWorkNumber(); // D.911

        if (work.getOpus() != null) {
            // This should never happen because MusicXmlImporter does not allow through
            // any Opus objects. But this is a jic for now.
            throw new RuntimeException("this program does not support musicxml opuses");
        }

        metadata.setWork(work);
    }

    private static void parse_identification(Identification identification) {
        var source = identification.getSource();
        var creator = identification.getCreator();
        parse_encoding(identification.getEncoding());
        var rights = identification.getRights(); // if not null log
        var misc = identification.getMiscellaneous(); // if not null log
        var relation = identification.getRelation(); // if not null log

        metadata.setIdentification(identification);
    }

    private static void parse_encoding(Encoding encoding) {

        List<JAXBElement<?>> elems = encoding.getEncodingDateOrEncoderOrSoftware();

        for (var e : elems) {
            switch (e.getName().getLocalPart()) {
                case "software" -> System.out.println(); // TODO: add to metadata
                case "encoding-date" -> System.out.println(); // TODO: add to metadata
                case "supports" -> { assert true; } // no-op
                default -> throw new RuntimeException(
                        "found encoding element that is not software, date, or supports");
            }
        }

    }

    private static void parse_credit(List<Credit> credits) {

        // 1 Credit has 2 JAXBElements which each have: type (e.g.title), value (e.g.Easter)
        Map<String, String> renderableMetadataMap = new HashMap<>();

        for (Credit c : credits) {
            List<JAXBElement<?>> JAXBList = c.getCreditTypeOrLinkOrBookmark();

            var JAXB1 = JAXBList.get(0);
            var name1 = JAXB1.getName(); // e.g. credit-type
            var value1 = JAXB1.getValue().toString(); // title

            var JAXB2 = JAXBList.get(1);
            var name2 = JAXB2.getName(); // e.g. credit-words
            var value2 = ((FormattedText) JAXB2.getValue()).getValue(); // Easter

            renderableMetadataMap.put(value1, value2);
        }

        metadata.setCredits(credits);
    }



    // == = = = = ================================================ = = = =  = = = = =  = = = = =  //



    private static void parse_partlist(PartList partList) {

        List<Object> scoreParts = partList.getPartGroupOrScorePart();

        for (Object o : scoreParts) {

            if (o instanceof ScorePart s) {
                parse_scorepart(s);
            } else {
                // the method getPartGroupOrScorePart cannot return a list of anything other than
                // ScorePart or PartGroup objects
                throw new RuntimeException("found PartGroup");
            }

        }

    }

    private static void parse_scorepart(ScorePart sp) {
        var id = sp.getId();

        // the rest of ScorePart.partName is x,y,color,etc.
        String partName = sp.getPartName().getValue();

        // the rest of ScorePart.partNameAbbreviation is x,y,color,etc.
        String partNameAbbrev = sp.getPartAbbreviation().getValue();

        // if size != 0 log
        var partLink = sp.getPartLink();
        // if != null log
        var partNameDisplay = sp.getPartNameDisplay();
        // if != null log
        var partAbbreviationDisplay = sp.getPartAbbreviationDisplay();
        // if size != 0 log
        var group = sp.getGroup();
        // if size != 0 log
        var player = sp.getPlayer();

        // We are basically going to always output piano-specific stuff for SI and MD, so retention
        //     is probably pointless. Only case would be: if part name is missing, but for some
        //     reason ScoreInstrument has a name, might be heuristic for instrument/part.

        // ------------------

        List<ScoreInstrument> scoreInstruments = sp.getScoreInstrument();
        for (ScoreInstrument si :scoreInstruments) {
            String instrumentName = si.getInstrumentName();
            String instrumentSound = si.getInstrumentSound();
            var instrumentAbbrev = si.getInstrumentAbbreviation(); // if not null log
            var instrumentSolo = si.getSolo(); // if not null log
            var instrumentEnsemble = si.getEnsemble(); // if not null log
            var instrumentVirtualInstrument = si.getVirtualInstrument(); // if not null log
        }

        // ------------------

        List<Object> midiDevicesAndMidiInstruments = sp.getMidiDeviceAndMidiInstrument();
        for (Object midi : midiDevicesAndMidiInstruments) {
            if (midi instanceof MidiDevice md) {
                md.getId(); // this is a ScoreInstrument
                md.getPort(); // this was 1 for test2
                md.getValue(); // if not "" log
            } else if (midi instanceof MidiInstrument mi) {
                mi.getId();
                mi.getElevation();
                mi.getMidiBank();
                mi.getMidiChannel();
                mi.getMidiName();
                mi.getMidiProgram(); // this was 41 for test2
                mi.getMidiUnpitched();
                mi.getPan();
                mi.getVolume();
            }
        }

    }


    // =========================================  PART  ========================================= //


    private static void parse_part(Part p) {
        // this is the only possible child element of Part
        for (Part.Measure m : p.getMeasure()) {
            Measure pieceMeasure = parse_XmlMeasure(m);
            measures.add(pieceMeasure);
        }
    }

    /*
    We are going to need to keep track of:
        1. Time Signature (measure/attributes/
    */
    private static Measure parse_XmlMeasure(Part.Measure xmlMeasure) {

        Measure measure = new Measure();

        int number = Integer.parseInt(xmlMeasure.getNumber());

        /* This implies Note, Backup, and Forward are the only Objects this can return,
         but it really can house any object that is a direct child of <measure>. */
        var children = xmlMeasure.getNoteOrBackupOrForward();
        for (Object o : children) {
            switch (o) {
                case Attributes a -> parse_attributes(a);
                case org.audiveris.proxymusic.Note n -> parse_XmlNote(n);
                case org.audiveris.proxymusic.Backup b -> parse_XmlNote(b);
                case org.audiveris.proxymusic.Forward f -> parse_XmlNote(f);
                case Barline barline -> log.debug("measure/barline: {}", barline);
                case Print print -> log.debug("measure/print: {}", print);
                case Direction direction -> log.debug("measure/direction: {}", direction);
                case Sound sound -> log.debug("measure/sound: {}", sound);
                case FiguredBass figuredBass -> log.debug("measure/figuredBass: {}", figuredBass);
                case Harmony harmony -> log.debug("measure/harmony: {}", harmony);
                case Grouping grouping -> log.debug("measure/grouping: {}", grouping);
                case Link link -> log.debug("measure/link: {}", link);
                case Bookmark bookmark -> log.debug("measure/bookmark: {}", bookmark);
                default -> throw new RuntimeException("unknown Part.Measure element");
            }
        }

        /* JUNK STUFF */
        var id = xmlMeasure.getId();

        // since we are taking multiple parts and combining it into a piano reduction,
        // we don't care if measure X in part A does not coincide with the meter in measure X in
        // part B. That is ingest-side-only.
        if (xmlMeasure.getNonControlling() == YesNo.YES) {
            log.debug("found non-controlling measure");
        }

        // this is a rendering software decision. Can preserve. Says "don't display measure
        // number on pickups and partials at repeats.
        if (xmlMeasure.getImplicit() == YesNo.YES) {
            log.debug("found implicit measure");
        }

        // this is a rendering decision
        var width = xmlMeasure.getWidth();

        // this has to do with timewise vs. partwise conversions? no idea, probably don't care
        String text = xmlMeasure.getText();
        if (!text.isEmpty()) {
            log.debug("found non-empty measure-text");
        }

        return null;
    }

    private static void parse_attributes(Attributes attributes) {
        attributes.getClef();
        attributes.getDirective();
        attributes.getDivisions();
        attributes.getForPart();
        attributes.getInstruments();
        attributes.getKey();
        attributes.getLevel();
        attributes.getMeasureStyle(); // % sign and |---4---| sign
        attributes.getStaffDetails(); // smaller staff above accompaniment in a piano-violin score
        attributes.getStaves();
        attributes.getTranspose(); // sounding vs. written in, say, Eb trumpet

        List<Time> times = attributes.getTime();
        if (times.size() != 1) {
            throw new RuntimeException("found multiple Time's in a measure/attributes");
        }
        attributes.getTime().getFirst().getTimeSignature().getFirst().getValue();
        attributes.getTime().getFirst().getTimeSignature().getFirst().getValue();

        /* JUNK STUFF */
        attributes.getFootnote();
        attributes.getPartSymbol(); // curly brace on grand staff
    }


    // https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/note/
    private static void parse_XmlNote(Object o) {

        // TODO: add
        //n.getAccidental(); // notation only, i.e. non-sounding
        //n.getDot();
        //n.getDuration();
        //n.getFootnote();
        //n.getGrace();
        //n.getNotations();
        //n.getRest();
        //n.getStaff();
        //n.getTie();
        //n.getTimeModification();
        //n.getVoice();

        org.audiveris.proxymusic.Note n;
        if (o instanceof org.audiveris.proxymusic.Note) {
            n = (org.audiveris.proxymusic.Note) o;
        } else {
            throw new RuntimeException("found backup or forward");
        }

        List<Instrument> instruments = n.getInstrument();
        String instrument = "";
        if (1 < instruments.size()) {
            throw new RuntimeException("found multi-instrument note");
        } else {
            instrument = instruments.getFirst().toString();
        }

        if (n.getChord() != null) {
            Chord.builder(); // TODO: adapt for use with MusicXML paradigm
        }

        BigDecimal durationCount = new BigDecimal("0");

        long start = durationCount.longValueExact(); // throws if fractional or overflow
        long stop = n.getDuration().longValueExact() + durationCount.longValueExact();

        Note.builder()
                .pitch(parse_pitch(n.getPitch()))
                .instrument(instrument)
                .start(start)
                .stop(stop)
                .build();

        /* JUNK STUFF */
        n.getAttack(); // no idea; opposite is getRelease
        n.getBeam();
        n.getColor();
        n.getCue(); // context notes for another part
        n.getDefaultX();
        n.getDefaultY();
        n.getDynamics();
        n.getEndDynamics();
        n.getFontFamily();
        n.getFontSize();
        n.getFontStyle();
        n.getFontWeight();
        n.getId();
        // has to do with editorial info, like a BRACKETED natural sign above a trill.
        // Un-bracketed seems to just be <accidental-mark>
        n.getLevel();
        n.getListen(); // still no idea what this is but seems n/a
        n.getLyric();
        n.getNotehead(); // like diamond, "x", for percussion, et al.
        n.getNoteheadText(); // note names inside a notehead, like in faber and bastian books
        n.getPizzicato(); // marking individual notes as SOUNDING pizzicato
        n.getPlay(); // specific playback instructions (con sordino, etc.)
        n.getPrintDot();
        n.getPrintLeger();
        n.getPrintLyric();
        n.getPrintObject();
        n.getPrintSpacing();
        n.getRelativeX();
        n.getRelativeY();
        n.getRelease(); // no idea; opposite of getAttack
        n.getStem();
        n.getTimeOnly(); // related to playback
        n.getType(); // note displayed (e.g. large, grace); if not specified, just "regular"
        n.getUnpitched(); // notated but lack definite pitch, like on percussion

    }

    private static Pitch parse_pitch(org.audiveris.proxymusic.Pitch xmlPitch) {

        Step letter = xmlPitch.getStep();
        // Microtonal alters are passed to reductor.core.Pitch for validation -- not handled here
        int alter = xmlPitch.getAlter().intValueExact();
        int register = xmlPitch.getOctave();

        // inline/implicit toString() for something like: "A", "-1", "3"
        String pitchStr = letter + PitchUtil.accidentalsItoS.get(alter) + register;

        return new Pitch(pitchStr);
    }


}
