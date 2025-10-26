package reductor.musicxml.exporter;


import org.audiveris.proxymusic.*;
import org.audiveris.proxymusic.ScorePartwise.Part;

import java.lang.String;
import java.util.ArrayList;
import java.util.List;

import static reductor.musicxml.exporter.Defaults.*;

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
                    i.   <encoding-date> (0+)
                    ii.  <encoder> (0+)
                    iii. <software> (0+)
                    iv.  <encoding-description> (0+)
                    v.   <supports> (0+)
                d. <source> (0 or 1)
                e. <relation> (0+)
                f. <miscellaneous> (0 or 1)
                    i. <miscellaneous-field> (0+)
        E.) <defaults> (0 or 1)
                ....
        F.) <credit> (0+)
                ....
        G.) <part-list> (1)
            a. <part-group> (0 or 1) + <score-part> (1) (group: 1+)
        H.) <part> (1 or more)
            a. <measure> (1 or more)
    2.) <score-timewise>
            ....
*/

/*
:~~~~~~~~~ ADDITIONALLY ~~~~~~~~~:


1.) <part>s are ordered within the <score-partwise> based on the order in which they appear in
the <part-list>.

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

/* https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/score-partwise/ */
public class ScorePartwiseBuilder {


    private String workNumber;
    private String workTitle;

    private String movementNumber;
    private String movementTitle;

    private Identification identification;

    private final PartList partList;

    private final List<Part> parts;


    private ScorePartwiseBuilder() {
        this.workNumber = "";
        this.workTitle = "";
        this.movementNumber = "";
        this.movementTitle = "";
        this.identification = FACTORY.createIdentification();
        this.partList = FACTORY.createPartList();
        this.parts = new ArrayList<>();

        // Add dummy.
        // Removed in build() if external Part objects are added.
        this.parts.add(FACTORY.createScorePartwisePart());
    }


    public static ScorePartwiseBuilder builder() {
        return new ScorePartwiseBuilder();
    }

    public ScorePartwiseBuilder workNumber(String v) {
        this.workNumber = v;
        return this;
    }

    public ScorePartwiseBuilder workTitle(String v) {
        this.workTitle = v;
        return this;
    }

    public ScorePartwiseBuilder movementNumber(String s) {
        this.movementNumber = s;
        return this;
    }

    public ScorePartwiseBuilder movementTitle(String s) {
        this.movementTitle = s;
        return this;
    }

    public ScorePartwiseBuilder identification(Identification identification) {
        this.identification = identification;
        return this;
    }

    public ScorePartwiseBuilder part(Part... parts) {
        for (Part p : parts) {
            if (p != null) {
                this.parts.add(p);
            }
        }
        return this;
    }

    public ScorePartwise build() {

        ScorePartwise sp = FACTORY.createScorePartwise();

        final String musicXmlVersion = "4.0";
        sp.setVersion(musicXmlVersion);

        Work work = FACTORY.createWork();
        work.setWorkNumber(this.workNumber);
        work.setWorkTitle(this.workTitle);

        sp.setWork(work);
        sp.setMovementNumber(this.movementNumber);
        sp.setMovementTitle(this.movementTitle);
        sp.setIdentification(this.identification);
        sp.setPartList(this.partList);

        // Remove dummy. See: ctor
        if (1 < this.parts.size()) {
            this.parts.removeFirst();
        }

        // Add Part objects.
        sp.getPart().addAll(this.parts);

        // Populate part list.
        for (Part part : parts) {
            // Part#getID() returns its respective scorePart object.
            this.partList.getPartGroupOrScorePart().add(part.getId());
        }

        return sp;
    }


}
