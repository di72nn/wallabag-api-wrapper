package com.di72nn.stuff.wallabag.apiwrapper;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static com.di72nn.stuff.wallabag.apiwrapper.Utils.nonEmptyCollection;
import static com.di72nn.stuff.wallabag.apiwrapper.Utils.nonEmptyString;

abstract class AbstractTagsBuilder<T extends AbstractTagsBuilder<T>> {

	Set<String> tags;

	AbstractTagsBuilder() {}

	abstract T self();

	public T tag(String tag) {
		nonEmptyString(tag, "tag");

		Set<String> tags = this.tags;
		if(tags == null) {
			this.tags = tags = new HashSet<>();
		}
		tags.add(tag);

		return self();
	}

	public T tags(Collection<String> tags) {
		nonEmptyCollection(tags, "tags");

		Set<String> tagsLocal = this.tags;
		if(tagsLocal == null) {
			this.tags = tagsLocal = new HashSet<>(tags.size());
		}
		tagsLocal.addAll(tags);

		return self();
	}

	String getTagsString() {
		if(tags != null && !tags.isEmpty()) {
			return Utils.join(tags, ",");
		}
		return null;
	}

	void copyTags(T copy) {
		if(tags != null) copy.tags = new HashSet<>(tags);
	}

}
