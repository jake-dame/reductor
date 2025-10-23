package reductor.musicxml.dev;


import jakarta.xml.bind.JAXBElement;
import org.audiveris.proxymusic.*;
import org.audiveris.proxymusic.ScorePartwise.Part;
import org.audiveris.proxymusic.util.Marshalling;
import reductor.musicxml.exporter.MeasureBuilder;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import java.io.IOException;
import java.lang.String;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static reductor.musicxml.exporter.HeaderBuilder.buildScorePart;


// in the end, this could be renamed MusicXml, and after ScorePartwiseBuilder.build() is called,
// it could just make a call to musicxml.Exporter and output a path. Having this pop out a
// ScorePartwise is sometimes nice for development, but there is always the extra step of calling
// the Exporter, etc. Rarely do we want to just end up with a raw proxymusic.ScorePartwise object.
public class ScorePartwise {

    static ObjectFactory factory = new ObjectFactory();

    public static ScorePartwiseBuilder builder() {
        return new ScorePartwiseBuilder();
    }

    // creator, credit, and other things that allow arbitrary/custom properties often get just
    // thrown into and re-marshaled as MiscellaneousField by MuseScore, so instead of allowing other
    // credits, creators, etc., to add custom elements and values, you must just use the
    // Miscellaneous API.
    public static class ScorePartwiseBuilder {

        /*
        In this order
            In this order
                <work> (Optional)
                <movement-number> (Optional)
                <movement-title> (Optional)
                <identification> (Optional)
                <defaults> (Optional)
                <credit> (Zero or more times)
                <part-list> (Required)
            <part> (One or more times)
        */

        // credits would be here but musescore does not like them; it sources from creators
        // defaults would be here but we aren't going to output those
        private final WorkBuilder workBuilder = new WorkBuilder(this);
        private String movementNumber = "999";
        private String movementTitle = "default";
        private final IdentificationBuilder identificationBuilder = new IdentificationBuilder(this);
        private final PartList partList = factory.createPartList();
        private final List<Part> parts = new ArrayList<>();


        ScorePartwiseBuilder() { }


        public ScorePartwiseBuilder addPart(Part... parts) {
            for (Part p : parts) {
                if (p != null) { this.parts.add(p); }
            }
            return this;
        }

        private static final Part defaultPart;
        static {

            // THIS IS REUSED FROM THE OLD HeaderBuilder.java for now!!!
            org.audiveris.proxymusic.ScorePart scorePart = buildScorePart();

            defaultPart = factory.createScorePartwisePart();
            defaultPart.setId(scorePart);

            Part.Measure measure = factory.createScorePartwisePartMeasure();
            measure.setNumber("1");

                Attributes attributes = factory.createAttributes();
                attributes.setDivisions(new BigDecimal("480"));

                    Key key = factory.createKey();
                    key.setFifths(new BigInteger("2"));
                    key.setMode("major");
                    //key.setNumber(new BigInteger("2"));
                    attributes.getKey().add(key);

                    Time time = factory.createTime();
                    time.getTimeSignature().add(factory.createTimeBeats("3"));
                    time.getTimeSignature().add(factory.createTimeBeatType("4"));
                    //time.setNumber(new BigInteger("1"));
                    attributes.getTime().add(time);

                    attributes.getClef().add(MeasureBuilder.buildTrebleClef());
                    attributes.getClef().add(MeasureBuilder.buildBassClef());

                    attributes.setStaves(new BigInteger("2"));

                    measure.getNoteOrBackupOrForward().add(attributes);

            Direction direction = factory.createDirection();
                Sound sound = factory.createSound();
                sound.setTempo(new BigDecimal("120"));
                direction.setSound(sound);
                measure.getNoteOrBackupOrForward().add(direction);

            // F# quarter note
            Note note = factory.createNote();
                Pitch pitch = factory.createPitch();
                    pitch.setStep(Step.F);
                    pitch.setAlter(new BigDecimal("1"));
                    pitch.setOctave(5);
                        note.setPitch(pitch);
                NoteType noteType = factory.createNoteType();
                    noteType.setValue("quarter");
                note.setDuration(new BigDecimal("480"));
                note.setStaff(new BigInteger("1"));

            measure.getNoteOrBackupOrForward().add(note);

            defaultPart.getMeasure().add(measure);
        }
        private void populateParts(org.audiveris.proxymusic.ScorePartwise sp) {

            // Add Part's or default
            if (parts.isEmpty()) {
                this.parts.add(defaultPart);
            }
            sp.getPart().addAll(this.parts);

            /*
            Add ScorePart's

            This should always be coupled with add Part's to the ScorePartwise,
             and, due to allowing the user to NOT add a Part necessarily (helpful when JUST
             testing score header elements), it must be called in this order. */
            for (Part part : parts) {
                this.partList.getPartGroupOrScorePart().add(part.getId());
            }
            sp.setPartList(this.partList);
        }

