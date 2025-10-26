package reductor.musicxml.exporter;


import jakarta.xml.bind.JAXBElement;
import org.audiveris.proxymusic.*;
import org.audiveris.proxymusic.ScorePartwise.Part.Measure;
import reductor.core.KeySignature;
import reductor.core.Range;

import javax.xml.namespace.QName;
import java.lang.String;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import static reductor.musicxml.exporter.Defaults.*;


    /*
    We will be concerned with only a few of the below:
       - <note>, <backup>, and <forward>
       - <direction>
       - <attributes>
       - <barline> (perhaps -- stub for now)
    */
    /*
    <measure> and/or <attributes> seems to be where all slash notation, repeats, rest bars, etc. is
    */
    /*
    """
    Zero or more of the following
        <note>
        <backup>
        <forward>
        <direction>
        <attributes>
        <harmony>
        <figured-bass>
        <print>
        <sound>
        <listening>
        <barline>
        <grouping>
        <link>
        <bookmark>
    """
    */


public class MeasureBuilder {

    public static Builder builder() { return new Builder(); }

    public static class Builder {

        private AttributesBuilder attributesBuilder; // this is technically 0 or more, but for now 1
        private final List<DirectionBuilder> directionBuilders = new ArrayList<>();
        private final List<Barline> barlines = new ArrayList<>();
        private YesNo nonControlling = YesNo.NO;

        Builder() { }


        public AttributesBuilder attributes() {
            this.attributesBuilder = new AttributesBuilder(this);
            return this.attributesBuilder;
        }
        public static class AttributesBuilder {

            /*
            In this order:
                <footnote> AND/OR <level> (both optional, in that order)
                <divisions> (Optional)
                <key> (Zero or more times)
                <time> (Zero or more times)
                <staves> (Optional)
                <part-symbol> (Optional)
                <instruments> (Optional)
                <clef> (Zero or more times)
                <staff-details> (Zero or more times)
                <transpose> OR <for-part> (both 0+, in that order)
                <directive> (Zero or more times) (deprecated)
                <measure-style> (Zero or more times)
            */

            private final Builder parent;

            private BigDecimal divisions;
            private final List<Key> keys = new ArrayList<>();
            private final List<TimeBuilder> timeBuilders = new ArrayList<>();
            private BigInteger staves;
            private final List<Clef> clefs = new ArrayList<>();

            AttributesBuilder(Builder parent) { this.parent = parent; }

            public TimeBuilder time() {
                TimeBuilder tb = new TimeBuilder(this);
                this.timeBuilders.add(tb);
                return tb;
            }
            public static class TimeBuilder {

                /*
                Exactly one of the following:
                    1.)
                        i.) <beats> (Required, <beat-type> (Required) (this pairing can occur 1+ times)
                        ii.) <interchangeable> (0 or 1)
                    2.) <senza-misura>
                */

                private final AttributesBuilder parent;

                private final List<JAXBElement<String>> beatsAndBeatTypes = new ArrayList<>();
                private Interchangeable interchangeable;
                private TimeSeparator separator;
                private TimeSymbol symbol;

                TimeBuilder(AttributesBuilder parent) { this.parent = parent; }

                /* Use this one for standard time sigs. */
                public TimeBuilder signature(int numerator, int denominator) {
                    return this.signature(String.valueOf(denominator), String.valueOf(numerator));
                }

                /* Use this one for non-standard time sigs, e.g.:
                     - numerator of "2 1/4"
                     - numerator of "2+3+2+2" (as String vararg, do not include '+'". */
                public TimeBuilder signature(String denominator, String... numerator) {

                    /* For additive meters. String#join will not alter the sole String in an array
                     of one. */
                    String numeratorString = String.join("+", numerator);

                    JAXBElement<String> beats = new JAXBElement<>(
                            new QName("beats"),
                            String.class,
                            numeratorString
                    );

                    JAXBElement<String> beatType = new JAXBElement<>(
                            new QName("beat-type"),
                            String.class,
                            denominator
                    );

                    this.beatsAndBeatTypes.add(beats);
                    this.beatsAndBeatTypes.add(beatType);

                    return this;
                }

                public TimeBuilder interchangeable() {
                    Interchangeable i = FACTORY.createInterchangeable();
                    return this;
                }

