package reductor.piece;


/**
 * Abstraction for objects that are associated with a {@link Range}.
 * <p>
 * This can include notes, measures, time signatures (regions), etc. -- ranged elements
 * that can be represented on a number line.
 */
public interface Ranged {

    /** All Ranged elements are guaranteed to have and provide their Range. */
    Range getRange();

}