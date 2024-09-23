package reduc;

import java.util.ArrayList;

public class IntervalTree {

    public static class Node {

        public Event event;
        public Node left, right;

        Node() {
            event = null;
            left = null;
            right = null;
        }

        Node(Event event) {
            this.event = event;
            left = null;
            right = null;
        }

    }

    public Node root;
    public int size;

    IntervalTree() {
        size = 0;
        root = null;
    }

    IntervalTree(ArrayList<Event> events) {
        size = 0;
        root = null;
        if(!addList(events)) throw new RuntimeException(":(");
    }

    // I wrote everything below before I remembered Comparables /////////////////////////////////////////

    public boolean add(Event event) {

        Node parent = search(root, event);

        if (parent == null) {
            System.out.println("DUPLICATE");
            return false;
        }

        if (event.start < parent.event.start) {
            parent.left = new Node(event); size++; return true;
        } else if (event.start > parent.event.start) {
            parent.right = new Node(event); size++; return true;
        } else {

            if (event.end < parent.event.end) {
                parent.left = new Node(event); size++; return true;
            } else if (event.end > parent.event.end) {
                parent.right = new Node(event); size++; return true;
            } else {

                if (event.pitch < parent.event.pitch) {
                    parent.left = new Node(event); size++; return true;
                } else {
                    parent.right = new Node(event); size++; return true;
                }
            }
        }

    }

    public boolean addList(ArrayList<Event> events) {

        if (root != null) {
            throw new RuntimeException("can't do that right now");
        }

        Event medianElement = events.get(events.size() / 2);
        root = new Node(medianElement);
        size++;

        events.remove(medianElement);

        boolean added = false;
        for(Event event : events) {
            if (add(event))  added = true;
        }
        return added;
    }

    public Node search(Node node, Event event) {

        long target = node.event.start;
        long val = event.start;

        if (val < target) {
            return node.left == null ? node : search(node.left, event);
        } else if (val > target) {
            return node.right == null ? node : search(node.right, event);
        } else { // (note.start == node.start)

            target = node.event.end;
            val = event.end;

            if (val < target) {
                return node.left == null ? node : search(node.left, event);
            }else if (val > target) {
                return node.right == null ? node : search(node.right, event);
            } else { // (note.end == node.end)

                target = node.event.pitch;
                val = event.pitch;

                if (val < target ) {
                    return node.left == null ? node : search(node.left, event);
                } else if (event.pitch > node.event.pitch) {
                    return node.right == null ? node : search(node.right, event);
                } else { // total duplicate
                    return null;
                }
            }
        }
    }

    // in-order
    public void print() {
        if (root == null)  throw new RuntimeException("no");
        recurse(root);
    }

    private void recurse(Node node) {
        if (node.left != null)  recurse(node.left);
        System.out.println(node.event);
        if (node.right != null)  recurse(node.right);
    }

}