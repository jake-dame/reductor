# Capstone Project: To-Piano Reduction Generation

Road map for the project (living document).

Will start with Part 2. 

Part 1 and 3 (automation of score/MIDI retrieval and pdf output) will be implemented depending on how quickly Part 2 is done.

## High-Level Plan for Desktop App

Java, using javax MIDI library for manipulation of MIDI data

1. UI
  + Button 1: File picker
  + Button 2: Browse IMSLP (for score) or classicalarchives.org (for MIDI)
  + After UI, we end up with a MIDI
    + If BYOM, we're done
    + If just a score, will need to:
      + Connect to musescore.com for their score-from-MIDI feature
      + Produce a MIDI

*--Have MIDI now--*

2. Run through algorithm
   + Pass through aggregation logic once (raw output, just merging)
   + If still too dense, pass through multiple times the thinning algorithm (monophonic is lower limit)
   + Each pass-through will remove/combine notes based on algorithm rules

*--Have altered MIDI now--*

3. Produce a score from MIDI file (again, through musescore.com) as a pdf file to some directory

*--Have score now--*

## Problems

1. Labeling melody (riskiest)
   + This should be the highest-priority line to keep. If this disappears somehow, the reduction is useless
   + Problem 1: unlabeled in MIDI file (all tracks are equal)
   + Problem 2: melody sharing (melody jumps around from part to part and is not confined to a single track)
     + This may be impossible to do algorithmically. These works may just be failure to process works
2. Accessing, browsing, and downloading from multiple websites through the desktop app
   + Is this as simple as having some kind of safari window in the desktop app? Want it to be as automated as possible and have all downloads from the desktop app go to the same file every time without having to manually select it