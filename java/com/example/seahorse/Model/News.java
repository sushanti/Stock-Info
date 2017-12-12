package com.example.seahorse.Model;

/**
 * Created by seahorse on 11/23/2017.
 */

public class News {
    private String title;
    private  String author;
    private String publishDate;
    private String articleLink;

    public News(String title, String author, String date, String articleLink) {
        this.title = title;
        this.author = author;
        this.publishDate = date;
        this.articleLink = articleLink;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getPublishDate() {
        return publishDate;
    }

    public void setPublishDate(String publishDate) {
        this.publishDate = publishDate;
    }

    public String getArticleLink() {
        return articleLink;
    }

    public void setArticleLink(String articleLink) {
        this.articleLink = articleLink;
    }
}
