package reductor.musicxml.builder;


import jakarta.xml.bind.JAXBElement;
import org.audiveris.proxymusic.Interchangeable;
import org.audiveris.proxymusic.Time;
import org.audiveris.proxymusic.TimeSeparator;
import org.audiveris.proxymusic.TimeSymbol;

import javax.xml.namespace.QName;
import java.util.regex.Pattern;

import static reductor.dev.Defaults.FACTORY;


/*
Exactly one of the following:
    1.) <beats> (1) + <beat-type> (1) (group: 1+) + <interchangeable> (0 or 1)
    2.) <senza-misura>
*/

public class TimeBuilder {


    private final Time time;
    private TimeBuilder() { this.time = FACTORY.createTime(); }
    public static TimeBuilder builder() { return new TimeBuilder(); }
    public Time build() { return this.time; }


    // Convenience method for standard time signatures.
    public TimeBuilder signature(int numerator, int denominator) {
        return this.signature(String.valueOf(denominator), String.valueOf(numerator));
    }

    // Dev: I have not decided yet whether or not to explicitly say only powers of 2 on the bottom
    static Pattern timeSignaturePattern = Pattern.compile(
        """
        (?x)
        ^
        [0-9]{1,2}
        (\\+[0-9]{1,2})*
        /
        [0-9]{1,2}
        $
        """
    );

    // Convenience method for string input like "3/4".
    public TimeBuilder signature(String v) {
        v = v.trim();
        if ( !timeSignaturePattern.matcher(v).matches() ) {
            throw new IllegalArgumentException("invalid time signature string: " + v);
        }
        String numerator = v.split("/")[0];
        String denominator = v.split("/")[1];
        this.signature(denominator, numerator);
        return this;
    }

    /* Use this one for non-standard time sigs, e.g.:
         - numerator of "2 1/4"
         - numerator of "2+3+2+2" (as String vararg, do not include '+'". */
    public TimeBuilder signature(String denominator, String... numerator) {

        /* For additive meters. String#join will not alter the sole String in an array
         of one. */
        String numeratorString = String.join("+", numerator);

        JAXBElement<String> beats = new JAXBElement<>(
                new QName("beats"), String.class, numeratorString
        );

        JAXBElement<String> beatType = new JAXBElement<>(
                new QName("beat-type"), String.class, denominator
        );

        this.time.getTimeSignature().add(beats);
        this.time.getTimeSignature().add(beatType);

        return this;
    }

    /*
    An <interchangeable> is basically identical to a <time> element, just stripped of two things:
        1. the ability to have a <senza-misura> sub-element
        2. all the position, color, alignment, etc. that a <time> has, which makes sense because
        interchangeable (i.e. dual) time signatures are assumed to be displayed next to each other.

    The "time-relation=" attribute is the only unique attribute to <interchangeable>. Values:
        - bracket, equals, hyphen, parentheses, slash, space

    And "time-symbol=" values:
        - common, cut, dotted-note, normal, note, single-number

    And "time-separator=" (poorly named, more so means "how to arrange the numerator and denominator
    with respect to each other). All include a bona fide "separator" (i.e. a line segment
    between) except for "adjacent", which just has a little space, and "none".:
        - adjacent, diagonal, horizontal, none, vertical

    */
    public TimeBuilder interchangeable(Interchangeable interchangeable) {
        this.time.setInterchangeable(interchangeable);
        return this;
    }

    public TimeBuilder separator(String v) {
        this.time.setSeparator(TimeSeparator.fromValue(v));
        return this;
    }

    public TimeBuilder symbol(String v) {
        this.time.setSymbol(TimeSymbol.fromValue(v));
        return this;
    }

    public TimeBuilder senzaMisura(String symbol) {
        this.time.setSenzaMisura(symbol);
        return this;
    }


}
