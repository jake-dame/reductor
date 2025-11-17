package reductor.musicxml.exporter.builder;


import org.audiveris.proxymusic.*;
import org.audiveris.proxymusic.ScorePartwise.Part;
import org.audiveris.proxymusic.opus.Score;
import reductor.dev.Defaults;

import java.lang.String;
import java.util.ArrayList;
import java.util.List;

import static reductor.dev.Defaults.*;

/*

# PURPOSE

    The MusicXML 4.0 spec reference docs are wonderful, well organized, minimal/simple... great in
    every respect. ...except for indicating frequency constraints, and ordering details. It is
    all indentation and circular/empty/diamond bullet shaped. The way the web page renders it,
    makes it almost impossible to follow what is going, especially in the complicated or
    heavily-nested hierarchies.

     I reproduced that stuff here, in a more clear way (at least, to me), with the added benefit
     that anything not having to do this program is left out (though it is indicated when it is).

# ORDERING

    Order of sub-elements is always MANDATORY, unless otherwise noted with "__UNORDERED__".

    However, for this program, while I try to organize classes (fields, methods, build()
    order-of-operations) consistently, order (in:re MusicXML spec) is not of functional concern.
    ProxyMusic handles ordering, even for grouped elements, in accordance with JAXB.

# NOTATIONS

    Frequency constraints:
        - `(0 or 1)` --> One or more times (optional, and unique)
        - `(0+)`     --> Zero or more times (optional, but not non-unique)
        - `(1)`      --> Exactly one of (required, and unique)
        - `(1+)`     --> At least one of (required, but not unique)

    Between elements:
        - `+`      --> indicates grouping
        - `OR`     --> indicates only one or the other can be present
        - `AND/OR` --> indicates any combination, including none, is valid

    Miscellaneous:
        - `....` --> stand-in; this program will not handle anything related to this
                  element and/or it's sub-elements

# Exporting Blank/Empty <score-partwise>

   Included here with this context: reductor will generally allow whatever ProxyMusic and
   MuseScore allow. This means that default values will not be implicitly provided, and will lead
   to more null checks. But that design trade-off was decided in order to not muddy the waters
   in terms of provenance, making reductor handle state that ProxyMusic is perfectly capable of
   handling, etc.

    ## ProxyMusic

    After creating an empty ScorePartwise object, like so:

    ```java
    ScorePartwise sp = FACTORY.createScorePartwise();
    ```

    and immediately marshaling and writing it out, this is the MusicXML document produced:

    ```xml
    <?xml version="1.0" encoding="UTF-8"?>
    <!DOCTYPE score-partwise PUBLIC "-//Recordare//DTD MusicXML 4.0.3 Partwise//EN" "http://www.musicxml.org/dtds/partwise.dtd">
    <score-partwise version="4.0.3">
      <identification>
        <encoding>
          <software>ProxyMusic 4.0.3</software>
          <encoding-date>2025-10-25</encoding-date>
        </encoding>
      </identification>
    </score-partwise>
    ```

    So, ProxyMusic will not marshal null objects into XML elements. However, it will create and
    output its own <identification>:
        - <identification>/<encoding>/<software>
            - Something like: ProxyMusic <version>
        - <identification>/<encoding>/<encoding-date>
            - Today's date in YYYY-MM-DD form

    ## MuseScore

    When that MusicXML is opened in MuseScore, MuseScore will happily export it as any format,
    but it will NOT allow you to save it as an actual `.mscz` file due to the fact that at least
    one <part> is a requirement.

    Upon export as a `.musicxml` file, it will (like ProxyMusic) create and output its own
    <identification> with:
        - Several <encoding>/<supports> elements
        - Several <miscellaneous>/<miscellaneous-field> elements
        - A <defaults>
        - And, inexplicably, an empty <part-list> element

    ```xml
    <?xml version="1.0" encoding="UTF-8"?>
    <!DOCTYPE score-partwise PUBLIC "-//Recordare//DTD MusicXML 4.0 Partwise//EN" "http://www.musicxml.org/dtds/partwise.dtd">
    <score-partwise version="4.0">
      <identification>
        <encoding>
          <software>MuseScore Studio 4.6.3</software>
          <encoding-date>2025-10-25</encoding-date>
          <supports element="accidental" type="yes"/>
          <supports element="beam" type="yes"/>
          <supports element="print" attribute="new-page" type="no"/>
          <supports element="print" attribute="new-system" type="no"/>
          <supports element="stem" type="yes"/>
          </encoding>
        <miscellaneous>
          <miscellaneous-field name="creationDate">2025-10-25</miscellaneous-field>
          <miscellaneous-field name="platform">Apple Macintosh</miscellaneous-field>
          </miscellaneous>
        </identification>

      <!-- <defaults> removed for space -->

      <part-list>
        </part-list>
      </score-partwise>
    ```
*/

