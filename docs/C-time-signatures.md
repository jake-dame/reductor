CAUTION: This doc under construction, has not yet been written well. It is chunks of notes from 
research compiled while trying to work on the parsing of <time> elements in MusicXML.


# Time Signatures

# Overview

Polymeter, or polymetric music, refers to portions of music where distinct parts play in 
different meters.

Polymeter is not to be confused with polyrhythm, or with cross-rhythm.

"Is X example polymeter, mixed meter, polyrhythm,...?" is one of those questions that doesn't 
have an absolute or pure answer, and can be argued about exhaustively between music theory nerds 
and what seems to be mostly percussionists.

Some say Chopin constantly writing tuplets of 11 or 15 or 17 over plain eighth notes in a 4/4 
meter is an example of polymeter that is simply notated NOT as polymeter.

Arguments like that.

But at its simplest and non-pedantic:

+ Polymeter: refers to different parts in different meters where the pulses line up in some way 
  and note values are generally kept the same (in terms of subdivision)
+ Dual meter: this is more of a notation than theory. Two time signatures where one is primary 
  and the other is a secondary grouping suggesting, such as 3/4 and 6/8 next to each other
+ Mixed meter: frequently but systematic/patterned alternating of meter. Example: 3/4, 3/4, 5/4, 
  repeated in that sequence over and over
+ Polyrhythm: this is not a meter thing so much as broadly referring to when two rhythmic parts 
  are played simultaneously but with different rhythmic feels/groupings. 2 against 3 is an 
  example of polyrhythm, and also a specific form of polyrhythm called hemiola (which can also 
  be 3 against 2, the inverse).
+ Cross-rhythm: a systemic use of polyrhythm (as in, it is the defining feature of a large 
  section or even the whole piece). Lots of dance music features pervasive polyrhythm in this way.


Polymeter is more prevalent in pop, rock, and folk music than it is in classical music. Within
classical music, it is more prevalent in 20th-Century and post-modern styles, where 
experimentation with meter and rhythm kind of went off the rails. It is much rarer in the Baroque, 
Classical, and Romantic styles.

