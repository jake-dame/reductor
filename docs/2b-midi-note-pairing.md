# MIDI Note Pairing

This concerns the
`toNotes()` function in [ConversionToMidi.java](../src/main/java/reductor/dataconverter/midi/ConversionFromMidi.java). The goal in this document is not necessarily to explain the logic in detail (that is in the inline documentation), but to describe in detail the edge cases in MIDI files that the algorithm must handle.

MIDI "splits" notes up into two separate events (an ON event and an OFF event) to represent one note occurring over time. To construct the domain object
`Note`, these need to be matched up.

But even with spec-compliant MIDI files, there are a few slightly tricky cases that need to be accounted for in pairing logic -- and that's not even mentioning the "messy" MIDI files.

The goal here is to make sure that all `Note` objects have `Range` members that are half-open:

+ Example: An ON event at 0 ticks, followed by an OFF event at 480 ticks ==> A
  `Note` object with a start/stop value (represented in the `Range` member) of `[0,479]`.

I have encountered all but [Case 1](#case-1-stuck-notes) during development -- though the algorithm still handles case 1.
+ A particular Mozart overture and the Brahms *Clarinet Quintet* both had redundant offs ([Case 2](#case-2-redundant-offs)), which took a collective 3 hours looking at raw bytes to track down and figure out.


## The Algorithm at a Glance

`toNotes()` is just a pairing/matching algorithm.

I experimented with applying some of the basic (e.g. two-pointer, search forward) approaches to this. And the algorithm essentially is just a search forward pair-matching algorithm that takes in two lists (ONs and OFFs). However, due to some of the imperfections in MIDI files and some of the nasty things that notation software allows authors to do that does NOT translate well from MusicXML to MIDI, a little extra care needs to be taken with the algorithm.

It essentially uses two maps/lists to put ONs into, then searched through it to find `unpairedOns` or `unpairedOffs`. Since some of the cases are harmless, or ones that I intentionally wanted to ignore, I ended up separating the pairing with the error-handling aspects.


## Case 1: "Stuck" Notes

"Stuck" notes are remain unpaired after all the MIDI data has been processed. There is not much to be done about these.

"Semi-stuck" notes can also occur. This is distinct from "running status", because no effective OFF (i.e., an ON event with a velocity of 0) is sent either.

Semi-stuck notes refer to notes that are stuck for a time, but are resolved eventually because they are effectively turned off by another ON event of the same pitch. For example:

+ A quarter note C4: ON @ `0` ---> ...
+ A second quarter note C4: ON @ `480` --> ...
+ A third quarter note C4: ON @ `960` ---> ...
+ A fourth quarter note C4: ON @ `1440` ---> OFF @ `1919`

The first three quarter notes are turned on, but never explicitly turned off. However, MIDI spec disallows for multiple notes of the same pitch occurring simultaneously. So they are "implicitly" turned off by the subsequent ON event for C4.

The implications for the naive approach are:

+ The first three would be treated as stuck notes
+ And, their Ranges would be constructed as inclusive, rather than half-open:
    + Example: `[0,479]` vs. `[0,480]`


## Case 2: Redundant OFFs

Redundant offs are extraneous OFF events sent for ON events that have already been shut off. They are relatively harmless, but can cause heartache for the naive approach.


## Case 3: Competing ONs

Competing ons are when, for a single pitch, two or more ON events corresponding to disparate notes are sent at the same time (on the same channel). For instance, with a resolution of 480 ticks-per-quarter:
+ A quarter note should have a length/duration of `479`/`480`
+ A whole note should have a length/duration of `1919`/`1920`

So if both those -- which are two perfectly valid notes in the musical sense -- are turned ON at the same time, they will _both_ be turned off when the first OFF event for a C4 is sent.

+ A quarter note C4: ON @ `0` --> OFF @ `479`
+ A whole note C4: ON @ `0` --> OFF @ `1919`

In the case above, _every_ C4 (i.e. all two) is turned off at `479` ticks. The second OFF event (the one that was supposed to be for the whole note), is now actually a redundant off according to the pairing algorithm (see: [Case 2](#case-2-redundant-offs)).