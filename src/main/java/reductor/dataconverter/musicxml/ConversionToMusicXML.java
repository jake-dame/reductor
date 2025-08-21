package reductor.dataconverter.musicxml;

import org.audiveris.proxymusic.*;
import reductor.piece.*;


public class ConversionToMusicXML {

    // This is how you get ANY object in the ProxyMusic library. It has a createX() method for every class
    //     that is possible to create. It is static in this class just so that every class isn't needing
    //     to create a brand new factory.
    static ObjectFactory factory;

    // The top-level function in the creation pipeline. Pretty high-level, mostly in charge of orchestrating the
    //     creation and attachment of elements whose direct parent is a scorepartwise (the root element for musicxml).
    public static ScorePartwise createScorePartwise(Piece piece) {

        /*
         Direct children of ScorePartwise:
             + work
             + identification (handled by proxymusic)
             + credit(s) - title, subtitle, composer
             + part-list
                 + scorepart(s)
             + part(s)

         Additional notes:
             1.) The first five things are collectively known as the "score header"
             2.) The part is where the vast majority of information is
             3.) We will only have 1 part, and thus, 1 scorepart. Because the scorepart is the one
                 thing we are interested in in the score header, that is what the HeaderBuilder returns.
                 Although a little funny to have a Builder return a constituent part, 1.) this is what fits
                 best with the proxymusic API 2.) it is simpler code-wise (even if not ideologically pure) 3.)
                 this keeps the HeaderBuilder from trying to create/set/attach a Part, which is on the same level
                 hierarchically. This code is organized so that elements of the musicxml file that are on the same
                 level hierarchically don't handle/assign each other -- only child elements.
             4.) The PartBuilder does NOT follow the pattern of static utility class, but... there was just no way
                 around it with the time-crunch. The logic is very complex and fragile, so it has members.
        */

        // Initialize the factory.
        factory = new ObjectFactory();

        // Create empty ScorePartwise.
        ScorePartwise scorePartwise = factory.createScorePartwise();

        // Build all the header elements (the first five in the list above), and give us a reference to
        //     the ScorePart so we can attach a Part to it.
        ScorePart scorePart = HeaderBuilder.build(scorePartwise, piece.getName());

        // Create empty part.
        ScorePartwise.Part part = factory.createScorePartwisePart();
        // This is how you attach/identify with the API for this particular element -- not an integer id or anything.
        part.setId(scorePart);
        // This is named singularly... it is a list.
        scorePartwise.getPart().add(part);

        // Create empty partBuilder.
        PartBuilder partBuilder = new PartBuilder(part, piece.getMeasures());
        // All the PartBuilder needs to know is the part to attach measures to, and the reductor.piece.Measure objects
        //     to parse/process.
        partBuilder.build(piece.getTPQ());

        return scorePartwise;
    }

}