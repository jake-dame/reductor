package reductor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reductor.IntervalTree.Node;

import java.util.ArrayList;
import java.util.Comparator;

import static org.junit.jupiter.api.Assertions.*;
import static reductor.RhythmBase.*;


/// Unit tests for {@link reductor.IntervalTree}. Tests construction (which inherently tests insertion),
/// and query
class IntervalTreeTest<T extends Ranged> {

    static { Context context = Context.createContext(); }

    /*
        For list of T with ranges:
            + [5,30], [7,20], [10,15], [10,17], [10,20], [10,22], [10,25], [12,30], [15,20]

        The constructed tree should look like:

                       _____10,20_____
                      /               \
                 7,20                  10,25
                /     \               /     \
           5,20       10,15      10,22       12,30
           /  \        /  \       /  \        /  \
                          10,17                 15,20

        T with identical ranges but different associated data should be added to the node with the
        corresponding range.

        Exact duplicates should occur nowhere in the tree.
    */
    /// Pitch constants
    static final int C = 0x3C;
    static final int D = 0x3E;
    private ArrayList<Note> distinctElems;
    private ArrayList<Note> quasiDupElems;

    @BeforeEach
    void setup() {
        distinctElems = new ArrayList<>();
        quasiDupElems = new ArrayList<>();
        Note e1 = new Note(C, new Range(5, 30));
        distinctElems.add(e1);
        Note e2 = new Note(C, new Range(7, 20));
        distinctElems.add(e2);
        Note e3 = new Note(C, new Range(10, 15));
        distinctElems.add(e3);
        Note e4 = new Note(C, new Range(10, 17));
        distinctElems.add(e4);
        Note e5 = new Note(C, new Range(10, 20));
        distinctElems.add(e5);
        Note e6 = new Note(C, new Range(10, 22));
        distinctElems.add(e6);
        Note e7 = new Note(C, new Range(10, 25));
        distinctElems.add(e7);
        Note e8 = new Note(C, new Range(12, 30));
        distinctElems.add(e8);
        Note e9 = new Note(C, new Range(15, 20));
        distinctElems.add(e9);
        for (Note note : distinctElems) {
            quasiDupElems.add(new Note(D, note.getRange()));
        }
    }

    @Test
    void constructionWithDistinctElems() {
        IntervalTree<Note> tree = new IntervalTree<>(distinctElems);
        assertTrue(isInOrder(tree), "in order traversal list should be in ascending order");
        checkMaxes(tree);
        assertEquals(distinctElems.size(), tree.getNumNodes(), "number of nodes should be equal to elems added");
        assertEquals(distinctElems.size(), tree.getNumElements(), "number of elements should be equal to elems added");
    }

    @Test
    void constructionWithQuasiDupElems() {
        ArrayList<Note> distinctPlusQuasiDups = new ArrayList<>();
        distinctPlusQuasiDups.addAll(distinctElems);
        distinctPlusQuasiDups.addAll(quasiDupElems);
        IntervalTree<Note> tree = new IntervalTree<>(distinctPlusQuasiDups);
        assertTrue(isInOrder(tree), "in order traversal list should be in ascending order");
        checkMaxes(tree);
        assertEquals(distinctElems.size(), tree.getNumNodes(), "number of nodes should be equal to size of one list");
        assertEquals(distinctElems.size() + quasiDupElems.size(), tree.getNumElements(), "number of elements should be equal to sum of size of both lists");
    }

    @Test
    void constructionWithExactDuplicatesPresent() {
        ArrayList<Note> distinctElemsWithExactDups = new ArrayList<>();
        distinctElemsWithExactDups.addAll(distinctElems);
        distinctElemsWithExactDups.addAll(distinctElems);
        IntervalTree<Note> tree = new IntervalTree<>(distinctElemsWithExactDups);
        assertTrue(isInOrder(tree), "in order traversal list should be in ascending order by Comp");
        checkMaxes(tree);
        assertEquals(distinctElems.size(), tree.getNumNodes(), "number of nodes should be equal to elems added");
        assertEquals(distinctElems.size(), tree.getNumElements(), "number of elements should be equal to elems added");
    }

    @Test
    void constructionFromNull() {
        assertThrows(NullPointerException.class, () -> new IntervalTree<>(null), "should throw when passed null");
    }

    @Test
    void constructionWithEmptyList() {
        ArrayList<Note> notes = new ArrayList<>();
        IntervalTree<Note> tree = new IntervalTree<>(notes);
        assertTrue(isInOrder(tree));
        checkMaxes(tree);
        assertEquals(0, tree.getNumNodes());
        assertEquals(0, tree.getNumElements());
    }

