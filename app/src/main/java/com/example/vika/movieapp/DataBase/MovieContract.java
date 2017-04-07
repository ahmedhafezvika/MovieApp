package com.example.vika.movieapp.DataBase;

import android.provider.BaseColumns;

/**
 * Created by Eng.Ahmed on 10/20/2016.
 */

public class MovieContract {

    public static final class MovieEntry implements BaseColumns {

        public static final String TABLE_NAME = "movies";

        public static final String COLUMN_ID = "movie_id";

        public static final String COLUMN_NAME = "name";

        public static final String COLUMN_RELEASE_DATE = "release_date";

        public static final String COLUMN_RATE = "rate";

        public static final String COLUMN_POSTER_PATH = "poster_path";

        public static final String COLUMN_OVERVIEW = "overview";
    }

    public static final class TrailerEntry implements BaseColumns {

        public static final String TABLE_NAME = "trailers";

        public static final String COLUMN_MOVIE_KEY = "movie_id";

        public static final String COLUMN_NAME = "name";

        public static final String COLUMN_SITE = "site";

        public static final String COLUMN_PATH = "path";
    }

    public static final class ReviewEntry implements BaseColumns {

        public static final String TABLE_NAME = "reviews";

        public static final String COLUMN_MOVIE_KEY = "movie_id";

        public static final String COLUMN_AUTHOR = "author";

        public static final String COLUMN_CONTENT = "content";
    }
}
