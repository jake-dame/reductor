package reductor;

import java.util.*;


/**
 * An interval tree (augmented BST) that can contain any object with a {@link reductor.Range}
 * (i.e., implements the {@link reductor.Ranged} interface), e.g. {@link reductor.Note}, {@link Column}, etc.
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
    class Node implements Ranged {

        /// The {@link reductor.Range} (i.e., interval) this node represents
        private final Range range;

        /// Max endpoint in subtree rooted at this node (used to ignore left subtrees during queries)
        private long max;

        /// This node's data (a Set of elements with the same range but possibly differing pitches)
        ArrayList<T> elements;

        /// This node's left child
        Node left;
        /// This node's right child
        Node right;

        /// Flag used during queries
        //boolean queried;

        /// Primary constructor which takes a {@link reductor.Range}
        Node(Range range) {
            this.range = new Range(range);
            this.max = -1;
            this.left = null;
            this.right = null;
            this.elements = new ArrayList<>();
            //this.queried = false;
        }

        /// Adds an element to this node's list
        boolean add(T elem) {
            assert elem.getRange().equals(this.getRange());
            if (this.elements.contains(elem)) { return false; }
            return this.elements.add(elem);
        }

        /// The number of elements this node holds
        int size() { return this.elements.size(); }

        long getMax() { return this.max; }

        @Override
        public String toString() {
            String str = this.range + " -> {";
            for (T note : this.elements) { str += note + " "; }
            return str + "}";
        }

        @Override
        public Range getRange() { return new Range(this.range); }

    }


    /// Root of this interval tree
    private final Node root;

    /// Size (number of nodes)
    private int numNodes;

    /// Size (number of elements)
    private int numElements;

    //Stack<Node> queriedNodes = new Stack<>();


    /// Primary constructor
    IntervalTree(ArrayList<T> elements) {

        if (elements == null) { throw new NullPointerException(""); }

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


    /* ====
     * BUILD
     * ===*/


    /// Adds ranges of elements to Set to remove duplicate ranges, and gives back (sorted) List
    private ArrayList<Range> getUniqueRanges(ArrayList<T> elements) {

        HashSet<Range> rangesSet = new HashSet<>();

        // Remove duplicate ranges
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
        for (T elem : elements) { add(root, elem); }
    }

    private void add(Node node, T elem) {

        // Update node.max if elem to be added will set the new max right endpoint of elems in subtree rooted at
        // this node
        if (node.max < elem.getRange().high()) {
            node.max = elem.getRange().high();
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


    /* ====
    * QUERY
    * ===*/


    public ArrayList<T> query(long point) {

        if (point < 0  ||  isEmpty()) { return new ArrayList<>(); }

        ArrayList<T> matches = new ArrayList<>();
        query(root, point, matches);
        var out = new ArrayList<>(matches);
        out.sort(null);
        return out;
    }

    private ArrayList<T> query(Node node, long point, ArrayList<T> matches) {

        // Target hit.
        if (node.getRange().contains(point)) { matches.addAll(node.elements); }

        // Base case.
        if (node.left == null  &&  node.right == null) { return matches; }

        // Look left & right.
        if (node.left != null  &&  point <= node.left.getMax()) { query(node.left, point, matches); }
        if (node.right != null  &&  point >= node.getRange().low()) { query(node.right, point, matches); }

        // Continue to search
        return matches;
    }


    public ArrayList<T> query(Range window) {

        if (window == null) { throw new NullPointerException(); }
        if (isEmpty()) { return new ArrayList<>(); }

        ArrayList<T> matches = new ArrayList<>();
        query(root, window, matches);
        return new ArrayList<>(matches);
    }

    private ArrayList<T> query(Node node, Range window, ArrayList<T> matches) {

        // Target hit.
        if (window.overlaps(node.getRange())) { matches.addAll(node.elements); }

        // Base case.
        if (node.left == null  &&  node.right == null) { return matches; }

        // Look left & right.
        if (node.left != null  &&  window.low() <= node.left.getMax()) {
            query(node.left, window, matches);
        }

        if (node.right != null  &&  window.high() >= node.getRange().low()) {
            query(node.right, window, matches);
        }

        // Continue to search
        return matches;
    }


    //public ArrayList<T> queryWithoutDuplicates(Range window) {
    //
    //    if (window == null) { throw new NullPointerException(); }
    //    if (isEmpty()) { return new ArrayList<>(); }
    //
    //    ArrayList<T> matches = new ArrayList<>();
    //    query(root, window, matches);
    //    return new ArrayList<>(matches);
    //}
    //
    //private ArrayList<T> queryWithoutDuplicates(Node node, Range window, ArrayList<T> matches) {
    //
    //    if (window.overlaps(node.getRange())) { matches.addAll(node.elements); }
    //
    //    if (node.left == null  &&  node.right == null) { return matches; }
    //
    //    if (node.left != null  &&  window.low() <= node.left.getMax()) {
    //        query(node.left, window, matches);
    //    }
    //
    //    if (node.right != null  &&  window.high() >= node.getRange().low()) {
    //        query(node.right, window, matches);
    //    }
    //
    //    return matches;
    //}


    /* ====
     * OTHER
     * ===*/


    /// Returns the tree as an ArrayList, in order.
    public ArrayList<T> toList() {
        ArrayList<T> inOrderList = new ArrayList<>();
        toList(root, inOrderList);
        return inOrderList;
    }

    private void toList(Node node, ArrayList<T> inOrderList) {
        if (node == null) { return; }
        toList(node.left, inOrderList);
        inOrderList.addAll(node.elements);
        toList(node.right, inOrderList);
    }

    /**
     * @return An in-order list of each node by its key (i.e. range/interval).
     */
    public ArrayList<Range> toListOfRanges() {
        ArrayList<Range> ranges = new ArrayList<>();
        toListOfRanges(root, ranges);
        return ranges;
    }

    private void toListOfRanges(Node node, ArrayList<Range> ranges) {
        if (node == null) { return; }
        toListOfRanges(node.left, ranges);
        ranges.add(node.getRange());
        toListOfRanges(node.right, ranges);
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
    public boolean isEmpty() { return this.root == null; }

    /**
     * @return The number of nodes in this tree.
     */
    public int getNumNodes() { return numNodes; }

    /**
     * @return The total number of elements stored at all nodes in this tree.
     */
    public int getNumElements() { return numElements; }

    /* There is some redundancy in the following methods,
    but they each suit my needs for specific purposes, for now. */

    public T getFirst() {
        Node node = this.root;
        while(node.left != null) { node = node.left; }
        return node.elements.getFirst();
    }

    public T getLast() {
        Node node = this.root;
        while(node.right != null) { node = node.right; }
        return node.elements.getLast();
    }

    /**
     * @return The range of the first sounding note.
     * @see IntervalTree#getFirstTick
     */
    public Range getMin() {
        Node node = this.root;
        while(node.left != null) { node = node.left; }
        return node.getRange();
    }

    /**
     * @return The range of the last sounding note.
     * @see IntervalTree#getLastTick
     */
    public Range getMax() {
        Node node = this.root;
        while(node.right != null) { node = node.right; }
        return node.getRange();
    }

    /**
     * @return The first tick in the tree. Equivalent to calling {@link Range#low()} on {@link IntervalTree#getMin()}.
     */
    public long getFirstTick() { return this.getMin().low(); }

    /**
     * This does not necessarily correspond to calling {@link Range#high()} on {@link IntervalTree#getMax()} due to the
     * fact that the tree orders first by NOTE ON ticks, and then NOTE OFF ticks. The last note of a piece may start
     * *and* stop before the penultimate note ceases.
     *
     * @return The last tick in the tree
     */
    public long getLastTick() { return root.max; }


    /* ===
     * DEV
     * ==*/

    IntervalTree() { this.root = null; }
    Node getRoot() { return this.root; }

}