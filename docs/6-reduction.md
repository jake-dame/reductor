# Reduction

After the domain classes, I/O, and MIDI/MusicXML processing stuff is all done, reduction (the purpose of the program), will be started.

The general plan is to make reduction algorithms fit into the architecture using functional composition: injecting reduction strategies/behaviors into the pre-existing data model. This willd be done for two reasons:

1. There will probably need to be multiple passes/reductions of the same measure, phrase, or piece, depending on the context. If a certain section is really thick with textural reduction targets, but not so much on intervallic/harmonic reduction (and vice versa), different algorithms can be applied. Some algorithms will do hardly anything to a certain passage, while some might even be counter-productive.
2. I am not even that qualified to make a reduction (using pen and paper). Only a trained/experienced composer (especially one that writes orchestral music but works from the piano) would be able to construct good reduction techniques. reductor is more about making a framework that might allow for someone (more qualified) to come in and specify a better approach, and have it easy for me or another developer to simply "plug" that algorithm in to the architecture.
