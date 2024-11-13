//package reductor;
//
//import java.util.ArrayList;
//import java.util.HashSet;
//import java.util.List;
//import java.util.Set;
//
//
///*
//This is an implementation of an interval tree, mostly following the approach described in CLR, where each
//node is keyed on a single interval, stores its associated data as a List, and also stores the max right endpoint
//of the subtree rooted at that node (which allows, using the interval trichotomy, for ignoring the left subtree
//of certain nodes at times, leading to logN/binary searches.
//
//The only exposed functionality for this tree besides construction, which can only be done once, is query, as
//this meets the needs of the program.
//
//The purpose of the tree is to store objects representing different musical elements (e.g. notes, chords,
//measures, etc.). We will use notes (which in this program are represented by the Note class, which extends
//both Range and Comparable<Note>) as an example of the unique challenges of implementing this data structure
//in a useful way.
//
//Additional context:
//1.The Ranged interface looks like this:
//
//public interface Ranged {
//    Range range(); // this is merely a getter for an object's Range field
//}
//
//2. All Range objects represent a musical element's start and end values, in terms of MIDI ticks:
//
//A range representing [0,480] in turn represents a musical element whose starting/ON tick is at 0 and
//ending/OFF tick is at 480.
//
//3. The tree is not self-balancing because the use cases for this tree do not involve addition or removal;
//they always involve construction of a tree from prepared and static data.
//
//Unique challenges:
//
//1. While we can safely ignore/prevent exact duplicates (say two Note objects both representing identical
//ranges and pitches), Notes with different pitches but identical ranges should all be stored in the tree.
//Therefore: C4 [0,480], E4 [0,480], and G4 [0,480] should all be added to the list at the Node for [0,480]; i
//.e., the Node at [0,480] contains the notes C4, E4, and G4. There should never be duplicates in the list
//at the Node, since those would be cases of notes with exactly equal ranges *and* pitches (which are both
//not allowed on the same channel in the MIDI spec, in addition to be problematic for this program).
//
//2. Construction using the recursive list/median approach is further complicated by the fact that many
//duplicate ranges may exist in a list of otherwise perfectly distinct elements. This causes clustering
//that throws off the median calculation. The approach that I took to solve this is to construct the tree
//from a Set of unique ranges represented in the List, creating a skeleton or scaffold of sorts, after
//which each element is actually added. There is probably a way to do this all in one loop, but in terms of
//clarity and maintainability, the former approach is better as the latter would be rather complicated and
//verbose.
//
//3. When querying (which is usually done in a sequential, "chunking" manner, notes that have long durations
//whilst many other notes occur should not be added to a list of targets every single query; in other words,
//a query window spanning the entire length of a piece which consists of 1 tied, lengthy note spanning the
//entire length of the piece with 1000 other notes occurring at various points in the piece should still
//only return a list of targets/matches of size 1001.
//
//This implementation currently controls for exact duplicates using List.contains(), which is not optimal, but
//I thought it better than enforcing the argument given to the constructor to be a Set, which would put more
//data manipulation responsibility on classes not necessarily suited for data manipulation tasks.
//
//I also experimented with using HashSets and TreeSets for the container in both Node.elements and the "matches"
//container of the query() function. The problems were:
//+ HashSet is disastrous if elements are anything but perfectly immutable. While Range() objects are
//themselves immutable, the objects that implement Ranged (such as Note and Chord) are *not*. The purpose
//of this program at this point largely revolves around mutation of musical elements. At some point in the
//future, this may be controlled for by constructing entirely new structures, but for now, I could not risk
//using hashCode() with mutable objects.
//+ TreeSet uses an object's compareTo(). While all objects that implement Ranged, coincidentally, also
//implement Comparable, their respective compareTo() methods are *not* always involving their Range fields.
//For instance, Note objects compare by pitch, not by range. This led to very confusing test results before
//I realized what the problem was (notes with the same pitch were not being added to the Set because their
//compareTo() determined they were equal).
//*/
//
//
//// todo the construction time complexity analysis is not accurate given the current implementation
///**
// * An interval tree (augmented BST) that can contain any object with a {@link reductor.Range}
// * (i.e., implements the {@link reductor.Ranged} interface), e.g. {@link reductor.Note}, {@link reductor.Chord}, etc.
// * <p>
// * This implementation follows that which is described in CLR:
// * <ul>
// *     <li> Construction in O(NlogN) time </li>
// *     <li> Queries in O(logN + m) time </li>
// * </ul>
// * <p>
// * Since the purpose of this tree within the context of this application is to provide efficient
// * look-up, it only provides query functionality, and can only be constructed once, from
// * fixed data.
// */
//public class IntervalTree<T extends Ranged> {
//
//
//    /*
//        Natural ordering of T.range():
//
//            Primary ordering:              by low value
//            Secondary ordering:            by high value
//            Ts with duplicate ranges:      allowed as long as associated data is different (using equals())
//
//        Within context of tree insert/query operations:
//
//                        Range of        Interval
//                      current node:      to add:          Action:
//
//            Case 1.1:   [10,20]          [10,15]          Go left
//            Case 1.2:   [10,20]          [5,20]           Go left
//
//            Case 2:     [10,20]          [10,20]          Add to Current Node if not !equals()
//
//            Case 3.1:   [10,20]          [10,25]          Go right
//            Case 3.2:   [10,20]          [15,20]          Go right
//    */
//
//
//    //region <Node>
//
//
//    /// A binary node which represents a range/interval and stores a list of elements
//    class Node implements Ranged { // todo make private again and members
//
//        /// The {@link reductor.Range} (i.e., interval) this node represents
//        private final Range range;
//
//        /// Max endpoint in subtree rooted at this node (used to ignore left subtrees during queries)
//        private long max;
//
//        /// This node's data (a Set of elements with the same range but possibly differing pitches)
//        List<T> elements;
//
//        /// This node's left child
//        Node left;
//
//        /// This node's right child
//        Node right;
//
//
//        /// Primary constructor which takes a {@link reductor.Range}
//        Node(Range range) {
//            this.range = new Range(range);
//            this.max = -1;
//            this.left = null;
//            this.right = null;
//            this.elements = new ArrayList<>();
//        }
//
//
//        /// Adds an element to this node's list
//        boolean add(T elem) {
//
//            assert elem.getRange().equals(this.getRange());
//
//            if (this.elements.contains(elem)) {
//                return false;
//            }
//
//            return this.elements.add(elem);
//
//        }
//
//        @Override
//        public String toString() {
//
//            String str = this.range + "->{";
//
//            for (T note : this.elements) {
//                str += note + ",";
//            }
//
//            str = str.substring(0, str.length() - 1); // get last comma off
//
//            // looks like: "[240,480]->{C4,E4,G4}"
//            return str + "}";
//
//        }
//
//        @Override
//        public Range getRange() {
//            return new Range(this.range);
//        }
//
//        /// The number of elements this node holds
//        int size() {
//            return this.elements.size();
//        }
//
//        public long getMax() {
//            return this.max;
//        }
//
//    }
//
//
//    //endregion
//
//
//    /// Root of this interval tree
//    Node root; // todo should be private, but hard to test if so
//
//    /// Size (number of nodes)
//    private int numNodes;
//
//    /// Size (number of elements)
//    private int numElements;
//
//    /// Primary constructor
//    IntervalTree(ArrayList<T> elements) {
//
//        if (elements == null) {
//            throw new NullPointerException("whatever was passed to IntervalTree constructor was null");
//        }
//
//        numNodes = 0;
//        numElements = 0;
//
//        // Get a "set" of the ranges of the elements
//        ArrayList<Range> uniqueRanges = getUniqueRanges(elements);
//
//        // Ensure the list is sorted before doing median-based recursive construction
//        uniqueRanges.sort(null);
//
//        // Build the outline/structure/skeleton of the tree
//        root = buildSkeleton(uniqueRanges);
//
//        // Add the actual elements to the nodes' lists
//        addAll(elements);
//
//    }
//
//
//    //region <Build>
//
//
//    /// Adds ranges of elements to Set to remove duplicate ranges, and gives back (sorted) List
//    private ArrayList<Range> getUniqueRanges(ArrayList<T> elements) {
//
//        HashSet<Range> rangesSet = new HashSet<>();
//
//        // Remove duplicates
//        for (T elem : elements) {
//            rangesSet.add(elem.getRange());
//        }
//
//        // Convert back to List
//        return new ArrayList<>(rangesSet);
//    }
//
//    /// Recursively constructs the structure of the tree; all nodes are still empty when this function returns
//    private Node buildSkeleton(ArrayList<Range> ranges) {
//        return buildSkeleton(ranges, 0, ranges.size() - 1);
//    }
//
//    private Node buildSkeleton(ArrayList<Range> ranges, int firstIndex, int lastIndex) {
//
//        // e.g., firstIndex: 25; lastIndex: 24
//        if (firstIndex > lastIndex) {
//            // Base case (return a null child)
//            return null;
//        }
//
//        // e.g., 0 to 100 --> 50 (upper median)
//        int middleIndex = (firstIndex + lastIndex) / 2;
//
//        // e.g., add 50 here (median of subtree rooted at this node)
//        Node node = new Node(ranges.get(middleIndex));
//        numNodes++;
//
//        // e.g.,start left subtree with 0 to 49
//        node.left = buildSkeleton(ranges, firstIndex, middleIndex - 1);
//
//        // e.g.,start right subtree with 51 to 100
//        node.right = buildSkeleton(ranges, middleIndex + 1, lastIndex);
//
//        // return the actual node you just created here once all of the functions you called have returned
//        return node;
//
//    }
//
//    private void addAll(ArrayList<T> elements) {
//        for (T elem : elements) {
//            add(root, elem);
//        }
//    }
//
//    private void add(Node node, T elem) {
//
//        // Update node.max if elem to be added will set the new max right endpoint of elems in subtree rooted at
//        // this node
//        if (node.max < elem.getRange().getHigh()) {
//            node.max = elem.getRange().getHigh();
//        }
//
//        if (elem.getRange().compareTo(node.getRange()) < 0) {
//            if (node.left != null) {
//                add(node.left, elem);
//            }
//        } else if (elem.getRange().compareTo(node.getRange()) > 0) {
//            if (node.right != null) {
//                add(node.right, elem);
//            }
//        } else {
//            // Only increment element count if node added elem (exact duplicates are not added)
//            if (node.add(elem)) {
//                numElements++;
//            }
//        }
//
//    }
//
//
//    //endregion
//
//
//    //region <Query>
//
//
//    ArrayList<T> query(Range window) {
//
//        if (root == null) {
//            return new ArrayList<>();
//        }
//
//        if (window == null) {
//            throw new NullPointerException("null Range was passed to IntervalTree#query");
//        }
//
//        Set<T> matches = new HashSet<>();
//
//        query(root, window, matches);
//
//        return new ArrayList<>(matches);
//
//    }
//
//    private Set<T> query(Node node, Range window, Set<T> matches) {
//
//        if (window.overlaps(node.getRange())) {
//            matches.addAll(node.elements);
//        }
//
//        if (node.left == null && node.right == null) {
//            return matches;
//        }
//
//        if (node.left != null && window.getLow() <= node.left.getMax()) {
//            query(node.left, window, matches);
//        }
//
//        //if (node.right != null) {
//        if (node.right != null && window.getHigh() >= node.getRange().getLow()) {
//            query(node.right, window, matches);
//        }
//
//        return matches;
//
//    }
//
//
//    //endregion
//
//
//    //region <Miscellaneous>
//
//
//    /// Returns the tree as an ArrayList, using in-order traversal
//    public ArrayList<T> toList() {
//        ArrayList<T> inOrderList = new ArrayList<>();
//        toList(root, inOrderList);
//        return inOrderList;
//    }
//
//    private void toList(Node node, ArrayList<T> inOrderList) {
//
//        if (node == null) {
//            // Base case
//            return;
//        }
//
//        toList(node.left, inOrderList);
//        inOrderList.addAll(node.elements);
//        toList(node.right, inOrderList);
//
//    }
//
//    public void print() {
//        print(root);
//    }
//
//    private void print(Node node) {
//
//        if (node == null) {
//            // Base case
//            return;
//        }
//
//        print(node.left);
//        for (T elem : node.elements) { System.out.println(elem); }
//        print(node.right);
//
//    }
//
//    int getNumNodes() {
//        return numNodes;
//    }
//
//    int getNumElements() {
//        return numElements;
//    }
//
//    long getMax() {
//        return root.max;
//    }
//
//
//    //endregion
//
//
//}





