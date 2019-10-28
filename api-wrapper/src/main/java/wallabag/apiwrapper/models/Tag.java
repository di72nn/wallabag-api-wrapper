package wallabag.apiwrapper.models;

/**
 * The {@code Tag} class represents an article tag.
 * <p>Tags exist outside of articles: multiple articles may have the same tag associated with them.
 * <p>Server versions prior to 2.3.0 had case-sensitive tags spelling.
 */
public class Tag {

    /** The ID of the tag. */
    public int id;
    /** The human readable label. */
    public String label;
    /** The internal representation of the label. */
    public String slug;

    @Override
    public String toString() {
        return "Tag{" +
                "id=" + id +
                ", label='" + label + '\'' +
                ", slug='" + slug + '\'' +
                '}';
    }

}
