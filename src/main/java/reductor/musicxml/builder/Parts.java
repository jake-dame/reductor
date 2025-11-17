package reductor.musicxml.builder;


import org.audiveris.proxymusic.PartList;
import org.audiveris.proxymusic.ScorePart;
import org.audiveris.proxymusic.ScorePartwise.Part;

import java.util.*;

import static reductor.dev.Defaults.FACTORY;

/*
The methods here used to be instance methods in ScorePartwiseBuilder.

Turns out there is a bit more to making sure ScorePart-s and Part-s are coupled correctly,
and the overload explosion was really muddying up the ScorePartwiseBuilder.

So an API boundary was drawn, and now ScorePartwiseBuilder just has a couple of from()s and of()s
. It use's the Parts API, and Parts is responsible for all the actual business
logic.
*/
public class Parts {

    /*
    Input:
        - Part(s)
        - ScorePart(s)
        - Part(s), ScorePart(s)

    State:
        -
    */

    //public record Bundle(String id, ScorePart scorePart, Part part){}
    //private final Map<String, ScorePart> scorePartMap;
    //private final Map<Object, Part> partMap;


    private List<Part> parts;
    private List<ScorePart> scoreParts;

    private final PartList partList;

    private Parts() {
        this.partList = FACTORY.createPartList();
    }

    void generatePartListFromParts(Part... part) {
        List<Part> parts = Arrays.stream(part).toList();

        int id = 0;
        for (Part p : part) {
            ScorePart sp = FACTORY.createScorePart();
            sp.setId(createScorePartId(id++));
            p.setId(sp);
            this.partList.getPartGroupOrScorePart().add(sp);
            this.scoreParts.add(sp);
            this.parts.add(p);
        }
    }

    void generatePartListFromScoreParts(ScorePart... scorePart) {
        List<ScorePart> scoreParts = Arrays.stream(scorePart).toList();

        int id = 0;
        for (ScorePart sp : scorePart) {
            sp.setId(createScorePartId(id++));
            Part p = FACTORY.createScorePartwisePart();
            p.setId(sp);
            this.partList.getPartGroupOrScorePart().add(sp);
            this.scoreParts.add(sp);
            this.parts.add(p);
        }
    }



    // =======================================  GETTERS  ======================================== //

    public PartList getPartList() {
        return this.partList;
    }


    public List<Part> getParts() {
        return Collections.unmodifiableList(this.parts);
    }

    public List<ScorePart> getScoreParts() {
        return Collections.unmodifiableList(this.scoreParts);
    }


    // =======================================  STATIC  ======================================= //


    // Because in most situations involving musicxml id-s, incrementing, doing math, etc., is
    // much easier with ints, rather than having to do messy string parsing, char arithmetic, and
    // back-and-forth conversions everywhere. So, internally, all id stuff is done with ints.
    // This just automates conformation with MusicXML conventions.
    public static String createScorePartId(int id) {
        return "P"+id;
    }

    public static String createScoreInstrumentId(String scorePartId, int id) {

        return scorePartId+"I"+id; // "P1", 1 --> "P1-I1"
    }

}
