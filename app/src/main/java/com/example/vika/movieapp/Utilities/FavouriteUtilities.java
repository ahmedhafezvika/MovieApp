package com.example.vika.movieapp.Utilities;

import com.example.vika.movieapp.Objects.Movie;

import java.util.List;

/**
 * Created by Eng.Ahmed on 10/24/2016.
 */

public  class FavouriteUtilities {

    private static List<String> favourites;
    private static Movie removed;

    public static void setFavourites(List<String> favourites) {
        FavouriteUtilities.favourites = favourites;
    }

    public static boolean foundInFavourites(String name) {
        return favourites.contains(name);
    }

    public static void addToFavourite(String name) {
        favourites.add(name);
    }

    public static void removeFromFavourite(String name) {
        favourites.remove(name);
    }

    public static int getFavouriteSize () {
        return favourites.size();
    }

    public static boolean isNull () {
        if (favourites == null) {
            return true;
        }
        return false;
    }

    public static Movie getRemoved() {
        return removed;
    }

    public static void setRemoved(Movie removed) {
        FavouriteUtilities.removed = removed;
    }
}