package com.di72nn.stuff.wallabag;

import com.di72nn.stuff.wallabag.apiwrapper.BasicParameterHandler;
import com.di72nn.stuff.wallabag.apiwrapper.WallabagService;
import com.di72nn.stuff.wallabag.apiwrapper.models.Article;
import com.di72nn.stuff.wallabag.apiwrapper.models.Entries;
import com.di72nn.stuff.wallabag.apiwrapper.models.Tag;

import java.io.IOException;
import java.util.List;

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

			Article article = service.addEntryBuilder("http://doc.wallabag.org/en/master/developer/api.html")
					.starred(true)
					.tag("new_test_tag1").tag("new_test_tag2").tag("test_rm")
					.title("Custom title test")
					.execute();

			article = service.modifyEntryBuilder(article.id)
					.title("Modified title for API documentation article").execute();
			System.out.println("Modified article title: " + article.title);

			System.out.println("Exists: " + service.entryExists("http://doc.wallabag.org/en/master/developer/api.html"));

			System.out.println("Article title: " + service.getEntry(article.id).title);

			List<Tag> tags = service.getTags();
			int idForRemoval = -1;
			for(Tag tag: tags) {
				System.out.println("Tag id: " + tag.id);
				System.out.println("Tag label: " + tag.label);
				if("test_rm".equals(tag.label)) idForRemoval = tag.id;
			}

			if(idForRemoval >= 0) System.out.println("Deleted tag label: " + service.deleteTag(idForRemoval).label);

			System.out.println("Deleted article title: " + service.deleteEntry(article.id).title);

			Entries entries = service.getEntriesBuilder().perPage(3).execute();
			System.out.println("Items length: " + entries.embedded.items.size());

			for(Article a: entries.embedded.items) {
				System.out.println("ID: " + a.id);
				System.out.println("Title: " + a.title);
				System.out.println("Is archived: " + a.archived);
				System.out.println("Created at: " + a.createdAt);
			}
		} catch(IOException e) {
			e.printStackTrace();
		}
	}

}