                /*
                https://www.w3.org/2021/06/musicxml40/musicxml-reference/data-types/time-separator/
                */
                public TimeBuilder separator(String v) {
                    this.separator = TimeSeparator.fromValue(v);
                    return this;
                }

                /*
                 https://www.w3.org/2021/06/musicxml40/musicxml-reference/data-types/time-symbol/
                 */
                public TimeBuilder symbol(String v) {
                    TimeSymbol s = TimeSymbol.fromValue(v);
                    this.symbol = s;
                    return this;
                }

                //public TimeBuilder senzaMisura() {
                //    this.senzaMisura("");
                //    return this;
                //}
                //
                //public TimeBuilder senzaMisura(String symbol) {
                //    Time time = FACTORY.createTime();
                //    time.getSenzaMisura();
                //    time.setSenzaMisura(symbol);
                //    this.sen
                //    return this;
                //}

                public AttributesBuilder done() { return this.parent; }
                private Time build() {
                    Time time = FACTORY.createTime();
                    time.getTimeSignature().addAll(this.beatsAndBeatTypes);
                    time.setInterchangeable(this.interchangeable);
                    time.setSeparator(this.separator);
                    time.setSymbol(this.symbol);
                    return time;
                }
            } // TimeBuilder =======================================================================

            public AttributesBuilder divisions(int divisions) {
                this.divisions = BigDecimal.valueOf(divisions);
                return this;
            }

            public AttributesBuilder key(String v) { return this.key(v, null); }

            /* A convenience function that uses reductor.core package stuff. */
            public AttributesBuilder key(String v, Integer staff) {
                reductor.core.KeySignature e = new KeySignature(v, new Range());
                int fifths = e.accidentals();
                String modeString = e.mode() == 0 ? "major" : "minor";
                return this.key(e.accidentals(), modeString, staff);
            }

            public AttributesBuilder key(int fifths, String mode, Integer staff) {
                Key key = FACTORY.createKey();
                key.setFifths(BigInteger.valueOf(fifths));
                key.setMode(mode);
                if (staff != null) { key.setNumber(BigInteger.valueOf(staff)); }
                this.keys.add(key);
                return this;
            }

            public AttributesBuilder staves(int numberOfStaves) {
                this.staves = BigInteger.valueOf(numberOfStaves);
                return this;
            }

            public AttributesBuilder clefTreble(int staff) { return clef(DEF_TREBLE_CLEF, staff); }
            public AttributesBuilder clefBass(int staff) { return clef(DEF_BASS_CLEF, staff); }
            public AttributesBuilder clefAlto(int staff) { return clef(DEF_ALTO_CLEF, staff); }
            public AttributesBuilder clefTenor(int staff) { return clef(DEF_TENOR_CLEF, staff); }

            public AttributesBuilder clef(Clef clef, int staff) {
                clef.setNumber(BigInteger.valueOf(staff));
                this.clefs.add(clef);
                return this;
            }

            /* https://www.w3.org/2021/06/musicxml40/musicxml-reference/data-types/clef-sign/ */
            public AttributesBuilder clef(String letterNameOfClef, BigInteger line,
                                          BigInteger octave, BigInteger staff) {
                Clef clef = FACTORY.createClef();
                clef.setSign(ClefSign.fromValue(letterNameOfClef));
                clef.setLine(line);
                clef.setClefOctaveChange(octave);
                this.clefs.add(clef);
                return this;
            }

            public Builder done() { return this.parent; }
            private Attributes build() {
                Attributes attributes = FACTORY.createAttributes();
                attributes.setDivisions(divisions);
                attributes.getKey().addAll(keys);
                attributes.getTime().addAll(
                        this.timeBuilders.stream()
                                .map(TimeBuilder::build)
                                .toList()
                );
                attributes.setStaves(staves);
                attributes.getClef().addAll(clefs);
                return attributes;
            }
        } // AttributesBuilder =====================================================================

        public DirectionBuilder direction() {
            var db = new DirectionBuilder(this);
            this.directionBuilders.add(db);
            return db;
        }
        public static class DirectionBuilder {