How these things are [notated](https://libres.uncg.edu/ir/uncg/f/umi-uncg-1566.pdf) is as much a
setting of convention as is formal theory.

In the Baroque, Classical, and Romantic eras, meters outside duple, triple, or quadruple time were 
very rare. Anything other than a 2, 4, 8, or (rarely 16) as the denominator was also very rare.

# Baroque, Classical, and Romantic examples of interesting time signatures

## Bach WTC I: Prelude XV in G Major

RH is in 24/16; LH is in common time

## Telemann: Gulliver Suite

https://benjaminpesetsky.com/georg-philipp-telemann-suite-for-two-violins-gullivers-travels/
https://vmirror.imslp.org/files/imglnks/usimg/e/e2/IMSLP963613-PMLP203419-Intrada-Suite_Gullivers_Travels_for_2_Violins,_TWV40_108_-_Facsimilie.pdf

## Minuet from Don Giovanni

[Liszt's whack at this.](https://youtu.be/ZVevtrU8Oik?si=DwjXZ7VB6XOu2GgR&t=821)

An oft-cited example of pre-20th Century polymetric music is the minuet from *Don
Giovanni*. A good, quick explanation can be found [here](https://en.euterpe.blog.br/analysis-of-work/minuet-from-don-giovanni).

[Wiki article on that ^^ piece](https://en.wikipedia.org/wiki/Fantasy_on_Themes_from_Mozart%27s_Figaro_and_Don_Giovanni)

 - The minuet starts at m.406 of the finale of Act I.
 - It ends at m.468 -- abruptly. (The story/dramatic element here is that Don
 Giovanni is trying to force himself on Zerlina in a side-room while hosting a ball
 for all his rich friends at his mansion. The connection between what is happening on
  stage and the music, here, is that the forms get truncated in all sorts of messy
  ways. Mozart was a master of connecting drama and music in his operas like this).
      - The final B section of the minuet is cut short by 2 measures
      - The final B section of the contradanse is truncated by about 4 measures, and
      its final measure is a half measure -- exceedingly rare for something not
      related to anacrusis/a pickup measure. Though the contradanse starts with a
      pickup, the half-measure in question is not the appropriate complement measure.
      - The German dance is basically just interrupted altogether before it has even
      had a chance to repeat itself, resulting in ambiguity to its overall form.

The minuet -- 3/4:
 - m.406-468 (not including 468)
 - A-A-B-B-A-B-A-B, each 8 measures long
 - 8-8-8-8-8-8-8-6 (actually)
 - Should be 64 full measures
 - But the last B is cut short, resulting in 62 full measures
 - Perfectly symmetric/balanced form; stately and elegant
 - What aristocrats would have danced to as a social custom


The contradanse -- 2/4:
SPECS
 - m.430: tuning
 - m.438: written in 3/4, but can be considered a pickup in 2/4 (two 8th's preceded by
 rests)
 - m.439: first full measure
 - Cut off just after the middle of the B section
 - Pickup (1 beat) + 43 full measures + a half measure (1 beat)
 - Stops at its own m.472,
FEEL
 - On its on, this is very much 2/4. In relation to the minuet, however, it is a 4
 against 3 situation, the 4 coming from 2/4 --> 4/8. Which is very weird sounding.
 Another way to put it:
     3 (2+2+2)   vs.   2 (2+2)
     4   (8)           4  (8)
 Very difficult, even for well-trained musicians. Chopin did crap like this all the
 time.
CONTEXT
 - Looser in form; asymmetric/unbalanced, but somewhat organized
 - Think appalachian violin or scottish dance in 2/4, or something that Merry and
 Pippin would dance to
 - From England/Scotland "country dance", translated through sound to the French
 "contredanse"
 - Held in fields and barns with big groups of people where class lines were often
 crossed (rich mixed with poor -- like Giovanni, a noble, and Zerlina, a peasant)


The German dance -- 3/8:
SPECS
  - m.446: tuning
  - m.454: entrance
  - 42 full measures
  - 454+42 ≠ 468
  - It's actually at its own measure, m.496
  - Cut off before even finishing the "B section"
FEEL
  - Thankfully, 3/8 is easily layered onto 3/4 and vice-versa. It's just the
  3-to-a-quarter triplet feel. In relation to the minuet, it's basically a 9/8. On
  its own, it can be argued the German dance is still in 3/8.
CONTEXT
  - Sort of all of the place in terms of form, with a shrieking siren sort of thing
  indicating "back to the beginning" or "starting a new section". Just kinda wild.
  - A peasant dance that "drinkers in an inn" might dance. Meant mostly for comic
  relief and danced by the comic actors (Leporello).

So we can determine that they all end up at different measures (counts). The
quarters/eighths of the minuet have the same timing/subdivisions/tempo, and the German
dance is simply tuplet-ized (i.e. the minuet in triplets).


# Further Reading

+ [Wikipedia: List of musical works in unusual time signatures](https://en.wikipedia.org/wiki/List_of_musical_works_in_unusual_time_signatures)
    + You will notice that the vast, vast majority of examples here are works from the 
      20th-Century and beyond. Stravinsky and Boulez are frequent offenders.
+ [A very talented drummer.](https://www.reddit.com/r/oddlysatisfying/comments/1lhul8e/346812_polyrhythm_on_drums/)
+ [More head pain.](https://www.youtube.com/watch?v=KuO0x_9c3xU)

# OTHER NOTES

        /*
         (Most examples are taken from the [Wikipedia article](https://en.wikipedia
         .org/wiki/Time_signature) on Time Signatures, but re-written).

         MusicXML spec allows for a lot more in terms of complicated time signature stuff than
         MuseScore is capable of (currently). Pricey but more high-caliber notation software like
         Dorico and Sibelius are (anecdotally -- didn't look too deeply into this as I don't have
         these programs).

         This includes, but is not limited to:
             - Dual time signatures
             - Irrational meters (e.g. 4/3)
             - Some polymetric stuff (when it comes to measure numbers vs. tempo/subdivision values)

         In MusicXML, the [interchangeable element](https://www.w3
         .org/2021/06/musicxml40/musicxml-reference/examples/interchangeable-element/) handles
         dual time signatures. Dual time signatures have two flavors, both are essentially
         non-functional:
             - One is to indicate grouping/metric pulse differences. These two time signatures
             will always be rationally related, e.g.: {3 6}
                                                      {4 8}
             - One is simply shorthand to avoid writing time signatures over and over again when
             the alternation pattern of the time signatures is consistent (which ends up creating
              a sort of "meta-" meter. For instance, {6 9}
                                                     {8 8}
              in Tchaikovsky String Quarter No. 2 (Allegro giusto)
         Some composers alternate time signatures frequently anyway, or when there is no
         consistent pattern, like in the Sacrificial Dance from Rite of Spring.

         Reductor would have no problem with that last example, because those are explicitly
         noted time signatures (per measure). The first example would also not be a problem
         per-se, except round-trip conversion might not preserve perfectly that convention (as it
         is more courtesy than functional). MuseScore doesn't support it, anyway, so I wouldn't
         be able to create scores with this and test it in reductor. As for the second example,
         I assume that even in scores produced by Dorico or Sibelius, there is some explicit
         time signature attribute within the measure, even though the rendered time signatures
         are in the dual-form at the beginning of the portion of music in question, and
         invisible (unrendered) in following measures.

         In terms of polymeters, MuseScore can:
             - Notate this, although playback favors keeping measure numbers in sync, rather
             than applying subdivisions correctly across all staves/parts. It manipulates
             tempo to accomplish this. I may be unaware that it is fixable, but for now that
             is my understanding.
             - This is accomplished with command+clicking on time signatures for a selected
             measure.
             - The above issue doesn't apply to rationally related meters, naturally. For instance:
                 - A 18/16 in the RH and a 3/4 in the LH, like in Goldberg Variations (no. 26),
                 are functionally "canonically" equivalent, but indicate different metrical
                 rhythms, pulses, or feels to the performer.

         Things can get messy though (I am referring to the aforementioned tempo manipulation and
          measure number syncing) for non-rationally related meters.
        */

        /*
         From what I can tell, MuseScore does not have powerful features in terms of
         "non-standard" time signature stuff, including but not limited to:

             Dual time signatures:
                3 (9)    <-- imagine the parentheses merged into one giant parenthesis
                4 (8)    <-- " "

         The symbol ("=", "+") is the child <time-relation> element.
         <interchangeable> is not exclusive to meter representations (e.g. chord diagrams).

         Mixed-meter:

            2  3
            4  4

         It would appear MusicXML itself supports all this. And from what I can tell,
         enterprise software like Dorico can do it.

         There are all sorts of workarounds some of the regular moderators suggest on forums,
         like using the glyph or symbol element for the second time signature.

         However, you __can__ do:

             2+3+2
               8

        Of course, there is different meters between hands in piano scores:

        TREBLE 18
               16

        BASS   3
               4

        like in variation 26 from Goldberg. But, as this is NON-piano to piano, I don't think
        this needs to be a consideration. There might be the rare case where it is a piano
        concerto or trio, etc., where the piano has this setup. But I think almost certainly
        this would need a human in the loop to decide what to do, and probably so rare that it
        isn't worth dealing with in an algorithmic reduction program.

        In MuseScore, this is done by: command+click the time signature in the palette, and drag
        to the staff in question.

        In MusicXML, this is supposedly controlled with the "staff-number" attribute of <time>,
        but I need to look more into that.

        Just FYI: The people at MuseScore are extremely competent. But even they are not
        theory-purists or pedants. "Use a workaround", or even, "the musical convention itself is
         stupid and shouldn't be used" <-- they say stuff like this all the time. Sometimes the
         answer is simply "MuseScore does not directly support X."

         IRL, there is no way to have different meters in different parts if the different meters
         are not __rationally__ related, __WITHOUT__ changing tempo or subdivision values.

         Example: 2/4 in RH, 7/8 in LH. 2/4 ==> 4/8. 4/8 over 7/8 is impossible for both to share
          common subdividing/tempo. This is distinct from tuplets (e.g. 11 eights over quarters,
          like in Chopin.)

          According to ChatGPT, mid-measure time signature changes were used by Carter and
          Boulez, though we don't care about them.

          Polymeter: distinct parts have different time signatures at the same time
          Polyrhythm: different rhythm groupings at the same time (e.g. 3 against 2)

        Trois Gnossiennes: no bars or time signatures

        */

        /*

        senza misura:
        0:

        simple meter (WYSIWYG; numerator informs tempo):
        2: /2, /4, /8 (duple)
        3: /2, /4, /8 (triple)
        4: /2, /4, /8 (quadruple)

        Aside: tempo giusto: practice of using ratios to indicate tempo (cut vs. common time)

        compound meters (Groupings (of 3) inform tempo, i.e., strong vs. weak beats):
        6: /4, /8 (duple)
        9: /2, /4, /8, /16 (triple)
        12: /4, /8, /16 (quadruple)

        complex meters:
        5: /4, /8
        7: /8

        mixed meter:
        5/4, 6/4, 5/4, 6/4 (pictures at an exhibition)

        6 9    <-- tchaikovsky
        8 8

        additive meters:
        2+3+2 / 8

        irrational meters:
        4/3

        */

        /*
         Measures can have multiple time signatures. The MusicXML element
         corresponding to meter information is in the <time> element.

         `timeSignature` is a field of the Time object in proxymusic, though
         it is NOT a spec element in MusicXML.

         timeSignature is of type List<JAXBElement<java.lang.String>>.

         The element at index 0 is assumed to be <beats> string value, and the element
         at index 1 is assumed to be <beat-type> string value. So it __is__ order-dependent.
        */