        public WorkBuilder work() { return this.workBuilder; }
        public static class WorkBuilder {

            /*
            In this order
                <work-number> (Optional)
                <work-title> (Optional)
                <opus> (Optional)
            */

            private final ScorePartwiseBuilder parent;
            private String workNumber = "999";
            private String workTitle = "default";
            private Opus opus = factory.createOpus();

            WorkBuilder(ScorePartwiseBuilder parent) { this.parent = parent; }

            public WorkBuilder number(String v) {
                this.workNumber = v;
                return this;
            }

            public WorkBuilder title(String v) {
                this.workTitle = v;
                return this;
            }

            /* This is barebones because this program won't work with opera.
             but here for testing (any .musicxml with an opus should throw an exception). */
            public WorkBuilder opus(Opus v) {
                this.opus = v;
                return this;
            }

            public ScorePartwiseBuilder done() { return this.parent; }

            private Work build() {
                Work work = factory.createWork();
                work.setWorkNumber(this.workNumber);
                work.setWorkTitle(this.workTitle);
                work.setOpus(this.opus);
                return work;
            }

        }

        public ScorePartwiseBuilder movementNumber(String s) {
            this.movementNumber = s;
            return this;
        }

        public ScorePartwiseBuilder movementTitle(String s) {
            this.movementTitle = s;
            return this;
        }

        public IdentificationBuilder identification() { return this.identificationBuilder; }
        public static class IdentificationBuilder {

            /*
            In this order
                <creator> (Zero or more times)
                <rights> (Zero or more times)
                <encoding> (Optional)
                <source> (Optional)
                <relation> (Zero or more times)
                <miscellaneous> (Optional)
            */

            private final ScorePartwiseBuilder parent;
            private final List<TypedText> creators = new ArrayList<>();
            private final List<TypedText> rights = new ArrayList<>();
            private final EncodingBuilder encodingBuilder = new EncodingBuilder(this);
            private String source = "default";
            private final List<TypedText> relations = new ArrayList<>();
            private final List<MiscellaneousField> miscellaneous = new ArrayList<>();


            IdentificationBuilder(ScorePartwiseBuilder parent) { this.parent = parent; }


            /* How to: TypedText.setType(); TypedText.setValue() allowed types:
            https://www.w3.org/2021/06/musicxml40/musicxml-reference/data-types/xsd-token/

            "Distinguishes different creative contributions. Thus there can be multiple <creator>
            elements within an <identification> element. Standard values are composer, lyricist,
            and arranger. Other values may be used for different types of creative roles. This
            attribute should usually be used even if there is just a single <creator> element."
            */
            private void creator(String type, String value) {
                TypedText t = factory.createTypedText();
                t.setType(type);
                t.setValue(value);
                creators.add(t);
            }

            public IdentificationBuilder composer(String v) { this.creator("composer", v); return this; }
            public IdentificationBuilder lyricist(String v) { this.creator("lyricist", v); return this; }

            // MuseScore imports these as types of <creator>, NOT as <credit> types
            // rights are extracted from identification/rights
            public IdentificationBuilder translator(String v) { this.creator("translator", v); return this; }
            public IdentificationBuilder arranger(String v) { this.creator("arranger", v); return this; }
            public IdentificationBuilder subtitle(String v) { this.creator("subtitle", v); return this; }

            /* How to: TypedText.setType(); TypedText.setValue(); allowed types:
            https://www.w3.org/2021/06/musicxml40/musicxml-reference/data-types/xsd-token/.

            "Standard type values are music, words, and arrangement, but other types may be used.
            This attribute is only needed when there are multiple <rights> elements."
            */
            public IdentificationBuilder rights(String type, String value) {
                TypedText t = factory.createTypedText();
                t.setType(type);
                t.setValue(value);
                rights.add(t);
                return this;
            }

