package com.di72nn.stuff.wallabag.apiwrapper;

import com.di72nn.stuff.wallabag.apiwrapper.exceptions.UnsuccessfulResponseException;
import com.di72nn.stuff.wallabag.apiwrapper.models.Article;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import retrofit2.Call;

import java.io.IOException;

import static com.di72nn.stuff.wallabag.apiwrapper.Utils.nonEmptyString;

public class AddArticleBuilder extends AbstractArticleBuilder<AddArticleBuilder> {

	final WallabagService wallabagService;

	final String url;

	AddArticleBuilder(WallabagService wallabagService, String url) {
		this.wallabagService = wallabagService;
		this.url = nonEmptyString(url, "url");
	}

	@Override
	AddArticleBuilder self() {
		return this;
	}

	RequestBody build() {
		FormBody.Builder bodyBuilder = new FormBody.Builder()
				.add("url", url);

		return populateFormBodyBuilder(bodyBuilder).build();
	}

	public Call<Article> buildCall() {
		return wallabagService.addArticleCall(build());
	}

	public Article execute() throws IOException, UnsuccessfulResponseException {
		return wallabagService.addArticle(build());
	}

}
