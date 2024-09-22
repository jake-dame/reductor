## Questions

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