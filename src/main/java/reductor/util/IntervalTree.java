package reductor.util;

import reductor.core.Range;
import reductor.core.Ranged;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;


/*
 An interval tree that can contain any object which exposes a Range somehow.
   - Construction in O(NlogN) time
   - Queries in O(logN + m) time
   - No insertion or removal;tree is constructed once, from fixed data
*/

/*
Natural ordering of T => T.getRange():

    Primary ordering:              by low value
    Secondary ordering:            by high value
    Elems with duplicate ranges:   allowed as long as associated data is different (using equals())

Within context of tree insert/query operations:

                Range of        Interval
              current node:      to add:          Action:

    Case 1.1:   [10,20]          [10,15]          Go left
    Case 1.2:   [10,20]          [5,20]           Go left

    Case 2:     [10,20]          [10,20]          Add to current node if !equals()

    Case 3.1:   [10,20]          [10,25]          Go right
    Case 3.2:   [10,20]          [15,20]          Go right
*/


public class IntervalTree<T extends Ranged> {

    // Root of this interval tree
    private final Node root;
    // Size (in nodes)
    private int sizeNodes;
    // Size (in elements)
    private int sizeElements;

    //Stack<Node> queriedNodes = new Stack<>(); // TODO


    public IntervalTree(List<T> elements) {

        if (elements == null) { throw new NullPointerException(""); }

        sizeNodes = 0;
        sizeElements = 0;

        // Represent each range in input List just once
        ArrayList<Range> uniqueRanges = getUniqueRanges(elements);

        // Ensure the list is sorted before doing median-based recursive construction
        uniqueRanges.sort(null);

        // Build the outline/structure/skeleton of the tree
        root = buildScaffolding(uniqueRanges);

        // Add the actual elements to the nodes' lists
        addAll(elements);
    }

    // dev
    public IntervalTree() { this.root = null; }
    // dev
    public Node getRoot() { return this.root; }


    //region construction helpers

    // List -> Set -> List to remove duplicates and get Range members only
    private ArrayList<Range> getUniqueRanges(List<T> elements) {
        HashSet<Range> rangesOnly = new HashSet<>();
        for (T elem : elements) {
            rangesOnly.add(elem.getRange());
        }
        return new ArrayList<>(rangesOnly);
    }

    // Driver that recursively constructs the structure of the tree; all nodes are still empty when this function returns
    private Node buildScaffolding(ArrayList<Range> ranges) {
        return buildScaffolding(ranges, 0, ranges.size() - 1);
    }

    private Node buildScaffolding(ArrayList<Range> ranges, int firstIndex, int lastIndex) {

        // e.g., firstIndex: 25; lastIndex: 24
        if (firstIndex > lastIndex) {
            // Base case (return a null child)
            return null;
        }

        // e.g., 0 to 100 --> 50 (upper median)
        int middleIndex = (firstIndex + lastIndex) / 2;

        // e.g., add 50 here (median of subtree rooted at this node)
        Node node = new Node(ranges.get(middleIndex));
        sizeNodes++;

        // e.g.,start left subtree with 0 to 49
        node.left = buildScaffolding(ranges, firstIndex, middleIndex - 1);

        // e.g.,start right subtree with 51 to 100
        node.right = buildScaffolding(ranges, middleIndex + 1, lastIndex);

        // return the actual node you just created here once all of the functions you called have returned
        return node;

    }

