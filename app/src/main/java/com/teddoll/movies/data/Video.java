package com.teddoll.movies.data;


import com.google.gson.annotations.SerializedName;

public class Video {
/*
    id: "54acb1909251415646005224"
    iso_639_1: "en"
    key: "xInh3VhAWs8"
    name: "First look"
    site: "YouTube"
    size: 1080
    type: "Featurette"
 */

    public final String id;

    @SerializedName("iso_639_1")
    public final String language;

    public final String key;

    public final String name;

    public final String site;

    public final int size;

    public final String type;


    public Video(String id, String language, String key, String name, String site, int size, String type) {
        this.id = id;
        this.language = language;
        this.key = key;
        this.name = name;
        this.site = site;
        this.size = size;
        this.type = type;
    }
}