package reductor;

import java.util.*;


/*
This is an implementation of an interval tree, mostly following the approach described in CLR, where each
node is keyed on a single interval, stores its associated data as a List, and also stores the max right endpoint
of the subtree rooted at that node (which allows, using the interval trichotomy, for ignoring the left subtree
of certain nodes at times, leading to logN/binary searches.

The only exposed functionality for this tree besides construction, which can only be done once, is query, as
this meets the needs of the program.

The purpose of the tree is to store objects representing different musical elements (e.g. notes, chords,
measures, etc.). We will use notes (which in this program are represented by the Note class, which extends
both Range and Comparable<Note>) as an example of the unique challenges of implementing this data structure
in a useful way.

Additional context:
1.The Ranged interface looks like this:

public interface Ranged {
    Range range(); // this is merely a getter for an object's Range field
}

2. All Range objects represent a musical element's start and end values, in terms of MIDI ticks:

A range representing [0,480] in turn represents a musical element whose starting/ON tick is at 0 and
ending/OFF tick is at 480.

3. The tree is not self-balancing because the use cases for this tree do not involve addition or removal;
they always involve construction of a tree from prepared and static data.

Unique challenges:

1. While we can safely ignore/prevent exact duplicates (say two Note objects both representing identical
ranges and pitches), Notes with different pitches but identical ranges should all be stored in the tree.
Therefore: C4 [0,480], E4 [0,480], and G4 [0,480] should all be added to the list at the Node for [0,480]; i
.e., the Node at [0,480] contains the notes C4, E4, and G4. There should never be duplicates in the list
at the Node, since those would be cases of notes with exactly equal ranges *and* pitches (which are both
not allowed on the same channel in the MIDI spec, in addition to be problematic for this program).

2. Construction using the recursive list/median approach is further complicated by the fact that many
duplicate ranges may exist in a list of otherwise perfectly distinct elements. This causes clustering
that throws off the median calculation. The approach that I took to solve this is to construct the tree
from a Set of unique ranges represented in the List, creating a skeleton or scaffold of sorts, after
which each element is actually added. There is probably a way to do this all in one loop, but in terms of
clarity and maintainability, the former approach is better as the latter would be rather complicated and
verbose.

3. When querying (which is usually done in a sequential, "chunking" manner, notes that have long durations
whilst many other notes occur should not be added to a list of targets every single query; in other words,
a query window spanning the entire length of a piece which consists of 1 tied, lengthy note spanning the
entire length of the piece with 1000 other notes occurring at various points in the piece should still
only return a list of targets/matches of size 1001.

This implementation currently controls for exact duplicates using List.contains(), which is not optimal, but
I thought it better than enforcing the argument given to the constructor to be a Set, which would put more
data manipulation responsibility on classes not necessarily suited for data manipulation tasks.

I also experimented with using HashSets and TreeSets for the container in both Node.elements and the "matches"
container of the query() function. The problems were:
+ HashSet is disastrous if elements are anything but perfectly immutable. While Range() objects are
themselves immutable, the objects that implement Ranged (such as Note and Chord) are *not*. The purpose
of this program at this point largely revolves around mutation of musical elements. At some point in the
future, this may be controlled for by constructing entirely new structures, but for now, I could not risk
using hashCode() with mutable objects.
+ TreeSet uses an object's compareTo(). While all objects that implement Ranged, coincidentally, also
implement Comparable, their respective compareTo() methods are *not* always involving their Range fields.
For instance, Note objects compare by pitch, not by range. This led to very confusing test results before
I realized what the problem was (notes with the same pitch were not being added to the Set because their
compareTo() determined they were equal).
*/