    // convenience/driver
    private void addAll(List<T> elements) {
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
                sizeElements++;
            }
        }

    }

    //endregion


    //region query methods

    public List<T> query(int point) {

        if (point < 0  ||  isEmpty()) { return new ArrayList<>(); }

        List<T> matches = new ArrayList<>();
        query(root, point, matches);
        var out = new ArrayList<>(matches);
        out.sort(null);
        return out;
    }

    private List<T> query(Node node, int point, List<T> matches) {

        // Target hit.
        if (node.getRange().contains(point)) { matches.addAll(node.elements); }

        // Base case.
        if (node.left == null  &&  node.right == null) { return matches; }

        // Look left & right.
        if (node.left != null  &&  point <= node.left.getMax()) { query(node.left, point, matches); }
        if (node.right != null  &&  point >= node.getRange().getLow()) { query(node.right, point, matches); }

        // Continue to search
        return matches;
    }


    public List<T> query(Range window) {

        if (window == null) { throw new NullPointerException(); }
        if (isEmpty()) { return new ArrayList<>(); }

        List<T> matches = new ArrayList<>();
        query(root, window, matches);
        return new ArrayList<>(matches);
    }

    private List<T> query(Node node, Range window, List<T> matches) {

        // Target hit.
        if (window.overlaps(node.getRange())) { matches.addAll(node.elements); }

        // Base case.
        if (node.left == null  &&  node.right == null) { return matches; }

        // Look left & right.
        if (node.left != null  &&  window.getLow() <= node.left.getMax()) {
            query(node.left, window, matches);
        }

        if (node.right != null  &&  window.getHigh() >= node.getRange().getLow()) {
            query(node.right, window, matches);
        }

        // Continue to search
        return matches;
    }

    //endregion


    //region getters

    /**
     * @return The tree as List, in order.
     */
    public List<T> toList() {
        List<T> inOrderList = new ArrayList<>();
        toList(root, inOrderList);
        return inOrderList;
    }

    private void toList(Node node, List<T> inOrderList) {
        if (node == null) { return; }
        toList(node.left, inOrderList);
        inOrderList.addAll(node.elements);
        toList(node.right, inOrderList);
    }

    /**
     * @return An in-order list of each node by its key (i.e. range/interval).
     */
    public List<Range> toListRangesOnly() {
        List<Range> ranges = new ArrayList<>();
        toListRangesOnly(root, ranges);
        return ranges;
    }

    private void toListRangesOnly(Node node, List<Range> ranges) {
        if (node == null) { return; }
        toListRangesOnly(node.left, ranges);
        ranges.add(node.getRange());
        toListRangesOnly(node.right, ranges);
    }

    /**
     * Prints the tree in order. Each node appears like "[0,480] -> {  C4  E4  G4  }"
     * @see Node#toString
     */
    public void print() { print(root); }

    private void print(Node node) {
        if (node == null) { return; }
        print(node.left);
        System.out.println(node);
        print(node.right);
    }

    /*
     * There is no size() method for this data structure because that could mean two very different things.
     * Explicitly named size-related getters are provided instead.
     */

    /**
     * @return True if this tree is empty.
     */
    public boolean isEmpty() {
        return this.root == null;
    }

    /**
     * @return The number of nodes in this tree.
     */
    public int getSizeNodes() {
        return sizeNodes;
    }

    /**
     * @return The total number of elements stored at all nodes in this tree.
     */
    public int getSizeElements() {
        return sizeElements;
    }

    /* There is some redundancy in the following methods,
    but they each suit my needs for specific purposes, for now. */

    public T getFirstElement() {
        Node node = this.root;
        while(node.left != null) { node = node.left; }
        return node.elements.getFirst();
    }

    public T getLastElement() {
        Node node = this.root;
        while(node.right != null) { node = node.right; }
        return node.elements.getLast();
    }

    /**
     * @return The range of the first sounding note.
     * @see IntervalTree#getFirstTick
     */
    public Range getMinNodeRange() {
        Node node = this.root;
        while(node.left != null) { node = node.left; }
        return node.getRange();
    }

    /**
     * @return The range of the last sounding note.
     * @see IntervalTree#getLastTick
     */
    public Range getMaxNodeRange() {
        Node node = this.root;
        while(node.right != null) { node = node.right; }
        return node.getRange();
    }

    /**
     * @return The first tick in the tree. Equivalent to calling {@link Range#getLow()} on {@link IntervalTree#getMinNodeRange()}.
     */
    public long getFirstTick() {
        return this.getMinNodeRange().getLow();
    }

    /**
     * This does not necessarily correspond to calling {@link Range#getHigh()} on {@link IntervalTree#getMaxNodeRange()} due to the
     * fact that the tree orders first by NOTE ON ticks, and then NOTE OFF ticks. The last note of a piece may start
     * *and* stop before the penultimate note ceases. Thus, the penultimate note contains the true last tick.
     *
     * @return The last tick in the tree
     */
    public int getLastTick() {
        return root.max;
    }

    //endregion


    //region Node

    // A binary node which represents a range and stores a list of ranged elements
    public class Node implements Ranged {

        // the Range (i.e., interval of interval tree, not musical interval) this node represents
        private final Range range;
        // max endpoint in subtree rooted at this node (used to ignore left subtrees during queries)
        private int max;
        // This node's data: elements with the same range
        List<T> elements;

        // Child nodes
        public Node left;
        public Node right;

        //boolean queried; // TODO flag used for queries

        Node(Range range) {
            this.range = new Range(range);
            this.max = -1;
            this.left = null;
            this.right = null;
            this.elements = new ArrayList<>();
            //this.queried = false; // TODO
        }

        boolean add(T elem) {
            assert elem.getRange().equals(this.getRange());
            if (this.elements.contains(elem)) { return false; }
            return this.elements.add(elem);
        }

        // The number of elements this node holds
        int size() {
            return this.elements.size();
        }

        public long getMax() {
            return this.max;
        }

        @Override public String toString() {
            StringBuilder str = new StringBuilder(this.range + " -> {");
            for (T note : this.elements) { str.append(note).append(" "); }
            return str + "}";
        }

        @Override public Range getRange() {
            return new Range(this.range);
        }

    }

    //endregion


}
