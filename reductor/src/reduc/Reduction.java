//package reduc;
//
//class Reduction {
//
//    // This goes in reduc.Reductor
//    //public Midi reduce(int reductionLevel) {
//    //
//    //    if (reductionLevel < 0 || reductionLevel > 3) {
//    //        throw new RuntimeException("valid reduction values are 0-3; 0 returns the aggregate");
//    //    }
//    //
//    //    switch(reductionLevel) {
//    //        case 0 -> { return aggregate; }
//    //        case 1 -> { return Reduction.levelOne(aggregate, tree); }
//    //        case 2 -> { return Reduction.levelTwo(aggregate); }
//    //        case 3 -> { return Reduction.levelThree(aggregate); }
//    //        default -> { return null; }
//    //    }
//    //
//    //}
//
//
//    static Midi levelOne(Midi aggregate, IntervalTree tree) {
//        return null;
//    }
//
//    static Midi levelTwo(Midi aggregate) {
//        return null;
//    }
//
//    static Midi levelThree(Midi aggregate) {
//        return null;
//    }
//
//    // This is very old code
//    //private Midi convertListOfListsToMidiObject(ArrayList<ArrayList<Note>> listOfLists) {
//    //
//    //    Sequence seq;
//    //    try {
//    //        seq = new Sequence(
//    //                aggregate.getSequence().getDivisionType(),
//    //                aggregate.getSequence().getResolution()
//    //        );
//    //    } catch (InvalidMidiDataException e) {
//    //        throw new RuntimeException(e);
//    //    }
//    //
//    //
//    //
//    //    Track track = seq.createTrack();
//    //    ArrayList<MidiEvent> metas = original.getMetaEvents();
//    //    for(MidiEvent e : metas ) {
//    //        track.add(e);
//    //    }
//    //
//    //    for (int L = 0; L < listOfLists.size(); L++) {
//    //        ArrayList<Note> list = listOfLists.get(L);
//    //        ArrayList<MidiEvent> midiEvents = Note.notesToEvents(list);
//    //
//    //
//    //        for (MidiEvent e : midiEvents) {
//    //            track.add(e);
//    //        }
//    //
//    //    }
//    //
//    //    return new Midi(seq, "RED_" + original.getName());
//    //}
//
//}
