package reduc;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static reduc.IntervalTree.Node;

public class IntervalTreeTest {

    public static final int C = 0x3C;
    public static final int D = 0x3E;
    public static final int E = 0x40;


    @Test
    public void testDuplicate() {

        Event median = new Event(60, 10, 20);
        IntervalTree tree = new IntervalTree();
        tree.root = new Node(median);

        Event c = new Event(C, 0, 10);
        Event d = new Event(D, 20, 30);

        tree.add(c);
        tree.add(d);

        Event diffEnd = new Event(D, 20, 31);
        assertTrue(tree.add(diffEnd));

        Event diffPitch = new Event(E, 20, 30);
        assertTrue(tree.add(diffPitch));

        Event diffStart = new Event(D, 21, 30);
        assertTrue(tree.add(diffStart));

        Event dup = new Event(D, 20, 30);
        assertFalse(tree.add(dup));

        tree.print();
    }

}
