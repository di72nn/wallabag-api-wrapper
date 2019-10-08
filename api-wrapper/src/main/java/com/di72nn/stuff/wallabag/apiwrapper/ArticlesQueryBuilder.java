package com.di72nn.stuff.wallabag.apiwrapper;

import com.di72nn.stuff.wallabag.apiwrapper.exceptions.UnsuccessfulResponseException;
import com.di72nn.stuff.wallabag.apiwrapper.models.Articles;
import retrofit2.Call;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.di72nn.stuff.wallabag.apiwrapper.Utils.positiveNumber;

public class ArticlesQueryBuilder extends AbstractTagsBuilder<ArticlesQueryBuilder> {

	public enum SortCriterion {
		CREATED("created"), UPDATED("updated");

		private String value;

		SortCriterion(String value) {
			this.value = value;
		}

		public String apiValue() {
			return value;
		}

	}

	public enum SortOrder {
		ASCENDING("asc"), DESCENDING("desc");

		private String value;

		SortOrder(String value) {
			this.value = value;
		}

		public String apiValue() {
			return value;
		}

	}

	private final WallabagService wallabagService;

	private Boolean archive;
	private Boolean starred;
	private SortCriterion sortCriterion = SortCriterion.CREATED;
	private SortOrder sortOrder = SortOrder.DESCENDING;
	private int page = 1;
	private int perPage = 30;
	private long since = 0;
	private Boolean isPublic;

	ArticlesQueryBuilder(WallabagService wallabagService) {
		this.wallabagService = wallabagService;
	}

	@Override
	ArticlesQueryBuilder self() {
		return this;
	}

	public ArticlesQueryBuilder archive(boolean archive) {
		this.archive = archive;
		return this;
	}

	public ArticlesQueryBuilder starred(boolean starred) {
		this.starred = starred;
		return this;
	}

	public ArticlesQueryBuilder sortCriterion(SortCriterion sortCriterion) {
		this.sortCriterion = sortCriterion;
		return this;
	}

	public ArticlesQueryBuilder sortOrder(SortOrder sortOrder) {
		this.sortOrder = sortOrder;
		return this;
	}

	public ArticlesQueryBuilder page(int page) {
		this.page = positiveNumber(page, "page");
		return this;
	}

	public ArticlesQueryBuilder perPage(int perPage) {
		this.perPage = positiveNumber(perPage, "perPage");
		return this;
	}

	public ArticlesQueryBuilder since(long since) {
		this.since = since;
		return this;
	}

	public ArticlesQueryBuilder setPublic(Boolean isPublic) {
		this.isPublic = isPublic;
		return this;
	}

	private Map<String, String> build() {
		Map<String, String> parameters = new HashMap<>();

		if(archive != null) parameters.put("archive", Utils.booleanToNumberString(archive));
		if(starred != null) parameters.put("starred", Utils.booleanToNumberString(starred));
		parameters.put("sort", sortCriterion.apiValue());
		parameters.put("order", sortOrder.apiValue());
		parameters.put("page", String.valueOf(page));
		parameters.put("perPage", String.valueOf(perPage));
		String tagsString = getTagsString();
		if (tagsString != null) parameters.put("tags", tagsString);
		parameters.put("since", String.valueOf(since));
		if (isPublic != null) parameters.put("public", Utils.booleanToNumberString(isPublic));

		return parameters;
	}

	public Call<Articles> buildCall() {
		return wallabagService.getArticlesCall(build());
	}

	public Articles execute() throws IOException, UnsuccessfulResponseException {
		return wallabagService.getArticles(build());
	}

	public ArticlesPageIterator pageIterator() {
		return pageIterator(true);
	}

	public ArticlesPageIterator pageIterator(boolean notFoundAsEmpty) {
		return new ArticlesPageIterator(copy(), notFoundAsEmpty);
	}

	private ArticlesQueryBuilder copy() {
		ArticlesQueryBuilder copy = new ArticlesQueryBuilder(wallabagService);

		copy.archive = archive;
		copy.starred = starred;
		copy.sortCriterion = sortCriterion;
		copy.sortOrder = sortOrder;
		copy.page = page;
		copy.perPage = perPage;
		copyTags(copy);
		copy.since = since;
		copy.isPublic = isPublic;

		return copy;
	}

}