/*
:~~~~~~~~~ SPEC SUMMARY ~~~~~~~~~:

Exactly one of the following:

    1.) <score-partwise>
        A.) <work> (0 or 1)
                a. <work-number> (0 or 1)
                b. <work-title> (0 or 1)
                c. .... <opus> (0 or 1)
        B.) <movement-number> (0 or 1)
        C.) <movement-title> (0 or 1)
        D.) <identification> (0 or 1)
                a. <creator> (0+)
                b. <rights> (0+)
                c. <encoding> (0 or 1)  __UNORDERED__
                    i.   .... <encoding-date> (0+)
                    ii.  <encoder> (0+)
                    iii. <software> (0+)
                    iv.  <encoding-description> (0+)
                    v.   .... <supports> (0+)
                d. <source> (0 or 1)
                e. <relation> (0+)
                f. <miscellaneous> (0 or 1)
                    i. <miscellaneous-field> (0+)
        E.) .... <defaults> (0 or 1)
        F.) .... <credit> (0+)
        G.) <part-list> (1)
            a. <part-group> (0 or 1) + <score-part> (1) (group: 1+)
        H.) <part> (1 or more)
            a. <measure> (1 or more)
    2.) .... <score-timewise>
*/

/*
:~~~~~~~~~ ADDITIONALLY ~~~~~~~~~:


1.) <part>s are ordered within the <score-partwise> based on the order in which they appear in
the <part-list>.

2.) Rationale for exclusions:
    - <opus>: this may be supported in the future, but the percentage of use cases vs. complexity
              to implement vs. payoff debate is a losing one. The day that this program actually
              succeeds in what it does, maybe allowing for mass import/export of
              <score-partwise>-s will be in the cards. But for now, this is a
              one-piece-one-process type deal.
    - <identification>/<encoding>/<encoding-date>: because ProxyMusic overrides it anyway
    - <identification>/<encoding>/<supports>: this seems to be a notation software decision
    - <defaults>: this is a notation software decision
    - <credits>: this is a notation software decision. I wrote a manifesto on it somewhere but
                 can't find it now because it is bed time
    - <score-timewise>: it is insanely rare in the wild, and seems to be looked down upon,
                        and would require an almost entirely new program to parse and export
                        correctly.

2.) <score-timewise> is identical to <score-partwise> in every way except for a crucial
difference in organization, where the parent-child relationship between <part> and <measure>
is inverted. <score-partwise>/<part>/<measure> becomes <score-partwise>/<measure>/<part>.

In a score partwise:
    - Piano
        - m 1
        - m 2
        - m 3
        - ...
    - Violin
        - m 1
        - m 2
        - m 3
        - ...
    - Cello
        - m 1
        - ...
, and so on.

For a 3-measure long piece/snippet for piano + violin + cello:
    - In a score partwise:
        - Piano
            - m 1
            - m 2
            - m 3
        - Violin
            - m 1
            - m 2
            - m 3
        - Cello
            - m 1
            - m 2
            - m 3
    - In a score timewise:
        - m 1
            - piano
            - violin
            - cello
        - m 2
            - piano
            - violin
            - cello
        - m 3
            - piano
            - violin
            - cello
```
*/

/**
 * A wrapper for {@link org.audiveris.proxymusic.ScorePartwise} with a fluent builder API, catered
 * for
 * this program.
 *
 * @see
 * <a href="https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/score-partwise/">
 *     &lt;score-partwise&gt; (W3C)
 * <a/>
 * */
public class ScorePartwiseBuilder {


