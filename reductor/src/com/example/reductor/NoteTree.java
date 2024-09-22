package com.example.reductor;

import java.util.ArrayList;

public class NoteTree {

    private static class Node {

        int data;
        long start;
        long end;

        Node left;
        Node right;

        Node(int data, long start, long end) {
            this.data = data;
            this.start = start;
            this.end = end;

            this.left = null;
            this.right = null;
        }
    }

    private final Node root;
    private int size;

    NoteTree() {
        root = null;
        size = 0;
    }

    NoteTree(ArrayList<Note> notes) {
        root = null;
        size = 0;
    }

    private void add(Note note) {

    }

    private void addList(ArrayList<Note> notes) {
        for (Note note : notes) {
            add(note);
        }
    }

}