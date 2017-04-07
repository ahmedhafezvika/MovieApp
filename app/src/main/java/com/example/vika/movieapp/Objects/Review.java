package com.example.vika.movieapp.Objects;

/**
 * Created by Eng.Ahmed on 10/19/2016.
 */

public class Review {

    private String author;
    private String content;

    public Review(String author, String content) {
        this.author = author;
        this.content = content;
    }

    public String getAuthor() {
        return author;
    }

    public String getContent() {
        return content;
    }
}
