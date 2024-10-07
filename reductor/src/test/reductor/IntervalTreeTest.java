package reductor;

import reductor.IntervalTree;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reductor.IntervalTree.Interval;
import reductor.IntervalTree.Node;

import java.util.ArrayList;
import java.util.Comparator;

import static org.junit.jupiter.api.Assertions.*;

class IntervalTreeTest {

    // Pitches
    static final int C = 0x3C;
    static final int D = 0x3E;

    // Can be used for xAll() tests
    private ArrayList<Note> notes;
    private ArrayList<Note> quasiDups;
    private ArrayList<Note> dups;

    // Blank-slate tree
    private IntervalTree tree;

    // Test "nodes" (9)
    Note root;
    Note l;
    Note r;
    Note ll;
    Note lr;
    Note rl;
    Note rr;
    Note llr;
    Note rrl;

    // Initialized with same range, different associated data as namesake (9)
    Note root_sameRange;
    Note l_sameRange;
    Note r_sameRange;
    Note ll_sameRange;
    Note lr_sameRange;
    Note rl_sameRange;
    Note rr_sameRange;
    Note llr_sameRange;
    Note rrl_sameRange;

    // Initialized with identical data as namesake (9)
    Note root_dup;
    Note l_dup;
    Note r_dup;
    Note ll_dup;
    Note lr_dup;
    Note rl_dup;
    Note rr_dup;
    Note llr_dup;
    Note rrl_dup;

    @BeforeEach
    void setup() {

        /*
         If the member notes are inserted in breadth-first order (according to their name), the
             template tree should look like this:
        */

        /*
                           _____10,20_____
                          /               \
                    10,15                  10,25
                   /     \                /     \
               05,20      10,17      10,22       15,20
               /  \        /  \       /  \        /  \
                 07,20                        12,20
         */

        root = new Note(10,20,C);
        l = new Note(10,15,C);
        r = new Note(10,25,C);
        ll = new Note(5,20,C);
        lr = new Note(10,17,C);
        rl = new Note(10,22,C);
        rr = new Note(15,20,C);
        llr = new Note(7,20,C);
        rrl = new Note(12,20,C);

        root_sameRange = new Note(root,D);
        l_sameRange = new Note(l,D);
        r_sameRange = new Note(r,D);
        ll_sameRange = new Note(ll,D);
        lr_sameRange = new Note(lr,D);
        rl_sameRange = new Note(rl,D);
        rr_sameRange = new Note(rr,D);
        llr_sameRange = new Note(llr,D);
        rrl_sameRange = new Note(rrl,D);

        root_dup = new Note(root);
        l_dup = new Note(l);
        r_dup = new Note(r);
        ll_dup = new Note(ll);
        lr_dup = new Note(lr);
        rl_dup = new Note(rl);
        rr_dup = new Note(rr);
        llr_dup = new Note(llr);
        rrl_dup = new Note(rrl);

        notes = new ArrayList<>();
        notes.add(root);
        notes.add(l);
        notes.add(r);
        notes.add(ll);
        notes.add(lr);
        notes.add(rl);
        notes.add(rr);
        notes.add(llr);
        notes.add(rrl);

        quasiDups = new ArrayList<>();
        quasiDups.add(root_sameRange);
        quasiDups.add(l_sameRange);
        quasiDups.add(r_sameRange);
        quasiDups.add(ll_sameRange);
        quasiDups.add(lr_sameRange);
        quasiDups.add(rl_sameRange);
        quasiDups.add(rr_sameRange);
        quasiDups.add(llr_sameRange);
        quasiDups.add(rrl_sameRange);

        dups = new ArrayList<>();
        dups.add(root_dup);
        dups.add(l_dup);
        dups.add(r_dup);
        dups.add(ll_dup);
        dups.add(lr_dup);
        dups.add(rl_dup);
        dups.add(rr_dup);
        dups.add(llr_dup);
        dups.add(rrl_dup);

        tree = new IntervalTree();
        assertNull(tree.root);
        assert(tree.nodes == 0);
        assert(tree.events == 0);

    }

    @Test
    void TestEmptyTree() {

        Note note = new Note(0,480,C);

        // Test: remove on empty tree should always return false
        assertFalse(tree.remove(note));
        assertFalse(tree.removeAll(notes));

        // Test: query on empty tree should always return an empty list
        assert(tree.query(note) != null  &&  tree.query(note).isEmpty());
        assert(tree.queryAll(notes) != null  &&  tree.queryAll(notes).isEmpty());

        // Test: toArrayList on empty tree should always return an empty list
        assert(tree.toArrayList().isEmpty());

    }

