1. Convert project to Maven project
2. Add Logging fw
3. Implement EventSorter using chaining hash table or something like that?
4. make paths not absolute paths but still make openWithGarageBand() and write() happy
5. Piece.scaleTempo --> some kind of check to make sure scale doesn't push things out of range
6. The end plan is to get rid of some of the Event subclasses AND Piece lists that are not needed, and NOT
   throw exceptions in the sorting process when unknown event types are encountered. We will just ignore them