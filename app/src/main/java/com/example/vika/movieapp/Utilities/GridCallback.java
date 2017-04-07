package com.example.vika.movieapp.Utilities;

import com.example.vika.movieapp.Objects.Movie;

import java.util.List;

/**
 * Created by Eng.Ahmed on 11/21/2016.
 */

public interface GridCallback {

    void handleFavourites(List<Movie> movieList);

    void handleMovies(List<Movie> movies, String localType);
}
