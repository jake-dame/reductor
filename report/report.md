# Capstone: Final Report

I re-wrote my report to:
1. Include more diagrams as per your recommendation
2. Re-done to consider audience more specifically.
3. Be more like a summary of a journal of the process of building this program.

I obviously don't go into every little thing, but I've tried to highlight some major decision points/pseudocode/etc.

I have eschewed most* background information (except if it helps me to make a point, or describe reasoning/decisions). 

*I included a lot about MusicXML, mostly to draw comparisons with how it is different than MIDI, but also to present the challenges that came with converting my internal representation to it. Maybe you will find it interesting, maybe not.

I would not blame you for just skipping down to [the description of the piece package](#reductorpiece), that's where the most specific and succinct information relating to stuff I came up with is.

## MIDI

My program chopped off two large sections from the whole of MIDI protocol to work with:
+ I exclude any real-time (a.k.a. Sysex message) stuff
+ I do not support SMPTE timing

As a quick overview, the MIDI standard has two top-level protocols:
+ SMF (Standard MIDI File) protocol: how performance/MIDI data is stored in a static file
+ Wire protocol: the same, but how it is sent over the wire and to be interpreted by MIDI-capable devices/instruments

Furthermore, MIDI messages fall into 3 categories:
+ Channel (also called voice) messages: note stuff and how stuff is to sound, basically
+ Meta messages: metadata stuff (time signatures, key signatures, tempo, track name, etc.)
  + Only stored in SMFs, *never* sent across the wire (because it has no effect/nothing to do with how stuff sounds)
+ Sysex messages: not even sure. But real-time performance stuff
  + Never stored in SMFs, always sent across the wire

![midi messages venn]()

Finally, in regards to my program, I decided not to support SMPTE timing on two painstakingly and carefully considered criteria:
+ It's really, really rare, especially for my use cases (amateurs notating, deriving, and posting MIDI files)
+ It's hard

![divison type flow](../Desktop/divisionType.png)

(I have also seen PPQ (Pulses-per-Quarter) referred to as ticks-per-quarter (TPQ) or ticks-per-quarter-note (TPQN). I tend towards TPQ but as "pulses" may make sense in terms of MIDI beat clock stuff, but is really a misnomer from normal musician POV.)

But the whole deal with MIDI resolution (speaking only in terms of PPQ) is that it defines a quarter note. You have a resolution of 24? The lowest note value you can represent before resorting to fractional ticks (which are not even supported in MIDI -- they will always be rounded, depending on the sequencing software) is a 16th note (24 / 2 / 2 / 2 == 3). You could have a resolution of 100000, but it would be a pain for audio engineers to work with (think trying to align a visual element when the grid is overkill in terms of granularity -- except in MIDI, the grid axes are usually irregular and not round numbers). The 120-960 range seems to be the sweet spot. After all, it is all just an abstraction of the microsecond, on which the PPQ value is based. 

### Quantization

MIDI was designed, and is still best, for transmitting and storing exact details of a real-time performance, e.g.:
+ The exact velocity (i.e. volume) of a key being pressed --> some integer between 0 and 127
+ The exact time a key was pressed --> some integer as a delta time from the last event
+ The exact time a key was released --> "   "

It was never meant to translate perfectly to notation on a musical score. It is more concerned with accurate playback.

The last point is the reason quantization exists and is needed for notating (on a musical score) or visualizing (e.g. as rectangles/bars in some DAW) MIDI files in any (meaningful) way.

Humans are not perfect, and do not play perfectly. If a quarter note is defined as 480 ticks, a performer will release the key AROUND 480 ticks after they pressed it - say 478, 483, or some other "close to" value.

This does not translate well to notation software, because there is no original source of truth indiciating what the note was *supposed* to be.

For instance, say notation software gets a note whose length is 171. We will define a quarter note as 480 ticks:
+ The example note is close to both 180, which is the exact length of dotted 16th note, a very common rhythm
+ The example note is also close to 160, which is the exact length of a triplet 8th note, another very common rhythm

It should be noted that, although somewhat dependent on the tempo and resolution, a difference of 10 ticks will be imperceptible to the human ear 99% of the time. However, on paper, a dotted 16th and triplet 8th is a *huge* difference rhythmically.

Another huge issue is that notation programs are free to implement start/stop ticks however they want (MIDI standard does not specify any sort of convention, for obvious reasons - think legato, overlapping effects, etc.). My blood froze late in the process of writing this program when I started encountering MIDI files whose tick values perfectly overlapped (in:re preceding and current note), e.g.:
+ Program A encodes all its notated notes as rhythm values with start/stops ON the grid (always)
+ Program B encodes all its notated notes as rhythm values where only the start of each note is on the grid (and the off is -1 before)

This essentially broke my program, since all rhythmic ranges in my program are encoded as half-open (when considering a length 480 in [0,480]). In many of the range-constructing/deriving functionalities in various parts of the program, I was getting invalid interval exceptions for stuff like [480,480] or [481,480].

In fact, that's what prompted the quantizing functionality at the last minute, anyway.

#### Workarounds

I looked first for libraries that quantized. There were a couple options, but none of them looked easy enough to integrate with my program. In fact, the best matches (I give you a range, you quantize it), were not in Java, and with my time-crunch, I chose the lesser of two evils:
+ Learn (for the first time) how to write JNI stuff for a C++ quantization library
+ Implement my own (even if simplified)

#### Implementation

To implement this, you could go the route of having some map of determined rhythm values, and some heuristic/algorithmic approaches to determining acceptable threshold or MOE values when mapping incoming note lengths to what they "probably" are. 

This work well most of the time. Professional grade notation software, however, has to be able to support (nearly) any type of rhythm the author can conjure. This means your quantization map needs to account for a quintuplet-dotted-64th note. 

The problem you run into is that a lot of the pre-determined note values start getting close enough in range that they either start to overlap, or screw up the MOE calculations. You could adjust the MOE to be logarithmic or something, so that it too shifts based on the initial length of the note, but you can't fully escape the principle of: the more granular the rhythms you want to support (no matter how exotic are frequently seen in practice), the more your accuracy can suffer.

Furthermore, there is no way to tell if they performer simply executed a rhythm with a truncating or extending articulation (staccato or legato, respectively), which adds yet another "impossible to perfect" element to the quantization implementation.

My quantization function, which seems to work a lot of the time (although not vigorously tested as it was a late addition to the program), started out as around 300 lines of code. It originally intended to preserve the original resolution, but when I realized that even MuseScore (when outputting MIDI based on the notated score) basically just converts everything to a resolution of 480 (which is the convention in MIDI -- not amazingly fine-grained, not super coarse), I decided to just scale everything to 480 myself, and stop caring about the in-resolution. Uniform scaling did simplify things, and after several other realizations (that also had to do with unnecessary transformations -- quantizing in the end is a pretty simple formula) the function diminished a lot in size.

The code (omitted getter/setter, exclusive vs. inclusive range stuff) is as follows:

```java
public static Range quantize(Range inRange, int inResolution) {

	// Philosophically, this should be a double. In MIDI, it really won't make a difference and will prevent some heartache and 
	//     unnecessary coddling of data types and fractional amounts that get thrown away in the end anyway
	long scale = 480 / inResolution;  

	Range scaledRange = new Range(inRange.low * scale, inRange.high * scale);

	// This is an enum value that includes various fields about a rhythm
	Rhythm rhythm = Rhythm.fromRange(scaledRange);

	// This is the "simple", essential formula for finding what a rhythm should be when quantizing.
	//    rhythm.base is a constant, in 480 resolution, of note lengths for whole, half, qtr, 8th, 16th, 32nd, 64th, and 128th
	//    rhythm.divisor is if the rhythm is (likely) part of a tuplet (an 8th in 3, or a triplet 8th, would have divisor 3)
	double gridWindowSize = rhythm.base / rhythm.divisor;
	Range gridWindow = new Range(0, gridWindowSize);

	// "Ratchet" the gridwindow up to where it's supposed to be (javax.sound.midi ticks are absolute, not relative). 
	//   This (using an arbitrary tolerance)worked best when considering that you didn't know if you were catching 
	//   the head-end of the sloppily executed target note, or the tail-end of the previously quantized note
    final long tolerance = 4;
    while (gridWindow.overlappingRegion(range) < tolerance) {
        gridWindow = Range.getShiftedInstance(gridWindow, gridWindow.duration());
    }

	// To snap, we just set its low to be the current grid window
	// To set the high, we rely upon the Rhythm enum yet again to tell us how long the note should be *exactly*, from the new low
    scaledRange.setLow(gridWindow.low());
    scaledRange.setHigh(scaledRange.low() + rhythm.duration);

    return scaledRange;
}
```

To test this "en masse", I used the logarithm thing I mentioned earlier. This is because it shouldn't be a linear relationship (the test offset, that is), because even with larger rhythm values like a quarter, you're not going to have someone (unless they've had one too many while performing at open-mic night) executing a quarter note with a 50 tick MOE. Yes, the threshold/offset should be based on length of tick, but there should be a sort of "cap" at some point.

### Notating MIDI

MusicXML (which I will go into later) is sort of a response to the shortcomings of MIDI in terms of notation.

MIDI was developed during a time when GUI's and displays could not accomodate advanced music notation. It was never intended to be something that stored/transmitted detailed notation information. It was intended to be a protocol for digital instruments to communicate with each other and computers.

MusicXML was developed much later (early 2000's) by a W3C group, which is still in charge of its standard today, as early notation programs (like Finale) and GUI's that allowed plopping notes onto a staff with the mouse became doable.
+ Fun fact: Finale, an industry standard for the last 20+ years announced it was ceasing production the first week of this semester.
+ Other prominent notation software includes Sibelius, Dorico (both pricey), and MuseScore (FOSS).

Some of the metadata the MIDI *does* include that can be helpful for notation:
+ Time signature events
+ Key signature events
+ Tempo events

For instance, MIDI data can be parsed and used to display this in notation software:

![midi vs musicxml](../Desktop/paper%20examples/midi_vs_musicxml.png)

The thing is, however, that how much metadata a MIDI file includes usually correlates to whether it was "recorded" (by somebody playing a keyboard), or notated (somebody went in and manually made a score in notation software, and then the notation software exported a MIDI file based on the MusicXML data associated with the score).

#### Time Signature Events

There is no way to calculate *anything* regarding measures without time signature events. Thankfully, if the author of the MIDI file notated things accurately, time signature events will exist *and* be placed correctly within the score.

This means that time signature events can be ranged (i.e. [start tick, stop tick]), and a note happening at *x* time can be placed into a measure context.

My program does not attempt to parse files without time signature data, and throws them out.

If you have the time signature, and the value of a quarter note (i.e. the resolution), you can calculate measure size, which I have as a utility function in the TimeSignature class, and takes the upper and lower numerals of the time signature (which can be extracted from MIDI data, although it is not the most straight-forward process, and happens in the MIDI file processing part of my program). It essentially just involves getting the "fraction" denominator to be 4, and then multiplying the resolution by the numerator, which would give you the size of the measure in ticks. I took out all of the error-handling/assertions for simplicity:

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

In the image above, there is a pickup measure. I spent a long time writing an algorithm to "detect" the presence of a pickup measure:
+ Put all the time signature events into a stack; calculate and fill a container with measures; if the last measure created is shorter than the last time signature popped, it is probably a pickup
+ If the first note happens after a sizeable amount of rest, it probably occurs in a pickup measure

Turns out a lot of my work was pointless, because notation software (generalizing to really mean MuseScore) will encode pickup measures as a discrete time signature, with an identical lower numeral but smaller upper numeral. For example, the snippet displayed above outputs as two time signature events: 1 measure of 1/4, followed by measures in 4/4.

However, the near-full measure of rest with a pickup note case still applied, so at least I had that. It really depends on the author of the MIDI file/score at that point.

#### Key Signature Events

Key signature events are pretty pointless in terms of my program, as far as aiding in harmonic analysis (what is the root of this chord, what key area are we in at this measure?) because even in music, key signatures don't indicate anything but usually the key area of the first and last measure (even then, though, there are a litany of counterexamples).

#### Tempo Events

Tempo events are also kind of pointless in my program, except I handle and include them because it became really nice to control the playback speed of the MIDI files I outputted without having to put them into notation software and manually do it.

The thing with tempo events is that they need to be increased/decreased by a scale, rather than literally, because there may be many tempo events in a MIDI file. Depending on the notation software, tempo events may be used to sort of "frankenstein" together a ritardando, fermata, or any other time diminution/augmentation expression.

## MusicXML

Like explained above, MIDI really is not intended for storing/transmitting score details. It is for performance details, which any musician knows, can be very different from what is in the score (that's the whole point of artistic interpretation, not to mention imperfect execution).

But, if you play back a MusicXML-turned-MIDI file, it will sound much more robotic than a MIDI performance (altough parsing software is getting better and better, along with providing more options to notating authors to really meticulously control playback based on the score). So they each have their domains:

![midi vs musicxml](../Desktop/paper%20examples/midi_vs_musicxml.png)

I knew that at some point, this program would have to start dealing in MusicXML, since the whole point of the program is to produce scores, not recordings. Additionally, having exact control of which hand was notated on which staff (upper or lower) in a piano grand staff was paramount. The notation software I had experience with (MuseScore and GarageBand -- which isn't notation software per se but includes most of the functionality needed to display MIDI as a score) used the most barebones approach at assigning hands: middle C and above notes go on the upper staff, and the rest go on the lower staff.

### A MusicXML Document

MusicXML is, of course, hierarchical, rather than serial.

The hierarchy is as follows:
+ ScorePartwise
  + Holds the score header (various metadata), with one mandatory part-list element, containing essentially a mapping to how many parts (and their associated metadata) are in the score (e.g. piano, violin, cello, etc.)
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

ScorePartwise is the root element. Score header elements (i.e. the part list, work info, credits, encoding software name and instructions, etc.) and the various Part elements are contained within. Each Part element contains Measures. Each Measure contains Notes.

This being a piano score, means that there is only 1 Part element, with 2 staffs.

### Timing

MusicXML essentially follows MIDI PPQ timing stuff (i.e. its all based on the value of a quarter note). Instead of resolution, it is called divisions, and it defines how many "divisions" are in a quarter note.

Most MusicXML files I came across had, vis-a-vis MIDI resolutions, small values. Instead of 480, 960, etc., I saw a lot of 24, 48, and 96. At first glance I thought "ok, they are just lowering the granularity and dividing everything by 10." There seems to be some more convention to this, but I didn't have time to look too deeply into it. 

I decided to just straight-across use the MIDI resolution (which is what the value in my internal respresentation is based on) as the divisions value. It doesn't seem to have an negative affect. So, MusicXML **divisions** for all the files I output is 480.

### Note Placement

A lot of MusicXML stuff is pretty straightforward, except the placement of notes is can be pretty tricky.

I will use the below example, from the first measure of the C minor prelude of Chopin:

![chopin c multivoice](../Desktop/paper%20examples/chopin_voices.png)

If you look closely, you will notice that in the 3rd beat of the right hand, there are 2 independent voices: the quarter "base", and the moving line (in thirds) in the melody/soprano. 
+ The base (the g and b quarters) would be held throughout the duration of the beat by fingers 1 and 2
+ The thirds above it would be played by fingers 4 and 5, and then by 3 (or 4) and 5, respectively.

In musicxml, and if you noticed above, notes are placed sequentially. At first glance, this sounds like it is essentially the same as MIDI. TLDR: it's note.

In MIDI, if I wanted to "play" a C major triad (quarter notes -- recall a quarter is usually 480 ticks), it would look something like this:
+ 0 ON C4 --> 480 OFF C4 --> 0 ON E4 --> 480 OFF E4 -->0 ON G4 --> 480 OFF G4
+ It is all sequential, and it is up to the *sequencer* (i.e. sequencing software) to take that "flat map" and put everybody into their corrals and send them off at the right time during playback.

With that same level of simplicity, if you were to encode that same sequence of notes in MusicXML without any of the MusicXML-specific workarounds, it would look like this:

![bad musicxml sequence](../Desktop/paper%20examples/musicxml_sequence.png)

Where each note looked something like this (this is just for the first C):

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

In MusicXML, pitches don't mean anything to following pitches: each note looks like a note to each other, so each note effectively "pushes" the following notes forward by its own duration.

Now, MusicXML has a really easy fix to this: if something is a bona fide chord (i.e. all the noteheads are attached to the same stem), you simply prepend a chord (an empty element) tag:

```xml
      <note>
        <chord/>
        <pitch>
          <step>C</step>
		...
```

Simple enough. MusicXML has provided a way to easily attach noteheads to the same staff. But how you would place noteheads (i.e. notes) that are to occur at the same time as other notes but are *not* to be attached to the same stem?

Essentially, each time you want to place notes within the same time range (on the grid) that don't have contiguous stems, you have to add a `<backup>how_many_divisions</backup>` element to get the "counter" (helpful to think about it when writing the conversion code as a counter or cursor) back to where you wanted to place something.

In the Chopin example, and adding in the left hand for consideration, there are three non-contiguous stem groups all happening on beat 3:

![chopin stems](../Desktop/paper%20examples/chopin_stems.png)

One thing to add before explaining summarizing whole process (which will probably be clearest): if you want gaps between notes, you need to add a forward element. It works exactly like the backup element, but progresses the cursor forward.

Finally, the left hand in the piano Part is usually "gotten to" by first filling in the whole right hand, and then backing up to the beginning of the measure (which requires you know who far forward you are), and starting over.

So, to use a slightly simpler example than the Chopin:

![note placement process](../Desktop/paper%20examples/note_placement_process.png)

Now in English/pseudocode (note: I already separated the lists of notes into RH/LH):
```
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
```

My code uses prevNote and currNote for various reasons, but this was simpler for the pseudocode.

Additionally, notes that are tied over the barline are an absolute pain, but I won't go into that here.

Additionally-additionally, there are more nitty-gritty things and edge cases, and the logic is rather lengthy. I'm sure it could be simplified, but the note placement stuff consitutes by far the largest share of musicxml conversion logic.

### On the External Library I Used

For the marshaling/unmarshaling stuff, I used the [proxymusic](https://github.com/Audiveris/proxymusic?tab=readme-ov-file) library from the Audiveris project. I didn't have time to look too deeply into it, but I am actually looking forward to looking into Audiveris as a whole, as it is a OMR (Optical Music Recognition) software! Again, haven't looked too deeply into it, but... apparently can notate (uses MuseScore, I believe) based off of an image/pdf of a score.

The library seems to be fine, but: there is ZERO documentation (except for a small and pretty uninformative unit test).

Thankfully, most of the code is just getters and setters.

So I basically went through the W3C/MusicXML documentation (which has a tutorial), and then used their [reference](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/) page to look up cases as I came across them. Then, I literally just used cmd+f to look for possibly-related functionality in the proxymusic library.

There were some surprises, but on the whole, it was mostly just tedious, and I got the pattern down eventually.

I am nearly done with the musicxml package of my program, except for the not placement nastiness I mentioned above. I fix one thing, and a new case presents the need for handling. Then the code gets so busy with case-handling that I re-design... rinse and repeat.

However (not to toot my own horn), the conversion to musicxml process was made even *remotely* possible because I had already implemented a bunch of the stuff it needed in my internal representation: the biggest being Measures, which could hold time signature, key signature, notes. I had also already implemented (the default/basic) hand-splitting stuff.

However, I definitely was missing some things too, mostly related to pitch stuff. This is probably because I had written the program based on MIDI for 3.5 months, and in MIDI, pitches are literally just 0-127 values. Any of the stuff in my program to extract register, semitone, string-parsing, etc., was to make *my* life easier, and wasn't the most robust. Some of it didn't align perfectly with how musicxml does things (especially regarding enharmonic spelling, which my program doesn't handle -- mostly because my program does not have key area analysis capabilities and that is the big in determining if something should be spelled as an A# or as a Bb).

The whole internal representation of my program could probably be combed through and re-designed to be a bit more general and/or include stuff that will make things easier for musicxml. It's either that, or remain ideologically "pure" and keep the internal representation completely divorced from any MIDI/MusicXML paradigms (already impossible since most all my timing stuff is based on MIDI PPQ) and just have the conversion functionality be extreme heavy lifting.

## Package/Program Structure

reductor is split thusly:

![package structure](../Desktop/paper%20examples/package_structure.png)

Additionally there is a reductor.util that handles file I/O stuff; playback using javax.sound.midi (easier than opening another application to just confirm the file sounds right in the first 3 seconds); opening with various applications using `ProcessBuilder`, and a class called `MidiDebugging` that just has a bunch of printing utility functions to look at midi bytes in various ways.

### Application

The `Application` class was a late addition to the program. It may not be final, either. It was called `DevelopmentHelper` for a long time. It is just to coordinate package duties in one, single program flow. It takes a String filepath, and pops out either a MIDI or musicxml file, which I change around depending on what I'm working on. It isn't final or anything, but I include it here because it shows well how the program is intended to flow, and show how the different packages are kept separate and interface with each other:

![application class](../Desktop/paper%20examples/application.png)

## reductor.midi

The purpose of this package is to get midi data in a form that the `reductor.dataconversion` classes expect.

In the future, it will probably become part of a sub-package that it shares with a sister musicxml-importing-related package, similar in the way that the dataconversion package is split into two.

It is outlined as:
+ `MidiFile`: top-level things about the midifile, including a `File`, `Sequence`, and `Events` member
+ `Events`: a "list" class (just contains a bunch of lists)
  + In charge of sorting, typing, and creating all the `Event` instances
+ `EventType`: an enum used to support the next bullet
+ `Event`: an abstract wrapper class
  + Subclasses have 
  + Gives TimeSignatureEvent and KeySignatureEvent have specific fields and utility methods

The biggest impetus for the Event hierarchy was literally to override toString in all of them because it made debugging *and* simply learning MIDI 10x easier. A nice side-effect was that all the complex MIDI-to-English conversion stuff (undoing the encoding scheme) ended up being lifted and placed into the `dataconversion` package later almost wholesale.

I hesitate to call the javax.sound.midi library "feature-poor", because I have gone through the documentation and I believe it does exactly what it intends to do. It isn't intended to be a full-fledged "composing with MIDI" library. It is meant for reliable and robust file I/O, and acting as a conduit through a Java program to other software (e.g. sequencers), with *some* manipulation abilities. However, that said, using it in the way where seeing every message type and manipulating or sorting stuff in a type-safe way is not really a feature of the library. Everything below was written as a respone to the needs I had while debugging and figuring out what to do with the data.

Writing all the code described below was basically done by reading documentation on various websites, the biggest help and best (most concise, easiest to follow), is [recordingblogs' wiki](https://www.recordingblogs.com/wiki/standards-in-music-index). I didn't really consult official MIDI documentation because their (the Music Manufacturer's Association) sites were hard to follow, some of their stuff is behind a paywall, and I didn't really need to know the amount of detail in different MIDI standards like MIDI 2.0 or General Midi, etc. Just tell me what the 3rd byte of a time signature message corresponds to!

The `Event` class is pretty messy, and I'm not too happy with it. What I am happy about is stated above (it was a great exercise in learning MIDI and basically gave me everything I needed to have/know to implement conversion stuff later). However, although I *thought* I did initially, I still don't fully grasp the details of generics and compile- vs. run-time type-checking.

The Event class used to be parameterized so that its `message` field was of type `T`. This was useful because the two (of three that I used) MidiMessage concrete classes have *different* methods to check even basic things, like the status byte (e.g. in MetaMessage, it is `getStatus()` and in ShortMessage it is `getCommand()`), so not having to constantly cast before calling those was nice. My ChannelEvent and MetaEvent subclasses looked like `class MetaEvent extends Event<MetaMessage>` or `...<ShortMessage>`.

I tried to fix the unchecked thing with all sorts of approaches (the enum, a factory method, etc.). But I guess I really just did not ultimately fully understand what the right approach was.

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

However, at that point in the Capstone, I was cognizant of the fact that I was spending too much time making my data acquisition process "optimized" with constant re-designs, and, with the knowledge that it was robust and worked, while not being great, I decided it was time to move on.

Finally, the whole `.midi` package could probably do away with any typing, complex sorting, and just have 1-2 classes: just loop through all the events, grab the ones you want (only need like 30% of the what the subclasses represented in the end), but, I what I *was* happy with was how deeply I got to know MIDI and it was a good realization in that I need to be more careful with generics, and really get to know what the whole deal with typing is in that regard.

The reductor.midi's biggest job is to produce a valid `MidiFile` object.

## reductor.dataconversion

dataconversion is meant to be the bridge between the native format package (either MIDI or musicxml) and my internal representation (`Piece`). 

dataconversion doesn't make any (consequential) decisions on its own. It expects acquired data in a certain form (the job of the reductor.midi or future reductor.musicxml packages), and converts them into their analogous `reductor.piece` classes, usually using static utility functions that are *in* that package. The constructors or factories in the piece package can be said to slightly cater to the dataconversion class, but that's kind of a chicken-or-the-egg thing.

This is also where quantization happens.

### Note-Pairing

dataconversion is where note pairing happens (`reductor.midi` just has gettable list of NoteOnEvents and NoteOffEvents).

We talked plenty about note-pairing headaches, so I will try and just summarize case-by-case. I *did* encounter a couple of these in the wild (a particular Mozart oveture and the Brahms *Clarinet Quintet* both had redundant offs, e.g., which each took a collective 2 hours to find/figure out).

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

It turns out that some of these are harmless. Even though it may not be the actual use of custom exceptions (another thing I struggle to understand still - exception, try/catch, error propagation, etc., I thought it would be cool to make my own exception at one point and this seemed the perfect place to have `UnpairedNoteException`. In the far future, maybe another developer can choose to ignore certain UnpairedNoteExceptions or something, rather than me throwing a RuntimeException or something in the note-pairing algorithm.

That final "reverse conversion" stuff turned out to be: yeah, MIDI can't really do this at all. This is a job for MusicXML and voice control. I experimented for some time writing out overlapping voices on different channels, then using MuseScore's "implode" functionality (puts voices from different staves onto one selected staff), but it was unwieldy and annoying and, of course, not a good solution for an product (can't expect the user to do that).

## reductor.piece

I am going to have to do a real fly-over view of this stuff.

The Piece class's heavy-lifting "utility" classes are `Range` and `Pitch`.

### IntervalTree

There is, of course, the `IntervalTree`, which currently only supports construction from fixed data, and then queries with either a Range or a point (long). 

I was able to genericize the tree:

![interval tree](../Desktop/paper%20examples/interval_tree.png)

which means it can be used to store any Ranged object:

![tree uses](../Desktop/paper%20examples/tree_uses.png)

This is, of course, probably a place where I could take a more functional programming approach and dynamically construct things. So I would just have the one Note tree, and anytime I needed a Measure, it could be lazily created from a query. This would mean either more query methods, or, perhaps, a "plug-in" style where each class has its own specific query method, and the tree can just use that method. However, it was really necessary for me to see exactly what lists contained at certain points during the debugging process, so they needed to exist.

A final note: one of the main purposes of the quantization functionality was to force the use of Ranges in my program to represent half-open ranges relative to the note duration. However, Range also includes to getters with different purposes, which might be a design mistake, and I am still working this out:
+ `length()` represents the true length of the Range: [0,479] has a length of 479
+ `duration()` is the "inclusive" representation of the Range: [0,479] has a duration of 480

### Measures

Measures have to be created from scratch when working with MIDI data, since they is nothing in MIDI having to do with measures.

I briefly [mentioned](#time-signature-events) some of the issues with pickup measures. Treating a collection of Measures as a simple list does not suffice. Measure 0 should only exist if there is a pickup measure.

Secondly, there is the issue of measures being a 1-indexed sort of thing.

So access to a collection of Measures has to be controlled some way. If you were a developer and using the API, what would you expect these method calls to return:
```java
// The first measure of the piece? Measure 1? The second element in the Measures list?
piece.getMeasure(1);

// Should this throw an out of bounds index exception if there is no pickup measure?
piece.getMeasure(0);

// Should this always return Measure 1, regardless of whether or not there is a pickup measure?
piece.getFirstMeasure();
```

The `Piece#assignPickup` method handles the heuristic approaches (although one is slightly redundant, and one isn't heuristic per se) to handling pickups:
```java
// TimeSignature#compareTo will return a negative integer if both denominators are the same and the numerator is less than
boolean heuristic1 = firstTimeSig.compareTo(secondTimeSig) < 0;

// This is technically redundant, I just haven't decided which is best yet, and need to see more cases. Checks if the final measure complements the anacrusis (which was fairly traditional in classical/baroque works).
boolean heuristic2 = lastTimeSig.compareTo(penultimateTimeSig) < 0
		&& firstTimeSig.getDenominator() + lastTimeSig.getNumerator()  ==  firstTimeSig.getNumerator();

// The next heuristic handles the case where the initial measure is *not* encoded as a distinct, lesser time signature, and is, instead, authored as a normal measure with a subtantial period of rest before the first note. This is the only *truly* heuristic technique of the 3, but I named them all like this anyway.

// Right now, this is exactly an eighth rest (half of the value of a quarter)
final long THRESHOLD = (long) (TPQ * 0.5);
long amountOfRest = Math.abs(firstMeasure.getRange().low() - firstMeasure.getColumn(0).getRange().low());
boolean heuristic3 = firstTimeSig.compareTo(secondTimeSig) == 0
		&&  THRESHOLD < amountOfRest;

if (heuristic1 || heuristic2 || heuristic3) {
	measures.getFirst().setIsPickup(true);
	return true;
}
```

### Data Representations

The idea is that you have the Piece, which (theoretically) contains:
+ Measures which contain
+ Boxes which contain
+ Columns which contain
+ Notes

![data structures](../Desktop/paper%20examples/data_structures.png)

An entire Piece can split into just Columns, or just Boxes, or just Measures, but the important subdivisions of a piece, in terms of analysis, are:
+ Box (horizontal analysis/manipulation of notes in Columns -- left to right, or pitch- *and* texture-wise)
  + A Measure is just a "special case" of a Box (not really class-wise, but theoretically)
  + Can "plug-in" further algorithms for hand-splitting (is there lot of jumping around going on that would make certain Column-only hand-splitting decisions untenable?)
+ Column (vertical analysis/manipulation of notes -- up and down, or purely pitch-wise)
  + Triage first stop for hand splitting and basic texture-thinning (remove doubled octaves, etc.)
+ Note (leaf element)

Even if Measures were never constructed (or were an on-demand thing), specialized queries could label a Box as being: 
+ A full beat
+ Some subdivision of a beat
+ A strong beat, a weak beat, a pickup beat, etc.

And then Columns would take that information into account, as well. A sort of "indexing" (that is to say, locating using a determined set of "coordinates") scheme now exists. The following example shows just the "middle" (vs. RH vs. LH) section of a measure.

![indices](../Desktop/paper%20examples/indices.png)

So, one could say: get me the (middle/LH/RH) notes of beat 3 (or some subdivision of beat 3) of Measure n.

As I write more actual reduction stuff, rhythmic/beat analysis is going to very important (knowing *where* in a measure a certain Column or Note occurs.)

#### Column

I will only go into depth on Columns, but even then, I will try to keep this brief. My `Column` class has the most documentation of any of my classes by far, and explains what "pure" and "semi-pure" Columns are, how it assigns "holdovers", splits hands, etc. It makes use of a `Consumer<Column>` type that will (in the future, hopefully) allow Boxes or Measures or other actors to re-split hands based on wider contextual information.

But what I am most proud of is the Column construction process, so that's what I will highlight here.

A Column represents the **smallest unit of musical change, regardless of where it happens in the staff.** Basically:
1. Each time a new note occurs, a new Column should be created
2. The Column should know about notes that extend into it from previous Columns
3. The Column should only ever manipulate notes that are native to it (i.e. not holdovers -- that's another Column's job/responsibility)
4. A Column should contain all other notes on (a "vertical" segment, thus the name Column) during its Range, but its Range should never include more than one start tick. Again, if a new start tick occurs, that signals new Column creation.

The purpose of the Column is to compare, analyze, and manipulate notes by pitch. Everywhere else in the program, Notes are used in the "Range" sense of things, NOT by pitch.

## Design Patterns

### Builder

I had a lot of fun implementing these. My only regret is that I did not do them earlier.

I dealt with a lot of constructor explosion problems that the NoteBuilder solved, preserving immutability while streamlining things. Here it is in action:

```java
Note note1 = Note.builder()
		.pitch(60)
		.isHeld(true)
		.instrument("violin")
		.start(0)
		.stop(480 - 1)
		.build();
```

Any "setters" in the Note class return a new instance using the copy-construtor capabilites of the NoteBuilder (it will set all the defaults to match the passed Note object):
```java
Note setPitch(int pitch) {
	return builder(this)
			.pitch(pitch)
			.build();
}
```

The ChordBuilder has a method that takes a vararg which makes use of my `Pitch.toInt()` string parser as well. The below will output a C7 chord rooted at middle C:

```java
Chord chord = Chord.builder()
		.start(0)
		.stop(479)
		.add("C4", "E4", "G4", "Bb4")
		.build();
```

The other main impetus for writing Builders for certain classes was writing tests. I was needing really specific and SHORT (i.e. contained/focused) test cases, and it was really, really becoming a pain going into MuseScore, creating the score like that, exporting the MIDI, adding that into my Files development-helping class... Just to test one tiny thing that I knew my internal representation had the ability to create really quickly, but was just inaccessible. (The immutability was the other problem, and needing more control over the construction process with very specific parameters).

Thus, the phrase builder (which combinder the NoteBuilder and ChordBuilder capabilites). If you output this object (with the appropriate util class for writing midi files) and open it in MuseScore, it looks exactly like this, which is the exact first measure of the e minor Chopin prelude:

![noteBuilder](../Desktop/paper%20examples/phrase_builder.png)

### Composite

Not much to say here other than: the `Noted` interface includes a single getter `getNotes()` that means that anyone implementing it needs to yield its Notes, whether it is a Note container, a container of Note containers, or even a Note itself.

Here, the Measure does not need to know if the Column is returning a filtered/transformed collection of notes, or a deep or shallow copy, or anything. It just calls `getNotes()` on all its constituent components, and those components in turn call `getNotes()`, until the leaf Note, which returns itself.

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

We talked a bit about a related thing (in the form of the plub-in architecture stuff), and I would like to implement it even more in the future.

The problem is that at compile-time, I obviously don't know which MIDI file I will be processing. 

The bigger problem is that certain data structures (Column vs. Box) have different breadth of context and might know something the other doesn't in terms of hand-splitting functions or reduction algorithms.

Right now, the only place to implement this design pattern is the Column class, which has a:

```java
Consumer<Column> splitFunc;
```

which is complemented by this utlity class:

```java
class HandSplittingFunctions {

    static final int MIDDLE_C = 60;
    static final int SPAN_MAX = 14; // major 9th. I can't reach a 10th of any kind except double black keys
    static final int NOTES_MAX = 6; // I have yet to come across a piano chord with 7+ notes


    static void defaultHandSplitter(Column col) {

        final int size = col.notes.size();

        if (col.notes.isEmpty()) { return; }

	//...
```

I hope to adopt the same approach, or something similar, for the reduction algorithms.

## Biggest Problem with Current Implementation of Reductor

1. Still no actual reduction algorithms! Except, at various times in the last couple months, *very* primitive functionality related to eliminating too large of jumps (melody/line recognition) and double octave removal (something commonly found in orchestrations and commonly omitted in reductions).
2. Mutability/Immutabilty/Java handling of passing by value

Concerning number 1: providing the foundation for reduction (deciding how to split up the data, knowing exactly how to manipulate the data, converting to actual output forms with all the edge cases and nastiness) really did take all my time. But every day it feels like I'm that much closer to having something where implementing actual reduction will "simply" (ha) be a matter of writing the algorithms and applying them to the correct data structures (boxes or columns or measures) rather than having to make design decisions every step of the way.

Concerning number 2: I really, and I mean *really*, bungled this one. And it wasn't until the last couple weeks (when I was neck-deep in Builder patterns, quantization, musicxml, and other last-minute fix-em-ups) that I realized how big of a problem I had created for myself here. If anything in my program is a damocles' sword right now, its this, and I am quite intimidated by fixing it, because it involves some very foundational design decisions.

Additionally, I don't think it will be fixable until my IntervalTree is self-balancing and has add/remove functionality, which is another thing I am procrastinating. Technically I could pretty quickly/easily just create add/remove functionality, and like we talked about earlier in the semester, the scale of the data being added removed would mean that, yes, the tree would techinically be unbalanced, but note enough to have any kind of effect on performance. (I mean, I don't anticipate that amount of data/computation this program does *ever* will fall into the performance-concerned arena.)

The issue is: I applied the immutablity, safe access (getters/setters), and encapsulation principles so blindly and overarchingly, that in the end, every single object was an island and the whole purpose of the program (to change/manipulate, or at least create a "manipulated" representation of the original) was defeated because:
+ One Column decides a Note needs to be removed from itself because it is a doubled octave. Guess what, that change is not reflected ANYWHERE else.

When I first realized this 2-3 weeks ago, I thought to myself: "this sounds exactly like some observer pattern thing, where a change to one thing means everybody else with a reference to that thing needs to update too." But there was no time to implement a whole MVVM-style observer pattern and SSOT. I am also convinced that's not the correct re-design. There are some other approaches I have in mind but I am not sure about those either. I don't think I will even start that until I fully finish musicxml capabilites (both in and out) for the program, and write more unit tests. I think I just need to have some combination of *allowing* Columns and Boxes and Notess to be mutable or at least semi-mutable, and then base
