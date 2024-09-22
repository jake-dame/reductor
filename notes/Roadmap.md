# Roadmap

## Phase 1: Basic classes implemented

`Reductor` class will be the most-encompassing class:
+ Will contain:
  + Original in-Sequence
  + Aggregate Sequence (literal reduction)
  + The interval tree data structure
  + Several "reduction-level" (akin to -O1, -O2, etc.) algorithms which will return various sequences
  + Aggregation/sanitization functions
+ Purpose:
  + "Sanitize" and aggregate the in-Sequence
  + Facilitate/control access to what Sequences get fed into the tree structure (important because NoteTree will perform better and basically expect aggregated Sequence events, NOT raw MIDI data)

`Midi` class is a wrapper class intended to separate Sequence instances while providing sorted event lists from that Sequence:
+ Handles I/O more easily too, constructed with either a file path or another Sequence
+ Will probably end up as a private inner class to `Reductor`

`Note` class:
+ An intermediate class that may make it easier to build the tree
+ Contains a basic struct-like `Note`
+ Two static concersion methods (list of MidiEvents -> list of Notes, and vice verse)

`NoteTree` class will just be the interval tree data structure
+ Main constructor takes a list of `Note` objects
+ Capabilities:
  + Build balanced tree (midi data should already be sorted...)
  + Perform range queries
  + Remove nodes (whole point of the program)
+ The tree must be converted back into Notes or directly to MidiEvents, although I'm not sure where this will happen yet

`MidiUtility` class:
+ Mostly printing/debugging helpers
+ Hashmaps for various MIDI bytes -> String
+ "Wrapper" methods for play() and write() that operate just need Sequence objects passed to them

## Phase 2: Reduction algorithms

2-3 reduction algorithms that reduce the literal reduction (aggregate) progessively aggressively
+ The user will pick, and a new Sequence is produced every time
  + Something like `public Sequence getReduction(int reductionLevel)` which will dispatch to whatever method to do
  + Will cascade? If the 2nd is picked, the 1st will also do its thing

## Phase 3: UI (if time)

Desktop app for me.

UI would just need:
+ A file picker -> select MIDI
+ Specify a directory to write out to
+ Specify an optimization level

Then, some script for:
+ MIDI gets opened in GarageBand/MuseScore
+ The score from the MIDI will pop up
+ Save that score to a PDF
+ Output the PDF instead of just a MIDI

---

# Algorithm

Notes/sketches/questions specifically pertaining to the non-piano to piano reduction algorithm.

Also tries to establish some terminology that might make writing about this stuff easier here and later in documentation.

## Overview & Terminology

General:
+ Score: the whole thing
+ Reduction: used almost exclusively for non-piano to piano works; sometimes used interchangeably with transcription, but more literal/utilitarian than a transcription
+ Transcription: technically any instrument-to-other-instrument work, but mostly used in a piano context; sometimes used interchangebly with reduction, but little --> lot more editorializing usually

Polyphony stuff:
+ Voices: general term for discrete instruments/voices (i.e. choral) in a score
+ Voicing: it's own distinct term referring to the "positional" relationship of the same tones in a chord; usually in the context of hierarchy of important intervals within thicker chord
+ Spelling: defining chords in certain ways/inversions; e.g., a CM chord is "spelled" C-E-G

Algorithm stuff:
+ Aggregate: process of getting all notes at a given time represented as a single chord
+ Chord: chord
+ Chord-texture: vague term I am coming up with now to mean a bunch of tones spread over time and also there is a rhythm component to it; important for thinning decisions
+ Duplicate: the same tone, in the same register, in different voices
+ Doubling/tripling: the same tone, in different registers, in different voices
+ Thinning: this will be the bulk of the algorithms rules, and how it is done will vary
  + Subtractively: thinning out chords by removing duplicates, doubles, or triples, or prioritizing important voices at the expense of less important voices
  + Creatively: thinning out chords/textures using more liberal approaches (in relation to the source material), e.g., arpeggiation
+ Prioritization: when a choice has to be made somebody has got to go
+ Arpeggiation: spreading a single chord out over time (each note is played one at a time, over time); can be ascending, descending, or often just both
+ Tremolo/trill: split a chord any which way and those two pieces oscillate over time
+ Alberti bass: a very piano-specific term but it's a specific arpeggiation pattern named after a guy and 18th centruy keyboard music is littered with it; it's basically just low-high-mid-high pattern for a 3-piece chord, over and over again

### Example: Mozart Symphony No. 40 (i), mm. 1-4; LH

Here are a bunch of reductions (called so because they - for the most part - try to transcribe the orchestral score literally to piano) for a Mozart symphony (orchestral work) by a bunch of different guys who took different approaches to translating it to piano-ese.

Criteria is mostly subjective, but largely based around playability and how closely it mimics the original textures of the score. The first one is chosen as best because:
+ It mimics the texture of the score (in terms of voicing, i.e. 1-3-8) and not resorting to "comfier" (for the hand) reorganizations of the tones. 
+ It incorporates all voices (1 bass + 1 cello (duplicate) + 1 viola + 1 viola - playing divisi)
+ It thins out the texture of the divisi violi through arpeggiation


**Original orchestral score:**
![](res/Mozart_40/full.png)
+ Violas are divisi
+ Cellos+Basses are doubled up exactly

**Best:**
![](res/Mozart_40/stradal.png)
+ The ossia (italian referring basically to "and here's the harder version if you're interested") passage is *nearly* (i.e. "too") literal, and all but unplayable (at tempo, for sane people)
+ The G is kept as a sustain note, therefore omitted
+ The 1-3-8 chord-texture is thinned out over over time (i.e. arpeggiated)

**Good:**
![](res/Mozart_40/hummel.png)
+ I don't like the arpeggiation (not totally correct voicing of chord-texture) but rhythm and other stuff mostly well-represented (except bass jumps)

**Bad:**
![](res/Mozart_40/novegno.png)
+ Truly a nightmare, this has and never will be performed by a human

The rest destroy the voicing of the chord-texture (resort to 1-3-5) and introduce lazy thinning techniques (e.g. tremolo in place of arpeggiation)
![](res/Mozart_40/godowsky.png)
![](res/Mozart_40/horn.png)
![](res/Mozart_40/meves.png)
![](res/Mozart_40/wenzel.png)

## Rules

1. Exact duplicates --> keep 1
2. Octave duplicates --> keep < 5 if LH range < 8 && RH range < 8
3. Repeated notes like EEGGEEGG are not fun on piano and should be EGEGEGEG if possible
4. Deciding between tremolo and arpeggiation
5. Divisi separation
6. Register prioritization
7. Interval prioritization
