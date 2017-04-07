package com.example.vika.movieapp.Objects;

/**
 * Created by Eng.Ahmed on 10/18/2016.
 */

public class Movie {

    private int id;
    private String posterPath;
    private String overview;
    private String title;
    private String release_date;
    private float vote_average;

    public Movie(int id, String posterPath, String overview, String title, String release_date, float vote_average) {
        this.id = id;
        this.posterPath = posterPath;
        this.overview = overview;
        this.title = title;
        this.release_date = release_date;
        this.vote_average = vote_average;
    }

    public int getId() {
        return id;
    }

    public String getPosterPath() {
        return posterPath;
    }

    public String getOverview() {
        return overview;
    }

    public String getTitle() {
        return title;
    }

    public String getRelease_date() {
        return release_date;
    }

    public float getVote_average() {
        return vote_average;
    }
}
