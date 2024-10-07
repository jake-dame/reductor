package reductor;

import java.util.ArrayList;

/*
    Primary ordering:   by low value
    Secondary ordering: by high value

              Current Node   Interval to add      Action:
    Case 1.1:   [10,20]          [10,15]          Look left
    Case 1.2:   [10,20]          [5,20]           Look left
    Case 2:     [10,20]          [10,20]          Add to Current Node
    Case 3.1:   [10,20]          [10,25]          Look right
    Case 3.2:   [10,20]          [15,20]          Look right
*/

/**
 * Yes.
 */
public class IntervalTree {

    Node root;
    int nodes;
    int events;

    IntervalTree() {
        nodes = 0;
        events = 0;
        root = null;
    }

    ArrayList<Interval> query (long begin, long end) {
        var list = new ArrayList<Interval>();
        if (root == null) { return list; }
        Interval query = new Interval(begin, end - 1);
        return query(root, query, list);
    }

    ArrayList<Interval> query (Note note) {
        Interval interval = new Interval(note);
        return query(interval);
    }

    ArrayList<Interval> query (Interval interval) {
        return query(interval.begin, interval.end);
    }

    private ArrayList<Interval> query (Node node, Interval query, ArrayList<Interval> list) {

        if( query.overlaps(node.interval) ) {
            list.addAll(node.list);
        }

        // base case: any matches that should have been added to the list are in the list, and we can go no further
        if(node.left == null  &&  node.right == null) {
            return list;
        }

        // Use max of left subtree to potentially ignore left subtree
        if (node.left != null  &&  query.begin <= node.left.max) {
            query(node.left, query, list);
        }

        // Else, use the natural ordering and traverse the right subtree
        if (node.right != null  &&  query.compareTo(node.interval) >= 0) {
            query(node.right, query, list);
        }

        return list;
    }

    // this is mostly for testing purposes
    ArrayList<Interval> queryAll (ArrayList<Note> notes) {
        var list = new ArrayList<Interval>();

        for (Note note : notes) {
            var sublist = query(note.begin, note.end);
            if (sublist == null) {
                continue;
            }
            list.addAll(sublist);
        }

        return list;
    }

    boolean add(Note note) {
        return add(new Interval(note));
    }

    private boolean add(Interval interval) {
        return recurseAdd(root, interval, 0);
    }

    private boolean recurseAdd(Node node, Interval interval, int depth) {

        // Handle the case where the tree is new
        if (root == null) {
            root = new Node(interval);
            root.addToList(interval);
            root.depth = depth;
            nodes++;
            events++;
            return true;
        }

        // Handles case 2
        if(interval.compareTo(node.interval) == 0) {

            if(interval.note.pitch == node.interval.note.pitch) {
                return false;
            }

            node.addToList(interval);
            events++;
            return true;
        }

        // Update the max at every node visited, if applicable
        if (node.max < interval.end) {
            node.max = interval.end;
        }

        // Handles cases 1.1, 1.2
        if (interval.compareTo(node.interval) < 0) {
            if (node.left == null) {
                return appendLeftChild(node, interval, ++depth);
            } else {
                return recurseAdd(node.left, interval, ++depth);
            }
        }
        // Handles cases 3.1, 3.2
        else {
            if (node.right == null) {
                return appendRightChild(node, interval, ++depth);
            } else {
                return recurseAdd(node.right, interval, ++depth);
            }
        }

    }

    private boolean appendLeftChild(Node node, Interval interval, int depth) {
        node.left = new Node(interval);
        node.left.addToList(interval);
        nodes++;
        events++;
        node.left.parent = node;
        node.left.depth = depth;
        return true;
    }

    private boolean appendRightChild(Node node, Interval interval, int depth) {
        node.right = new Node(interval);
        node.right.addToList(interval);
        nodes++;
        events++;
        node.right.parent = node;
        node.right.depth = depth;
        return true;
    }

    boolean remove(Note note) {
        if(note == null || root == null)  return false;
        return remove(root, new Interval(note));
    }

    private boolean remove(Interval interval) {
        if(interval == null || root == null)  return false;
        return remove(root, interval);
    }