            /* ProxyMusic doesn't have nice methods to set this, because it handles all this
            without your input. So this Builder is a bit messier than the others. */
            public EncodingBuilder encoding() { return this.encodingBuilder; }
            public static class EncodingBuilder {

                /*
                Zero or more of the following [jake: order doesn't matter]

                    <encoding-date>
                    <encoder>
                    <software>
                    <encoding-description>
                    <supports>
                */

                private final IdentificationBuilder parent;
                private final JAXBElement<XMLGregorianCalendar> encodingDate = encodingDate();
                private final List<JAXBElement<TypedText>> encoders = new ArrayList<>();
                private final List<JAXBElement<String>> softwares = new ArrayList<>();
                private final List<JAXBElement<String>> encodingDescriptions = new ArrayList<>();
                private final List<JAXBElement<Supports>> supports = new ArrayList<>();


                EncodingBuilder(IdentificationBuilder parent) { this.parent = parent; }


                private JAXBElement<XMLGregorianCalendar> encodingDate() {

                    LocalDate date = LocalDate.now();


                    XMLGregorianCalendar xmlDate;
                    try {
                        xmlDate = DatatypeFactory.newInstance()
                                .newXMLGregorianCalendar(date.toString());
                    } catch (DatatypeConfigurationException e) {
                        throw new RuntimeException(e);
                    }

                    JAXBElement<XMLGregorianCalendar> encodingDate = new JAXBElement<>(
                            new QName("", "encoding-date"),
                            XMLGregorianCalendar.class,
                            Encoding.class,
                            xmlDate
                    );

                    return encodingDate;
                }

                /* How to: TypedText.setType(); TypedText.setValue(); allowed types:
                https://www.w3.org/2021/06/musicxml40/musicxml-reference/data-types/xsd-token/
                */
                public EncodingBuilder encoder(String type, String value) {
                    TypedText t = factory.createTypedText();
                    t.setType(type);
                    t.setValue(value);

                    JAXBElement<TypedText> j = new JAXBElement<>(
                            new QName("", "encoder"),
                            TypedText.class,
                            Encoding.class,
                            t
                    );

                    encoders.add(j);
                    return this;
                }

                public EncodingBuilder software(String v) {

                    JAXBElement<String> j = new JAXBElement<>(
                            new QName("", "software"),
                            String.class,
                            Encoding.class,
                            v
                    );

                    this.softwares.add(j);
                    return this;
                }

                public EncodingBuilder encodingDescription(String v) {

                    JAXBElement<String> j = new JAXBElement<>(
                            new QName("", "encoding-description"),
                            String.class,
                            Encoding.class,
                            v
                    );

                    this.encodingDescriptions.add(j);
                    return this;
                }

                /* This is barebones for now. May just copy over MuseScore defaults later.
                See: https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/supports/ */
                public EncodingBuilder supports(YesNo type, String element, String attribute,
                                                String value) {

                    Supports s = factory.createSupports();
                    s.setAttribute(attribute);
                    s.setElement(element);
                    s.setType(type);
                    s.setValue(value);

                    JAXBElement<Supports> j = new JAXBElement<>(
                            new QName("", "supports"),
                            Supports.class,
                            Encoding.class,
                            s
                    );

                    this.supports.add(j);
                    return this;
                }

                public IdentificationBuilder done() { return this.parent; }

                private Encoding build() {

                    Encoding e = factory.createEncoding();

                    var guy = e.getEncodingDateOrEncoderOrSoftware();

                    guy.add(this.encodingDate);

                    guy.addAll(this.encoders);

                    guy.addAll(this.softwares);

                    guy.addAll(this.encodingDescriptions);

                    guy.addAll(this.supports);

                    return e;
                }


            }

            public IdentificationBuilder source(String v) {
                this.source = v;
                return this;
            }

            /* How to: TypedText.setType(); TypedText.setValue(); allowed types:
            https://www.w3.org/2021/06/musicxml40/musicxml-reference/data-types/xsd-token/

            Standard type values are music, words, and arrangement, but other types may be used.
            */
            public IdentificationBuilder relation(String type, String value) {
                TypedText t = factory.createTypedText();
                t.setType(type);
                t.setValue(value);
                relations.add(t);
                return this;
            }

