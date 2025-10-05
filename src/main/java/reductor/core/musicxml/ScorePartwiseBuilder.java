package reductor.core.musicxml;


import org.audiveris.proxymusic.ObjectFactory;
import org.audiveris.proxymusic.ScorePart;
import org.audiveris.proxymusic.ScorePartwise;
import org.audiveris.proxymusic.ScorePartwise.Part;
import reductor.core.Piece;


public class ScorePartwiseBuilder {


    /**
     * This is how you "get" any object in the ProxyMusic library.
     * It has a `createX()` method for every class.
     */
    static ObjectFactory factory = new ObjectFactory();


    private ScorePartwiseBuilder() {}


    /**
     * Creates a ScorePartwise object, the root element of a MusicXML doc.
     * <p>
     * Responsible for calling all the builders/functions of the direct children of a {@code <score-partwise>} element.
     * <p>
     * Direct children of ScorePartwise:
     * <ol>
     *     <li>{@code <work>}</li>
     *     <li>{@code <identification>} (handled by ProxyMusic)</li>
     *     <li>{@code <credit>}(s) - e.g., title, subtitle, composer</li>
     *     <li>{@code <part-list>}</li>
     *     <li>{@code <part>}(s)</li>
     * </ol>
     * <p>
     * Additional notes:
     * <ol>
     *     <li>
     *         The first four things in the list above are collectively known as the "score header". They are
     *         comprised of file metadata (unrendered), piece/work metadata (rendered), part metadata, etc.
     *     </li>
     *     <li>
     *         One other direct child is the {@code <defaults>} element, but that concerns rendering only and is
     *         not created/attached by this program or ProxyMusic. It is done by notation software.
     *     </li>
     *     <li>
     *         The actual <part> elements make up the vast majority of a musicxml doc
     *     </li>
     *     <li>
     *         We will only have 1 part, and thus, 1 scorepart. Because the scorepart is the one thing we are
     *         interested in the score header, that is what the HeaderBuilder returns.
     *     </li>
     * </ol>
     */
    public static ScorePartwise getScorePartwise(Piece piece) {

        ScorePartwise scorePartwise = factory.createScorePartwise();

        // HeaderBuilder builds the whole "score header", but it returns just the ScorePart
        // object. This is because all score header elements are directly attached to the
        // ScorePartwise in HeaderBuilder (consistent with the design of all other builders in this
        // package. However, since the ScorePart is a map key of sorts, the Part element
        // needs it upon creation. This seemed like the best way to do it without
        // making HeaderBuilder needing to be aware of anything Part-related.
        ScorePart scorePart = HeaderBuilder.build(scorePartwise);

        Part part = factory.createScorePartwisePart();
        // This is how you attach/identify with the API for this particular element -- not an integer id or anything.
        part.setId(scorePart);
        // `getPart()` is named with a singular noun (due to legacy stuff), but really, it gets the Part List.
        scorePartwise.getPart().add(part);

        // PartBuilder does not follow the pattern of the other builders in this package in that it is
        // __not__ stateless. MusicXML parts are inherently sequential, and to avoid arglist/parameter
        // explosion, while keeping track of the "caret", measures filled, etc., state was deemed
        // the best option.
        PartBuilder partBuilder = new PartBuilder(part, piece.getMeasures());
        partBuilder.build(piece.getTPQ());

        return scorePartwise;
    }


}
