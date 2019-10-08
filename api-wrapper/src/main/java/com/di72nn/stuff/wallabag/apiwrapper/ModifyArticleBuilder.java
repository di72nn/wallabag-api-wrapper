package com.di72nn.stuff.wallabag.apiwrapper;

import com.di72nn.stuff.wallabag.apiwrapper.exceptions.UnsuccessfulResponseException;
import com.di72nn.stuff.wallabag.apiwrapper.models.Article;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import retrofit2.Call;

import java.io.IOException;

import static com.di72nn.stuff.wallabag.apiwrapper.Utils.nonNegativeNumber;

public class ModifyArticleBuilder extends AbstractArticleBuilder<ModifyArticleBuilder> {

	final WallabagService wallabagService;

	final int id;

	ModifyArticleBuilder(WallabagService wallabagService, int id) {
		this.wallabagService = wallabagService;
		this.id = nonNegativeNumber(id, "id");
	}

	@Override
	ModifyArticleBuilder self() {
		return this;
	}

	RequestBody build() {
		FormBody formBody = populateFormBodyBuilder(new FormBody.Builder()).build();

		if(formBody.size() == 0) {
			throw new IllegalStateException("No changes done");
		}

		return formBody;
	}

	public Call<Article> buildCall() {
		return wallabagService.modifyArticleCall(id, build());
	}

	public Article execute() throws IOException, UnsuccessfulResponseException {
		return wallabagService.modifyArticle(id, build());
	}

}