            /* How to: MiscellaneousField.setType() <-- __REQUIRED__;
            MiscellaneousField.setValue(); allowed types:
            https://www.w3.org/2021/06/musicxml40/musicxml-reference/data-types/xsd-token/ */
            public IdentificationBuilder miscellaneous(String name, String value) {
                MiscellaneousField m = factory.createMiscellaneousField();
                m.setName(name);
                m.setValue(value);
                miscellaneous.add(m);
                return this;
            }

            public ScorePartwiseBuilder done() { return this.parent; }
            private Identification build() {

                Identification i = factory.createIdentification();

                i.getCreator().addAll(this.creators);

                i.getRights().addAll(this.rights);

                Encoding e = this.encodingBuilder.build();
                i.setEncoding(e);

                i.setSource(this.source);

                i.getRelation().addAll(this.relations);

                // can have 0 or 1 <miscellaneous>,  which itself can have 0+ <miscellaneous-field>
                Miscellaneous m = factory.createMiscellaneous();
                i.setMiscellaneous(m);
                m.getMiscellaneousField().addAll(this.miscellaneous);

                return i;
            }

        }

        public org.audiveris.proxymusic.ScorePartwise build() {

            org.audiveris.proxymusic.ScorePartwise sp = factory.createScorePartwise();
            final String musicXmlAndScorePartwiseVersion = "4.0";
            sp.setVersion(musicXmlAndScorePartwiseVersion);

            Work work = this.workBuilder.build();
            sp.setWork(work);

            sp.setMovementNumber(this.movementNumber);
            sp.setMovementTitle(this.movementTitle);

            Identification identification = this.identificationBuilder.build();
            sp.setIdentification(identification);

            populateParts(sp);

            return sp;
        }


    } // ScorePartwiseBuilder


} // ScorePartwise


class Main {

    /*
    This is a typing test for my new keyboard, so that I can figure out if my screen works
    I think this angle will work and it feels like my neck is much more comfortable not pulling
    all my back muscles down. My laptop is going to die probably.
    */

    static void main() {

        // FOR DEMO PURPOSES ONLY -- EMPTY PARTS
        // Will later be provided by Part.builder(), once implemented
        ObjectFactory factory = new ObjectFactory();
        Part piano = factory.createScorePartwisePart();
        Part violin = factory.createScorePartwisePart();
        Part cello = factory.createScorePartwisePart();
        // FOR DEMO PURPOSES ONLY -- ^^^^^^^^^^^^^^^^^

        org.audiveris.proxymusic.ScorePartwise sp = ScorePartwise.builder()
                .work()
                    .number("D.911")
                    .title("Winterreise")
                    .done()
                .movementNumber("Der Wegweiser")
                .movementTitle("XX.")
                .identification()
                    .composer("Franz Schubert")
                    .lyricist("Wilhelm Müller")
                    .arranger("HELLO arranger")
                    .translator("HELLO translator")
                    .subtitle("HELLO subtitle")
                    .encoding()
                        .encoder("", "jd")
                        .software("reductor")
                        .encodingDescription("Using reductor 1.0")
                        .supports(YesNo.YES, "accidental", "", "")
                        .done()
                        .source("""
                                    This is just the first two lines, from the Breitkopf edition on IMSLP.
                                    This is a good test for all things score header, as it is from
                                    a multi-movement work, with a catalog number, lyricist, etc.
                                    """
                        )
                        .relation("", """
                                      https://vmirror.imslp.org/files/imglnks/usimg/8/8d/IMSLP60822-PMLP02203-Schubert_Werke_Breitkopf_Serie_XX_Band_9_F.S.878-904.pdf
                                      """
                        )
                    .rights("", "(c) 1827")
                    .miscellaneous("misc-type-one", "misc value 1")
                    .miscellaneous("misc-type-two", "misc value 2")
                    .done()
                //.addPart(piano)
                //.addPart(violin)
                //.addPart(cello)
                //.addParts(piano, violin, cello)
                .build();

        try {
            reductor.musicxml.writer.Writer.write(sp, "der-wegweiser-test-1");
        } catch (IOException | Marshalling.MarshallingException e) {
            throw new RuntimeException(e);
        }

    }

}
