package com.example.reductor;

public class Note {

    int pitch_m;
    long start_m, end_m;

    Note(int pitch, long start, long end) {
        pitch_m = pitch;
        start_m = start;
        end_m = end;
    }

    @Override
    public String toString() {
        return "[" + pitch_m + ", " + start_m + ", " + end_m + "]";
    }

}