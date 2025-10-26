//package reductor.core;
//
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.Stack;
//
//import static reductor.core.RhythmTypeAlpha.*;
//
//
//@SuppressWarnings("unchecked")
//public class Phrase<T extends Ranged>  {
//
//    static void main(String[] args) {
//
//        //ArrayList<Ranged> list = Phrase.builder(0)
//        //        .timeSignature(4,4)
//        //        .keySignature("e")
//        //        .tempo(60)
//        //        .then(DOTTED_EIGHTH, "b3")
//        //        .then(SIXTEENTH, "b4")
//        //        .mark()
//        //        .then(DOTTED_HALF, "b4")
//        //        .then(QUARTER, "c")
//        //        .goToMark()
//        //        .then(EIGHTH,"g3", "b3", "e4")
//        //        .repeat(7)
//        //        .build();
//
//    }
//
//
//    private Phrase() { }
//
//    public static <T extends Ranged> PhraseBuilder<T> builder(long startTick) {
//        return new PhraseBuilder<>(startTick);
//    }
//
//    public static class PhraseBuilder<T extends Ranged>  {
//
//        private TimeSignature timeSig = null;
//        private Integer pickupBeats = null;
//        private KeySignature keySig = null;
//        private Tempo tempo = null;
//
//        private final long startTick;
//
//        private Range mark;
//
//        private final Stack<T> stack = new Stack<>();
//
//        private PhraseBuilder(long startTick) { this.startTick = Math.max(startTick, 0); }
//
//        // ====================================== METADATA ====================================== //
//
//        public PhraseBuilder<T> keySignature(String str) {
//            keySig = new KeySignature(str, new Range(startTick, Long.MAX_VALUE));
//            return this;
//        }
//
//        public PhraseBuilder<T> timeSignature(int numerator, int denominator) {
//            timeSig = new TimeSignature(numerator, denominator, new Range(startTick, Long.MAX_VALUE));
//            return this;
//        }
//
//        public PhraseBuilder<T> tempo(int bpm) {
//            tempo = new Tempo(bpm, new Range(startTick, Long.MAX_VALUE));
//            return this;
//        }
//
//        public PhraseBuilder<T> pickupOf(int numBeats) {
//            this.pickupBeats = numBeats;
//            return this;
//        }
//
//        // ======================================== THEN ======================================== //
//
//        public PhraseBuilder<T> then(String... pitches) {
//            RhythmTypeAlpha rhythm = stack.isEmpty()
//                    ? QUARTER
//                    : RhythmTypeAlpha.getEnumType(stack.peek().getRange().length() + 1L);
//            return then(rhythm, pitches);
//        }
//
//        public PhraseBuilder<T> then(Pitch... pitches) {
//            RhythmTypeAlpha rhythm = stack.isEmpty()
//                    ? QUARTER
//                    : RhythmTypeAlpha.getEnumType(stack.peek().getRange().length() + 1L);
//            return then(rhythm, pitches);
//        }
//
//        public PhraseBuilder<T> then(RhythmTypeAlpha rhythm, String... pitches) {
//
//            int[] arr = Arrays.stream(pitches)
//                    .mapToInt(s -> new Pitch(s).value())
//                    .toArray();
//
//            return then(rhythm, arr);
//        }
//
//        public PhraseBuilder<T> then(RhythmTypeAlpha rhythm, Pitch... pitches) {
//
//            long start = stack.isEmpty() ? 0 : stack.peek().getRange().high() + 1;
//
//            Note note = null;
//            Chord chord = null;
//            if (pitches.length == 1) {
//                note = Note.builder()
//                        .pitch(pitches[0])
//                        .start(start)
//                        .stop(start + rhythm.getDuration() - 1)
//                        .build();
//            } else {
//                chord = Chord.builder()
//                        .add(pitches)
//                        .start(start)
//                        .stop(start + rhythm.getDuration() - 1)
//                        .build();
//            }
//
//            T elem = null;
//            if (note != null) { elem = (T) note; }
//            if (chord != null) { elem = (T) chord; }
//
//            if (elem != null) { stack.add(elem); };
//
//            return this;
//        }
//
//        // ======================================== REPEAT ====================================== //
//
//        public PhraseBuilder<T> repeat() { return this.repeat(1); }
//
//        public PhraseBuilder<T> repeat(int n) {
//
//            for (int i = 0; i < n; i++) {
//
//                T lastAdded = stack.peek();
//
//                Note note = null;
//                Chord chord = null;
//
//                if (lastAdded instanceof Chord) {
//                    chord = Chord.builder((Chord) lastAdded).then().build();
//                } else if (lastAdded instanceof Note) {
//                    note = Note.builder((Note) lastAdded).then().build();
//                }
//
//                T elem = null;
//                if (note != null) { elem = (T) note; }
//                if (chord != null) { elem = (T) chord; }
//
//                if (elem != null) { stack.add(elem); };
//
//            }
//
//            return this;
//        }
//
//        // ======================================== MARK ======================================== //
//
//        public PhraseBuilder<T> mark() {
//
//            mark = stack.isEmpty()
//                    ? new Range(-1, -1, false)
//                    : new Range(startTick, stack.peek().getRange().high());
//            return this;
//        }
//
//        public PhraseBuilder<T> goToMark() {
//            T elem = null;
//            stack.add((T) mark);
//            return this;
//        }
//
//        // ======================================  BUILD  ======================================= //
//
//        public ArrayList<T> build() {
//
//            // Pre-process elems
//            ArrayList<T> out = new ArrayList<>(stack);
//            out.remove((T) mark);
//
//            handleMetaElements(out);
//
//            return out;
//        }
//
//        private void handleMetaElements(ArrayList<T> out) {
//
//            // Handle creation of Meta elements and final tick.
//            final long DEFAULT_END = timeSig != null
//                    ?  Piece.TPQ * (long) timeSig.numerator()
//                    :  Piece.TPQ * 4L;
//
//            final long FINAL_TICK = Math.max(stack.peek().getRange().high(), DEFAULT_END);
//
//            if (timeSig == null) {
//                timeSig = new TimeSignature(4, 4, new Range(startTick, FINAL_TICK));
//            }
//
//            if (pickupBeats != null) {
//
//                double multiplier = (double) pickupBeats / timeSig.denominator();
//                double length = multiplier * timeSig.range().length();
//                long endTick = (long) length - 1;
//
//                TimeSignature pickupSig = new TimeSignature(
//                        pickupBeats,
//                        timeSig.denominator(),
//                        new Range(startTick, endTick)
//                );
//
//                out.add((T) pickupSig);
//
//                timeSig = new TimeSignature(
//                        timeSig.numerator(),
//                        timeSig.denominator(),
//                        new Range(pickupSig.getRange().high() + 1, FINAL_TICK)
//                );
//                System.out.println();
//            }
//
//            if (keySig == null) {
//                keySig = new KeySignature("C", new Range(startTick, FINAL_TICK));
//            } else {
//                keySig = new KeySignature(
//                        keySig.accidentals(),
//                        keySig.mode(),
//                        new Range(this.startTick, FINAL_TICK)
//                );
//            }
//
//            if (tempo == null) {
//                tempo = new Tempo(120, new Range(startTick, FINAL_TICK));
//            } else {
//                tempo = new Tempo(
//                        tempo.getBpm(),
//                        new Range(this.startTick, FINAL_TICK)
//                );
//            }
//
//            out.add((T) timeSig);
//            out.add((T) keySig);
//            out.add((T) tempo);
//        }
//
//    }
//
//
//}
