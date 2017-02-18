package com.di72nn.stuff.wallabag;

import com.di72nn.stuff.wallabag.apiwrapper.BasicParameterHandler;
import com.di72nn.stuff.wallabag.apiwrapper.WallabagService;
import com.di72nn.stuff.wallabag.apiwrapper.exceptions.UnsuccessfulResponseException;
import com.di72nn.stuff.wallabag.apiwrapper.models.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Main {

	private static final Logger LOG = LoggerFactory.getLogger(Main.class);

	public static void main(String[] args) {
		String baseUrl = "";

		String username = "";
		String password = "";

		String clientID = "";
		String clientSecret = "";

		String refreshToken = "";
		String accessToken = "";

		WallabagService service = new WallabagService(baseUrl, new BasicParameterHandler(
				username, password, clientID, clientSecret, refreshToken, accessToken) {
			@Override
			public boolean tokensUpdated(TokenResponse token) {
				LOG.info("Got token: " + token);

				return super.tokensUpdated(token);
			}
		});

		try {
			System.out.println("Server version: " + service.getVersion());

			Article article = service.addArticleBuilder("http://doc.wallabag.org/en/master/developer/api.html")
					.starred(true)
					.tag("new_test_tag1").tag("new_test_tag2").tag("test_rm")
					.title("Custom title test")
					.execute();

			System.out.println("Reloaded article is null: " + (service.reloadArticle(article.id) == null));

			System.out.println("Exported as text:" + service.exportArticle(
					article.id, WallabagService.ResponseFormat.TXT).string());

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

			Annotation.Range range = new Annotation.Range();
			range.startOffset = 0;
			range.endOffset = 100;
			range.start = "/p[1]";
			range.end = "/p[1]";
			List<Annotation.Range> ranges = new ArrayList<>(1);
			ranges.add(range);
			Annotation annotation = service.addAnnotation(article.id, ranges, "Test annotation from API", null);

			System.out.println("Updated annotation text: " + service.updateAnnotation(annotation.id, "Test updated annotation from API").text);

			Annotations annotations = service.getAnnotations(article.id);
			for(Annotation an: annotations.rows) {
				System.out.println("Annotation id: " + an.id);
				System.out.println("Annotation text: " + an.text);
			}

			System.out.println("Deleted annotation text: " + service.deleteAnnotation(annotation.id).text);

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
			LOG.error("IOException", e);
		} catch(UnsuccessfulResponseException e) {
			LOG.error("UnsuccessfulResponseException", e);
		}
	}

}
