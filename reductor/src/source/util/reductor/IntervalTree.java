package reductor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;


    /*

    This tree stores `Note` objects.

    `Note` objects represented a unison of two corresponding MIDI Note On/Off events, and have three
    important pieces of data represented by two member fields - start tick, end tick, and pitch, represented
    by the range (type Range - just an interval object) and pitch (int) fields.

    The unique challenges of this tree are:

    1. Although there will be no exact duplicates (MIDI spec does not allow exact duplicate events),
    there will be a handful of notes with duplicate ranges (different pitches, but occurring at the same time),
    such as in a chord: C-E-G-Bb-C may be represented by 5 different Note objects, but have identical ranges,
    such as [0,480], because they are played, say, by a pianist as a chord in the left hand.

    2. Depending on the thickness of the textures in the piece (a Bach violin sonata vs. a Mahler symphony),
    storing every single Note object in its own Node could increase the height of the tree in a not-insignificant
    way. Therefore, Note objects with identical ranges are stored in a list at each node.

    3. Constructing the tree to be balanced is also challenging due to duplicate clustering. For instance
    (using integers), a list might look like this:

        011111234567  // len 12
              ^
           (upper) median

     So if I used 2 to split my subtree, the tree would look like this:


                          2
                      1*      5
                    0       3   6
                           2 4   7


           *Five 1's are stored in the list here


     For some MIDI sequences, the number of Note objects can easily be in the tens of thousands.

     Although when compared to the numbers heavy-duty programs deal in this is not really that big of a deal,
     I still wanted as balanced a tree as possible.

     A "scaffolding" of this tree is constructed with a Set of ranges derived from the original list of notes.
     This allows for accurate median selection to build the tree so that it is keyed on these ranges and balanced.
     After the scaffolding is constructed, the notes themselves are added using a basic query algorithm for
     intervals which takes lgN time.
    */


    /*

        Primary ordering:              by low value
        Secondary ordering:            by high value
        Notes with duplicate ranges:   allowed if data associated with range is different

                   Range of          Interval
                  current node:      to add:          Action:

        Case 1.1:   [10,20]          [10,15]          Look left
        Case 1.2:   [10,20]          [5,20]           Look left
        Case 2:     [10,20]          [10,20]          Add to Current Node
        Case 3.1:   [10,20]          [10,25]          Look right
        Case 3.2:   [10,20]          [15,20]          Look right

    */

/**
 * In implementation of an interval tree that sorts {@link reductor.Note}s
 * (objects constructed from MIDI events which contain, in part, a start/stop or low/high).
 * <p>
 * This is an augmented BST and follows the implementation described in CLR. Comparisons for addition
 * use a range's natural ordering (primary: low value; secondary: high value); queries are accomplished
 * in O(logN + k) time by ignoring subtrees where the max high value does not overlap the query, and where
 * k is the number of notes that match the query.
 * <p>
 * Since the purpose of this tree, within the context of this application, is to provide efficient
 * look-up, it only provides query() functionality, and can only be constructed once, from
 * fixed data.
 */
public class IntervalTree {


    /// A binary node which represents a range/interval and stores a list of notes
    private static class Node {

        /// The {@link reductor.Range} (interval) this node represents
        Range range;

        /// Max endpoint in subtree rooted at this node (used to ignore left pathways)
        long max;

        /// This node's data (a Set of notes with the same range but differing pitches)
        List<Note> notes;

        /// This node's left child
        Node left;

        /// This node's right child
        Node right;


        /// Primary constructor which takes a {@link reductor.Note}
        Node(Range range) {

            this.range = new Range(range);

            this.max = this.range.high();

            this.left = null;
            this.right = null;

            this.notes = new ArrayList<>();

        }

        // Mostly for debugging
        /// The number of notes this node holds
        int size() {

            return notes.size();

        }

