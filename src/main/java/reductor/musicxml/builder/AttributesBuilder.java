package reductor.musicxml.builder;


import org.audiveris.proxymusic.*;
import reductor.core.KeySignature;
import reductor.core.Range;
import reductor.dev.Defaults;

import java.lang.String;
import java.math.BigDecimal;
import java.math.BigInteger;

import static reductor.dev.Defaults.*;


public class AttributesBuilder {


    private final Attributes attributes;
    private AttributesBuilder() { this.attributes = FACTORY.createAttributes(); }
    public static AttributesBuilder builder() { return new AttributesBuilder(); }
    public Attributes build() { return this.attributes; }


    public AttributesBuilder time(Time time) {
        this.attributes.getTime().add(time);
        return this;
    }

    public AttributesBuilder divisions(int divisions) {
        this.attributes.setDivisions(BigDecimal.valueOf(divisions));
        return this;
    }

    public AttributesBuilder key(int fifths, String mode, Integer staff) {
        Key key = FACTORY.createKey();
        key.setFifths(BigInteger.valueOf(fifths));
        key.setMode(mode);
        if (staff != null) { key.setNumber(BigInteger.valueOf(staff)); }
        this.attributes.getKey().add(key);
        return this;
    }

    // These next 2 are convenience functions.
    public AttributesBuilder key(String v, Integer staff) {
        reductor.core.KeySignature keySignature = new KeySignature(v, new Range());
        int fifths = keySignature.accidentals();
        String modeString = keySignature.mode() == 0 ? "major" : "minor";
        return this.key(keySignature.accidentals(), modeString, staff);
    }

    public AttributesBuilder key(String v) { return this.key(v, null); }

    /*
     TODO: consider a convenience function that just takes a Clef... clefs.
         - If null, no clef
         - Then, number of staves == number of clefs passed, in that order

         It would transform call sites like:
             .staves(2)
             .clefTreble(1)
             .clefBass(2)

         To:
             .staves(Clef.TREBLE, CLef.BASS, Clef.BASS) // for an organ, perhaps

         Not only is it much simpler, but it is easier to see, and probably safer to not put the
         onus on the user to wire up things correctly in terms of the staff #'s
    */
    public AttributesBuilder staves(int numberOfStaves) {
        this.attributes.setStaves(BigInteger.valueOf(numberOfStaves));
        return this;
    }

    // TODO: flesh this out (?)
    public AttributesBuilder clef(Clef clef, int staff) {
        clef.setNumber(BigInteger.valueOf(staff));
        this.attributes.getClef().add(clef);
        return this;
    }

    // These next 4 are convenience methods.
    public AttributesBuilder clefTreble(int staff) { return clef(reductor.dev.Defaults.trebleClef(), staff); }
    public AttributesBuilder clefBass(int staff) { return clef(reductor.dev.Defaults.bassClef(), staff); }
    public AttributesBuilder clefAlto(int staff) { return clef(reductor.dev.Defaults.altoClef(), staff); }
    public AttributesBuilder clefTenor(int staff) { return clef(Defaults.tenorClef(), staff); }


}
