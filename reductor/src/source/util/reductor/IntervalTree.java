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
        List<T> elements;

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
            String str = this.range + "->{";
            for (T note : this.elements) { str += note + ","; }
            str = str.substring(0, str.length() - 1); // get last comma off
            return str + "}"; // looks like: "[240,480]->{C4,E4,G4}"
        }

        @Override
        public Range getRange() { return new Range(this.range); }

    } //endregion


    /// Root of this interval tree
    Node root;

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


    ArrayList<T> query(Range window) {

        if (root == null) { return new ArrayList<>(); }

        if (window == null) { throw new NullPointerException(); }

        ArrayList<T> matches = new ArrayList<>();

        query(root, window, matches);

        //while(!this.queriedNodes.isEmpty()) {
        //    var guy = queriedNodes.pop();
        //    guy.queried = false;
        //}

        return new ArrayList<>(matches);

    }

    private ArrayList<T> query(Node node, Range window, ArrayList<T> matches) {

        //if (!node.queried &&  window.overlaps(node.getRange())) {
        //    matches.addAll(node.elements);
        //    node.queried = true;
        //    queriedNodes.push(node);
        //}

        if (window.overlaps(node.getRange())) { matches.addAll(node.elements); }

        if (node.left == null  &&  node.right == null) { return matches; }

        if (node.left != null  &&  window.low() <= node.left.getMax()) {
            query(node.left, window, matches);
        }

        if (node.right != null  &&  window.high() >= node.getRange().low()) {
            query(node.right, window, matches);
        }

        // Return from this path/branch, but may need to keep going on other paths -- return above too.
        return matches;

    }


    ArrayList<T> getColumns(Range window) {

        if (root == null) { return new ArrayList<>(); }
        if (window == null) { throw new NullPointerException(); }

        ArrayList<T> matches = new ArrayList<>();

        query(root, window, matches);

        //while(!this.queriedNodes.isEmpty()) {
        //    var guy = queriedNodes.pop();
        //    guy.queried = false;
        //}

        return new ArrayList<>(matches);

    }

    /*

                [0,

        [0,240]
        G, B,  E


     */


    private ArrayList<T> getColumns(Node node, Range window, ArrayList<T> matches) {

        if (window.overlaps(node.getRange())) { matches.addAll(node.elements); }

        if (node.left == null  &&  node.right == null) { return matches; }

        if (node.left != null  &&  window.low() <= node.left.getMax()) {
            query(node.left, window, matches);
        }

        if (node.right != null  &&  window.high() >= node.getRange().low()) {
            query(node.right, window, matches);
        }

        // Return from this path/branch, but may need to keep going on other paths -- return above too.
        return matches;

    }

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

    /// Prints the tree in order.
    public void print() { print(root); }

    private void print(Node node) {
        if (node == null) { return; }
        print(node.left);
        for (T elem : node.elements) { System.out.println(elem); }
        print(node.right);
    }

    /*
     * There is no size() method for this data structure because that could mean two very different things.
     * Explicitly named size-related getters are provided instead.
     */

    public int getNumNodes() { return numNodes; }
    public int getNumElements() { return numElements; }

    /*
     * Note on these four methods: They return the min/max TICK values in the tree, not the min/max of the thing the
     * tree is built on (the interval/range of nodes). It can be implemented, and the getMin would look exactly the
     * same except return the actual Range instead of the range left endpoint. However, when looking for the max
     * range, this could be independent of node actually sitting at the bottom-right-most position in the tree. It is
     * perfectly valid for that leaf to be something like [15,20], and it's parent to be something like [12,30] due to
     *  the way the tree is ordered (primarily by low endpoint).
     *
     * TLDR: when looking for the max tick, in the tree, use getMaxTick() -- it's already stored in the root.
     * When looking for the last sounding/struck note (or the node holding it) in the tree, use getMax().
     */

    public Range getMin() {
        Node node = this.root;
        while(node.left != null) { node = node.left; }
        return node.getRange();
    }

    public Range getMax() {
        Node node = this.root;
        while(node.right != null) { node = node.right; }
        return node.getRange();
    }

    public long getFirstTick() { return this.getMin().low(); }
    public long getLastTick() { return root.max; }

}