    private boolean remove(Node node, Interval interval) {

        if (interval.compareTo(node.interval) == 0) {
           if (node.contains(interval)) {
               if (node.list.size() == 1) {
                    return removeNode(node);
               } else {
                   node.removeFromList(interval);
                   events--;
                   return true;
               }
           } else {
               return false;
           }
        }
        else if (interval.compareTo(node.interval) < 0) {
            if (node.left == null) {
                return false;
            }

            return remove(node.left, interval);
        }
        else {
            if (node.right == null) {
                return false;
            }

            return remove(node.right, interval);
        }
    }

    private boolean removeNode(Node node) {

        // Handle removal of last node.
        if (nodes == 1) {
            root = null;
            nodes--;
            events--;
            return true;
        }

        // Node to remove has 0 children.
        if ( node.right == null  &&  node.left == null ) {
            if (node.interval.compareTo(node.parent.interval) < 0) {
                node.parent.left = null;
            } else {
                node.parent.right = null;
            }
            nodes--;
            events--;
            return true;
        }

        // Node to remove has only a left child.
        else if ( node.left != null  &&  node.right == null ) {
            node.left.parent = node.parent;
            if (node.parent != null  &&  node.interval.compareTo(node.parent.interval) < 0) {
                node.parent.left = node.left;
            }
            else if(node.parent != null  &&  node.interval.compareTo(node.parent.interval) > 0) {
                node.parent.right = node.left;
            }
            else {
                root = node.left;
            }
            nodes--;
            events--;
            return true;
        }

        // Node to remove has only a right child.
        else if ( node.left == null ) {
            node.right.parent = node.parent;
            if(node.parent != null  &&  node.interval.compareTo(node.parent.interval) < 0){
                node.parent.left = node.right;
            }
            else if(node.parent != null  &&  node.interval.compareTo(node.parent.interval) > 0) {
                node.parent.right = node.right;
            }
            else {
                root = node.right;
            }
            nodes--;
            events--;
            return true;
        }

        // Node to remove has 2 children.
        else {
            if (node.left.right != null) {
                Node predecessor = findMax(node.left);
                Interval interval = predecessor.interval;
                ArrayList<Interval> list = predecessor.list;
                remove(predecessor.interval);
                node.list = list;
                node.interval = interval;
            }
            else if (node.right.left != null) {
                Node successor = findMin(node.right);
                Interval interval = successor.interval;
                ArrayList<Interval> list = successor.list;
                remove(successor.interval);
                node.list = list;
                node.interval = interval;
            }
            else {
                Interval interval = node.left.interval;
                ArrayList<Interval> list = node.left.list;
                remove(node.left.interval);
                node.list = list;
                node.interval = interval;
            }
        }

        return true;
    }

    private Node findMax(Node node) {
        while (node.right != null) { node = node.right; }
        return node;

    }

    private Node findMin(Node node) {
        while ( node.left != null ) { node = node.left; }
        return node;
    }

    boolean addAll(ArrayList<Note> notes) {
        if (notes == null || notes.isEmpty())  { return false; }
        boolean changed = false;
        for (Note note : notes) { changed = add( new Interval(note) ); }
        return changed;
    }

    boolean removeAll(ArrayList<Note> notes) {
        if (notes == null || notes.isEmpty())  { return false; }
        boolean changed = false;
        for (Note note : notes) { changed = remove(note); }
        return changed;
    }

    void print() {
        print(root);
    }

    private void print(Node node) {
        if (node.left != null)  print(node.left);
        for(Interval I : node.list) {
            System.out.println(I);
        }
        if (node.right != null)  print(node.right);
    }

    ArrayList<Note> toArrayList() {
        ArrayList<Note> list = new ArrayList<>();
        arrayListTraversal(root, list);
        return list;
    }

    private void arrayListTraversal(Node node, ArrayList<Note> list) {

        if ( node == null ) { return; }

        arrayListTraversal(node.left, list);

        ArrayList<Note> notes = new ArrayList<>();
        for (Interval I : node.list) {
            notes.add(I.note);
        }
        list.addAll(notes);

        arrayListTraversal(node.right, list);

    }

}