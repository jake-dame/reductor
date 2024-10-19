package reductor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Comparator;

import static org.junit.jupiter.api.Assertions.*;


class IntervalTreeTest {

    /*
        If the member notes are inserted in breadth-first order (according to their name), the
        template tree should look like this:

                       _____10,20_____
                      /               \
                10,15                  10,25
               /     \                /     \
           05,20      10,17      10,22       15,20
           /  \        /  \       /  \        /  \
             07,20                        12,20


        New insertion strategy (using medians) does this instead:

                       _____10,20_____
                      /               \
                  7,20                 10,25
                /     \               /     \
           05,20      10,15      10,22       12,20
           /  \        /  \       /  \        /  \
                          10,17                 15,20

    */


    /// Pitch constants
    static final int C = 0x3C;
    static final int D = 0x3E;


    /// The tree above with pitches of C
    private ArrayList<Note> testNotes;

    /// Note objects with the same range but different pitches (pitches of D)
    private ArrayList<Note> quasiDups;

    /// Exact duplicates of {@code testNotes}
    private ArrayList<Note> dups;

    /// Notes for {@code testNotes}
    Note root;
    Note l;
    Note r;
    Note ll;
    Note lr;
    Note rl;
    Note rr;
    Note llr;
    Note rrl;

    /// Notes for {@code quasiDups}
    Note root_sameRange;
    Note l_sameRange;
    Note r_sameRange;
    Note ll_sameRange;
    Note lr_sameRange;
    Note rl_sameRange;
    Note rr_sameRange;
    Note llr_sameRange;
    Note rrl_sameRange;

    /// Notes for {@code dups}
    Note root_dup;
    Note l_dup;
    Note r_dup;
    Note ll_dup;
    Note lr_dup;
    Note rl_dup;
    Note rr_dup;
    Note llr_dup;
    Note rrl_dup;

    /// The tree
    private IntervalTree<Note> testTree;

    @BeforeEach
    void setup() {

        testNotes = new ArrayList<>();
        quasiDups = new ArrayList<>();
        dups = new ArrayList<>();


        root = new Note(C, new Range(10,20));
        l = new Note(C, new Range(10,15));
        r = new Note(C, new Range(10,25));
        ll = new Note(C, new Range(5,20));
        lr = new Note(C, new Range(10,17));
        rl = new Note(C, new Range(10,22));
        rr = new Note(C, new Range(15,20));
        llr = new Note(C, new Range(7,20));
        rrl = new Note(C, new Range(12,20));

        root_sameRange = new Note(root);
        root_sameRange.setPitch(D);
        l_sameRange = new Note(l);
        l_sameRange.setPitch(D);
        r_sameRange = new Note(r);
        r_sameRange.setPitch(D);
        ll_sameRange = new Note(ll);
        ll_sameRange.setPitch(D);
        lr_sameRange = new Note(lr);
        lr_sameRange.setPitch(D);
        rl_sameRange = new Note(rl);
        rl_sameRange.setPitch(D);
        rr_sameRange = new Note(rr);
        rr_sameRange.setPitch(D);
        llr_sameRange = new Note(llr);
        llr_sameRange.setPitch(D);
        rrl_sameRange = new Note(rrl);
        rrl_sameRange.setPitch(D);

        root_dup = new Note(root);
        l_dup = new Note(l);
        r_dup = new Note(r);
        ll_dup = new Note(ll);
        lr_dup = new Note(lr);
        rl_dup = new Note(rl);
        rr_dup = new Note(rr);
        llr_dup = new Note(llr);
        rrl_dup = new Note(rrl);


        testNotes.add(root);
        testNotes.add(l);
        testNotes.add(r);
        testNotes.add(ll);
        testNotes.add(lr);
        testNotes.add(rl);
        testNotes.add(rr);
        testNotes.add(llr);
        testNotes.add(rrl);

        quasiDups.add(root_sameRange);
        quasiDups.add(l_sameRange);
        quasiDups.add(r_sameRange);
        quasiDups.add(ll_sameRange);
        quasiDups.add(lr_sameRange);
        quasiDups.add(rl_sameRange);
        quasiDups.add(rr_sameRange);
        quasiDups.add(llr_sameRange);
        quasiDups.add(rrl_sameRange);

        quasiDups.addAll(testNotes);

        dups.add(root_dup);
        dups.add(l_dup);
        dups.add(r_dup);
        dups.add(ll_dup);
        dups.add(lr_dup);
        dups.add(rl_dup);
        dups.add(rr_dup);
        dups.add(llr_dup);
        dups.add(rrl_dup);

        dups.addAll(quasiDups);

    }


    @Test
    void testConstructWithTestNotes() {

        IntervalTree<Note> testTree = new IntervalTree<>(testNotes);

        assertTrue(orderIsMaintained(testTree), "in order traversal list should be in ascending order by Comp");
        assertEquals(testNotes.size(), testTree.numNodes(), "number of nodes at this point should be equal to testNotes size (1:1 note:node)");
        assertEquals(testNotes.size(), testTree.numElements(), "number of elements between tree and list should match");

    }

    @Test
    void testConstructWithQuasiDups() {

        IntervalTree<Note> testTree = new IntervalTree<>(quasiDups);

        assertTrue(orderIsMaintained(testTree), "in order traversal list should be in ascending order by Comp");
        assertEquals(testNotes.size(), testTree.numNodes(), "number of nodes at this point should still be equal to testNotes size (2 notes per node, but node count should not have changed)");
        assertEquals(quasiDups.size(), testTree.numElements(), "number of elements between tree and list should match");

    }


    @Test
    void testConstructWithDups() {

        IntervalTree<Note> testTree = new IntervalTree<>(dups);

        assertTrue(orderIsMaintained(testTree), "in order traversal list should be in ascending order by Comp");
        assertEquals(testNotes.size(), testTree.numNodes(), "number of nodes at this point should still be equal to testNotes size (2 notes per node, but node count should not have changed; dups should not have been added)");
        assertEquals(quasiDups.size(), testTree.numElements(), "number of elements between tree and list should match");

    }



    boolean orderIsMaintained(IntervalTree<Note> tree) {

        // Notes are Comparable by pitch, not range. And since toList returns Notes, this seemed easiest
        var comp = new Comparator<Note>() {
            @Override public int compare(Note n1, Note n2) {
                if (n1.start() == n2.start()) {
                    return Long.compare(n1.stop(), n2.stop());
                }
                return Long.compare(n1.start(), n2.start());
            }
        };


        ArrayList<Note> inOrderList = tree.toList();

        for (int i = 1; i < inOrderList.size(); i++) {
            Note prev = inOrderList.get(i - 1);
            Note curr = inOrderList.get(i);

            if (comp.compare(curr, prev) < 0) {
                return false;
            }

        }

        return true;

    }


    @Test
    void testQuery() {



    }


}