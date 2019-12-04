package wallabag;

import wallabag.apiwrapper.*;
import wallabag.apiwrapper.exceptions.UnsuccessfulResponseException;
import wallabag.apiwrapper.models.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

public class Main {

    private static final Logger LOG = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        String baseUrl = "https://wallabag.example.com"; // point to your wallabag instance

        String username = "";
        String password = "";

        String clientID = "";
        String clientSecret = "";

        String refreshToken = "";
        String accessToken = ""; // provide at least the access token

        WallabagService service = WallabagService.instance(baseUrl, new BasicParameterHandler(
                username, password, clientID, clientSecret, refreshToken, accessToken) {
            @Override
            public boolean tokensUpdated(TokenResponse token) {
                LOG.info("Got token: " + token);

                return super.tokensUpdated(token);
            }
        });

        try {
            String serverVersion = service.getVersion();
            System.out.println("Server version: " + serverVersion);

            String testUrl = "http://doc.wallabag.org/en/master/developer/api.html";

            Article article = service.addArticleBuilder(testUrl)
                    .starred(true)
                    .tag("new_test_tag1").tag("new_test_tag2").tag("test_rm")
                    .title("Custom title test")
                    .execute();

            System.out.println("Reloaded article is not null: " + (service.reloadArticle(article.id) != null));

            System.out.println("Exported as text: " + service.exportArticle(
                    article.id, WallabagService.ResponseFormat.TXT).string());

            article = service.modifyArticleBuilder(article.id)
                    .title("Modified title for API documentation article").execute();
            System.out.println("Modified article title: " + article.title);

            System.out.println("Exists: " + service.articleExists(testUrl));
            if (CompatibilityHelper.isArticleExistsWithIdSupported(serverVersion)) {
                System.out.println("Exists id: " + service.articleExistsWithId(testUrl));
            }

            List<String> urls = new ArrayList<>();
            urls.add(testUrl);
            urls.add("http://google.com");
            for (Map.Entry<String, Boolean> entry : service.articlesExistByUrls(urls).entrySet()) {
                System.out.println("URL: " + entry.getKey() + ", exists: " + entry.getValue());
            }
            if (CompatibilityHelper.isArticleExistsWithIdSupported(serverVersion)) {
                for (Map.Entry<String, Integer> entry : service.articlesExistByUrlsWithId(urls).entrySet()) {
                    System.out.println("URL: " + entry.getKey() + ", id: " + entry.getValue());
                }
            }

            BatchExistQueryBuilder existQueryBuilder = service.getArticlesExistQueryBuilder();
            existQueryBuilder.addUrl(testUrl);
            existQueryBuilder.addUrl("http://google.com");
            for (Map.Entry<String, Boolean> entry : existQueryBuilder.execute().entrySet()) {
                System.out.println("URL: " + entry.getKey() + ", exists: " + entry.getValue());
            }

            System.out.println("Article title: " + service.getArticle(article.id).title);

            List<String> additionalTags = new ArrayList<>();
            additionalTags.add("additional_tag1");
            additionalTags.add("additional_tag2");
            additionalTags.add("additional_tag3");
            article = service.addTags(article.id, additionalTags);

            int tagIdForRemoval = -1;
            for (Tag tag : article.tags) {
                if ("additional_tag2".equals(tag.label)) {
                    tagIdForRemoval = tag.id;
                    break;
                }
            }

            if (tagIdForRemoval >= 0) System.out.println("Deleted tag from article: "
                    + service.deleteTag(article.id, tagIdForRemoval).title);

            List<Tag> tags = service.getTags();
            int idForRemoval = -1;
            for (Tag tag : tags) {
                System.out.println("Tag id: " + tag.id);
                System.out.println("Tag label: " + tag.label);
                if ("test_rm".equals(tag.label)) idForRemoval = tag.id;
            }

            if (idForRemoval >= 0) System.out.println("Deleted tag label: " + service.deleteTag(idForRemoval).label);

            Annotation.Range range = new Annotation.Range();
            range.startOffset = 1;
            range.endOffset = 5;
            range.start = "/ul[1]/li[1]";
            range.end = "/ul[1]/li[1]";
            List<Annotation.Range> ranges = new ArrayList<>(1);
            ranges.add(range);
            Annotation annotation = service.addAnnotation(article.id, ranges, "Test annotation from API", null);

            System.out.println("Updated annotation text: " + service.updateAnnotation(annotation.id, "Test updated annotation from API").text);

            Annotations annotations = service.getAnnotations(article.id);
            for (Annotation an : annotations.rows) {
                System.out.println("Annotation id: " + an.id);
                System.out.println("Annotation text: " + an.text);
                System.out.println("Annotation quote: " + an.quote);
            }

            System.out.println("Deleted annotation text: " + service.deleteAnnotation(annotation.id).text);

            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.YEAR, 2000);
            article = service.modifyArticleBuilder(article.id)
                    .content("Modified content")
                    .language("eo")
                    .previewPicture("https://example.com/pic")
                    .publishedAt(calendar.getTime())
                    .author("author1").author("author2")
                    .originUrl("https://example.com/origin")
                    .isPublic(true)
                    .execute();
            System.out.println(String.format("Modified article content: %s, language: %s, preview picture: %s, " +
                            "publishedAt: %s, authors: %s, originUrl: %s, isPublic: %s, publicUid: %s",
                    article.content, article.language, article.previewPicture, article.publishedAt, article.authors,
                    article.originUrl, article.isPublic, article.publicUid));

            System.out.println("Deleted article title: " + service.deleteArticle(article.id).title);

            article = service.addArticleBuilder("https://example.com/test")
                    .title("Test article")
                    .content("Test content")
                    .language("eo")
                    .previewPicture("https://example.com/pic")
                    .publishedAt(calendar.getTime())
                    .author("author1").author("author2")
                    .originUrl("https://example.com/origin")
                    .execute();
            System.out.println(String.format("Test article title: %s, content: %s, language: %s, preview picture: %s, " +
                            "publishedAt: %s, authors: %s, originUrl: %s",
                    article.title, article.content, article.language, article.previewPicture, article.publishedAt,
                    article.authors, article.originUrl));

            if (CompatibilityHelper.isDeleteArticleWithIdSupported(serverVersion)) {
                System.out.println("Deleted article id: " + service.deleteArticleWithId(article.id));
            } else {
                System.out.println("Deleted article title: " + service.deleteArticle(article.id).title);
            }

            Articles articles = service.getArticlesBuilder().perPage(3).execute();
            System.out.println("Items length: " + articles.embedded.items.size());

            for (Article a : articles.embedded.items) {
                System.out.println("ID: " + a.id);
                System.out.println("Title: " + a.title);
                System.out.println("Is archived: " + a.archived);
                System.out.println("Created at: " + a.createdAt);
            }
        } catch (IOException e) {
            LOG.error("IOException", e);
        } catch (UnsuccessfulResponseException e) {
            LOG.error("UnsuccessfulResponseException", e);
        }
    }

}
