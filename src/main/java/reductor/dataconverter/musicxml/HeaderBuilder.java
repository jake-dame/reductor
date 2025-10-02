package reductor.dataconverter.musicxml;

import jakarta.xml.bind.JAXBElement;
import org.audiveris.proxymusic.*;

import javax.xml.namespace.QName;
import java.lang.String;
import java.math.BigDecimal;
import java.math.BigInteger;

// Everything related to score header elements gets created/assigned here (limited to part-list for now)
// This will essentially be the same for every file we output (they are all solo piano pieces)
public class HeaderBuilder {

    public static ScorePart build(ScorePartwise scorePartwise, String pieceName) {

        Work work = buildWork("Untitled Score");
        scorePartwise.setWork(work); // attach to parent

        Credit title = buildTitle(pieceName);
        scorePartwise.getCredit().add(title); // attach to parent

        Credit subtitle = buildSubtitle("subtitleTest");
        scorePartwise.getCredit().add(subtitle); // attach to parent

        Credit composer = buildComposer("composerTest");
        scorePartwise.getCredit().add(composer); // attach to parent

        PartList partList = buildPartList();
        scorePartwise.setPartList(partList); // attach to parent

        ScorePart scorePart = buildScorePart(partList);
        partList.getPartGroupOrScorePart().add(scorePart); // attach to parent (PartList)

        return scorePart;
    }

    private static Work buildWork(String name) {

        // EXAMPLE:
        /*
           <work>
             <work-title>Untitled score</work-title>
             </work>
         */

        Work work = ConversionToMusicXml.factory.createWork();

        work.setWorkTitle(name);

        return work;
    }

    private static Credit buildTitle(String title) {

        // EXAMPLE:
        /*
          <credit page="1">
            <credit-type>title</credit-type>
            <credit-words default-x="616.9347" default-y="1511.047129" justify="center" valign="top" font-size="22">Prelude in E Minor</credit-words>
            </credit>
        */

        Credit credit = ConversionToMusicXml.factory.createCredit();

        /*
         For some reason the proxymusic has these as JAXBElements in the annotated list in Credit, but there is no way to create
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

    private static Credit buildSubtitle(String subtitle) {

        // EXAMPLE:
        /*
          <credit page="1">
            <credit-type>subtitle</credit-type>
            <credit-words default-x="616.9347" default-y="1453.897088" justify="center" valign="top" font-size="14">Op. 28, No. 4</credit-words>
            </credit>
        */

        Credit credit = ConversionToMusicXml.factory.createCredit();

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

    private static Credit buildComposer(String composer) {

        // EXAMPLE:
        /*
          <credit page="1">
            <credit-type>composer</credit-type>
            <credit-words default-x="1148.144364" default-y="1411.047256" justify="right" valign="bottom">F. Chopin</credit-words>
            </credit>
        */

        Credit credit = ConversionToMusicXml.factory.createCredit();

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

    private static PartList buildPartList() {

        /*
            <part-list>
            <score-part id="P1">
              ... (see below)
              </score-part>
            </part-list>
        */

        PartList partList = ConversionToMusicXml.factory.createPartList();

        return partList;
    }

    private static ScorePart buildScorePart(PartList partList) {

        /*
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
         */

        ScorePart scorePart = ConversionToMusicXml.factory.createScorePart();
        scorePart.setId("P1");

        PartName partName = ConversionToMusicXml.factory.createPartName();
        partName.setValue("Piano");
        scorePart.setPartName(partName); // attach to parent (ScorePart)

        PartName partNameAbbrev = ConversionToMusicXml.factory.createPartName();
        partNameAbbrev.setValue("Pno.");
        scorePart.setPartAbbreviation(partNameAbbrev); // attach to parent (ScorePart)

        ScoreInstrument scoreInstrument = ConversionToMusicXml.factory.createScoreInstrument();
        scoreInstrument.setId("P1-I1");
        scoreInstrument.setInstrumentName("Piano");
        scoreInstrument.setInstrumentSound("keyboard.piano");
        scorePart.getScoreInstrument().add(scoreInstrument); // attach to parent (ScorePart)

        MidiDevice midiDevice = ConversionToMusicXml.factory.createMidiDevice();
        midiDevice.setId(scoreInstrument);
        midiDevice.setPort(1);
        scorePart.getMidiDeviceAndMidiInstrument().add(midiDevice); // attach to parent (ScorePart)

        MidiInstrument midiInstrument = ConversionToMusicXml.factory.createMidiInstrument();
        midiInstrument.setId(scoreInstrument);
        midiInstrument.setMidiChannel(1); // TODO hmmm
        midiInstrument.setMidiProgram(1); // TODO hmmm
        midiInstrument.setVolume(new BigDecimal("78.7402"));
        midiInstrument.setPan(new BigDecimal("0"));
        scorePart.getMidiDeviceAndMidiInstrument().add(midiInstrument); // attach to parent (ScorePart)

        return scorePart;
    }


}