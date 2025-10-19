package reductor.core.musicxml;


import jakarta.xml.bind.JAXBElement;
import org.audiveris.proxymusic.*;
import reductor.app.Application;

import javax.xml.namespace.QName;
import java.lang.String;
import java.math.BigDecimal;
import java.math.BigInteger;


@SuppressWarnings("SameParameterValue") // fine using xPlaceholder for now
class HeaderBuilder {


    private HeaderBuilder() {}


    static ScorePart build(ScorePartwise scorePartwise) {

        Work work = buildWork("workPlaceholder"); // jake: don't inline any of these
        scorePartwise.setWork(work);

        Credit title = buildTitle("titlePlaceholder");
        scorePartwise.getCredit().add(title);

        Credit subtitle = buildSubtitle("subtitlePlaceholder");
        scorePartwise.getCredit().add(subtitle);

        Credit composer = buildComposer("composerPlaceholder");
        scorePartwise.getCredit().add(composer);

        PartList partList = buildPartList();
        scorePartwise.setPartList(partList);

        ScorePart scorePart = buildScorePart(

        );
        partList.getPartGroupOrScorePart().add(scorePart);

        return scorePart;
    }

    /**
     * Work is file metadata, and is not rendered.
     * <p>
     * Example:
     * <pre>
     * {@code
     * <work>
     *     <work-title>Mass in B minor</work-title>
     * </work>
     * }
     * </pre>
     */
    private static Work buildWork(String name) {
        Work work = ScorePartwiseBuilder.factory.createWork();
        work.setWorkTitle(name);
        return work;
    }

    /**
     * Title is rendered in large font at the top of the first page.
     * <p>
     * Example:
     * <pre>
     * {@code
     * <credit page="1">
     *     <credit-type>title</credit-type>
     *     <credit-words default-x="616.9347" default-y="1511.047129" justify="center" valign="top" font-size="22">Et Incarnatus Est</credit-words>
     * </credit>
     * }
     * </pre>
     */
    private static Credit buildTitle(String title) {

        Credit credit = ScorePartwiseBuilder.factory.createCredit();

        /*
         For some reason the proxymusic has these as JAXBElements in the annotated list in Credit, but there is no
         way to create
         them as proxymusic objects. All you can do is access the Arraylist (Credit#getCreditTypeOrLinkOrBookmark).
         So these have to to be created manually, following the MusicXML spec and MuseScore's use of them.
         */
        JAXBElement<String> creditType = new JAXBElement<>(
                new QName("credit-type"),   // the element type
                String.class,               // what data type the body of the element has
                "title"                     // the actual value in the body
        );

        JAXBElement<String> creditWords = new JAXBElement<>(
                new QName("credit-words"),
                String.class,
                title
        );

        credit.setPage(new BigInteger("1"));

        credit.getCreditTypeOrLinkOrBookmark().add(creditType);  // attach to parent (Credit)
        credit.getCreditTypeOrLinkOrBookmark().add(creditWords); // "    "

        return credit;
    }

    /**
     * Subtitle is rendered in slightly smaller font below the Title.
     * <p>
     * Example:
     * <pre>
     * {@code
     * <credit page="1">
     *     <credit-type>subtitle</credit-type>
     *     <credit-words default-x="616.9347" default-y="1453.897088" justify="center" valign="top" font-size="14">"And was incarnate"</credit-words>
     * </credit>
     * }
     * </pre>
     */
    private static Credit buildSubtitle(String subtitle) {

        Credit credit = ScorePartwiseBuilder.factory.createCredit();

        JAXBElement<String> creditType = new JAXBElement<>(
                new QName("credit-type"),
                String.class,
                "subtitle"
        );

        JAXBElement<String> creditWords = new JAXBElement<>(
                new QName("credit-words"),
                String.class,
                subtitle
        );

        credit.setPage(new BigInteger("1"));

        credit.getCreditTypeOrLinkOrBookmark().add(creditType); // attach to parent (Credit)
        credit.getCreditTypeOrLinkOrBookmark().add(creditWords); // attach to parent (Credit)

        return credit;
    }