    @Test
    void TestAdd() {

        /*
         Test: Add novel notes
             - Should always return true
             - Should always increase nodes
             - Should always increase notes
        */

        int eventsCount = 0;

        // Add root
        assert(tree.add(root));
        assertNotNull(tree.root);
        assertEquals(tree.root.max, root.end);
        eventsCount++;

        assert(tree.nodes == eventsCount); // Checks related to IntervalTree
        assert(tree.events == eventsCount);

        Node node = tree.root; // Checks related to Node
        assert(node.depth == 0);
        assert(node.size() == 1);
        assert(tree.toArrayList().get(0) == root);

        assertNull(node.left); // Checks related to hierarchy
        assertNull(node.right);
        assertNull(node.parent);

        // Add left child
        assert(tree.add(l));
        assertNotNull(tree.root.left);
        assertNull(tree.root.right);
        eventsCount++;

        assert(tree.nodes == eventsCount);
        assert(tree.events == eventsCount);

        node = tree.root.left;
        assert(node.depth == 1);
        assert(node.size() == 1);
        var node2 = tree.toArrayList();
        assert(tree.toArrayList().get(0) == l);

        assertNull(node.left);
        assertNull(node.right);
        assert(node.parent == tree.root);

        // Add right child
        assert(tree.add(r));
        assertNotNull(tree.root.right);
        eventsCount++;

        assert(tree.nodes == eventsCount);
        assert(tree.events == eventsCount);

        node = tree.root.right;
        assert(node.depth == 1);
        assert(node.size() == 1);
        assert(tree.toArrayList().get(2) == r);

        assertNull(node.left);
        assertNull(node.right);
        assert(node.parent == tree.root);

        // Add left-left grandchild
        assert(tree.add(ll));
        assertNotNull(tree.root.left.left);
        assertNull(tree.root.left.right);
        eventsCount++;

        assert(tree.nodes == eventsCount);
        assert(tree.events == eventsCount);

        node = tree.root.left.left;
        assert(node.depth == 2);
        assert(node.size() == 1);
        assert(tree.toArrayList().get(0) == ll);

        assertNull(node.left);
        assertNull(node.right);
        assert(node.parent == tree.root.left);

        // Add left-right grandchild
        assert(tree.add(lr));
        assertNotNull(tree.root.left.right);
        eventsCount++;

        assert(tree.nodes == eventsCount);
        assert(tree.events == eventsCount);

        node = tree.root.left.right;
        assert(node.depth == 2);
        assert(node.size() == 1);
        assert(tree.toArrayList().get(2) == lr);

        assertNull(node.left);
        assertNull(node.right);
        assert(node.parent == tree.root.left);

        // Add right-left grandchild
        assert(tree.add(rl));
        assertNotNull(tree.root.right.left);
        assertNull(tree.root.right.right);
        eventsCount++;

        assert(tree.nodes == eventsCount);
        assert(tree.events == eventsCount);

        node = tree.root.right.left;
        assert(node.depth == 2);
        assert(node.size() == 1);
        assert(tree.toArrayList().get(4) == rl);

        assertNull(node.left);
        assertNull(node.right);
        assert(node.parent == tree.root.right);

        // Add right-right grandchild
        assert(tree.add(rr));
        assertNotNull(tree.root.right.right);
        eventsCount++;

        assert(tree.nodes == eventsCount);
        assert(tree.events == eventsCount);

        node = tree.root.right.right;
        assert(node.depth == 2);
        assert(node.size() == 1);
        assert(tree.toArrayList().get(6) == rr);

        assertNull(node.left);
        assertNull(node.right);
        assert(node.parent == tree.root.right);

        // Add left-left-right great-grandchild
        assert(tree.add(llr));
        assertNotNull(tree.root.left.left.right);
        assertNull(tree.root.left.left.left);
        eventsCount++;

        assert(tree.nodes == eventsCount);
        assert(tree.events == eventsCount);

        node = tree.root.left.left.right;
        assert(node.depth == 3);
        assert(node.size() == 1);
        assert(tree.toArrayList().get(1) == llr);

        assertNull(node.left);
        assertNull(node.right);
        assert(node.parent == tree.root.left.left);

        // Add right-right-left great-grandchild
        assert(tree.add(rrl));
        assertNotNull(tree.root.right.right.left);
        assertNull(tree.root.right.right.right);
        eventsCount++;

        assert(tree.nodes == eventsCount);
        assert(tree.events == eventsCount);

        node = tree.root.right.right.left;
        assert(node.depth == 3);
        assert(node.size() == 1);
        assert(tree.toArrayList().get(7) == rrl);

        assertNull(node.left);
        assertNull(node.right);
        assert(node.parent == tree.root.right.right);

        /*
        Test: Add identical ranges but different associated data
            - Should always return true
            - Should not affect nodes eventsCount
            - Should increment notes eventsCount
            - Should increment node's individual size
        */

        final int NODES_COUNT = eventsCount;

        assert(tree.add(root_sameRange));
        eventsCount++;
        assert(tree.nodes == NODES_COUNT);
        assert(tree.events == eventsCount);
        assert(tree.root.size() == 2);

        assert(tree.add(l_sameRange));
        eventsCount++;
        assert(tree.nodes == NODES_COUNT);
        assert(tree.events == eventsCount);
        assert(tree.root.left.size() == 2);

        assert(tree.add(r_sameRange));
        eventsCount++;
        assert(tree.nodes == NODES_COUNT);
        assert(tree.events == eventsCount);
        assert(tree.root.right.size() == 2);

        assert(tree.add(ll_sameRange));
        eventsCount++;
        assert(tree.nodes == NODES_COUNT);
        assert(tree.events == eventsCount);
        assert(tree.root.left.left.size() == 2);

        assert(tree.add(lr_sameRange));
        eventsCount++;
        assert(tree.nodes == NODES_COUNT);
        assert(tree.events == eventsCount);
        assert(tree.root.left.right.size() == 2);

        assert(tree.add(rl_sameRange));
        eventsCount++;
        assert(tree.nodes == NODES_COUNT);
        assert(tree.events == eventsCount);
        assert(tree.root.right.left.size() == 2);

        assert(tree.add(rr_sameRange));
        eventsCount++;
        assert(tree.nodes == NODES_COUNT);
        assert(tree.events == eventsCount);
        assert(tree.root.right.right.size() == 2);

        assert(tree.add(llr_sameRange));
        eventsCount++;
        assert(tree.nodes == NODES_COUNT);
        assert(tree.events == eventsCount);
        assert(tree.root.left.left.right.size() == 2);

        assert(tree.add(rrl_sameRange));
        eventsCount++;
        assert(tree.nodes == NODES_COUNT);
        assert(tree.events == eventsCount);
        assert(tree.root.right.right.left.size() == 2);

        // Test: Add same exact object should always return false

        assertFalse(tree.add(root));
        assertFalse(tree.add(l));
        assertFalse(tree.add(r));
        assertFalse(tree.add(ll));
        assertFalse(tree.add(lr));
        assertFalse(tree.add(rl));
        assertFalse(tree.add(rr));
        assertFalse(tree.add(llr));
        assertFalse(tree.add(rrl));

        assert(tree.nodes == NODES_COUNT); // should be same as before dups test
        assert(tree.events == eventsCount);

        // Test: Add different object with identical data should always return false

        assertFalse(tree.add(root_dup));
        assertFalse(tree.add(l_dup));
        assertFalse(tree.add(r_dup));
        assertFalse(tree.add(ll_dup));
        assertFalse(tree.add(lr_dup));
        assertFalse(tree.add(rl_dup));
        assertFalse(tree.add(rr_dup));
        assertFalse(tree.add(llr_dup));
        assertFalse(tree.add(rrl_dup));

        assert(tree.nodes == NODES_COUNT); // should be same as before dups test
        assert(tree.events == eventsCount);

    }

