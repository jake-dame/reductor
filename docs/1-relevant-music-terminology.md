# Music Terms Used in reductor

Here is a glossary of musical or domain-specific terms that are used in `reductor` -- either in symbols
(e.g. variable names), or documentation/comments.

If you would like an entry added here, no matter how "fundamental", just do a PR for this file on the 
glossary below. Preferably alphabetized and consistent with the rest of the style.

## Vocabulary Conventions

In this project, "mode" refers to major/minor dichotomy in place of Ionian/Aeolian. This is because 
that is how MIDI documentation refers to major/minor. E.g. the mode byte in a key signature message refers 
to major/minor.

Additionally, always assume Ionian/Aeolian modes, unless otherwise noted (both in documentation and in code).

## Notation Conventions

Notation conventions in formal music theory -- many are case-sensitive. These conventions are not necessarily 
universal, but are common, and they are the ones adopted in this project.

+ Key area shorthand: 
  + `C` ==> C Major
  + `c` ==> C minor
+ Written out: 
  + `C Major` (mode is capitalized)
  + `C minor` (key is capitalized, while mode is not)
+ And hybrid:
  + `CM` ==> C Major
  + `Cm` ==> C minor
+ Roman numeral notation (harmonic analysis): 
  + `III` ==> major "three" chord
  + `iii` ==> minor "three" chord
+ Interval notation (2nd, 3rd, 6th, 7th intervals): 
  + `M2` ==> major 2nd
  + `m2` --> minor 2nd
+ Interval notation (1st, 4th, 5th, 8th intervals) -- no lowercase:
  + `P1` ==> unison
  + `P4` ==> perfect 4th
  + `P5` ==> perfect fifth
  + `P8` ==> octave
+ Alphanumeric pitch notation (pitch + register):
  + `C6` or `c6` (not case-sensitive)
  + `C-1` is read "C negative 1". The hyphen is a numerical symbol, not a readability separator.
+ Accidentals (in plaintext):
  + `b` ==> flat
  + `#` ==> sharp
  + `bb` ==> double flat
  + `**` ==> double sharp


## Glossary

##### Chord

+ Two or more notes occurring at the same time. However, this term is usually applied to instances of
  three or more notes occurring at the same time (the term "interval" is usually applied to the two-note
  version). There is a distance/clustering factor that implies that after a certain distance, a note is
  not part of a clustering, e.g., with piano and the separatio of hands, all notes played at one time are
  not considered a chord unless they are near each other. Otherwise, they comprise 2 or more chords.
+ Example: `C` is a note; `C-E` is an interval; `C-E-B` is a chord.


##### Chromatic

+ Movement through adjacent semitones (as opposed to diatonic ones). Starting from the bottom of the piano
  and playing every key up to the top would be an ascending chromatic scale.


##### Degree (Scale Degree)

+ The unit (pitch) of a formal scale, relative to the tonic (key area).
+ Referred to ordinally: 1st degree, 2nd degree... 7th degree.
    + Example: `C` is the first scale degree in a C Major scale; `E` is the 3rd; and so on.
+ Referred to by formal name: tonic, supertonic... leading tone/subtonic.
    + Example: `C` is the tonic of the C Major scale; `E` is the mediant; and so on.

List:
+ 1st degree ==> Tonic
+ 2nd degree ==> Supertonic
+ 3rd degree ==> Mediant
+ 4th degree ==> Subdominant
+ 5th degree ==> Dominant
+ 6th degree ==> Submediant
+ 7th degree ==> Leading Tone (LT); Subtonic


##### Diatonic

+ Among other things, refers to movement through scale degrees within the context of a specific mode (not
  freeform, like chromatic).


##### Enharmonic

+ Polymorphic spellings that refer to the same pitch.
+ Example: `A#` and `Bb`; `B` and `A**`


##### Grand Staff

+ Refers to a piano/keyboard staves where the top staff usually corresponds to the right hand and the bottom staff usually corresponds to the left hand.


##### Interval

+ The distance between two pitches in terms of scale degrees, which "major" and "minor" as modifiers in 
  order to adapt to semitonal relationships.
+ Example: The distance between middle C and the next black key up is a `m2` (minor second), while the 
  distance to the next white key up is a `M2` (major second). See: [Notation Conventions](#notation-conventions)


##### Inversion

+ Usually applied to triads, but not exclusive to them. Special permutations of a chord that involve 
  moving the top/bottom pitch and raising/lowering it by an octave. There are `n` inversions for a chord of 
  `n` notes. You "invert up", or you "invert down".
+ Example: For a C Major triad, `C-E-G`:
  + Root position: `C4-E4-G4`
  + 1st inversion: `E4-G4-C5`
  + 2nd inversion: `G4-C5-E5`


##### Mode

+ The arrangement of eight pitches and their intervallic spacing within an octave. Defines what is
  considered diatonic within a mode.
+ See: [Vocabulary Conventions](#vocabulary-conventions)
+ Example: The Ionian mode consists of this series of scale degrees: `M2-M2-m2-M2-M2-M2-m2`


##### Register

+ Casually referred to as octave (whereas octave refers formally to an intervallic relationship). The
  "octave" on a piano where a pitch occurs. There are 7 full registers on a piano (and 2 partial ones). On a
  standard* piano:
    + Lowest register: 0th (consists only of three note: `A0`, `A#0`, `B0`).
    + Middle register: the 4th register is referred to as middle; i.e. `C4` ==> middle C.
    + Highest register: 8th (consists only of one note, `C8`).
+ Some high-end/legacy piano manufacturers piano or historical keyboard instruments


##### Root

+ The lowest pitch of a triad while in root position (non-inverted)
+ Example: the root of a C major triad (`C-E-G`) is `C`


##### Root position

+ The non-inverted form of a chord.
+ Example: see "Inversion"


##### Scale

+ A strictly increasing sequence of pitches within an octave, separated by 2nd
  intervals.


##### Semitone

+ The most atomic separation between disparate pitches within an octave. In western classical music, there 
  are 12. Each key on a piano "is" a semitone.


##### Spelling

+ How a tone is represented as a written note on the page. There are many rules/conventions related to
  this, usually involving melodic direction (scale movement) and mode; but even composer eccentricities or
  making a "statement" of sorts can come into play
+ Example: The tone corresponding to `Bb` can be spelled as: `Bb`, `A#`, or `Cbb` (traditionally).


##### Staff

+ Plural: staves
+ The horizontal lines where notes live on the page


##### System

+ A grouping of staves


##### Triad
+ A species of chord made of three pitches, each separated by some kind of 3rd interval.
+ Example: `C-E-G` and `C-Eb-G` are C major/minor triads (respectively)