    /**
     *
     * <p>
     * Example:
     * <pre>
     * {@code
     * <credit page="1">
     *     <credit-type>composer</credit-type>
     *     <credit-words default-x="1148.144364" default-y="1411.047256" justify="right" valign="bottom">J.S. Bach (1685-1750)</credit-words>
     * </credit>
     * }
     * </pre>
     */
    private static Credit buildComposer(String composer) {

        Credit credit = ScorePartwiseBuilder.factory.createCredit();

        JAXBElement<String> creditType = new JAXBElement<>(
                new QName("credit-type"),
                String.class,
                "composer"
        );

        JAXBElement<String> creditWords = new JAXBElement<>(
                new QName("credit-words"),
                String.class,
                composer
        );

        credit.setPage(new BigInteger("1"));

        credit.getCreditTypeOrLinkOrBookmark().add(creditType); // attach to parent (Credit)
        credit.getCreditTypeOrLinkOrBookmark().add(creditWords); // attach to parent (Credit)

        return credit;
    }

    /**
     *
     * <p>
     * Example:
     * <pre>
     * {@code
     * <part-list>
     *     <score-part id="P1">
     *         ... (see #buildScorePart)
     *     </score-part>
     * </part-list>
     * }
     * </pre>
     */
    private static PartList buildPartList() {
        return ScorePartwiseBuilder.factory.createPartList();
    }

    /**
     *
     * <p>
     * Example:
     * <pre>
     * {@code
     *            <score-part id="P1">
     *
     *               <part-name>Piano</part-name>
     *
     *               <part-abbreviation>Pno.</part-abbreviation>
     *
     *               <score-instrument id="P1-I1">
     *                 <instrument-name>Piano</instrument-name>
     *                 <instrument-sound>keyboard.piano</instrument-sound>
     *               </score-instrument>
     *
     *               <midi-device id="P1-I1" port="1"></midi-device>
     *
     *               <midi-instrument id="P1-I1">
     *                 <midi-channel>1</midi-channel>
     *                 <midi-program>1</midi-program>
     *                 <volume>78.7402</volume>
     *                 <pan>0</pan>
     *               </midi-instrument>
     *
     *             </score-part>
     * }
     * </pre>
     */
    private static ScorePart buildScorePart() {

        final String scorePartId = "P1";
        final String partNameContent = "Piano";
        final String partAbbrevContent = "Pno.";
        final String scoreInstrumentId = "P1-I1";
        final String instrumentNameContent = "Piano";
        final String instrumentSoundContent = "keyboard.piano";
        final int midiDevicePort = 1;
        final int midiChannel = 1;
        final int midiPort = 1;
        // TODO: see notes about volume and pan, below

        ScorePart scorePart = ScorePartwiseBuilder.factory.createScorePart();

        scorePart.setId(scorePartId);

        PartName partName = ScorePartwiseBuilder.factory.createPartName();
        partName.setValue(partNameContent);
        scorePart.setPartName(partName); // attach to parent (ScorePart)

        PartName partNameAbbrev = ScorePartwiseBuilder.factory.createPartName();
        partNameAbbrev.setValue(partAbbrevContent);
        scorePart.setPartAbbreviation(partNameAbbrev); // attach to parent (ScorePart)

        ScoreInstrument scoreInstrument = ScorePartwiseBuilder.factory.createScoreInstrument();
        scoreInstrument.setId(scoreInstrumentId);

        scoreInstrument.setInstrumentName(instrumentNameContent);
        scoreInstrument.setInstrumentSound(instrumentSoundContent);
        scorePart.getScoreInstrument().add(scoreInstrument); // attach to parent (ScorePart)

        MidiDevice midiDevice = ScorePartwiseBuilder.factory.createMidiDevice();
        midiDevice.setId(scoreInstrument);
        midiDevice.setPort(midiDevicePort);
        scorePart.getMidiDeviceAndMidiInstrument().add(midiDevice); // attach to parent (ScorePart)

        final BigDecimal museScoreVolumeDefault = new BigDecimal("78.7402");
        final BigDecimal museScorePanDefault = new BigDecimal("0");
        MidiInstrument midiInstrument = ScorePartwiseBuilder.factory.createMidiInstrument();
        midiInstrument.setId(scoreInstrument);
        midiInstrument.setMidiChannel(midiChannel);
        midiInstrument.setMidiProgram(midiPort);
        midiInstrument.setVolume(museScoreVolumeDefault);
        midiInstrument.setPan(museScorePanDefault);
        // Those todos ^^^ is what MuseScore seems to output by default from what I've seen so far
        scorePart.getMidiDeviceAndMidiInstrument().add(midiInstrument); // attach to parent (ScorePart)

        return scorePart;
    }


}
