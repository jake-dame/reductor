1. Toronto: The "span" rule (minimal spanning is where we store an interval); key (separator) of elementary intervals; same Interval stored no more than 2 times on each level of tree
   1. Each node contains up to 2 intervals
   2. Seems more like typical and efficient binary search recursion
2. CMU: "Two-Step" search Each node stores one's it intersects, and then splits fully left/right subtrees
   1. Can eliminate left/right subtree, but always have to search current node which stores:
      1. Endpoints, but split into two lists
   2. 2n endpoints are sorted at beginning and those determine the medians of trees/subtrees (for each internal node)
   3. Median stored with every node
3. G4G:
   1. Explicit interval + MAX of subtree (furthest right descedenant's b)
   2. nlgn IF self-balancing only

**TORONTO**:
Separate the number line into elementary intervals (intervals created by each and every endpoint of each and every interval)
+ Leaf nodes span 1 elementary interval each
+ Internal nodes span the union of its descendants

This leads to 2n + 1 elementary intervals *assuming* none share endpoints.

Every interval can be expressed as an aggregate of the sub-intervals that it spans.

Each internal node stores a key that separates the elementary intervals it spans

An interval [a,b] is stored in a node x if and only if

1)
span(x) is completely contained within [a,b] and
2)
span(parent(x)) is not completely contained in [a,b].

So basically intervals are stored with the node when they are minimally overlapping (their span min is greater than or equal to the interval min AND less then or equal to the interval max) AND that condition is not also true for their parent

Each interval can be stored in many nodes of the tree. However, the conditions (1) and (2) ensure that any particular interval is stored in at most two nodes on each level of the tree. 2nlgn storage


CMU one says O(n) storage



## Questions

Is it worth it to make a read-only wrapper for a Sequence? How else to expose safely.  "Java is not what I reach for." 
+ You either need to make decisions about what you want to expose or re-design your class structure so that stuff that is needed by this or that class is correct (just move MIDI class into Reductor?)
  1. read-only stuff Or
  2. move into class (re-design)

If I can't have the median element from the outset, I need the tree to be self-balancing. That won't really be a problem in this case though? The other classes kind of make sure that I will always have a full set prior to tree construction...
+ "Don't anticipate that you are appending a bunch to the end each time, and that insertions and deletions will be more-or-less pretty net zero"

+ use quickselect or median function to get median from list in java
-------------------------------------------------------------------------------------------------------
Is it generally common to omit visibility specifiers during early- to mid-development, and have everything just be package-private, EXCEPT for internal helpers and stuff you know will ALWAYS need to be private (which you then mark private)? I find myself overdoing it in terms of conservative encapsulation and marking tons of stuff as private, but it ends up making testing and refactoring more difficult, even if those things should be private in the end anyway. Or is this a habit/mentality that I shouldn't be fighting

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

Balance between making small amounts of data into small classes vs. not