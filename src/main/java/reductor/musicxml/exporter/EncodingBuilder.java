package reductor.musicxml.exporter;


import jakarta.xml.bind.JAXBElement;
import org.audiveris.proxymusic.Encoding;
import org.audiveris.proxymusic.Supports;
import org.audiveris.proxymusic.TypedText;
import org.audiveris.proxymusic.YesNo;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static reductor.musicxml.exporter.Defaults.FACTORY;


/*
ProxyMusic automatically handles basically anything having to do with <encoding>, so it
doesn't have nice methods to set this. So this sub-builder is a bit messier than the
others in the parent class(es).

Additionally, it seems that ProxyMusic will always add itself as a <software> and its
own <encoding-date>.

This class is less settled in terms of what I want to do with it than the other builders in this
package.

In the end, I may remove the <support> and <encoding-date> support. That's why some of the
function signatures or their entire purpose may be a little odd.
*/

public class EncodingBuilder {


    private final JAXBElement<XMLGregorianCalendar> encodingDate;
    private final List<JAXBElement<TypedText>> encoders;
    private final List<JAXBElement<String>> softwares;
    private final List<JAXBElement<String>> encodingDescriptions;
    private final List<JAXBElement<Supports>> supportsList;


    private EncodingBuilder() {
        this.encodingDate = null;
        this.encoders = new ArrayList<>();
        this.softwares = new ArrayList<>();
        this.encodingDescriptions = new ArrayList<>();
        this.supportsList = new ArrayList<>();
    }


    public static EncodingBuilder builder() {
        return new EncodingBuilder();
    }

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

    public EncodingBuilder encoder(String type, String value) {
        TypedText t = FACTORY.createTypedText();
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

    public EncodingBuilder supports(
            YesNo type, String element, String attribute,
            String value
    ) {

        Supports s = FACTORY.createSupports();
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

        this.supportsList.add(j);
        return this;
    }

    public Encoding build() {

        Encoding encoding = FACTORY.createEncoding();

        List<JAXBElement<?>> encodingSubElements = encoding.getEncodingDateOrEncoderOrSoftware();

        encodingSubElements.add(this.encodingDate);
        encodingSubElements.addAll(this.encoders);
        encodingSubElements.addAll(this.softwares);
        encodingSubElements.addAll(this.encodingDescriptions);
        encodingSubElements.addAll(this.supportsList);

        return encoding;
    }


}