// todo the construction time complexity analysis is not accurate given the current implementation
/**
 * An interval tree (augmented BST) that can contain any object with a {@link reductor.Range}
 * (i.e., implements the {@link reductor.Ranged} interface), e.g. {@link reductor.Note}, {@link reductor.Chord}, etc.
 * <p>
 * This implementation follows that which is described in CLR:
 * <ul>
 *     <li> Construction in O(NlogN) time </li>
 *     <li> Queries in O(logN + m) time </li>
 * </ul>
 * <p>
 * Since the purpose of this tree within the context of this application is to provide efficient
 * look-up, it only provides query functionality, and can only be constructed once, from
 * fixed data.
 */
public class IntervalTree<T extends Ranged> {


    /*
        Natural ordering of T.range():

            Primary ordering:              by low value
            Secondary ordering:            by high value
            Ts with duplicate ranges:      allowed as long as associated data is different (using equals())

        Within context of tree insert/query operations:

                        Range of        Interval
                      current node:      to add:          Action:

            Case 1.1:   [10,20]          [10,15]          Go left
            Case 1.2:   [10,20]          [5,20]           Go left

            Case 2:     [10,20]          [10,20]          Add to Current Node if not !equals()

            Case 3.1:   [10,20]          [10,25]          Go right
            Case 3.2:   [10,20]          [15,20]          Go right
    */


