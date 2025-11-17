package reductor.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reductor.core.builders.NoteBuilder;
import reductor.util.IntervalTree;
import reductor.util.IntervalTree.Node;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


/**
 * Unit tests for {@link IntervalTree}. Tests construction (which inherently tests insertion),
 * and query
 */
class IntervalTreeTest<T extends Ranged> {

    private List<Range> inputRanges;
    private List<Note> uniqueElements;
    private List<Note> halfDuplicates;
    private List<Note> fullDuplicates;

    @BeforeEach
    void setup() {
        inputRanges = RangeUtil.listOf(
                5, 30,
                7, 20,
                10, 15,
                10, 17,
                10, 20,
                10, 22,
                10, 25,
                12, 30,
                15, 20
        );

        // list of input ranges mapped to Note objects with pitches of C
        // these should each account for 1 new node as well as 1 new element
        uniqueElements = inputRanges.stream()
                .map(r -> NoteBuilder.builder().range(r).pitch("C").build())
                .toList();

        // same ranges as above but with pitches of D (same range, different data)
        // these should not create new nodes, but should be added to an existing node's list of elements
        halfDuplicates = uniqueElements.stream()
                .map(n -> NoteBuilder.from(n).pitch("D").build())
                .toList();

        // full copies of the unique elements (same data AND same range)
        // these should not contribute to the tree at all
        fullDuplicates = uniqueElements.stream()
                .map(n -> NoteBuilder.from(n).build())
                .toList();
    }

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

    T's with identical ranges but different associated data should be added to the node with the
    corresponding range.

    Exact duplicates should occur nowhere in the tree.
    */

    @Test
    void withUniqueElementsOnly() {
        IntervalTree<Note> testTree = new IntervalTree<>(uniqueElements);
        assertTrue(isInOrder(testTree), "in order traversal list should be in ascending order");
        assertMaxesAreCorrect(testTree);
        assertEquals(uniqueElements.size(), testTree.getSizeNodes(), "number of nodes should be equal to elems added");
        assertEquals(uniqueElements.size(), testTree.getSizeElements(), "number of elements should be equal to elems added");
    }

    @Test
    void withHalfDuplicateElements() {
        List<Note> inputList = new ArrayList<>(uniqueElements);
        inputList.addAll(halfDuplicates);
        //------------------
        IntervalTree<Note> tree = new IntervalTree<>(inputList);
        assertTrue(isInOrder(tree), "in order traversal list should be in ascending order");
        assertMaxesAreCorrect(tree);
        assertEquals(uniqueElements.size(), tree.getSizeNodes(), "number of nodes should be equal to size of one list");
        assertEquals(inputList.size(), tree.getSizeElements(), "number of elements should be equal to sum of size of both lists");
    }

    @Test
    void withFullDuplicates() {
        List<Note> inputList = new ArrayList<>(uniqueElements);
        inputList.addAll(fullDuplicates);
        //------------------
        IntervalTree<Note> tree = new IntervalTree<>(inputList);
        assertTrue(isInOrder(tree), "in order traversal list should be in ascending order");
        assertMaxesAreCorrect(tree);
        assertEquals(uniqueElements.size(), tree.getSizeNodes(), "number of nodes should be equal to size of one list");
        assertEquals(uniqueElements.size(), tree.getSizeElements(), "number of elements should be equal to sum of size of both lists");
    }

    @Test
    void constructionFromNull() {
        assertThrows(NullPointerException.class, () -> new IntervalTree<>(null), "should throw when passed null");
    }

    @Test
    void constructionWithEmptyList() {
        List<Note> notes = new ArrayList<>();
        IntervalTree<Note> tree = new IntervalTree<>(notes);
        assertTrue(isInOrder(tree));
        assertMaxesAreCorrect(tree);
        assertEquals(0, tree.getSizeNodes());
        assertEquals(0, tree.getSizeElements());
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
        distinctPlusQuasiDups.addAll(uniqueElements);
        distinctPlusQuasiDups.addAll(halfDuplicates);
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
            List<Note> matches = tree.query(queryWindow);
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
        IntervalTree<Note> tree = new IntervalTree<>();
        assertEquals(new ArrayList<>(), tree.query(new Range(0, 100)),
                "query on a null tree should return an empty list"
        );
    }

