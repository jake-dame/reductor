package reductor.musicxml.exporter;


public class PartBuilder {

    public static PartBuilderBuilder builder() {
        return new PartBuilderBuilder();
    }

    /* This will just be <score-part> for now -- no <group-part> >*/
    public static class PartBuilderBuilder {

    /*
    <part-group> (Zero or more times)
    <score-part> (Required)
    Zero or more of the following
        <part-group>
        <score-part>
    */
    /* The above just means that you always need to have at least one scorepart, and that
    if you __do__ have any <part-group>'s, they always need to precede any
    <score-part>'s that they reference. */

        PartBuilderBuilder() { }

    }

}
