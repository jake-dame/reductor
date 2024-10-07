package reductor;

import java.util.ArrayList;

public class Node {

    Interval interval;

    // Max endpoint in subtree rooted at this node (used to ignore left pathways)
    long max;

    // To deal with the many expected cases where `Interval` objects will have identical ranges but
    //     different associated data, such as: [0,480] + data1; [0,480] + data2; etc., this list will
    //     store such cases, rather than try and add them as separate nodes, which would greatly
    //     increase the height of the tree. Also, when searching for overlapping intervals, I can
    //     just return the entire list, since they will all have the same range.
    //     There will be no exact duplicates within the scope of this application.
    ArrayList<Interval> list;

    Node left, right, parent;

    int depth;

    Node(Interval interval) {

        this.interval = interval;
        // This node's max is equivalent to the interval.high it was just created with, at this point in time.
        this.max = this.interval.end;
        this.left = null;
        this.right = null;
        this.parent = null;
        this.list = new ArrayList<>();
        this.depth = -1;
    }

    int size() {

        return list.size();
    }

    void addToList(Interval interval) {

        this.list.add(interval);
    }

    boolean contains(Interval interval) {

        for (Interval I : list) {
            if (I.compareTo(interval) == 0) {
                return true;
            }
        }

        return false;
    }

    void removeFromList(Interval interval) {

        Interval intervalToRemove = new Interval();
        for (Interval i : this.list) {
            if (interval.compareTo(i) == 0 && interval.note.pitch == i.note.pitch) {
                intervalToRemove = i;
                break;
            }
        }

        this.list.remove(intervalToRemove);
    }

}