    @Test
    void TestAddAll() {

        assert(tree.addAll(notes));
        assert(tree.nodes == notes.size());
        assert(tree.events == notes.size());

        assert(tree.addAll(quasiDups));
        assert(tree.nodes == notes.size());
        assert(tree.events == notes.size() * 2);

        assert(!tree.addAll(dups));
        assert(tree.nodes == notes.size());
        assert(tree.events == notes.size() * 2);

    }

    @Test
    void TestRemoveFromNodeListsOnly() {

        tree.addAll(notes);
        tree.addAll(quasiDups);

        final int NODES_COUNT = tree.nodes;
        final int EVENTS_COUNT = tree.events;

        ArrayList<Note> currentTree = tree.toArrayList();

        int removals = 0;
        int element = 0;

        while(element < quasiDups.size()) {
            Note note = quasiDups.get(element);
            assert (currentTree.contains(note));
            tree.remove(note);
            removals++;
            currentTree = tree.toArrayList();
            assert (!currentTree.contains(note));
            assert (tree.events == EVENTS_COUNT - removals);
            element++;
        }

        assert(tree.nodes == NODES_COUNT);
        assert(tree.events == EVENTS_COUNT / 2);

    }

    @Test
    void TestRemoveLeaf() {

        tree.addAll(notes);
        final int NODES_COUNT = tree.nodes;
        final int EVENTS_COUNT = tree.events;
        int removals = 0;

        // Remove left-left-right great grandchild
        assertNotNull(tree.root.left.left.right);
        assert(tree.remove(llr));
        removals++;
        assert(tree.nodes == NODES_COUNT - removals);
        assert(tree.events == EVENTS_COUNT - removals);
        assertNull(tree.root.left.left.right);

        // Remove right-right-left great grandchild
        assertNotNull(tree.root.right.right.left);
        assert(tree.remove(rrl));
        removals++;
        assert(tree.nodes == NODES_COUNT - removals);
        assert(tree.events == EVENTS_COUNT - removals);
        assertNull(tree.root.right.right.left);

    }

