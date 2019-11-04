# wallabag API wrapper

A simple Java library for accessing the [wallabag API](https://doc.wallabag.org/en/developer/api/readme.html).

Supported wallabag versions are `2.1.3`-`2.3.8`.
Earlier versions should still work with some features lacking.
Newer versions should work, but were not tested against.
Minimal required Java version is 7.


## How to get

With Gradle:

Add the Jitpack repo to your root build.gradle at the end of repositories:
```groovy
    allprojects {
        repositories {
            ...
            maven { url 'https://jitpack.io' }
        }
    }
```
Add the dependency:
```groovy
    dependencies {
        implementation 'com.github.di72nn.wallabag-api-wrapper:api-wrapper:v1.0.0-rc.1'
    }
```

More options [here](https://jitpack.io/#di72nn/wallabag-api-wrapper).


## How to use

Create an API client at `https://your.wallabag.instance/developer`,
use the `Client ID` and `Client secret` of the created client in the following code:
```java
// Get a service instance
WallabagService service = WallabagService.instance("https://wallabag.example.com",
        new BasicParameterHandler("username", "password", "client ID", "client secret"));

// Save an article
Article article = service.addArticle("https://doc.wallabag.org/en/developer/api/readme.html");

// Access article content
System.out.println(article.title + ": " + article.content);

// Mark article as read and add some tags
service.modifyArticleBuilder(article.id)
        .archive(true) // mark as read
        .tag("Programming").tag("OAuth").tag("wallabag") // add tags
        .execute();

// Export article as text (or other format)
System.out.println("Exported as text: " + service.exportArticle(
        article.id, WallabagService.ResponseFormat.TXT).string());

// Load the first 10 favorite articles
List<Article> articles = service.getArticlesBuilder()
        .starred(true) // only favorite
        .sortOrder(ArticlesQueryBuilder.SortOrder.ASCENDING) // starting from the oldest
        .perPage(10) // only the first 10
        .execute()
        .embedded.items;

// or iterate over all articles
for (ArticlesPageIterator pageIterator = service.getArticlesBuilder().pageIterator(); pageIterator.hasNext(); ) {
    Articles articlesPage = pageIterator.next();

    System.out.println("Page " + articlesPage.page + " out of " + articlesPage.pages);
    for (Article a: articlesPage.embedded.items) {
        System.out.println("Title: " + a.title);
        System.out.println("URL: " + a.url);
    }
}
```

[More code examples](api-wrapper-examples/src/main/java/wallabag/Main.java).

[wallabag Android app](https://github.com/wallabag/android-app) uses this library.
You can check out its code for some real-world uses.


## License

[GNU GPLv3](COPYING)
