#  Table of Contents

Background Information:
1. [Music Terms Used in reductor](1-relevant-music-terminology.md)
2. [MIDI Overview](2-midi.md)
   + [The Java MIDI Library](2a-the-java-midi-library.md)
   + [MIDI Note Pairing](2b-midi-note-pairing.md)
3. [MusicXML Overview](3-music-xml.md)
   + [The ProxyMusic Library (Audiveris)](3b-audiveris-proxymusic.md)

Conceptual Documentation:
4. [Architecture & Package Structure](4-arch.md)
5. [Domain Classes](5-domain-classes.md)
   + [Columns](5a-columns.md)

Appendices:
+ [Appendix A: The Term "Classical", The Term "Reduction", and Brief Historical Info](A-classical-music-and-reduction.md)
+ [Appendix B: Examples of Reductions, Transcriptions, Etc.](B-music-videos.md)


# Resources, Links, & Further Reading

## MIDI

[Recording Blogs](https://www.recordingblogs.com/wiki/musical-instrument-digital-interface-midi):

+ Was instrumental (no pun intended) in helping me learn MIDI. Phenomenal resource.
+ Scroll to the bottom of ^^^ that ^^^ page for a table of contents/index.
+ **CREDITS**: could not find specific author names other than:
    + The makers of Orinj (a DAW) and
    + The editors of _DSP for Audio Applications_.


[MIDI Manufacturer's Association](https://midi.org) (official):

+ Requires becoming a member to download most documentation
+ The RecordingBlogs resource is much better.


## MusicXML

[W3C - MusicXML](https://www.w3.org/2021/06/musicxml40/musicxml-reference/) (official)


## Assets

MIDI --> [Classical Archives](https://www.classicalarchives.com/midi.html)

+ Easy sign-up -- 5 free MIDI file downloads per day
+ High quality

Scores (PDF's) --> [IMSLP - Petrucci Music Library](https://imslp.org)

+ Near-frictionless, account not required
+ Well-established, reputable, and ubiquitous in classical music
+ Most all is public domain; non-PD will notify you (based on region)

Scores (MusicXml) --> [MuseScore Marketplace](https://musescore.com)

+ Paywall-y and not recommended

## Libraries

Used in reductor:
+ [The Java MIDI Library](https://docs.oracle.com/javase/tutorial/sound/overview-MIDI.html) (`javax.sound.midi`)
+ [ProxyMusic](https://github.com/Audiveris/proxymusic) by [Audiveris](https://github.com/Audiveris)

## Additional Software

[Muse Score 4](https://musescore.org/en)

+ FOSS --> [GitHub](https://github.com/musescore/MuseScore)
+ Disparate from `musescore.com`, which is the marketplace/db for scores
+ I recommend downloading without "MuseHub". MuseHub provides additional sound libraries, mostly, but is a separate application, has automatic update checking, network connection, etc. Noisy and annoying.