package com.example.vika.movieapp.Utilities;

/**
 * Created by Eng.Ahmed on 11/21/2016.
 */

public class Constants {

    private static final String BASE_IMAGE_URL = "http://image.tmdb.org/t/p/w185";
    private static final String BASE_MOVIE_URL = "https://api.themoviedb.org/3/movie/";
    private static final String DB_ADDING_MESSAGE = "Movie added to favourites";
    private static final String DB_REMOVING_MESSAGE = "Movie removed from favourites";
    private static final String API_KEY = "d31e8cfb701c0bdcafb53d7cca7596a0";
    private static final String NO_FAVOURITE_MESSAGE = "There is no favourite movies";
    private static final String OFFLINE_MESSAGE = "you are offline please check your network connection " +
            "or you can only show your favourites";
    private static final String OFFLINE_DETAIL_MESSAGE = "you are offline please check your network connection";

    public static String getApiKey () {
        return API_KEY;
    }

   public static String getBaseMovieUrl () {
       return BASE_MOVIE_URL;
   }

    public static String getBaseImageUrl () {
        return BASE_IMAGE_URL;
    }

    public static String getDbAddingMessage () {
        return DB_ADDING_MESSAGE;
    }

    public static String getDbRemovingMessage () {
        return DB_REMOVING_MESSAGE;
    }

    public static String getNoFavouriteMessage() {
        return NO_FAVOURITE_MESSAGE;
    }

    public static String getOfflineMessage() {
        return OFFLINE_MESSAGE;
    }

    public static String getOfflineDetailMessage() {
        return OFFLINE_DETAIL_MESSAGE;
    }
}