    private final ScorePartwise scorePartwise;
    private ScorePartwiseBuilder() { this.scorePartwise = FACTORY.createScorePartwise(); }
    public static ScorePartwiseBuilder builder() { return new ScorePartwiseBuilder(); }
    public ScorePartwise build() {

        if (this.scorePartwise.getPartList() == null) { this.partList(FACTORY.createPartList()); }

        List<Object> partList = this.scorePartwise.getPartList().getPartGroupOrScorePart();
        List<Part> parts = this.scorePartwise.getPart();

        List<ScorePart> generatedScoreParts = new ArrayList<>();
        List<Part> generatedParts = new ArrayList<>();
        if (partList.isEmpty()) {
            if (parts.isEmpty()) {
                Part p = Defaults.defaultPart();
                ScorePart s = Defaults.generateScorePartFromPart(p);
                p.setId(s);
                this.part(p);
                partList.add(s);
            } else {
                for (Part p : parts) {
                    generatedScoreParts.add(generateScorePartFromPart(p));
                }
            }
        } else {
            if (parts.isEmpty()) {
                for (Object o : partList) {
                    if (o instanceof ScorePart s) {
                        generatedParts.add(Defaults.generatePartFromScorePart(s));
                    } else {
                        throw new RuntimeException("does not support group parts");
                    }
                }
            }
        }

        assert partList.size() == parts.size();

        partList.addAll(generatedScoreParts);
        parts.addAll(generatedParts);

        return scorePartwise;
    }


    public ScorePartwiseBuilder workNumber(String v) {
        if (this.scorePartwise.getWork() == null) {
            this.scorePartwise.setWork(FACTORY.createWork());
        }
        this.scorePartwise.getWork().setWorkNumber(v);
        return this;
    }

    public ScorePartwiseBuilder workTitle(String v) {
        if (this.scorePartwise.getWork() == null) {
            this.scorePartwise.setWork(FACTORY.createWork());
        }
        this.scorePartwise.getWork().setWorkTitle(v);
        return this;
    }

    public ScorePartwiseBuilder movementNumber(String v) {
        this.scorePartwise.setMovementNumber(v);
        return this;
    }

    public ScorePartwiseBuilder movementTitle(String v) {
        this.scorePartwise.setMovementTitle(v);
        return this;
    }

    public ScorePartwiseBuilder identification(Identification identification) {
        this.scorePartwise.setIdentification(identification);
        return this;
    }

    private ScorePartwiseBuilder defaults(org.audiveris.proxymusic.Defaults defaults) {
        this.scorePartwise.setDefaults(defaults);
        return this;
    }

    public ScorePartwiseBuilder credit(List<Credit> credits) {
        credits.forEach(this::credit);
        return this;
    }
    public ScorePartwiseBuilder credit(Credit credit) {
        this.scorePartwise.getCredit().add(credit);
        return this;
    }

    public ScorePartwiseBuilder partList(PartList partList) {
        this.scorePartwise.setPartList(partList);
        return this;
    }

    public ScorePartwiseBuilder scorePart(ScorePart v) {
        if (this.scorePartwise.getPartList() == null) {
            this.scorePartwise.setPartList(FACTORY.createPartList());
        }
        this.scorePartwise.getPartList().getPartGroupOrScorePart().add(v);
        return this;
    }

    public ScorePartwiseBuilder part(List<Part> parts) {
        parts.forEach(this::part);
        return this;
    }
    public ScorePartwiseBuilder part(Part part) {
        this.scorePartwise.getPart().add(part);
        return this;
    }


    //region convenience

    public ScorePartwiseBuilder title(String v) {
        // MuseScore displays the <movement-title> as the big title at the top of the page.
        this.scorePartwise.setMovementTitle(v);
        return this;
    }

    // The only other way to not muddy up the IdentificationBuilder class with some static
    // utility functions that returned TypedText and had to rewire a bunch of the builder
    // chaining etc... was to just duplicate code here. It's 3 lines. Lesser of two evils.
    public ScorePartwiseBuilder composer(String v) {
        if (this.scorePartwise.getIdentification() == null) {
            this.scorePartwise.setIdentification(FACTORY.createIdentification());
        }

        // should match IdentificationBuilder#creator()
        TypedText tt = FACTORY.createTypedText();
        tt.setType("composer");
        tt.setValue(v);
        // end

        this.scorePartwise.getIdentification().getCreator().add(tt);
        return this;
    }

    public static ScorePartwise of() {
        return builder().build();
    }

    //public static ScorePartwise of(Parts parts) {
    //    return from(parts).build();
    //}
    //
    //public static ScorePartwiseBuilder from(Parts parts) {
    //    return builder()
    //            .partList(parts.getPartList())
    //            .part(parts.getParts());
    //}

    //endregion


}