    //region <Node>


    /// A binary node which represents a range/interval and stores a list of elements
    class Node implements Ranged { // todo make private again and members

        /// The {@link reductor.Range} (i.e., interval) this node represents
        private Range range;

        /// Max endpoint in subtree rooted at this node (used to ignore left subtrees during queries)
        private long max;

        /// This node's data (a Set of elements with the same range but possibly differing pitches)
        List<T> elements;

        /// This node's left child
        Node left;

        /// This node's right child
        Node right;

        /// Flag used during queries
        boolean queried;

        /// Primary constructor which takes a {@link reductor.Range}
        Node(Range range) {
            this.range = new Range(range);
            this.max = -1;
            this.left = null;
            this.right = null;
            this.elements = new ArrayList<>();
            this.queried = false;
        }


        /// Adds an element to this node's list
        boolean add(T elem) {

            assert elem.getRange().equals(this.getRange());

            if (this.elements.contains(elem)) {
                return false;
            }

            return this.elements.add(elem);

        }

        @Override
        public String toString() {

            String str = this.range + "->{";

            for (T note : this.elements) {
                str += note + ",";
            }

            str = str.substring(0, str.length() - 1); // get last comma off

            // looks like: "[240,480]->{C4,E4,G4}"
            return str + "}";

        }

