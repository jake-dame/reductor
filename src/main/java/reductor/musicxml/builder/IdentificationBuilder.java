package reductor.musicxml.builder;


import org.audiveris.proxymusic.*;

import java.lang.String;

import static reductor.dev.Defaults.FACTORY;


public class IdentificationBuilder {


    private final Identification identification;
    private IdentificationBuilder() { this.identification = FACTORY.createIdentification(); }
    public static IdentificationBuilder builder() { return new IdentificationBuilder(); }
    public Identification build() { return this.identification; }


    public IdentificationBuilder creator(String type, String value) {
        TypedText t = FACTORY.createTypedText();
        t.setType(type);
        t.setValue(value);
        this.identification.getCreator().add(t);
        return this;
    }

    // These next 5 are convenience methods.
    public IdentificationBuilder composer(String v) { return this.creator("composer", v); }
    public IdentificationBuilder lyricist(String v) { return this.creator("lyricist", v);}
    public IdentificationBuilder translator(String v) { return this.creator("translator", v); }
    public IdentificationBuilder arranger(String v) { return this.creator("arranger", v); }
    public IdentificationBuilder subtitle(String v) { return this.creator("subtitle", v); }

    // Both <rights> and relation have a type= attribute, but I haven't ever seen it used, and
    // even the MusicXML documentation examples don't show any use of it. But to make ProxyMusic
    // and JAXB happy, I provide an empty string. This results in the attribute being empty in
    // the resultant MusicXML doc, as desired.
    public IdentificationBuilder rights(String value) {
        TypedText t = FACTORY.createTypedText();
        t.setType("");
        t.setValue(value);
        this.identification.getRights().add(t);
        return this;
    }

    public IdentificationBuilder encoding(Encoding encoding) {
        this.identification.setEncoding(encoding);
        return this;
    }

    public IdentificationBuilder source(String v) {
        this.identification.setSource(v);
        return this;
    }

    // see: #rights()
    public IdentificationBuilder relation(String value) {
        TypedText t = FACTORY.createTypedText();
        t.setType("");
        t.setValue(value);
        this.identification.getRelation().add(t);
        return this;
    }

    public IdentificationBuilder miscellaneousField(String nameAttribute, String value) {

        // name= attribute of <miscellaneous-field> is required by spec.
        if (nameAttribute.isEmpty()) {
            throw new IllegalStateException("""
                                            name attribute of a miscellaneous-field element
                                             should not be an empty string
                                            """);
        }

        if (this.identification.getMiscellaneous() == null) {
            this.identification.setMiscellaneous(FACTORY.createMiscellaneous());
        }

        MiscellaneousField mf = FACTORY.createMiscellaneousField();
        mf.setName(nameAttribute);
        mf.setValue(value);

        this.identification.getMiscellaneous().getMiscellaneousField().add(mf);

        return this;
    }


}
