package reductor.musicxml.exporter;


import org.audiveris.proxymusic.*;

import java.lang.String;
import java.util.ArrayList;
import java.util.List;

import static reductor.musicxml.exporter.Defaults.FACTORY;


public class IdentificationBuilder {


    private final List<TypedText> creators;
    private final List<TypedText> rights;
    private Encoding encoding;
    private String source;
    private final List<TypedText> relations;
    private final List<MiscellaneousField> miscellaneous;


    private IdentificationBuilder() {
        this.creators = new ArrayList<>();
        this.rights = new ArrayList<>();
        this.encoding = FACTORY.createEncoding();
        this.source = "";
        this.relations = new ArrayList<>();
        this.miscellaneous = new ArrayList<>();
    }


    public static IdentificationBuilder builder() {
        return new IdentificationBuilder();
    }

    public void creator(String type, String value) {
        TypedText t = FACTORY.createTypedText();
        t.setType(type);
        t.setValue(value);
        creators.add(t);
    }

    // These next 5 are convenience methods.
    public IdentificationBuilder composer(String v) { this.creator("composer", v); return this; }
    public IdentificationBuilder lyricist(String v) { this.creator("lyricist", v); return this; }
    public IdentificationBuilder translator(String v) { this.creator("translator", v); return this; }
    public IdentificationBuilder arranger(String v) { this.creator("arranger", v); return this; }
    public IdentificationBuilder subtitle(String v) { this.creator("subtitle", v); return this; }

    public IdentificationBuilder rights(String type, String value) {
        TypedText t = FACTORY.createTypedText();
        t.setType(type);
        t.setValue(value);
        rights.add(t);
        return this;
    }

    public IdentificationBuilder encoding(Encoding encoding) {
        this.encoding = encoding;
        return this;
    }

    public IdentificationBuilder source(String v) {
        this.source = v;
        return this;
    }

    public IdentificationBuilder relation(String type, String value) {
        TypedText t = FACTORY.createTypedText();
        t.setType(type);
        t.setValue(value);
        relations.add(t);
        return this;
    }

    public IdentificationBuilder miscellaneousField(String name, String value) {
        if (name.isEmpty()) {
            throw new IllegalStateException("""
                                            name attribute of a miscellaneous-field element
                                             should not be an empty string
                                            """);
        }
        MiscellaneousField m = FACTORY.createMiscellaneousField();
        m.setName(name);
        m.setValue(value);
        miscellaneous.add(m);
        return this;
    }

    public Identification build() {

        Identification i = FACTORY.createIdentification();

        Miscellaneous m = FACTORY.createMiscellaneous();
        i.setMiscellaneous(m);

        i.getCreator().addAll(this.creators);
        i.getRights().addAll(this.rights);
        i.setEncoding(this.encoding);
        i.setSource(this.source);
        i.getRelation().addAll(this.relations);
        m.getMiscellaneousField().addAll(this.miscellaneous);

        return i;
    }


}