        @Override
        public Range getRange() {
            return new Range(this.range);
        }

        @Override
        public long start() {
            return this.range.getLow();
        }

        @Override
        public void setRange(Range range) {
            this.range = range;
        }

        /// The number of elements this node holds
        int size() {
            return this.elements.size();
        }

        public long getMax() {
            return this.max;
        }


    }


    //endregion


    /// Root of this interval tree
    Node root; // todo should be private, but hard to test if so

    /// Size (number of nodes)
    private int numNodes;

    /// Size (number of elements)
    private int numElements;

    Stack<Node> queriedNodes = new Stack<>();

    /// Primary constructor
    IntervalTree(ArrayList<T> elements) {

        if (elements == null) {
            throw new NullPointerException("whatever was passed to IntervalTree constructor was null");
        }

        numNodes = 0;
        numElements = 0;

        // Get a "set" of the ranges of the elements
        ArrayList<Range> uniqueRanges = getUniqueRanges(elements);

        // Ensure the list is sorted before doing median-based recursive construction
        uniqueRanges.sort(null);

        // Build the outline/structure/skeleton of the tree
        root = buildSkeleton(uniqueRanges);

        // Add the actual elements to the nodes' lists
        addAll(elements);

    }


    //region <Build>


    /// Adds ranges of elements to Set to remove duplicate ranges, and gives back (sorted) List
    private ArrayList<Range> getUniqueRanges(ArrayList<T> elements) {

        HashSet<Range> rangesSet = new HashSet<>();

        // Remove duplicates
        for (T elem : elements) {
            rangesSet.add(elem.getRange());
        }

        // Convert back to List
        return new ArrayList<>(rangesSet);
    }

    /// Recursively constructs the structure of the tree; all nodes are still empty when this function returns
    private Node buildSkeleton(ArrayList<Range> ranges) {
        return buildSkeleton(ranges, 0, ranges.size() - 1);
    }

