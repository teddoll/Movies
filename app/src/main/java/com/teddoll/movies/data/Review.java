package com.teddoll.movies.data;


public class Review {
    /*
        id: "55a58e46c3a3682bb2000065"
        author: "Andres Gomez"
        content: "The minions are a nice idea and the animation and London recreation is really good, but that's about it. The script is boring and the jokes not really funny."
        url: "http://j.mp/1HJpF2h"
     */

    public final String id;

    public final String author;

    public final String content;

    public final String url;

    public Review(String id, String author, String content, String url) {
        this.id = id;
        this.author = author;
        this.content = content;
        this.url = url;
    }
}
