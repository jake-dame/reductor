package reductor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;


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
 * It intends to provide: insert and query only
 */
public class IntervalTree {


    /// A binary node which represents a range/interval and stores a list of notes
    private static class Node {

        /// The {@link reductor.Range} (interval) this node represents
        Range range;

        /// Max endpoint in subtree rooted at this node (used to ignore left pathways)
        long max;

        /// This node's data (a Set of notes with the same range but differing pitches)
        Set<Note> notes;

        /// This node's left child
        Node left;

        /// This node's right child
        Node right;

        /// This node's parent
        Node parent;

        /// Primary constructor which takes a {@link reductor.Note}
        Node(Note note) {

            this.range = new Range(note.range());

            this.max = this.range.high();

            this.left = null;
            this.right = null;

            this.notes = new HashSet<>();

        }

        /// The number of notes this node holds
        int size() { return notes.size(); }

        /// Adds a note to this node
        boolean add(Note note) { return notes.add(note); }

    }


    /// Root of this tree
    Node root;

    /// Size (number of nodes)
    int size;


    /// Default constructor
    IntervalTree() {
        size = 0;
        root = null;
    }


    boolean addAll(ArrayList<Note> notes) {

        if (root == null
                || notes == null
                || notes.isEmpty()) {
            return false;
        }

        boolean changed = false;

        for (Note note : notes) {
            changed = add(note);
        }

        return changed;

    }


    boolean add(Note note) {

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
            return true;
        }

        if (node.max < target.high()) {
            // Update the max at every node visited, if applicable
            node.max = target.high();
        }

        // Target already exists, and note should be added to the node
        if (node.range == note.range()) {
            node.add(note);
            return true;
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
        node.left.parent = node;
        return true;
    }

    private boolean appendRightChild(Node node, Note note) {
        node.right = new Node(note);
        size++;
        node.right.parent = node;
        return true;
    }


    ArrayList<Note> query (Range query) {

        ArrayList<Note> matches = new ArrayList<>();

        if (root == null  ||  query == null) {
            return matches;
        }

        /// This is application specific...
        Range window = new Range(query.low(), query.high() - 1);

        return query(root, window, matches);

    }


    private ArrayList<Note> query (Node node, Range window, ArrayList<Note> matches) {

        if(window.overlaps(node.range)) {
            matches.addAll(node.notes);
        }

        if(node.left == null  &&  node.right == null) {
            // Base case: any matches that should have been added to the list are in the list, and we can go no further
            return matches;
        }

        if (node.left != null  &&  window.low() <= node.left.max) {
            // Use max of left subtree to ignore left subtree
            query(node.left, window, matches);
        }

        if (node.right != null  &&  window.compareTo(node.range) >= 0) {
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


    ///// Driver for remove
    //boolean remove(Note note) {
    //
    //    if (root == null) {
    //        return false;
    //    }
    //
    //    if(note == null)  {
    //        return false;
    //    }
    //
    //    return remove(root, note);
    //
    //}
    //
    //private boolean remove(Node node, Note note) {
    //
    //    if (interval.compareTo(node.interval) == 0) {
    //        if (node.contains(interval)) {
    //            if (node.notes.size() == 1) {
    //                return removeNode(node);
    //            } else {
    //                node.removeFromList(interval);
    //                elements--;
    //                return true;
    //            }
    //        } else {
    //            return false;
    //        }
    //    }
    //    else if (interval.compareTo(node.interval) < 0) {
    //        if (node.left == null) {
    //            return false;
    //        }
    //
    //        return remove(node.left, interval);
    //    }
    //    else {
    //        if (node.right == null) {
    //            return false;
    //        }
    //
    //        return remove(node.right, interval);
    //    }
    //}
    //
    //
    ///// Helper for remove
    //private boolean removeNode(Node node) {
    //
    //    // Handle removal of last node.
    //    if (size == 1) {
    //        root = null;
    //        size--;
    //        elements--;
    //        return true;
    //    }
    //
    //    // Node to remove has 0 children.
    //    if ( node.right == null  &&  node.left == null ) {
    //        if (node.interval.compareTo(node.parent.interval) < 0) {
    //            node.parent.left = null;
    //        } else {
    //            node.parent.right = null;
    //        }
    //        size--;
    //        elements--;
    //        return true;
    //    }
    //
    //    // Node to remove has only a left child.
    //    else if ( node.left != null  &&  node.right == null ) {
    //        node.left.parent = node.parent;
    //        if (node.parent != null  &&  node.interval.compareTo(node.parent.interval) < 0) {
    //            node.parent.left = node.left;
    //        }
    //        else if(node.parent != null  &&  node.interval.compareTo(node.parent.interval) > 0) {
    //            node.parent.right = node.left;
    //        }
    //        else {
    //            root = node.left;
    //        }
    //        size--;
    //        elements--;
    //        return true;
    //    }
    //
    //    // Node to remove has only a right child.
    //    else if ( node.left == null ) {
    //        node.right.parent = node.parent;
    //        if(node.parent != null  &&  node.interval.compareTo(node.parent.interval) < 0){
    //            node.parent.left = node.right;
    //        }
    //        else if(node.parent != null  &&  node.interval.compareTo(node.parent.interval) > 0) {
    //            node.parent.right = node.right;
    //        }
    //        else {
    //            root = node.right;
    //        }
    //        size--;
    //        elements--;
    //        return true;
    //    }
    //
    //    // Node to remove has 2 children.
    //    else {
    //        if (node.left.right != null) {
    //            Node predecessor = findMax(node.left);
    //            Interval interval = predecessor.interval;
    //            ArrayList<Interval> list = predecessor.notes;
    //            remove(predecessor.interval);
    //            node.notes = list;
    //            node.interval = interval;
    //        }
    //        else if (node.right.left != null) {
    //            Node successor = findMin(node.right);
    //            Interval interval = successor.interval;
    //            ArrayList<Interval> list = successor.notes;
    //            remove(successor.interval);
    //            node.notes = list;
    //            node.interval = interval;
    //        }
    //        else {
    //            Interval interval = node.left.interval;
    //            ArrayList<Interval> list = node.left.notes;
    //            remove(node.left.interval);
    //            node.notes = list;
    //            node.interval = interval;
    //        }
    //    }
    //
    //    return true;
    //}
    //
    //
    //boolean removeAll(ArrayList<Interval> intervals) {
    //
    //    if (root == null) {
    //        return false;
    //    }
    //
    //    if (intervals == null) {
    //        return false;
    //    }
    //
    //    boolean changed = false;
    //
    //    for (Interval interval : intervals) {
    //        changed = remove(interval);
    //    }
    //
    //    return changed;
    //
    //}
    //
    //
    ///// Helper for remove
    //private Node findMax(Node node) {
    //
    //    if (node.right == null) {
    //        return node;
    //    }
    //
    //    return findMax(node.right);
    //
    //}
    //
    //
    ///// Helper for remove
    //private Node findMin(Node node) {
    //
    //    if (node.left == null) {
    //        return node;
    //    }
    //
    //    return findMin(node.left);
    //
    //}
    //
    //
    //
    //private Node search(Node node, Range range) {
    //
    //    while (true) {
    //
    //        // Target is found
    //        if (node.range.equals(range)) {
    //            return node;
    //        }
    //
    //        // Target is greater...
    //        if (range.compareTo(node.range) > 0) {
    //            if (node.right == null)  return node;
    //            search(node.right, range);
    //        }
    //
    //        // Target is less...
    //        if (range.compareTo(node.range) < 0) {
    //            if (node.left == null)  return node;
    //            search(node.left, range);
    //        }
    //
    //    }
    //
    //}


}