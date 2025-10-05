/**
 * This package utilizes the ProxyMusic library to convert `{@link reductor.core}` objects to MusicXML elements.
 * <p>
 * Design decision: no matter how small, every component mapped to a single XML element should have its own building
 * function so that it can be tested in isolation. That way, any changes to the domain classes can rely on just
 * running tests to make sure none of the domain API's have changed.
 * <p>
 * "... for builder-style code that transforms domain data into an external representation, testability is usually the
 * most valuable design axis."
 *
 * @see <a href="https://github.com/Audiveris/proxymusic/blob/master/src/test/java/org/audiveris/proxymusic/util/HelloWorldTest.java">ProxyMusic GitHub</a>
 */

package reductor.core.musicxml;