    private Node buildSkeleton(ArrayList<Range> ranges, int firstIndex, int lastIndex) {

        // e.g., firstIndex: 25; lastIndex: 24
        if (firstIndex > lastIndex) {
            // Base case (return a null child)
            return null;
        }

        // e.g., 0 to 100 --> 50 (upper median)
        int middleIndex = (firstIndex + lastIndex) / 2;

        // e.g., add 50 here (median of subtree rooted at this node)
        Node node = new Node(ranges.get(middleIndex));
        numNodes++;

        // e.g.,start left subtree with 0 to 49
        node.left = buildSkeleton(ranges, firstIndex, middleIndex - 1);

        // e.g.,start right subtree with 51 to 100
        node.right = buildSkeleton(ranges, middleIndex + 1, lastIndex);

        // return the actual node you just created here once all of the functions you called have returned
        return node;

    }

    private void addAll(ArrayList<T> elements) {
        for (T elem : elements) {
            add(root, elem);
        }
    }

    private void add(Node node, T elem) {

        // Update node.max if elem to be added will set the new max right endpoint of elems in subtree rooted at
        // this node
        if (node.max < elem.getRange().getHigh()) {
            node.max = elem.getRange().getHigh();
        }

        if (elem.getRange().compareTo(node.getRange()) < 0) {
            if (node.left != null) {
                add(node.left, elem);
            }
        } else if (elem.getRange().compareTo(node.getRange()) > 0) {
            if (node.right != null) {
                add(node.right, elem);
            }
        } else {
            // Only increment element count if node added elem (exact duplicates are not added)
            if (node.add(elem)) {
                numElements++;
            }
        }

    }


    //endregion


    //region <Query>


    // TODO:
    // You know I am starting to think that the duplicate note thing might actually be better implemented in the
    // query function after all with flags. And it wouldn't be all that bad.
    //     + Add every Node you touch to a STACK and set its queried flag to true.
    //     + At the end of the query in IntervalTree, just pop everything out of the stack and set them all back to
    //     false.
    //     + I think this would make the code so much easier to maintain and simpler, AND it wouldn't really be a
    //     performance thing in the end when compared to how you're doing things now.
    //     + jake: double check that the queried flag can stay with Node and not have to be in Note, because that
    //     WOULD be a considerable design drawback (don't want a member in Note that has to do with whether or not it
    //     was queried during a method call).


    ArrayList<T> query(Range window) {

        if (root == null) {
            return new ArrayList<>();
        }

        if (window == null) {
            throw new NullPointerException("null Range was passed to IntervalTree#query");
        }

        ArrayList<T> matches = new ArrayList<>();

        query(root, window, matches);

        while(!this.queriedNodes.isEmpty()) {
            queriedNodes.pop().queried = false;
        }

        return new ArrayList<>(matches);

    }

    private ArrayList<T> query(Node node, Range window, ArrayList<T> matches) {

        if (!node.queried
                && window.overlaps(node.getRange())) {
            matches.addAll(node.elements);
            node.queried = true;
            queriedNodes.push(node);
        }

        if (node.left == null && node.right == null) {
            return matches;
        }

        if (node.left != null && window.getLow() <= node.left.getMax()) {
            query(node.left, window, matches);
        }

        //if (node.right != null) {
        if (node.right != null && window.getHigh() >= node.getRange().getLow()) {
            query(node.right, window, matches);
        }

        return matches;

    }


    //endregion


    //region <Miscellaneous>


    /// Returns the tree as an ArrayList, using in-order traversal
    public ArrayList<T> toList() {
        ArrayList<T> inOrderList = new ArrayList<>();
        toList(root, inOrderList);
        return inOrderList;
    }

    private void toList(Node node, ArrayList<T> inOrderList) {

        if (node == null) {
            // Base case
            return;
        }

        toList(node.left, inOrderList);
        inOrderList.addAll(node.elements);
        toList(node.right, inOrderList);

    }

    public void print() {
        print(root);
    }

    private void print(Node node) {

        if (node == null) {
            // Base case
            return;
        }

        print(node.left);
        for (T elem : node.elements) { System.out.println(elem); }
        print(node.right);

    }

    int getNumNodes() {
        return numNodes;
    }

    int getNumElements() {
        return numElements;
    }

    long getMax() {
        return root.max;
    }


    //endregion


}