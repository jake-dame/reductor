## Questions

```java
// Event class
class Event {
  int value;
  long start, end;
  //...
}

// Easier for readability and maintability, but tree method logic may be more verbose in accessing things *through* the Event object. Is this also a performance/space concern?
class Node {
  Event data;
  Node left, right;
  //...
}

// this uses only primitives
class Node {
  int pitch;
  long start, end;
  Node left, right;
  //...
}
```

If I can't have the median element from the outset, I need the tree to be self-balancing. That won't really be a problem in this case though? The other classes kind of make sure that I will always have a full set prior to tree construction...

Self-balancing... AVL vs. Red-Black?
+ Construction will add most everything that is going to be added HOWEVER
+ Insertions/Deletions:
  + The custom algorithm will be looking to re-arrange "chords", thin them out, and perhaps look a number of chords behind/ahead to make decisions about how to re-arrange
  + I imagine this will be a process of deleting stuff that doesn't belong, and then re-inserting the "re-arranged" stuff

The java midi library helps in giving us a SET of midi events (no duplicates, and in increasing order by starting tick value).
+ When building the tree, I am targeting by:
  + Start value
  + If start values are equal, then end value
  + If end values are equal, then by pitch (low to high is left to right)
  + This means if the target was (C4, 0 480) and the item was (E4, 0, 480), the latter would become the right child. We are guaranteed there will not be more than one (C4, 0, 480).
    + Side-note: There may, however, be a (C4, 0, 479) hypothetically... so the custom algorithm might have to deal with "pseudo-duplicates" although I don't think this will be a huge problem

This introduces a lot of if-else stuff, and I though about just saying "sort by start value and placement after that is just arbitrary -- whatever child is not null, put it there." But, the whole point of making the binary search efficient is that the data is perfectly ordered (it is predictable where something will be). I may have a need to know exactly where a certain pitch is?

Operation stuff that return booleans?




-------------------------------------------------------------------------------------------------------









Best practice for constructors:
+ What should/shouldn't be in constructor logic
  + Metric: successfully building a blank slate object that user then has to use a bunch of setting methods on, or building a useful object but construction is complex?
+ Throws exceptions or handles them?

Use Path class/vs. strings
+ Probably not a huge difference if I'm not doing complex file system stuff, right? Or taking String filePath is considered sloppy?

Using `this.___` (as opposed to unique field pre-/post-fixes (e.g. `m_` or `_m` or `_`))
+ Pros: total disambiguation, makes reading faster/easier sometimes
+ Cons: verbose, if lots of fields being referenced, can make the code significantly more messy

Overall design of a program/project:
+ I have changed overall structure like 15 times so far because as things develop over time, certain pieces of code go in different places, certain encapsulation barriers no longer make sense, or inversely become sensible, etc.
+ Is this just poor planning on my part, something that takes years of practice to avoid, context-dependent, a little bit of both, or neither

    // TODO: find way to expose sequence safely

    //public Sequence getSequence() throws InvalidMidiDataException {
    //
    //    Sequence copy = new Sequence(sequence_m.getResolution(),
    //            sequence_m.getResolution(), sequence_m.getTracks().length);
    //
    //    for (Track track : sequence_m.getTracks()) {
    //        Track copyTrack = copy.createTrack();
    //        for (int j = 0; j < track.size(); j++) {
    //
    //            MidiEvent event = track.get(j);
    //            MidiMessage msg = event.getMessage();
    //
    //            MidiMessage copyMsg = null;
    //            switch (msg) {
    //                case ShortMessage sh -> { copyMsg = new ShortMessage(sh.getStatus(), sh.getData1(), sh.getData2()); }
    //                case MetaMessage meta -> { copyMsg = new MetaMessage(meta.getType(), meta.getData(), meta.getLength()); }
    //                case SysexMessage sys -> { copyMsg = new SysexMessage(sys.getStatus(), sys.getData(), sys.getLength()); }
    //                default -> { }
    //            }
    //
    //            MidiEvent copyEvent = new MidiEvent( copyMsg, event.getTick());
    //            copyTrack.add(copyEvent);
    //        }
    //    }
    //
    //    return copy;
    //}
