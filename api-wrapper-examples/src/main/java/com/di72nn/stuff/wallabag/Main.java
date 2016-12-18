package com.di72nn.stuff.wallabag;

import com.di72nn.stuff.wallabag.apiwrapper.BasicParameterHandler;
import com.di72nn.stuff.wallabag.apiwrapper.WallabagService;
import com.di72nn.stuff.wallabag.apiwrapper.exceptions.UnsuccessfulResponseException;
import com.di72nn.stuff.wallabag.apiwrapper.models.Article;
import com.di72nn.stuff.wallabag.apiwrapper.models.Articles;
import com.di72nn.stuff.wallabag.apiwrapper.models.Tag;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Main {

	public static void main(String[] args) {
		String baseUrl = "";

		String username = "";
		String password = "";

		String clientID = "";
		String clientSecret = "";

		String refreshToken = "";
		String accessToken = "";

		WallabagService service = new WallabagService(baseUrl, new BasicParameterHandler(
				username, password, clientID, clientSecret, refreshToken, accessToken));

		try {
			System.out.println("Server version: " + service.getVersion());

			Article article = service.addArticleBuilder("http://doc.wallabag.org/en/master/developer/api.html")
					.starred(true)
					.tag("new_test_tag1").tag("new_test_tag2").tag("test_rm")
					.title("Custom title test")
					.execute();

			article = service.modifyArticleBuilder(article.id)
					.title("Modified title for API documentation article").execute();
			System.out.println("Modified article title: " + article.title);

			System.out.println("Exists: " + service.articleExists("http://doc.wallabag.org/en/master/developer/api.html"));

			List<String> urls = new ArrayList<String>();
			urls.add("http://doc.wallabag.org/en/master/developer/api.html");
			urls.add("http://google.com");
			for(Map.Entry<String, Boolean> entry: service.articlesExist(urls).entrySet()) {
				System.out.println("URL: " + entry.getKey() + ", exists: " + entry.getValue());
			}

			System.out.println("Article title: " + service.getArticle(article.id).title);

			List<String> additionalTags = new ArrayList<>();
			additionalTags.add("additional_tag1");
			additionalTags.add("additional_tag2");
			additionalTags.add("additional_tag3");
			article = service.addTags(article.id, additionalTags);

			int tagIdForRemoval = -1;
			for(Tag tag: article.tags) {
				if("additional_tag2".equals(tag.label)) {
					tagIdForRemoval = tag.id;
					break;
				}
			}

			if(tagIdForRemoval >= 0) System.out.println("Deleted tag from article: "
					+ service.deleteTag(article.id, tagIdForRemoval).title);

			List<Tag> tags = service.getTags();
			int idForRemoval = -1;
			for(Tag tag: tags) {
				System.out.println("Tag id: " + tag.id);
				System.out.println("Tag label: " + tag.label);
				if("test_rm".equals(tag.label)) idForRemoval = tag.id;
			}

			if(idForRemoval >= 0) System.out.println("Deleted tag label: " + service.deleteTag(idForRemoval).label);

//			System.out.println("Reloaded article is null: " + (service.reloadArticle(article.id) == null));

			System.out.println("Deleted article title: " + service.deleteArticle(article.id).title);

			Articles articles = service.getArticlesBuilder().perPage(3).execute();
			System.out.println("Items length: " + articles.embedded.items.size());

			for(Article a: articles.embedded.items) {
				System.out.println("ID: " + a.id);
				System.out.println("Title: " + a.title);
				System.out.println("Is archived: " + a.archived);
				System.out.println("Created at: " + a.createdAt);
			}
		} catch(IOException e) {
			e.printStackTrace();
		} catch(UnsuccessfulResponseException e) {
			System.out.println("Response code: " + e.getResponseCode() + ", body: " + e.getResponseBody());
			e.printStackTrace();
		}
	}

}
