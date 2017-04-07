package com.example.vika.movieapp.Objects;

/**
 * Created by Eng.Ahmed on 10/19/2016.
 */

public class Trailer {

    private String site;
    private String path;
    private String name;

    public Trailer(String site, String path, String name) {
        this.site = site;
        this.path = path;
        this.name = name;
    }

    public String getType() {
        return site;
    }

    public String getPath() {
        return path;
    }

    public String getName() {
        return name;
    }
}