    @Test
    void TestRemoveInternalNode() {

        tree.addAll(notes);

        int count = notes.size();
        for (Note e : notes) {
            tree.remove(e);
            assert(orderIsMaintained(tree));
            count--;
            assert(tree.nodes == count);
            assert(tree.events == count);
        }
        assert(tree.nodes == 0);
        assert(tree.events == 0);
        assertNull(tree.root);

    }

    @Test
    void TestRemoveFromRoot() {

        tree.addAll(notes);
        final int NODES_COUNT = tree.nodes;
        final int EVENTS_COUNT = tree.events;
        int removals = 0;

        // This is the node that should get rotated up to be the new node in this case
        assert(tree.root.left.right != null);
        var lr_list = tree.root.left.right.list;

        assert(tree.remove(root));
        assert(tree.root.list == lr_list);

        assert(tree.nodes == NODES_COUNT - 1);
        assert(tree.events == EVENTS_COUNT - 1);
        assertNull(tree.root.left.right);

        assert(orderIsMaintained(tree));
    }

    @Test
    void TestRemoveLastNode() {

        tree.add(root);
        assert(tree.nodes == 1);
        assert(tree.events == 1);
        assert(root != null);

        tree.remove(root);
        assert(tree.root == null);
        assert(tree.nodes == 0);
        assert(tree.events == 0);

    }

    @Test
    void TestRemoveAll() {

        tree.addAll(notes);
        tree.addAll(quasiDups);
        assertEquals(tree.nodes, notes.size(), "Tree is not same size");
        assert(tree.events == notes.size() + quasiDups.size());

        assert(tree.removeAll(quasiDups));
        assert(tree.nodes == notes.size());
        assert(tree.events == (notes.size() + quasiDups.size()) - quasiDups.size());

        assert(tree.removeAll(notes));
        assert(tree.nodes == 0);
        assert(tree.events == 0);
        assert(tree.root == null);

        /* Test falses */

        Note note1 = new Note(0,10,C);
        Note note2 = new Note(10,20,C);
        tree.add(note1);
        tree.add(note2);

        Note neverAdded1 = new Note(100,200,C);
        Note neverAdded2 = new Note(100,200,D);
        var neverAddedList = new ArrayList<Note>();

        assertFalse(tree.removeAll(neverAddedList));

        neverAddedList.add(note1);

        // This now has at least one match in it so the set should change still.
        assert(tree.removeAll(neverAddedList));

    }

    @Test
    void TestOverlaps() {

        Interval target = new Interval(100,200);

        /* Check */

        Interval bothEndpoints_shared = new Interval(100,200);
        assert(bothEndpoints_shared.overlaps(target));

        Interval fullyContained = new Interval(101,199);
        assert(fullyContained.overlaps(target));

        Interval fullyContaining = new Interval(99,201);
        assert(fullyContaining.overlaps(target));

        /* Check left endpoint edges */

        Interval leftEndpoint_shared = new Interval(200,10000);
        assert(leftEndpoint_shared.overlaps(target));

        Interval leftEndpoint_minusOne = new Interval(199,10000);
        assert(leftEndpoint_minusOne.overlaps(target));

        Interval fullyLeft = new Interval(201,10000);
        assertFalse(fullyLeft.overlaps(target));

        /* Check right endpoint edges */

        Interval rightEndpoint_shared = new Interval(0,100);
        assert(rightEndpoint_shared.overlaps(target));

        Interval rightEndpoint_plusOne = new Interval(0,101);
        assert(rightEndpoint_plusOne.overlaps(target));

        Interval fullyRight = new Interval(0,99);
        assertFalse(fullyRight.overlaps(target));

        assertThrows(IllegalArgumentException.class, () -> new Interval(10, 9));
        assertThrows(IllegalArgumentException.class, () -> new Interval(10, 10));
    }

    // Helper for removals
    boolean orderIsMaintained(IntervalTree tree) {

        var comp = new Comparator<Note>() {
            @Override public int compare(Note e1, Note e2) {
                if (e1.begin != e2.begin) {
                    return Long.compare(e1.begin, e2.begin);
                }
                else {
                    return Long.compare(e1.end, e2.end);
                }
            }
        };

        ArrayList<Note> inOrderList = tree.toArrayList();
        Note prev, curr;
        for (int i = 0, j = 1; i < inOrderList.size() - 1; i++, j++) {
            prev = inOrderList.get(i);
            curr = inOrderList.get(j);

            if (comp.compare(curr, prev) < 0) {
                return false;
            }
        }

        return true;
    }

}