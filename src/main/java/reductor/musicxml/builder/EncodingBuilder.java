package reductor.musicxml.exporter.builder;


import jakarta.xml.bind.JAXBElement;
import org.audiveris.proxymusic.Encoding;
import org.audiveris.proxymusic.TypedText;

import javax.xml.namespace.QName;

import static reductor.dev.Defaults.FACTORY;


/*
ProxyMusic automatically handles basically anything having to do with <encoding>, so it
doesn't have nice methods to set this. So the setters here are more "manual" than in the others.
*/

public class EncodingBuilder {


    private final Encoding encoding;
    private EncodingBuilder() { this.encoding = FACTORY.createEncoding(); }
    public static EncodingBuilder builder() { return new EncodingBuilder(); }
    public Encoding build() { return encoding; }


    // see: IdentificationBuilder#rights()
    public EncodingBuilder encoder(String v) {
        TypedText typedText = FACTORY.createTypedText();
        typedText.setValue(v);
        JAXBElement<TypedText> encoder = new JAXBElement<>(
                new QName("encoder"), TypedText.class, typedText
        );
        this.encoding.getEncodingDateOrEncoderOrSoftware().add(encoder);
        return this;
    }

    public EncodingBuilder software(String v) {
        JAXBElement<String> software = new JAXBElement<>(
                new QName("software"), String.class, v
        );
        this.encoding.getEncodingDateOrEncoderOrSoftware().add(software);
        return this;
    }

    public EncodingBuilder encodingDescription(String v) {

        JAXBElement<String> encodingDescription = new JAXBElement<>(
                new QName("encoding-description"),
                String.class,
                v
        );
        this.encoding.getEncodingDateOrEncoderOrSoftware().add(encodingDescription);
        return this;
    }


}