                        /*
             Look into this later, has a lot of notations we may want to preserve.
             Where all measure (as opposed to note-specific) text, performance directions, etc.
             <direction-type> has an enormous range of options.
             Look into "metronome" and see if you can set to staff/part, and if musescore will
             Otherwise, all we really care about for now is the <direction>/<sound>
             Additionally, <direction-type> is how you set words? According to MuseScore.

             These snippets are extracted from a 1-measure musescore doc where THREE tempos were
             added to that one and the same measure:
             ```xml
                 <!-- Clicked and dragged "Lento" from the palette -->
                 <direction placement="above" system="only-top">
                    <direction-type>
                        <words default-x="-37.68" relative-y="20" font-weight="bold" font-size="12">Lento</words>
                    </direction-type>
                    <sound tempo="52.5"/>
                </direction>

                <!-- Clicked and dragged "<8th note symbol> = 80" -->
                <direction placement="above" system="only-top">
                    <direction-type>
                        <metronome parentheses="no" default-x="-37.68" default-y="47.84" relative-y="20">
                            <beat-unit>eighth</beat-unit>
                            <per-minute>80</per-minute>
                        </metronome>
                    </direction-type>
                    <sound tempo="40"/>
                </direction>

                <!--
                Clicked and dragged "<8th note symbol> = 80" __AND THEN__ typed Lento in front of
                it inside that same text box
                -->
                <direction placement="above" system="only-top">
                    <direction-type>
                        <words default-x="-37.68" default-y="22.94" relative-y="20" font-weight="bold" font-size="12">Lento </words>
                    </direction-type>
                    <direction-type>
                        <metronome parentheses="no" default-x="-37.68" default-y="22.94" relative-y="20">
                            <beat-unit>eighth</beat-unit>
                            <per-minute>80</per-minute>
                        </metronome>
                    </direction-type>
                    <sound tempo="40"/>
                </direction>
             ```

            */

            private final Builder parent;

            private final List<DirectionType> directionTypes = new ArrayList<>(); // 1+
            private Sound sound; // 0 or 1

            DirectionBuilder(Builder parent) { this.parent = parent; }

            public DirectionBuilder tempoWords(String v) {
                FormattedText words = FACTORY.createFormattedText(); // this is a guess; no Words obj
                words.setValue(v);
                this.directionType(words);
                return this.directionType(words);
            }

            /*
             https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/metronome/

             This can get more much more complex and would need to be a builder. For now, to
             avoid that, we are keeping it simple.

             Calling info:
                 - noteType == <beat-unit>
                 - perMinute can include words and text, and seems to just be a text literal.
                 I don't know how MuseScore parses this, but best to stick to int strings like
                 "80", "124", etc.
            */
            public DirectionBuilder tempoMetronome(String noteType, String perMinute) {

                PerMinute pm = FACTORY.createPerMinute();
                pm.setValue(perMinute);

                Metronome metronome = FACTORY.createMetronome();
                metronome.setBeatUnit(noteType);
                metronome.setPerMinute(pm);

                return this.directionType(metronome);
            }

            public DirectionBuilder directionType(Object o) {

                DirectionType dt = FACTORY.createDirectionType();

                switch (o) {
                    case Metronome m -> dt.setMetronome(m);
                    case FormattedText words -> dt.getWordsOrSymbol().add(words);
                    default -> throw new RuntimeException("no support except <words and metronome");
                }

                this.directionTypes.add(dt);
                return this;
            }

            /*
            https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/sound/
            */
            public DirectionBuilder playbackSpeed(int bpm) {
                Sound sound = FACTORY.createSound();
                sound.setTempo(BigDecimal.valueOf(bpm));
                this.sound = sound;
                return this;
            }

            public Builder done() { return this.parent; }
            private Direction build() {
                Direction direction = FACTORY.createDirection();
                direction.getDirectionType().addAll(directionTypes);
                direction.setSound(this.sound);
                return direction;
            }
        } // DirectionBuilder ======================================================================

        public Builder barline(List<Barline> barlines) {
            this.barlines.addAll(barlines);
            return this;
        }

        public Builder nonControlling() {
            this.nonControlling = YesNo.YES;
            return this;
        }

        public Measure build() {

            Measure m = FACTORY.createScorePartwisePartMeasure();

            // Attributes
            m.getNoteOrBackupOrForward().add(this.attributesBuilder.build());

            // Direction(s)
            m.getNoteOrBackupOrForward().addAll(
                    this.directionBuilders.stream()
                            .map(DirectionBuilder::build)
                            .toList()
            );

            // Attribute: non-controlling
            m.setNonControlling(this.nonControlling);

            return m;
        }
    } // Builder


}