    @Test
    void queryNullWindow() {
        IntervalTree<Note> tree = new IntervalTree<>(uniqueElements);
        assertThrows(NullPointerException.class, () -> tree.query(null),
                "should throw when window is null and query is attempted"
        );
    }

    @Test
    void getLastTick() {

        ArrayList<Note> distinctPlusQuasiDups = new ArrayList<>();
        distinctPlusQuasiDups.addAll(uniqueElements);
        distinctPlusQuasiDups.addAll(halfDuplicates);
        IntervalTree<Note> tree = new IntervalTree<>(distinctPlusQuasiDups);

        assertEquals(30L, tree.getLastTick());
    }

    @Test
    void queryPastEndOfTree() {

        ArrayList<Note> distinctPlusQuasiDups = new ArrayList<>();
        distinctPlusQuasiDups.addAll(uniqueElements);
        distinctPlusQuasiDups.addAll(halfDuplicates);
        IntervalTree<Note> tree = new IntervalTree<>(distinctPlusQuasiDups);

        Range window = new Range(tree.getLastTick() + 1, tree.getLastTick() + 2);
        assertTrue(tree.query(window).isEmpty());
    }

    @Test
    void queryWithPoint() {

        Range qtr1 = new Range(0,479); // quarter on beat 1
        Range qtrSync = new Range(240, 719); // syncopated quarter
        Range qtr2 = new Range(480,959); // quarter on beat 2

        ArrayList<Range> ranges = new ArrayList<>( List.of(qtr1, qtrSync, qtr2) );

        IntervalTree<Range> tree = new IntervalTree<>(ranges);

        // 0
        assertEquals(List.of(), tree.query(-1));
        assertEquals(List.of(qtr1), tree.query(0));
        assertEquals(List.of(qtr1), tree.query(1));

        // 240
        assertEquals(List.of(qtr1), tree.query(239));
        assertEquals(List.of(qtr1, qtrSync), tree.query(240));
        assertEquals(List.of(qtr1, qtrSync), tree.query(241));

        // 480
        assertEquals(List.of(qtr1, qtrSync), tree.query(479));
        assertEquals(List.of(qtrSync, qtr2), tree.query(480));
        assertEquals(List.of(qtrSync, qtr2), tree.query(481));

        // 720
        assertEquals(List.of(qtrSync, qtr2), tree.query(719));
        assertEquals(List.of(qtr2), tree.query(720));
        assertEquals(List.of(qtr2), tree.query(721));

        // 960
        assertEquals(List.of(qtr2), tree.query(959));
        assertEquals(List.of(), tree.query(960));
        assertEquals(List.of(), tree.query(961));
    }

    // convenience
    private <E extends Ranged> void assertMaxesAreCorrect(IntervalTree<E> tree) {
        assertMaxesAreCorrect(tree.getRoot());
    }

    /*
    Recurse through the tree and determine if the node stores the correct max (right-most
    point, i.e. range.high, of its subtree). Post-order traversal is used as the max of both left
    and right subtrees are needed before checking this node's max
    */
    private int assertMaxesAreCorrect(Node node) {

        // the tree cannot store negative values naturally
        if (node == null) { return -1; }

        int leftMax = assertMaxesAreCorrect(node.left);
        int rightMax = assertMaxesAreCorrect(node.right);
        int parentMax = node.getRange().getHigh();

        int maxOfSubtreesAndThis = Math.max(parentMax, Math.max(leftMax, rightMax));

        assertEquals(node.getMax(), maxOfSubtreesAndThis,
                node + " does not store the correct max of its subtrees"
        );

        return maxOfSubtreesAndThis;
    }

    // helper
    private <E extends Ranged> boolean isInOrder(IntervalTree<E> tree) {

        List<E> inOrderList = tree.toList();

        for (int i = 1; i < inOrderList.size(); i++) {
            E prev = inOrderList.get(i - 1);
            E curr = inOrderList.get(i);
            if (curr.getRange().compareTo(prev.getRange()) < 0) { return false; }
        }

        return true;
    }

}