    @Test
    void query() {


        /*
            Intervals: [5,20], [7,20], [10,15], [10,17], [10,20], [10,22], [10,25], [12,20], [15,20]

            |-0-1-2-3-4-5-6-7-8-9-10-11-12-13-14-15-16-17-18-19-20-21-22-23-24-25-|
                        5---------------------------------------20
                            7-----------------------------------20
                                  10-------------15
                                  10-------------------17
                                  10----------------------------20
                                  10----------------------------------22
                                  10-------------------------------------------25
                                        12----------------------20
                                                 15-------------20
        */
        ArrayList<Note> distinctPlusQuasiDups = new ArrayList<>();
        distinctPlusQuasiDups.addAll(distinctElems);
        distinctPlusQuasiDups.addAll(quasiDupElems);
        // This should have 8 nodes, each node with a C4 and a D4 Note.
        IntervalTree<Note> tree = new IntervalTree<>(distinctPlusQuasiDups);
        // Just a bunch of arbitrary ranges that I know cover all possible ranges generously.
        ArrayList<Range> ranges = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            ranges.add(new Range(i, i + 1));
        }
        // This will be added to and compared with the list used to construct the tree to make sure
        // no-more-no-less matches are found.
        ArrayList<Note> totalMatches = new ArrayList<>();
        for (Range queryWindow : ranges) {
            ArrayList<Note> matches = tree.query(queryWindow);
            for (Note match : matches) {
                assertTrue(match.getRange().overlaps(queryWindow));
                if (!totalMatches.contains(match)) {
                    totalMatches.add(match);
                }
            }
        }
        // This is needed because for ArrayLists to be considered equal, their elements need to be equal and in the
        // same order. NoteList have a different compareTo() than Ranges.
        var comp = new Comparator<Note>() {
            @Override
            public int compare(Note n1, Note n2) {
                if (n1.pitch() != n2.pitch()) {
                    return n1.compareTo(n2);
                } else {
                    return n1.getRange().compareTo(n2.getRange());
                }
            }
        };
        distinctPlusQuasiDups.sort(comp);
        totalMatches.sort(comp);
        assertEquals(distinctPlusQuasiDups, totalMatches);
    }

    @Test
    void queryNullTree() {
        IntervalTree<Note> tree = new IntervalTree<>(distinctElems);
        tree.root = null;
        assertEquals(new ArrayList<>(), tree.query(new Range(0, 100)), "query on a null tree should return an empty " +
                "list");
    }

    @Test
    void queryNullWindow() {
        IntervalTree<Note> tree = new IntervalTree<>(distinctElems);
        assertThrows(NullPointerException.class, () -> tree.query(null), "should throw when window is null and query is attempted");
    }

    @Test
    void getLastTick() {

        ArrayList<Note> distinctPlusQuasiDups = new ArrayList<>();
        distinctPlusQuasiDups.addAll(distinctElems);
        distinctPlusQuasiDups.addAll(quasiDupElems);
        IntervalTree<Note> tree = new IntervalTree<>(distinctPlusQuasiDups);

        assertEquals(30L, tree.getLastTick());
    }

    @Test
    void queryPastEndOfTree() {

        ArrayList<Note> distinctPlusQuasiDups = new ArrayList<>();
        distinctPlusQuasiDups.addAll(distinctElems);
        distinctPlusQuasiDups.addAll(quasiDupElems);
        IntervalTree<Note> tree = new IntervalTree<>(distinctPlusQuasiDups);

        Range window = new Range(tree.getLastTick() + 1, tree.getLastTick() + 2);
        assertTrue(tree.query(window).isEmpty());
    }

    // These two methods are used to iterate over every node in the tree
    // and, for each node, check its entire subtree to determine if the node indeed
    // stores the highest right endpoint (i.e., node.range().high()) stored in its subtree
    // as its node.max field
    //
    // In order to do this correctly, POST-ORDER traversal is used (as the max of both left and right
    // are needed before checking this node's max)
    void checkMaxes(IntervalTree<Note> tree) {
        checkMaxes(tree.root);
    }

    private long checkMaxes(Node node) {
        if (node == null) {
            return -1;
        }
        long leftMax = checkMaxes(node.left);
        long rightMax = checkMaxes(node.right);
        long thisMax = node.getRange().high();
        long maxOfSubtreesAndThis = Math.max(thisMax, Math.max(leftMax, rightMax));
        assertEquals(node.getMax(), maxOfSubtreesAndThis);
        return maxOfSubtreesAndThis;
    }

    // Testing helper function that returns true if the passed tree's elements are indeed in order
    private boolean isInOrder(IntervalTree<Note> tree) {
        ArrayList<Note> inOrderList = tree.toList();
        for (int i = 1; i < inOrderList.size(); i++) {
            Note prev = inOrderList.get(i - 1);
            Note curr = inOrderList.get(i);
            if (curr.getRange().compareTo(prev.getRange()) < 0) {
                return false;
            }
        }
        return true;
    }

    @Test
    void getColumns() {

        ArrayList<Note> notes = new ArrayList<>();

        // RH
        var note1 = new MutableNote().setStart(0).setRhythm(DOTTED_EIGHTH).setPitch("B3");
        var note2 = new MutableNote().setStart(note1.stop() + 1).setRhythm(SIXTEENTH).setPitch("B4");

        // LH
        var note3 = new MutableNote().setStart(note2.stop() + 1).setRhythm(EIGHTH).setPitch("G3");
        var chord = new MutableChord().setRoot(note3).add("B4").add("E4");

    }


}