package wallabag.apiwrapper;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static wallabag.apiwrapper.Utils.nonEmptyString;
import static wallabag.apiwrapper.Utils.nonNullValue;

abstract class AbstractTagsBuilder<T extends AbstractTagsBuilder<T>> {

    protected Set<String> tags;

    AbstractTagsBuilder() {}

    protected abstract T self();

    /**
     * Adds a tag to this builder, returns the builder.
     * Duplicates are silently ignored.
     *
     * @param tag the tag to add
     * @return this builder
     * @throws NullPointerException     if the {@code tag} is {@code null}
     * @throws IllegalArgumentException if the {@code tag} is an empty {@code String}
     */
    public T tag(String tag) {
        nonEmptyString(tag, "tag");

        Set<String> tags = this.tags;
        if (tags == null) {
            this.tags = tags = new HashSet<>();
        }
        tags.add(tag);

        return self();
    }

    /**
     * Adds tags from the specified {@code Collection} to this builder, returns the builder.
     * Duplicates are silently ignored.
     *
     * @param tags a {@code Collection} with tags to add
     * @return this builder
     * @throws NullPointerException     if the {@code tags} collection is {@code null} or if it contains a {@code null}
     * @throws IllegalArgumentException if the {@code tags} collection contains an empty {@code String}
     */
    public T tags(Collection<String> tags) {
        nonNullValue(tags, "tags");

        for (String tag : tags) {
            tag(tag);
        }

        return self();
    }

    /**
     * Resets the tags that were previously added to this builder, returns the builder.
     *
     * @return this builder
     */
    public T resetTags() {
        if (tags != null) tags.clear();
        return self();
    }

    protected String getTagsString() {
        if (tags != null && !tags.isEmpty()) {
            return Utils.join(tags, ",");
        }
        return null;
    }

    protected void copyTags(T copy) {
        if (tags != null) copy.tags = new HashSet<>(tags);
    }

}
