package reductor;

import java.util.*;


    /*
     To deal with the many expected cases where `Interval` objects will have identical ranges but
     different associated data, such as: [0,480] + data1; [0,480] + data2; etc., this list will
     store such cases, rather than try and add them as separate nodes, which would greatly
     increase the height of the tree. Also, when searching for overlapping intervals, I can
     just return the entire list, since they will all have the same range.
     There will be no exact duplicates within the scope of this application.
    */


    /*

        Primary ordering:              by low value
        Secondary ordering:            by high value
        Notes with duplicate ranges:   allowed if data is different

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
 * look-up, it only provides addAll() and query() functionality, and can only be constructed once, from
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

        /// This node's parent
        Node parent;

        //experiment
        boolean queried;
        //end experiment

        /// Primary constructor which takes a {@link reductor.Note}
        Node(Note note) {

            this.range = new Range(note.range());

            this.max = this.range.high();

            this.left = null;
            this.right = null;

            this.notes = new ArrayList<>();

            add(note);

            // experiment
            this.queried = false;
            //end experiment

        }

        /// The number of notes this node holds
        int size() {

            return notes.size();

        }

        /// Adds a note to this node
        boolean add(Note note) {

            int index = Collections.binarySearch(notes, note);
            if (index < 0) {
                index = -(index + 1);
            }

            if (!notes.contains(note)) {
                notes.add(index, note);
                return true;
            }

            return false;

        }

    }


    /// Root of this tree
    Node root;

    /// Size (number of nodes)
    int size;

    /// Size (number of elements)
    int numNotes;


    /// Primary constructor
    IntervalTree(ArrayList<Note> notes) {
        size = 0;
        root = null;
        addAll(notes);
    }


    boolean addAll(ArrayList<Note> notes) {

        if (notes == null  ||  notes.isEmpty()) {
            return false;
        }

        boolean changed = false;

        for (Note note : notes) {
            changed = add(note);
        }

        return changed;

    }


    private boolean add(Note note) {

        if (note == null) {
            return false;
        }

        return addRecurse(root, note);
    }


    private boolean addRecurse(Node node, Note note) {

        Range target = note.range();

        // Handle: first addition
        if (root == null) {
            root = new Node(note);
            size++;
            numNotes++;
            return true;
        }

        if (node.max < target.high()) {
            // Update the max at every node visited, if applicable
            node.max = target.high();
        }

        // 7680 and 8160
        // Target already exists, and note should be added to the node
        if (node.range.equals(note.range())) {

            boolean added = node.add(note);

            if (added) {
                numNotes++;
                return true;
            }

            return false;

        }

        if (target.compareTo(node.range) < 0) {

            if (node.left == null) {
                return appendLeftChild(node, note);
            }

            return addRecurse(node.left, note);

        } else {

            if (node.right == null) {
                return appendRightChild(node, note);
            }

            return addRecurse(node.right, note);

        }

    }


    private boolean appendLeftChild(Node node, Note note) {
        node.left = new Node(note);
        size++;
        numNotes++;
        node.left.parent = node;
        return true;
    }


    private boolean appendRightChild(Node node, Note note) {
        node.right = new Node(note);
        size++;
        numNotes++;
        node.right.parent = node;
        return true;
    }


    ArrayList<Note> query (Range window) {

        ArrayList<Note> matches = new ArrayList<>();

        if (root == null  ||  window == null) {
            return matches;
        }

        return query(root, window, matches);

    }


    private ArrayList<Note> query (Node node, Range window, ArrayList<Note> matches) {

        if(window.overlaps(node.range)) {
            matches.addAll(node.notes);
            node.queried = true;
        }

        if(node.left == null  &&  node.right == null) {
            // Base case: any matches that should have been added to the list are in the list, and we can go no further
            return matches;
        }

        if (node.left != null  &&  window.low() <= node.left.max) {
            // Use max of left subtree to ignore left subtree
            query(node.left, window, matches);
        }

        if (node.right != null) {
            // Else, use the natural ordering and traverse the right subtree
            query(node.right, window, matches);
        }

        return matches;

    }


    /// Returns the tree as an ArrayList, using in-order traversal
    ArrayList<Note> toArrayList() {

        ArrayList<Note> inOrderList = new ArrayList<>();

        toArrayList(root, inOrderList);

        return inOrderList;

    }

    private void toArrayList(Node node, ArrayList<Note> inOrderList) {

        if ( node == null ) {
            return;
        }

        toArrayList(node.left, inOrderList);

        // It doesn't matter that these aren't ordered by pitch
        // They all share the same range, and that is the ordering that matters
        // for this tree
        inOrderList.addAll(node.notes);

        toArrayList(node.right, inOrderList);

    }

    int size() {

        return size;

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