        /// Adds a note to this node
        boolean add(Note note) {

            // This just sorts by pitch... not important but makes debugging easier
            int insertionIndex = Collections.binarySearch(notes, note);

            if (insertionIndex < 0) {
                insertionIndex = -(insertionIndex + 1);
            }

            if (notes.contains(note)) {
                return false;
            }

            notes.add(insertionIndex, note);

            return true;

        }


        @Override
        public String toString() {
            return range.toString() + "(" + size() + ")";
        }


    }


    /// Root of this tree
    Node root;

    /// Size (number of nodes)
    int numNodes;

    /// Size (number of elements)
    int numElements;


    /// Primary constructor
    IntervalTree(ArrayList<Note> notes) {

        assert notes != null  &&  !notes.isEmpty();

        numNodes = 0;
        root = buildScaffold(notes);
        addAll(notes);

    }


    private Node buildScaffold(ArrayList<Note> notes) {

        ArrayList<Range> ranges = getUniqueRanges(notes);

        ranges.sort(null);

        return addScaffold(ranges, 0, ranges.size() - 1);

    }


    private Node addScaffold(ArrayList<Range> ranges, int start, int end) {

        if (start > end) {
            // Base case (this will effectually create leaf nodes by assigning the caller's left/right children as null)
            return null;
        }

        // 0 to 100 --> 50
        int middleIndex = (start + end) / 2;

        // add 50 here
        Node node = new Node( ranges.get(middleIndex) );
        numNodes++;

        // start left subtree with 0 to 49
        node.left = addScaffold(ranges, start, middleIndex - 1);

        // start right subtree with 51 to 100
        node.right = addScaffold(ranges, middleIndex + 1, end);

        // return the actual node you just created here once all of the functions you called have returned
        return node;

    }


    private static ArrayList<Range> getUniqueRanges(ArrayList<Note> notes) {

        HashSet<Range> ranges = new HashSet<>();

        for (Note note : notes) {
            ranges.add(note.range());
        }

        return new ArrayList<>(ranges);

    }


    private void addAll(ArrayList<Note> notes) {

        for (Note note : notes) {
            add(root, note);
        }

    }


    private void add(Node node, Note note) {

        if (node.range.equals(note.range())) {
            if (node.add(note)) {
                numElements++;
            }
        }

        if(node.left == null  &&  node.right == null) {
            // Base case
            return;
        }

        if (node.left != null  &&  note.range().low() <= node.left.max) {
            // Use max of left subtree to ignore left subtree
            add(node.left, note);
        }

        if (node.right != null) {
            add(node.right, note);
        }

    }


    ArrayList<Note> query (Range window) {

        assert root != null  &&  window != null;

        ArrayList<Note> matches = new ArrayList<>();

        return query(root, window, matches);

    }


    private ArrayList<Note> query (Node node, Range window, ArrayList<Note> matches) {

        if(window.overlaps(node.range)) {
            matches.addAll(node.notes);
        }

        if(node.left == null  &&  node.right == null) {
            return matches;
        }

        if (node.left != null  &&  window.low() <= node.left.max) {
            query(node.left, window, matches);
        }

        if (node.right != null) {
            query(node.right, window, matches);
        }

        return matches;

    }


    /// Returns the tree as an ArrayList, using in-order traversal
    ArrayList<Note> toList() {

        ArrayList<Note> inOrderList = new ArrayList<>();

        toList(root, inOrderList);

        return inOrderList;

    }


    private void toList(Node node, ArrayList<Note> inOrderList) {

        if ( node == null ) {
            return;
        }

        toList(node.left, inOrderList);

        inOrderList.addAll(node.notes);

        toList(node.right, inOrderList);

    }


    int numNodes() {

        return numNodes;

    }


    int numElements() {

        return numElements;

    }


    void print() {

        print(root);

    }


    private void print(Node node) {

        if (node.left != null)  {
            print(node.left);
        }

        for(Note note : node.notes) {
            System.out.println(note);
        }

        if (node.right != null) {
            print(node.right);
        }

    }


}