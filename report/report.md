# Capstone: Final Report

The internal representation stuff starts [here](#reductorpiece).

If anything, you should definitely check out [Appendix B](#appendix-b-cool-reductions) for some cool YouTube/Spotify links. I think they're pretty cool.

## MIDI

My program chopped off two large sections from the whole of MIDI protocol to work with:
+ I exclude any real-time (a.k.a. Sysex message) stuff
+ I do not support SMPTE timing

### Protocols

As a quick overview, the MIDI standard has two top-level protocols:
+ SMF (Standard MIDI File) protocol: how performance/MIDI data is stored in a static file
+ Wire protocol: the same, but how it is sent over the wire and to be interpreted by MIDI-capable devices/instruments

Furthermore, MIDI messages fall into 3 categories:
+ Channel (also called voice) messages: note stuff and how stuff is to sound, basically
+ Meta messages: metadata stuff (time signatures, key signatures, tempo, track name, etc.)
  + Only stored in SMFs, *never* sent across the wire (because it has no effect/nothing to do with how stuff sounds)
+ Sysex messages: not even sure. But real-time performance stuff
  + Never stored in SMFs, always sent across the wire

![midi messages venn](images/midi_messages.png)

### Division Types

Finally, in regards to my program, I decided not to support SMPTE timing on two painstakingly and carefully considered criteria:
+ It's really, really rare, especially for my use cases (amateurs notating, deriving, and posting MIDI files)
+ It's hard

(I have also seen PPQ (Pulses-per-Quarter) referred to as ticks-per-quarter (TPQ) or ticks-per-quarter-note (TPQN). I tend towards TPQ, as "pulses" may make sense in terms of MIDI beat clock stuff, but is really a misnomer from normal musician POV.)

MIDI **resolution** (in the context of PPQ division type) essentially defines the duration of a quarter note, in ticks. 

This has implications for how much you can subdivide a quarter note. For example, a resolution of 24 means that the lowest note value you can represent before resorting to fractional ticks (which are not even supported in MIDI -- they will always be rounded, depending on the sequencing software) is a 16th note:

  24 ticks == quarter note
  12 ticks == 8th note
  6 ticks == 16th note
  3 ticks == 32nd note
  1.5 ticks == rounds down to 1, usually == ?

Ticks, of course, are just an abstraction from the microsecond. Each tick has an exact conversion to microseconds. You could have a resolution of 500,000 but it would be a nightmare for audio engineers or developers and probably sequencing software to have to deal with, especially for long pieces. You may even start getting into overflow issues, depending on whichever numerical type the software uses. The higher the resolution, however, the more expressive and subtle timing differences (in a recording of a performance or in manipulation in a DAW) can be represented. Most of the time, small tick differences are going to be imperceptible to the human ear and the bigger problem is **time drift** (accumulated rounding corrections or poor quantization leading to actual inaccuracies in note values or timing).

The 120-960 range seems to be the sweet spot (they are what I have seen the most). 480 is the conventional default resolution, and it translates to exactly 500,000 microseconds per quarter note. 480 is not amazingly fine-grained, and not super coarse.

![divison type flow](images/divisionType.png)

### Quantization

MIDI was designed, and is still best, for transmitting and storing exact details of a real-time performance, e.g.:
+ The exact velocity (i.e. volume) of a key being pressed --> some integer between 0 and 127
+ The exact time a key was pressed --> some integer as a delta time from the last event
+ The exact time a key was released --> "   "

It was never meant to translate perfectly to notation on a musical score. It is more concerned with accurate playback.

The last point is the reason quantization exists and is needed for notating (on a musical score) or visualizing (e.g. as rectangles/bars in some DAWs) MIDI files in any meaningful way.

Humans are not perfect, and do not play perfectly. If a quarter note is defined as 480 ticks, a performer will release a key *around* 480 ticks after they pressed it - say 478, 483, or some other close value.

This does not translate well to notation software, because there is no original source of truth indicating what the note was *supposed* to be.

For instance, say notation software gets a note whose length is 171. We will define a quarter note as 480 ticks:
+ The example note is close to 180, which is the exact length of a dotted 16th note, a very common rhythm
+ The example note is also close to 160, which is the exact length of a triplet 8th note, another very common rhythm

The is a somewhat simplified example, and it may be intuitive to say: "It is closer to 180 - clearly the performer was executing a dotted 16th".

It should be noted that, as mentioned in an earlier section, and somewhat dependent on the tempo and resolution, a difference of 10 ticks will be imperceptible to the human ear 99% of the time. However, on paper, a dotted 16th and triplet 8th is a *huge* difference rhythmically.

Another huge issue is that notation programs are free to implement start/stop ticks however they want (MIDI standard does not specify any sort of convention, for obvious reasons - think legato, overlapping effects, etc.). My blood froze late in the process of writing this program when I started encountering MIDI files whose tick values perfectly overlapped (in:re preceding and current note), e.g.:
+ File notated using Program A encodes all its notated notes as rhythm values with start/stops ON the grid (always)
+ File notated using Program B encodes all its notated notes as rhythm values where only the start of each note is on the grid (and the off is -1 before the next note)

This essentially broke my program, since all rhythmic ranges in my program are encoded as half-open (when considering a length 480 in [0,480]). In many of the range-constructing/deriving functionalities in various parts of the program, I was getting invalid interval exceptions for stuff like [480,480] or [481,480].

In fact, it was the definition of a Range issue (and not perfect rhythms) that prompted the quantizing functionality at the last minute.

#### Workarounds

I looked first for libraries that quantized. There were a couple options, but none of them looked easy enough to integrate with my program. In fact, the best matches ("I give you a range, you quantize it"), were not in Java, and with my time-crunch, I chose the lesser of two evils:
+ Learn (for the first time) how to write JNI stuff for a C++ quantization library
+ Implement my own (even if simplified and not the most robust)

I chose the latter.

#### Implementation

To implement this, you could go the route of having some map of determined rhythm values, and some heuristic/algorithmic approaches to determining acceptable threshold or MOE values when mapping incoming note lengths to what they "probably" are. 

This work well most of the time. Professional grade notation software, however, has to be able to support (nearly) any type of rhythm the author can conjure. This means your quantization map needs to account for a quintuplet-dotted-64th note. 

The problem you run into is that a lot of the pre-determined note values start getting close enough in range that they either start to overlap, or screw up the MOE calculations. You could adjust the MOE to be logarithmic or something, so that it too adjusts based on the initial length of the note, but you can't fully escape the principle of: the more granular the rhythms you want to support (no matter how exotic are frequently seen in practice), the more your accuracy *can* suffer.

Even in professional software, you can get a wacky MIDI file, and click quantize, even with various granularity options, and it will be almost equally as wacky after quantization.

Furthermore, there is no way to tell if they performer simply executed a rhythm with a truncating or extending articulation (staccato or legato, respectively), which adds yet another "impossible-to-perfect" element to quantization implementation. (The *actual* lesson here is MIDI is never meant to be perfectly notated - a theme that will be visited several times in this report).

My quantization function, which seems to work a lot of the time (although not vigorously tested as it was a late addition to the program), started out as around 300 lines of code. It originally intended to preserve the original resolution, but when I realized that even MuseScore (when outputting MIDI based on a notated score) basically just converts everything to a resolution of 480, I decided to just scale everything to 480 myself, and stop caring about the in-resolution. 

Uniform scaling did simplify things, and after several other realizations (that also had to do with unnecessary transformations - quantizing in the end is a pretty simple formula) the function diminished a lot in size.

The code (omitted getter/setter, the Range class is actually immutable and doesn't actual have setters shown below; inclusive vs. exclusive correction) is as follows:

```java
public static Range quantize(Range inRange, int inResolution) {

    /*
    Philosophically, this should be a double. In MIDI, it really won't make a difference 
    and will prevent some heartache and unnecessary coddling of data types and fractional 
    amounts that get thrown away in the end anyway.
    */
    long scale = 480 / inResolution;  

    Range scaledRange = new Range(inRange.low * scale, inRange.high * scale);

    /*	
    This is an enum value that includes various fields about a rhythm, e.g.:
      + `r_8in3` (triplet 8th)
      + `r_4` (quarter)
      + `r_16dot` (dotted 16th)
    There are some heuristic techniques in the .fromRange() method that attempt 
    to assign ambiguous inputs.
    */
    Rhythm rhythm = Rhythm.fromRange(scaledRange);

    /*
    This is the "simple", essential formula for finding what a rhythm should be when quantizing.
    `rhythm.base` is a constant (and based in 480 resolution) and is one of: 
        + whole, half, quarter, 8th, 16th, 32nd, 64th, 128th

    `rhythm.divisor` is if the rhythm is part of an irregular grouping (a triplet 8th would 
    have divisor 3).
    */
    double gridWindowSize = rhythm.base / rhythm.divisor;

    /* Now we have a grid window that is the "perfect" or pre-determined duration for x rhythm */
    Range gridWindow = new Range(0, gridWindowSize);
    
    /*
    "Ratchet" the grid window up to where it's supposed to be (`javax.sound.midi` ticks are absolute, not relative). 
    This (using an arbitrary tolerance) worked best when considering that you didn't know if you were catching the head-end of a poorly executed target note, or the tail-end of the previous note.
    You want to find the one you are *supposed* to fully overlap
    */
    final long tolerance = 4;
    while (gridWindow.overlappingRegion(range) < tolerance) {
        scaledRange.shift(gridWindowSize);
    }
    
    /* To "snap": */
    long quantizedLow = gridWindow.low();
    long quantizedLow = quantizedLow + rhythm.duration;
    
    return new Range(quantizedLow, quantizedHigh);
}
```

In unit tests, I used a bunch of helpers to test things "en masse", and I used the logarithm thing I mentioned earlier.
+ This is because the length of the rhythm and the MOE shouldn't have a linear relationship. Even with larger rhythm values like a quarter, you're not going to have someone (unless they've had one too many while performing at open-mic night) executing a quarter note with a 75 tick MOE. 

I basically have an ArrayList I fill with +/- someOffset (calculated as log of rhythm value), and then use Math.Random to pull out one of the offsets. So a test range to be quantized corresponding to a perfect quarter note `[480,959]` would pop out something like `[478,960]`. 

### Notating MIDI

MusicXML (which I will go into later) is sort of a response to the shortcomings of MIDI in terms of notation.

MIDI was developed during a time when GUIs and displays could not accommodate advanced music notation. It was never intended to be something that stored/transmitted detailed notation information, but rather, was intended to be a protocol for digital instruments to communicate with each other, and computers.

MusicXML was developed much later (early 2000's) by a W3C group, which is still in charge of its standard today, as early notation programs (like Finale) and GUIs that allowed plopping notes onto a staff with the mouse became doable.
+ Fun fact: Finale, an industry standard for the last 20+ years announced it was ceasing production the first week of this project/semester. I used to use it all the time during my undergrad in the music technology lab.
+ Other prominent notation software includes Sibelius, Dorico (both pricey, professional-grade), and MuseScore (FOSS, and very good; purportedly the most widely-used in the world).
  + Still mad nobody from their team helped me with my forum post a month ago. Makes it impossible to open MuseScore from my program using `ProcessBuilder` *and* is a pain when opening associated files without instance of MuseScore already running.

Some of the metadata the MIDI *does* include that can be helpful for notation:
+ Time signature events
+ Key signature events
+ Tempo events

For instance, MIDI data can be parsed and used to display everything seen here in notation software:

![midi vs musicxml](images/chopin_pickup.png)

The thing is, however, that how much metadata a MIDI file includes usually correlates to whether it was "recorded" (by somebody playing a keyboard), or notated (somebody went in and manually made a score in notation software, and then the notation software exported a MIDI file based on the MusicXML data associated with the score). Even then, not all authors include various metadata.

My program does not even attempt to parse files without time signature data, since it is crucial to the creation of Measure constructs.

#### Time Signature Events

There is no way to calculate *anything* regarding measures without time signature events. Thankfully, if the author of the MIDI file notated things accurately, time signature events will exist *and* be placed correctly within the score.

This means that time signature events can be ranged (i.e. `[start tick, stop tick]`), and a note happening at `x` time can have measure-related contextual properties.

If you have the time signature, and the value of a quarter note (i.e. the **resolution**), you can calculate measure size, which I have as a utility function in the `TimeSignature` class.

It takes the upper and lower numerals (a.k.a. numerator and denominator, apparently, although I had never heard that in my life before this so I don't think it's ever called that in most music circles) of the time signature, which can be extracted from MIDI data, and essentially just involves getting a "common denominator" of 4, i.e. quarter notes, and then multiplying the resolution (i.e. ticks-per-quarter) by the numerator, which would give you the size of the measure in ticks. 

I took out all of the error-handling/assertions for simplicity:

```java
public static long calculateMeasureSize(int upperNumeral, int lowerNumeral) {

	// These need to be floats for stuff like 3/8 or 7/8
	float upper = (float) upperNumeral;
	float lower = (float) lowerNumeral;

	// Get lower numeral to be in terms of quarter notes (which would be a lower numeral of 4)
	while (lower != 4) {

		if (lower > 4) {
			// e.g. 3/8 --> 1.5/4
			upper /= 2;
			lower /= 2;
		} else if (lower < 4) {
			// e.g. 2/2 --> 4/4
			upper *= 2;
			lower *= 2;
		}

	}

	// Quarters per measure * ticks per quarter
	float measureInTicks = upper * Piece.resolution;

	return (long) measureInTicks;
}
```

In the score image earlier, there is a pickup measure. I spent a long time writing an algorithm to "detect" the presence of a pickup measure:
+ Put all the time signature events into a stack; calculate and fill a container with measures; if the last measure created is shorter than the last time signature popped, it is probably a pickup
+ If the first note happens after a sizeable amount of rest, it probably occurs in a pickup measure

Turns out a lot of my work was pointless, because notation software (generalizing to really mean MuseScore) will encode pickup measures as a discrete time signature, with an identical lower numeral but smaller upper numeral.
+ For example, the snippet displayed above outputs as *two* time signature events: 1 measure of 1/4, followed by a measure(s) in 4/4.

I did not find this out until after the fact (I was completely done implementing and testing, and tried things out with an actual MuseScore file). I'm sure there is some sort of lesson to be learned here. Not sure what it is.

Thankfully, the "near-full measure of rest with a pickup note" case was salvageable / still applicable, and is shown later in this report when discussing [Measures](#measures).

#### Key Signature Events

Key signature events are pretty pointless in terms of my program, as far as aiding harmonic analysis (e.g. "what is the root of this chord", "what key area are we in at this measure") because even in music, key signatures don't indicate anything except (usually) the key area of the first and last measure (even then, though, there is a litany of counterexamples).

It does make displaying written-out files nice, aids a *bit* in spelling pitches, and is helpful for the MusicXML stuff later.

#### Tempo Events

Tempo events are also kind of pointless in my program, except I handle and include them because it became really nice to control the playback speed of the MIDI files I outputted without having to put them into notation software and manually doing it.

The thing with tempo events is that they need to be increased/decreased by a scale, rather than literally, because there may be many tempo events in a MIDI file. Depending on the notation software, tempo events may be used to sort of "frankenstein" together a ritardando, fermata, or any other time diminution/augmentation expression.

This could come in handy for reduction stuff ("how fast are things moving here, and will the hand have time to jump down"). However, the problem is their reliability - "Did the author include painstaking tempo directions? Incomplete tempo directions? etc.".

## MusicXML

Like explained above, MIDI really is not intended for storing/transmitting score details. It is for performance details, which any musician knows, can be very different from what is in the score, due to:
+ Artistic interpretation of the score
+ Imperfect execution

But, if you play back a MusicXML-turned-MIDI file, it will sound much more robotic than a MIDI performance (although parsing software is getting better and better, along with providing more options to notating authors to really meticulously control playback based on the score). 

So they each have their "what they do best" and their domains:

![midi vs musicxml](images/midi_vs_musicxml.png)

I knew that at some point, this program would have to start dealing in MusicXML, since the whole point of the program is to produce scores, not recordings. Additionally, having exact control of which hand was notated on which staff (upper or lower) in a piano grand staff was paramount. The notation software I had experience with (MuseScore and GarageBand - which isn't notation software per se but includes most of the functionality needed to display MIDI as a score) used the most bare-bones approach to assigning hands: middle C and above notes go on the upper staff, and the rest go on the lower staff.

So, while MIDI means my program can take input from a way wider array of files, *and* provided the basis for nearly everything in my internal representation, I knew I needed to confront the MusicXML spectre.

### A MusicXML Document (Background Info)

MusicXML is, of course, hierarchical, rather than serial.

The hierarchy is as follows:
+ ScorePartwise
  + Metadata 1
  + Metadata 2
  + ...
  + Part-List
  + Part 1
    + Measure 1
      + Note
      + Note
      + Note
      + etc.
    + Measure 2
      + Note
      + ...
    + Measure 3
    + ...
  + Part 2
  +  ...
  + Part 3
    + ...
  + etc.

ScorePartwise is the root element. 
+ There is one other "species" of root element, called ScoreMeasurewise. It basically reverses the Part-Measure relationship exhibited in ScorePartwise (i.e. Measures contain Parts, and measures are added one at a time). It is rarely used.

Everything other than the various Part elements is known as the **score header**, and occurs before the Parts start to get filled out. Score headers have 1 mandatory element (the Part-List) and various optional metadata concerning "work" info, credits, encoding software name and info, date, etc..

The Part-List is essentially a map that contains ScorePart elements - each containing metadata and info about an individual Part element (like instrument name, number of staves, etc.)

This being a piano score, means that there is only 1 Part element, with 2 staves.

### Timing

MusicXML essentially follows the MIDI resolution timing philosophy in that everything time-related is based on the value of a quarter note. Instead of resolution, it is called **divisions** (i.e. subdivisions per quarter note).

Most MusicXML files I came across had, vis-a-vis MIDI resolutions, small values. Instead of 480, 960, etc., I saw a lot of 24, 48, and 96. At first glance I thought "ok, they are just lowering the granularity and dividing everything by 10." There seems to be some more convention to this, but I didn't have time to look too deeply into it. 

I decided to just straight-across use the MIDI **resolution** (which is what the value in my internal representation is based on) as the **divisions** value. It doesn't seem to have an negative affect. So, MusicXML divisions for all the files I output is 480.

### Note Placement

A lot of MusicXML stuff is pretty straightforward, except the placement of notes is pretty tricky.

Consider the first measure of the C minor prelude of Chopin:

![chopin c multi-voice](images/chopin_voices.png)

You will notice that in the 3rd beat of the right hand, there are 2 independent voices: the quarter "base", and the moving line (in thirds) in the melody/soprano. 

If you think about this in terms of finger placement:
+ The G and B would be held by fingers 1 and 2
+ The thirds above it would be played by fingers 4 and 5 (for both thirds).

This means, when including the left hand, we essentially have three "regions" of notes occurring at exactly the same time, but on disparate stems:

![chopin c stems](images/chopin_stems.png)

In MusicXML, notes are placed sequentially. At first glance, this sounds like it is essentially the same as MIDI. TL;DR: it's not.

In MIDI, if I wanted to encode a C major triad in quarter notes with a resolution of 480:
+ 0 ON C4 --> 480 OFF C4 --> 0 ON E4 --> 480 OFF E4 --> 0 ON G4 --> 480 OFF G4

And it would look like this:

![midi C triad](images/midi_triad.png)

It is all sequential, and it is up to the sequencing software to take that "flat map" and put everybody into their corrals and send them off at the right time during playback.

If you were to encode that triad in MusicXML *without* any of the MusicXML-specific workarounds, it would look like this:

![bad musicxml sequence](images/musicxml_sequence.png)

Where each note looked something like this:

```xml
      <note>
        <pitch>
          <step>C</step>
          <octave>4</octave>
        </pitch>
        <duration>480</duration>
        <voice>1</voice>
        <type>quarter</type>
        <staff>1</staff>
      </note>
```

In MusicXML, pitches don't mean anything to following notes with the same pitch: each note looks like a note to each other, so each note effectively "pushes" the following notes forward by its own duration.

MusicXML has a really easy fix to this: if something is a bona fide chord (i.e. all the noteheads are attached to the same stem), you simply prepend a chord (an empty element) tag top subsequent applicable note elements:

```xml
      <note>
        <chord/>
        <pitch>
          <step>C</step>
		<!-- ... -->
```

But does one place noteheads (i.e. notes) that are to occur at the same time as other notes but are *not* to be attached to the same stem?

Essentially, each time you want to place notes within the same time range (on the grid) that *don't* have connected stems, you have to add a `<backup>how_many_divisions</backup>` element to get the "counter" (helpful to think about it when writing the conversion code as a counter or cursor) back to where you wanted to place something.

One thing to add before explaining summarizing whole process (which will probably be clearest): if you want gaps between notes, you need to add a forward element. It works exactly like the backup element, but progresses the cursor forward.

Finally, the left hand in the piano Part is usually "reached" by first filling in the whole right hand, and then backing up to the beginning of the measure (which requires you know who far forward you are), and starting over.

So, to use a slightly simpler example than the Chopin:

![note placement process](images/note_placement_process.png)

Now in English/pseudocode (Note: I already separated the lists of notes into RH/LH):

    // next and curr are Note objects, or their analogue in musicxml
    // .start/.stop are ticks

    // For rests (skip ahead)
    if curr.stop < next.start:
      forward = next.start - curr.stop
      list.add(forward)

    // For notes starting at the same time
    if next.start == curr.start  &&  next.stop == curr.stop

      // They are to be attached to the same stem
      if next.stop == curr.stop:
        list.add(chord);

      // They are part of a different voice
      else:
        backup = curr.length
        list.add(backup);
        bumpVoice();	

Notes that are tied over the barline are an absolute pain, but I won't go into that here.

Additionally, there are more nitty-gritty things and edge cases, and the logic is rather lengthy. I'm sure it could be simplified, but the note placement stuff constitutes by far the largest share of MusicXML conversion logic.

### On the External Library I Used

For the marshaling/unmarshaling stuff, I used the [proxymusic](https://github.com/Audiveris/proxymusic?tab=readme-ov-file) library from the Audiveris project. 

(I didn't have time to look too deeply into it, but I am actually looking forward to looking into Audiveris as a whole, as it is a OMR (Optical Music Recognition) software. Again, haven't looked too deeply into it, but... apparently can notate (uses MuseScore, I believe) based off of an image/pdf of a score!)

The library is good, but: there is ZERO documentation (except for a small and pretty uninformative unit test).

Thankfully, most of the code is just getters and setters.

So I basically went through the W3C/MusicXML documentation (which has a tutorial), and then used their [reference](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/) page to look up cases as I came across them. 

Then, I literally just used `cmd+f` to look for possibly-related functionality in the `proxymusic` library.

There were some surprises, but on the whole, it was merely tedious, and I got the pattern down eventually.

I am nearly done with the MusicXML package of my program (at least the writing-out portion), except for the note placement nastiness I mentioned above. Each time I fix one thing, and a new case presents the need for more unique handling. Then the code gets so busy with case-handling that I re-design... rinse and repeat.

However (not to toot my own horn), the conversion to musicxml process was made even *remotely* possible because I had already implemented a bunch of the stuff it needed in my internal representation - the most useful being Measures, which hold time signature, key signature, and note data. I had also already implemented (the default/basic) hand-splitting stuff, so staff assignment was helped their too.

However, I definitely was missing some things, too - mostly related to pitch stuff. This is probably because I had written the program based on MIDI for 3.5 months, and in MIDI, pitches are literally just 0-127 values. Any of the stuff in my program to extract register, semitone, string-parsing, etc., was to make *my* life easier, and isn't the most robust. Some of it didn't align perfectly with how MusicXML does things (especially regarding enharmonic spelling, which my program doesn't handle, partly because my program does not have key-area-analysis capabilities, and that is a big determinant of spelling).

The whole internal representation of my program could probably be combed through and re-designed to be a bit more general and/or include stuff that will make things easier for MusicXML. It's either that, or remain ideologically "pure" and keep the internal representation completely divorced from any MIDI/MusicXML paradigms (already impossible since most all my timing stuff is based on MIDI) and just have the conversion functionality be extremely heavy-lifting.

## Package/Program Structure

reductor is split thusly:

![package structure](images/package_structure.png)

Additionally there is a reductor.util that handles file I/O stuff; playback using `javax.sound.midi` methods (easier than opening another application, as it was usually just confirm a file sounds correct in the first 3 seconds); opening with various applications using `ProcessBuilder`, and a class called `MidiDebugging` that just has a bunch of printing utility functions to look at midi bytes in various ways (was actually very helpful).

### Application

The `Application` class was a late addition to the program. It may not be final, either. It was called `DevelopmentHelper` for a long time. It is just to coordinate package duties in one, single program flow. It takes a String filepath, and pops out either a MIDI or (eventually) MusicXML file. It isn't final or anything, but I include it here because it is a good descriptor of how the program is intended to flow as a whole, and show how the different packages are kept separate and interface with each other:

![application class](images/application.png)

## reductor.midi

The purpose of this package is to get MIDI data in a form that the `reductor.dataconversion` classes expect.

In the future, it will probably become part of a sub-package that it shares with a sister musicxml-importing-related package (similar in the way that the `dataconversion` package is bifurcated.

The general outline of major classes is as follows:
+ `MidiFile`: top-level things about the a MIDI file, including a `File`, `Sequence`, and `Events` member
+ `Events`: a "list" class (just contains a bunch of lists)
  + In charge of sorting, typing, and creating all the `Event` instances
+ `EventType`: an enum used to support the `Event` hierarchy
+ `Event<T extends MidiMessage>`: an abstract wrapper class
  + `MetaEvent extends Event<MetaMessage>` and `ChannelEvent extends Event<ShortMessage>` are the major subclasses
  + Members include `MidiEvent event`, `T message`, `long tick`, and `EventType type`

The biggest impetus for the Event hierarchy was literally to override `toString()` in all of the child classes because it made debugging *and* simply learning MIDI 10x easier. A nice side-effect was that all the complex MIDI-to-English conversion stuff (undoing the MIDI encoding scheme) ended up being lifted and placed into the `dataconversion` package almost wholesale.

### Aside: The Java MIDI Library

I hesitate to call the `javax.sound.midi` library "feature-poor", because I have gone through the documentation and I believe it does exactly what it intends to do. It isn't intended to be a full-fledged "composing with MIDI" library (which other libraries like Python's `mido` seems to be). It is meant for reliable and robust file I/O, and acting as a conduit through a Java program to other software (e.g. sequencers), with *some* manipulation abilities. 

However, that said, seeing every message type and manipulating / sorting stuff in a type-safe way is not really a feature of the library. Everything below was written as a response to the needs I had while debugging and figuring out what to do with the data.

Writing all the code described above was basically done by reading documentation on various websites. The biggest help and best (i.e. most concise, easiest to follow) was [recordingblogs' wiki](https://www.recordingblogs.com/wiki/standards-in-music-index). 
+ I didn't really consult official MIDI documentation because their (the Music Manufacturer's Association) sites were hard to follow, some of their stuff is behind a paywall, and I didn't really need to know the amount of detail in different MIDI standards like MIDI 2.0 or General Midi, etc. Just tell me what the 3rd byte of a time signature message corresponds to!

### Future Re-Design

The `Event` class is pretty messy, and I'm not too happy with it. What I am happy about is stated above: it was a great exercise in learning MIDI and basically gave me everything I needed to have/know to implement conversion stuff later; also, it was fun. 

However (although I *thought* I did initially), I still don't fully grasp the details of generics and compile- vs. run-time type-checking it turns out.

The `Event` class is parameterized so that its `message` field is of type `T`. This was useful because the two (of three that I used) MidiMessage concrete classes have *different* methods to check even basic things, like the status byte: in `MetaMessage`, it is `getStatus()`, and in `ShortMessage` it is `getCommand()`. So, not having to constantly cast before calling those was nice.

To illustrate as best I can: I tried to fix the unchecked thing with all sorts of approaches (the enum, a factory method, etc.). But I guess I still don't fully understand what the right approach is.

```java
/**
 * This is a wrapper class for a {@link javax.sound.midi.MidiEvent}
 *
 * @param <T> The type of MidiMessage the Event holds
 */
public abstract class Event<T extends MidiMessage> {

    private final MidiEvent event;
    private final EventType type;
    private final T message;
    private int trackIndex;
    private String trackName;
    private final Long tick;
    
    Event(MidiEvent event) {
        this.event = event;
        this.tick = event.getTick();
		// This is unchecked...
        this.message = (T) event.getMessage();
        this.type = EventType.getEnumType(event);
    }

	//...
```

However, at that point in the Capstone, I was cognizant of the fact that I was spending too much time making my data acquisition process "optimized" with constant re-designs, and, with the knowledge that it was robust and worked (while not being designed super well), I decided it was time to move on.

Ultimately, the whole `.midi` package can probably do away with any typing, complex sorting, and basically everything but 1-2 classes: just loop through all the events, grab the ones you want (only need like 30% of the subclasses/types in the end), and provide getters. 

## reductor.dataconversion

`dataconversion` is meant to be the bridge between the native format package (either MIDI or musicxml) and my internal representation (`Piece`). 

The reductor.midi's biggest job is to produce a valid `MidiFile` object. The `dataconversion` package expects certain constructs (e.g. `TimeSignatureEvent`, `NoteOnEvent`), and can convert them to their analogous internal representations.

`dataconversion` doesn't make any (consequential) decisions on its own. It converts the aforementioned constructs using static utility functions that are in the `piece` package (i.e. the internal representation package). The constructors or factories in `piece` can be said to slightly cater to the `dataconversion` class, but that's kind of a chicken-or-the-egg thing.

As shown above, it has 2 sub-packages, corresponding to MIDI and MusicXML. I will just go over the MIDI one, as I sort of went over everything I wanted to say about the MusicXML in the [corresponding](#musicxml) section.

Final note: `reductor.dataconversion.midi` contains the [quantization](#quantization) functionality.

### Note-Pairing

`dataconversion` is where note-pairing happens (`reductor.midi` just has gettable lists of `NoteOnEvent`s and `NoteOffEvent`s).

This is not new territory, so I will try my best to *summarize* here. 

Additionally: I *did* encounter (most) of these cases in the wild (a particular Mozart overture and the Brahms *Clarinet Quintet* both had redundant offs, which took a collective 2 hours to figure out).

The doc comment from the program (slightly edited for use here):

1. Stuck notes: note on events that are not paired by the end of the list of offs
2. Semi-stuck notes: notes that are stuck for a time:
   + A quarter note C @ 0 --> never turned off
   + A quarter note C @ 480 --> never turned off
   + A quarter note C @ 960 --> never turned off
   + A quarter note C @ 1440 --> turned off @ 1919
+ Each quarter is not turned off except the last, and the last off event turns ALL of them off.
+ Although each is "effectively" turned off because MIDI does not allow multiple note events of the same pitch to occur simultaneously (see the note about Case 3 below), the implication here is that the on event never received an off event means two things: This algorithm would treat the first three as stuck notes AND their constructed Ranges would not be [0, 479], but [0, 480] without special care!
3. Redundant offs: extraneous offs sent for ons that have already been shut off
4. Extra ons: when two notes with the same pitch are turned on at the same tick:
   + On channel 1 -- A whole note C @ 0 --> _should_ be turned off @ 1919
   + On channel 1 -- A quarter note C @ 0 --> _should_ turned off @ 479
+ "Every" C, even though there is really only ever 1, will be turned off at the first off pitch. All 
subsequent offs become case 2! In this algorithm, since I want to prevent a Range of [0,0] from being created, both the whole note's on and off will be unpaired (both at tick 0), _as well as_ the whole notes extraneous off at 1919. This occurs when multiple-features in notation software allow this sort of thing (like in a piano score), OR the reverse (see the next paragraph).
+ Case 4 is interesting because MIDI spec does not handle or allow two on events corresponding to the same pitch to happen. This won't matter if they are on different channels, of course, but when combining to the same channel, or track (as in the case of reduction -- a violin and trumpet both starting C's at the same time -- extra care needs to be taken. This is a job for the reverse conversion algorithm below.

That final "reverse conversion" ultimately turned out to be not a job for MIDI at all, since it really doesn't have any way to have overlapping voices. This is a job for MusicXML and voice control. 
+ I experimented for some time writing out overlapping voices on different channels, then using MuseScore's "implode" functionality (puts voices from different staves onto one selected staff), but it was unwieldy and annoying and, of course, not a good solution for an end-product (can't expect a user to do all that just to get a readable score).

It turns out that some of these cases are "harmless" - it just depends on how you want to handle it. 

Even though it may not be the actual use of custom exceptions (another thing I struggle to understand still - that is, exception, try/catch, error propagation, etc., I thought it would be cool to make my own exception at one point and this seemed the perfect place to have `UnpairedNoteException`. In the far future, maybe another developer can choose to ignore certain UnpairedNoteExceptions or something, rather than me throwing a RuntimeException or something in the note-pairing algorithm).

## reductor.piece

I am going to do a real fly-over view of this stuff.

The Piece classes can be roughly categorized as follows (this is not everything, and some of these are not fully implemented, but are placeholders of sorts at the current moment):
+ "Element" classes: `Note`, `Chord`, `Phrase`
+ "Sub-component" classes: `TimeSignature`, `KeySignature`, `Tempo`, `Rhythm`
+ Container classes: `Measure`, `Column`, `Box`, 
+ Heavy-lifting utility classes: `IntervalTree`, `Range`, `Pitch`
+ "Plug-in" utility classes (algorithms): `HandSplittingFunctions`, `ReductionFunctions`
+ Interfaces: `Ranged`, `Noted`
+ Enums: `RhythmType`, `Hand`

### IntervalTree

I was able to genericize the tree:

```java
public class IntervalTree<T extends Ranged> {

  //...

      public class Node implements Ranged {

        /// The {@link Range} (i.e., interval) this node represents
        private final Range range;

        /// Max endpoint in subtree rooted at this node (used to ignore left subtrees during queries)
        private long max;

        /// This node's data (a Set of elements with the same range but possibly differing associated data)
        ArrayList<T> elements;

        //...
```

which means it can be used to store any Ranged object:

```java
public class Piece implements Ranged, Noted {

    public static int TPQ = 480;

    private final IntervalTree<Note> notes;

    private final IntervalTree<Column> columns;

    private final IntervalTree<Measure> measures;

    private final IntervalTree<TimeSignature> timeSigs;
    private final IntervalTree<KeySignature> keySigs;
    private final IntervalTree<Tempo> tempos;

    //...
```

This is, of course, probably a place where I could take a more functional programming approach and dynamically construct things. So I would just have the one `Note` tree, and anytime I needed a Measure, it could be lazily created from a query. This would mean either more query methods, or, perhaps, a "plug-in" style where each class has its own specific query method, and the tree can just use that method. 

However, it is really necessary at this point in the process for me to see exactly what lists contained at certain points during the debugging process, so they needed to exist.

A final note: one of the main purposes of the quantization functionality was to force the use of ranges in my program to represent half-open ranges relative to the note duration. However, Range also includes two getters with different purposes (which might be a design mistake - still working this out):
+ `length()` represents the true length of the Range: `[0,479]` has a length of 479
+ `duration()` is the "inclusive" representation of the Range: `[0,479]` has a duration of 480

### Measures

Measures have to be created from scratch when working with MIDI data, since there is nothing in MIDI having to do with measures.

#### API

I briefly [mentioned](#time-signature-events) some of the issues with pickup measures. Treating a collection of Measures as a simple list does not suffice. Measure 0 should only exist if there is a pickup measure.

Secondly, there is the issue of measures being a 1-indexed sort of thing.

So access to a collection of Measures has to be controlled in some way. 

If you were a developer using the API, what would you expect these method calls to return... :

```java
// The first measure of the piece? Measure 1? The second element in the Measures list?
piece.getMeasure(1);

// Should this throw an out of bounds index exception if there is no pickup measure?
piece.getMeasure(0);

// Should this always return Measure 1, regardless of whether or not there is a pickup measure?
piece.getFirstMeasure();
```

May seem trivial, but this sort of design stuff gives me many headaches.

#### Pickups

The `assignPickup()` method handles the heuristic approaches (although one is slightly redundant, and one isn't heuristic per se) to detecting pickups:

```java
private boolean assignPickup() {

    //...

    /*
    TimeSignature#compareTo will return a negative integer if both denominators are 
    the same and the numerator is less than
    */
    boolean heuristic1 = firstTimeSig.compareTo(secondTimeSig) < 0;
    
    /*
    This is technically redundant, I just haven't decided which is best yet, 
    and need to see more cases. Checks if the final measure complements the 
    anacrusis (which was fairly traditional in classical/baroque works).
    +
    */
    boolean heuristic2 = lastTimeSig.compareTo(penultimateTimeSig) < 0
            && firstTimeSig.getDenominator() + lastTimeSig.getNumerator()  
            ==  firstTimeSig.getNumerator();

    /*
    The next heuristic handles the case where the initial measure is *not* encoded as a
    distinct, lesser time signature, and is, instead, authored as a normal measure 
    with a substantial period of rest before the first note. This is the only *truly* 
    heuristic technique of the 3, but I named them all like this anyway.
    */
    
    /* Right now, this is exactly an eighth rest (half of the value of a quarter) */
    final long THRESHOLD = (long) (TPQ * 0.5);
    long amountOfRest = Math.abs(firstMeasure.getRange().low() 
            - firstMeasure.getColumn(0).getRange().low());
    
    boolean heuristic3 = firstTimeSig.compareTo(secondTimeSig) == 0
            &&  THRESHOLD < amountOfRest;

    if (heuristic1 || heuristic2 || heuristic3) {
        measures.getFirst().setIsPickup(true);
        return true;
    }

    return false;
}
```

### Custom Data Structures

The idea hierarchically is that you have the Piece, which (theoretically) contains:
+ `Measure`s which contain
+ `Box`es which contain
+ `Column`s which contain
+ `Note`s

This is shown below, and the coloring corresponds to left (blue), middle (yellow), and right (red) hand clusters:

![data structures](images/data_structures.png)

Furthermore:

An entire `Piece` can be split into just Columns, or just Boxes, or just Measures, or any combination thereof, but the important subdivisions of a `Piece`, in terms of analysis, are:
+ `Box`: 
  + "Horizontal" analysis/manipulation of Notes in Columns - that is, left-to-right
  + Texture- (and pitch-) wise analysis
  + A `Measure` is just a "special case" of a `Box` (not really class-wise, but theoretically)
  + Can "plug-in" further algorithms for hand-splitting ("Is there lot of jumping around going on that would make certain Column-only hand-splitting decisions untenable?")
+ `Column`:
  + "Vertical" analysis/manipulation of notes - that is, up-and-down
  + Purely pitch-wise analysis
  + Triage first stop for hand-splitting and basic texture-thinning (remove doubled octaves, etc.)
+ `Note` (leaf element)

Even if Measures were never constructed (or were an on-demand thing), specialized queries could label a Box as being: 
+ A full beat
+ Some subdivision of a beat
+ A strong beat, a weak beat, a pickup beat, etc.

And then Columns could take that information into account, as well.A sort of "indexing" (that is to say, locating using a determined set of "coordinates") scheme now exists. The following example illustrates this, with just the "middle" hand area selected:

![indices](images/indices.png)

So, one could say:
+ "Get me the (middle/LH/RH) notes of beat `b` of Measure `m`.

As I write more actual reduction stuff, rhythmic/beat analysis is going to very important (knowing *where* in a measure a certain Column or Note occurs.)

#### Column

Of all the data structures, I will only go into depth on Columns, but even then, I will try to keep this brief. 

My `Column` class has the most documentation of any of my classes by far, and explains what "pure" and "semi-pure" Columns are, how it assigns "holdovers", splits hands, etc. 

It makes use of a `Consumer<Column>` type that will (in the future, hopefully) allow Boxes or Measures or other actors to re-split hands based on wider contextual information.

A Column represents the **smallest unit of musical change, regardless of where it happens in the staff.** 

Basically:
1. Each time a new note occurs, a new Column should be created
2. The Column should know about notes that extend into it from previous Columns
3. The Column should only ever manipulate notes that are native to it (i.e. not holdovers -- that's another Column's job/responsibility)
4. A Column should contain all other notes "on" (i.e. a "vertical" segment, thus the name Column) during its `Range`, but its `Range` should never include notes with different start ticks. Again, if a new start tick occurs, that signals new Column creation.

The purpose of the Column is to compare, analyze, and manipulate notes by pitch. Everywhere else in the program, Notes are used in the "Range" sense of things, NOT by pitch.

But what I am most proud of is the `Column` construction process, so that's what I will highlight here.

##### Column Construction

The algorithm to create Columns is something I am particularly proud of, although to somebody who knows math well, it's probably not that cool. But I thought it was. It was mostly cool to me because it came about after a super lengthy and round-about process, and, ultimately, was made possible by stuff my `Range` and `IntervalTree` stuff already paved the way for.

It is easier to think in terms of a number line here:
1. (Optional) Construct a the Notes / Ranges.
2. Sort all Notes by start tick, then end tick (i.e. Range's natural ordering)
3. Put all the start ticks into a Set (just to remove duplicates - very important)
4. Construct Ranges between all the start ticks, as well as a final "terminus" (e.g. last end tick of the last note, which is to be included)
5. Query the note tree with those Ranges and you have perfect Columns, even when syncopation is involved.

For instance:

![columns simple](images/columns_simple.png)

+ We do NOT want a Column for each Note (we do NOT want 13 Columns)
+ We do NOT want 5 Columns
+ We want 4 Columns that all know about the whole note, but only the first Column "owns" the whole note
+ If, in Column 3, the RH is occupied, it can't come down and help with notes. But, Column 3 should NOT be making decisions about the whole note.

And here is what that measure looks like in terms of the number line:

![number line](images/number_line.png)

Slightly more complex (syncopation):

![columns syncopation](images/columns_syncopation.png)

+ Should have 4 Columns
+ If you pat this on the table, and count hands-together as once, you will notice that you pat 4 times. This is a perfect indicator of Column creation.

And 1 more visualization, with a different example (this is some documentation from a unit test, but I like how it is visualized):
![columns syncopation](images/range_test.png)

Once a Column is filled with notes, it can decided if it is "pure" (no notes extend outside of it, either forward *or* back), and assign the `isHeld` field of each Note accordingly; apply a hand-splitting (the default, for now) function during construction to separate the notes into Left, Middle, and Right hand Columns; and do some other things, like calculate median pitch, mean pitch, split point (halfway between the thumbs), etc.
+ Also has leftThumb and rightThumb indices if I decide to do away with the member Columns, since those present a recursive construction issue. They do, however, behave exactly as I need them to for now, and it's nice to see in debugging exactly what is in a sub-Column at any given time.

## Design Patterns

### Builder

I had a lot of fun implementing these. My only regret is that I did not do them earlier.
+ Fluent patterns are pretty cool. I remember that D3.js and Android Compose stuff uses them a lot, so it was cool to implement one myself.

I dealt with a lot of "constructor explosion" problems that the various builders solved, preserving immutability while streamlining things. Here it is in action for the `Note` class:

```java
Note note1 = Note.builder()
		.pitch("C#4")
		.start(0)
		.stop(480)
		.build();
```

which creates a quarter note at middle C.

Furthermore, any "setters" in the `Note` class return a new instance using the copy-construction capabilities of `NoteBuilder` (i.e. it will set all the defaults to match the passed Note object):
```java
Note setPitch(int pitch) {
	return builder(this)
			.pitch(pitch)
			.build();
}
```

Moving along, the `ChordBuilder` has a method that takes a vararg which makes use of my `Pitch#toInt` string parser as well. The below will output a C7 chord rooted at middle C:

```java
Chord chord = Chord.builder()
		.start(0)
		.stop(479)
		.add("C4", "E4", "G4", "Bb4")
		.build();
```

Finally, and the main impetus for writing builders, is for ease in writing tests. I was needing really specific and SHORT (i.e. contained/focused) test cases, that were complex enough at this point in the project that it would be too unwieldy to try and craft them using what I had currently (i.e. no builder stuff). And it was really becoming a pain going into MuseScore, creating a "test score" snippet, exporting the MIDI, adding that into my `Files` development-helping class... just to test one tiny thing that I knew my internal representation had the ability to create really quickly.

Thus, the `Phrase` and `PhraseBuilder` was created. It combines the `NoteBuilder` and `ChordBuilder` capabilities). 

If you output the below (with the appropriate util call for writing midi files) and open it in MuseScore, it will look *exactly* like this, which is also the first measure of the e minor Chopin prelude:

![noteBuilder](images/phrase_builder.png)

Seems the .mark() and goToMark() stuff (fill in right hand; then go back and fill in left hand) is a commonly-arrived-at conclusion, because it's basically how MusicXML handles note appendage; the `PhraseBuilder` actually helped me understand the MusicXML paradigm a lot more quickly. With humility, this was a confidence boost in that I came up with something even remotely resembling an actual approach to doing `x` in a real-world application.

### Composite

The `Noted` interface includes a single getter `getNotes()` - anyone implementing it needs to yield its Notes, whether it is a Note container, a container of Note containers, or even a Note itself.

Here, the Measure does not need to know if the Column is returning a filtered/transformed collection of notes, or a deep or shallow copy, etc. It just calls `getNotes()` on all its constituent components (i.e. the Columns), and those components in turn call `getNotes()`, etc., until the leaf Note, which returns itself.

```java
@Override
public ArrayList<Note> getNotes() {

	ArrayList<Note> notes = this.columns.stream()
			.flatMap(col -> col.getNotes().stream())
			.collect(Collectors.toCollection(ArrayList::new));

	return new ArrayList<>();
}
```

### Strategy

We talked a bit about a related thing - unless I'm mistaken in the similarities between these two - in the form of the plug-in architecture stuff, which I would like to implement more of in the project, in the future.

The problem solved by the approach is that at compile-time, I obviously don't know which MIDI file I will be processing.

The bigger problem, however, is that certain data structures (Column vs. Box) have different breadth of context and might know something the other doesn't in terms of hand-splitting functions or reduction algorithms.

Right now, the only place implementing this design pattern is the `Column` class, which has a:

```java
Consumer<Column> splitFunc;
```

which is complemented by this utility class:

```java
class HandSplittingFunctions {

    static final int MIDDLE_C = 60;
    static final int SPAN_MAX = 14;
    static final int NOTES_MAX = 6;


    static void defaultHandSplitter(Column col) {

        final int size = col.notes.size();

        if (col.notes.isEmpty()) { return; }

	  //...
```

I hope to adopt the same approach, or something similar, for the reduction algorithms.

## Biggest Problem with Current Implementation of Reductor

1. Still no actual reduction algorithms! 
+ Except, at various times in the last couple months, *very* primitive functionality related to eliminating too large of jumps (melody/line detection) and double octave removal (something commonly found in orchestrations and, conversely, commonly omitted in reductions).
2. Mutability/Immutability/Java handling of passing by value.

Concerning number 1: providing the foundation for reduction (deciding how to split up the data, knowing exactly how to manipulate the data, converting to actual output forms with all the edge cases and associated nastiness) really did take all my time. But every day it feels like I'm that much closer to having something where implementing actual reduction will "simply" (ha) be a matter of writing the algorithms and applying them at the right time and place during the flow of the program. This will mean I won't have to make certain design decisions every step of the way when creating said algorithms.

Concerning number 2: I really, and I mean *really*, bungled this one. 

And it wasn't until the last couple weeks (when I was neck-deep in Builder patterns, quantization, musicxml, and other last-minute fix-em-ups) that I realized how big of a problem I had created for myself here. If anything in my program is turned out to be a sword of Damocles, it was this (and it fell).

The issue is: I applied the immutability, safe access (getters/setters), and encapsulation principles so blindly and indiscriminately, that in the end, every single object was an unreachable island, and the whole purpose of the program (to change/manipulate, or at least create a "manipulated" representation of, the original) was defeated. For example:
+ One Column decides a Note needs to be removed (from its own backing list )because it is a doubled octave. Guess what: that change is not reflected ANYWHERE else.

When I first realized this 2-3 weeks ago, I thought to myself: "this sounds exactly like some observer pattern thing, where a change to one thing means everybody else with a reference to that thing needs to update too." But there was no time to implement a whole MVVM-style observer pattern and/or SSOT. 

I am also convinced that's not the correct re-design. There are some other approaches I have in mind but I am not sure about those either. I don't think I will even start that until I fully finish with the MusicXML stuff (both in- and out- processing) for the program. I am quite intimidated by fixing it, because it involves some very foundational re-designs.

Additionally, I don't think it will be fixable until my IntervalTree is self-balancing and has add/remove functionality, which is another thing I am procrastinating.
+ Technically I could pretty quickly/easily just create add/remove functionality, and like we talked about earlier in the semester, the scale of the data being added removed would mean that, yes, the tree would technically be unbalanced, but not enough to have any kind of effect on performance. (I mean, I don't anticipate that the amount of data/computation this program does *ever* will fall into the performance-concerned arena.)

## Appendix A: The Term "Reduction" and Brief Historical Info

### Preface: "classical" Music

First:
1. Big "C" Classical refers to a specific era and style, roughly 1750-1820, within...
2. Little "c" classical, which is what people mean when they talk about "classical" music (the whole).

The term classical, in the music context, has the same meaning as it does in classical philosophy, classical architecture, or classical literature. It is all a throwback to Greek and Roman stuff. (In that sense, it might be better termed "neo-classical" music, although that term itself was consequently pushed forward to another movement altogether.)

It stems from the Classical era's shifting emphasis (vis-a-vis Baroque) to the virtues of symmetry, balance, simplicity; commonality, secular-ness; etc. (All the stuff going on during the Enlightenment was also, of course, happening in music).

More context-appropriate/-accurate terminology is being slowly adopted, such as:
+ Western art music
+ European art music

After all, who is to say that *the* classical (as in, standing the test of time, and regardless of origins or culture) music is the stuff coming from like 300 years of basically Germany, France, and Italy.

Little "c" classical can apply to any culture, such as:
+ Indian classical music
+ Chinese classical music

Furthermore, what distinguishes classical music from, say, folk music? These are arguments I won't make here (partially because those are hot areas of debate and there aren't definitive answers, and partially because its out of my wheelhouse - that's more of a musicology thing!).

I don't think the terminology will change any time soon, but these are good things to be aware of.

We talked about value decisions at the end of 6017.

Admittedly, this project focuses purely on **European art music**. It cannot handle different scales, microtonality, instruments, notations, forms, etc., of non-European art music.

This is because European art music is what my training is in, and what my passion is. I simply don't know enough about other classical musics, so much so, that I don't even know if reduction would apply in other cultures' classical musics.

It's a narrowly focused program, to be sure. But if there were applications in other musics, and another developer wanted to work on additions, I would be all for that.

### Terminology: Reduction

I want to take a moment to discuss terminology (mostly because I like terminology, but also because there are some implications regarding the project).

In the music world, "reduction" is 99% of the time used in the context of "a piano reduction." As in, taking a non-piano piano piece and making it for the piano. Less so, but still common, and referring to the same exact thing, is "orchestral reduction". 

It just depends on how pedantic you want to be about the qualifier: is it the piano part being reduced (no), or the non-piano part being reduced (yes). So technically speaking, a "piano reduction" is a misnomer.

Personally, I don't really care and I think I have always said "piano reduction". It wasn't until this project that I actually thought about this. When you're writing a program for months that aims to reduce symphonic works, string quartets, accompanied sonatas, etc., it feels kind of mismatched to call it a "piano reduction," and not a "reduction *for* piano." One is much easier to say, though.

Of course, the subject of the reduction is not always "orchestral", so "orchestral reduction" is itself a non-ideal generalization. 

If I had to describe the purpose of the program in the most accurate and pedantic way:

	Reductor aims to produce a reduction of a non-piano work so that it is suitable for a solo pianist to play.

For the rest of this section, however, I will use the casual *piano* reduction, and *piano* transcription, etc.

### Reduction vs. Transcription vs. Other Related Concepts

Disregarding what the words themselves mean outside of music, these terms have yet more connotations and usage-based meanings than what their dictionary definitions dictate. 

For instance, when hearing "transcription", it might conjure the notion of *exact* relocation of information from one medium to another, e.g. RNA transcription or what a stenographer does in a courtroom.

You might be surprised to find that piano "**transcriptions**" in the musical sense can imply a quite a wide range of artistic liberty. Novel asides and segues are added in; the score might be interpolated with runs and fioraturas (fancy runs); themes might even be independently developed. Many transcriptions are of "too large" of works (e.g. an entire 3-hour opera, condensed down into a 20-minute solo piano concert work), and be closer aligned with a "medley".

An **arrangement** doesn't need a whole lot of explanation, as its pretty familiar to even musicians who are not neck-deep in classical mumbo jumbo. It is the term with the least amount of "rules" or connotations. Some piece can be arranged for piano duet, or two guitars, etc. You just take a piece and make it playable for some other instrument or group of instruments. You might add in all sorts of stuff as well, there are no strict rules. There is no shortage of YouTube videos of people making piano arrangements for the Interstellar soundtrack, or Michael Jackson songs, or what have you.

A **fantasy** actually refers to two disparate types of compositions: a standalone, original composition (having nothing to do with transcription or any external work), as well as a type of work that is more akin to a transcription - but with even more liberty taken with the transcribing. It dates back to the Fantasias of the Baroque era (Bach wrote a handful, and are some of his greatest works). Beethoven and Mozart wrote some famous ones too. In the 19th Century, with the rise of the "pianist-composer-virtuoso", these individuals started writing pieces following the formula of "Fantasy on a Theme by Bellini" or "Fantasy on the opera Norma".

Aside: Transcriptions usually refer to their respective composers with hyphenation. The order is usually based on convention/tradition, such as: the Bach-Busoni Chaconne (Busoni's transcription of the Chaconne from the 2nd violin Partita by Bach) or the Liszt-Beethoven symphonies (backwards).

Other related terms that are more-or-less synonymous with fantasy or transcription include **rhapsody** (e.g. Rachmaninoff's [*Rhapsody on a Theme of Paganini*](https://www.youtube.com/watch?v=ThTU04p3drM) from the latter composer's [24th Caprice](https://www.youtube.com/watch?v=PZ307sM0t-0)), **paraphrase** (e.g. Liszt's *Concert Paraphrase on Rigoletto* from Verdi's opera), or **theme and variations** (e.g. Chopin's [*Variations on "Là ci darem la mano"*](https://www.youtube.com/watch?v=_BiMnduoOcE) from an [aria](https://youtu.be/SJRZxSclj70?si=tq8yeU-iKm2QmWrk&t=50) in Mozart's *Don Giovanni*).

### The "Veracity" of Transcriptions and Kin

There is some amount of pearl-clutching in the piano competition or formal jury world as to whether or not it is "proper" to include a transcription in a concert program. A lot of that depends on tradition and convention. Some pieces are considered absolutely okay, and are taken seriously as concert/competition works, like the Bach-Busoni Chaconne (a competition mainstay). But the Liszt-Beethoven symphonies... not so much.

A reduction is the workhorse, functional sibling of the other composition types, which are more for concert performance. A pianist might use a reduction of the score for Swan Lake during ballet rehearsals; a conductor might use a reduction for the musical "Fiddler on the Roof" to conduct off of in performance. They are meant to exactly show what is going on in the full score, but condensed onto a single grand staff. Reductions may even include unplayable notes in a smaller font, with directions as to which instrument is playing. They are more for utility than for performance.

All of Bach's solo harpsichord concertos (back then, "concerto" had a different meaning than how we typically think of concertos - soloist + orchestra) were just solo keyboard arrangements of orchestral works by Vivaldi, Marcello, et al. Those works are very much in the spirit of reduction rather than fantasy or transcription. However, today they are considered standalone works. 

There is a lot of variety/context, convention/tradition, and other factors at play, ultimately, on whether something is "just" a transcription or not! Most important at that point is one's own opinion. If you like it, then it's cool.

### Liszt

The Liszt-Beethoven symphonies are *actually* called transcriptions, even though they are really reductions. They are in a much different style and composition than Liszt's other transcriptions such as the *Rigoletto Paraphrase* or *Don Juan Fantasy*. 
+ Liszt wrote many transcriptions of French, German, and Italian operas by Mozart, Wagner, Bellini, Verdi, Meyerbeer, etc.

Interestingly, the Liszt-Beethoven symphonies (the work that inspired this project) have always existed in relative obscurity. Liszt wrote them - in what is considered to be the ultimate feat of piano transcription - in order to "democratize" them; that is, to make them available to lower or working class individuals to play at home because they could not afford the expensive lifestyle or tickets of the concert-going upper classes in fancy opera houses.

The ultimate irony of the Liszt-Beethoven symphonies: they are virtually unplayable. 

Bless his heart, but Liszt was just too much of an otherworldly virtuoso to know what was "playable" by the common (or even typical concert) pianist. They are rarely, if ever, performed. Only a handful of recordings exist. (When compared to the recording-frequency of other standards in the piano repertoire, it's not even close.)

### Conclusion

What does any of this have to with my project *other* than the mere fact that I love to talk about this stuff? Not a whole lot, but there *are* some implications, believe it or not.

Reduction needs to be a process of as little information loss *or* insertion as possible, and any sort of transformation needs to be purely out of necessity for the pianist to be able to physically play the work.

There are some common patterns that transcribers resort to for various musical textures. For instance, it is extremely common for 2 or more instrumental sections to play tremolos or straight 16ths. Perhaps they are playing at an interval with relation to each other or even doubling at the octave. For a pianist to play rapidly repeating notes takes *an entire hand*, and that's only for repetition on a single key and takes a very specialized technique requiring 2-4 fingers alternating on the key rapidly. To play a chord with the same rapidity is near-impossible, or, at the very least, considered highly un-pianistic. Transcribers will often transform these textures into tremolos, where the hand "rotates" back and forth between the two notes. This is technically information loss, but the texture is preserved, and so it is considered best practice.

Finally, I am not an expert transcriber. My training is in performance, not composition - and knowing your way around a piano is only 1 part of the equation in creating a good reduction. Having outside input from trained composers is what the project will need eventually.

## Appendix B: Cool Music

If you click any of these, let it be [this Beethoven](https://youtu.be/Hn0IS-vlwCI?si=QgjFa4rW9m8IRJIx&t=4399) and [this Liszt transcription](https://youtu.be/I42Afr-OUso?si=li0yIThxkUtwiEdI&t=3326) of the same clip. 

### Arrangement (Vivaldi's Concerto for 4 Violins; Bach-Vivaldi)

What I would call "arrangement". Bach did this *all* the time.
+ [Original](https://www.youtube.com/watch?v=QSs6HKwhbAA)
+ [Arranged for 4 Keyboards by Bach](https://youtu.be/emkJ0A7IfkY?si=8lv9MpQ9KhvXu8YP&t=7)

### Transcription (Bach's Chaconne from Partita No. 2 for solo violin; Bach-Busoni; Bach-Brahms)

Technically an "up-reduction" (just transcription actually); solo violin but on piano with added harmony. This is a very, very famous piece in both the violin *and* piano repertoire (the Busoni version, that is). Some consider it heretical to take Bach, especially solo violin Bach, and try and add harmonies to it to better leverage the more harmonically-capable keyboard. Others consider the transcriptions themselves amazing works of art.
+ [Original](https://www.youtube.com/watch?v=Nunk9fRaZZs)
+ [Bach-Busoni Chaconne - transcribed for piano by legendary 19th Century pianist/composer Ferruccio Busoni](https://youtu.be/dOHiI_5yycU?si=9dbrktNVR5z_1kdx&t=50)
+ [Brahms' rendition, for the left hand ONLY](https://youtu.be/Ljb5MvKv0Hw?si=hrNHsY_WXIcJuDSu&t=6)

### Paraphrase (quartet from Verdi's *Rigoletto*)

In the same league as transcription/fantasy. Lots of ad lib. You'll notice the Liszt starts with an extended "solo" before the "transcription" portion starts at `1:05`.

(Just) the quartet, from Verdi's opera *Rigoletto*
+ [Original](https://youtu.be/sTjbqS7gpBE?si=WzUJQwizKFpvq3Vh&t=93)
+ [*Rigoletto Paraphrase* by Liszt](https://youtu.be/66hWYzbppo0?si=wY2kvwjXXhvXd3HX&t=20)

### Fantasy (Themes from Mozart's *Don Giovanni*)

In the same league as transcription. Lots of ad lib.

I took the original clip from the corresponding scene in *Amadeus*, which everybody, music-lover or not, should see at least once in their lives.
+ [Original](https://www.youtube.com/watch?v=kBXt9Bn4qns)
+ [Liszt's *Don Juan Fantasy*](https://youtu.be/JI6JfJXcUjU?si=gN6vXlBQt2ZVETO3&t=175)
  + Horrifyingly difficult

### The Liszt-Beethovens

And, to close with what sparked the capstone project in the first place: the 9 Liszt-Beethoven Symphonies.
+ Called transcriptions, they are categorically reductions.

From *Reflections on Liszt* by [Dr. Alan Walker](https://en.wikipedia.org/wiki/Alan_Walker_(musicologist)) (via [Wikipedia](https://en.wikipedia.org/wiki/Beethoven_Symphonies_(Liszt))):

    "[Liszt's Beethoven Symphony transcriptions] are arguably the greatest work of 
    transcription ever completed in the history of music."

Beethoven's Symphony No. 9 "Choral":
+ [Original](https://youtu.be/Hn0IS-vlwCI?si=QgjFa4rW9m8IRJIx&t=4399)
+ [Liszt's Transcription](https://youtu.be/I42Afr-OUso?si=li0yIThxkUtwiEdI&t=3326)
  + One of the pianist Cyprien Katsaris' main claims to fame is being one of the only people *ever* to record the Liszt-Beethoven symphonies (his other claim to fame is being a "super-virtuoso", distinguishable from "regular" concert pianists/virtuosos, which tracks considering only someone with that title could perform these!).
+ [All 9 on Spotify](https://open.spotify.com/album/6FqRLd3MrOwk2cE7dceX7x?si=RYzBdW_DSgatOy4PIe1P0w)