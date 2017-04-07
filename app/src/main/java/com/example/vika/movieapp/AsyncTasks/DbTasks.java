package com.example.vika.movieapp.AsyncTasks;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;

import com.example.vika.movieapp.DataBase.MovieContract;
import com.example.vika.movieapp.DataBase.MovieDbHelper;
import com.example.vika.movieapp.Objects.Movie;
import com.example.vika.movieapp.Objects.Review;
import com.example.vika.movieapp.Objects.Trailer;
import com.example.vika.movieapp.Utilities.Constants;
import com.example.vika.movieapp.Utilities.DetailCallback;
import com.example.vika.movieapp.Utilities.FavouriteUtilities;
import com.example.vika.movieapp.Utilities.GridCallback;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Eng.Ahmed on 11/21/2016.
 */

public class DbTasks {

    private Fragment receiver;
    private Activity mReceiver;

    public DbTasks (Fragment receiver) {
        this.receiver = receiver;
    }

    public DbTasks (Activity mReceiver) {
        this.mReceiver = mReceiver;
    }

    public void fetchFavouritesNames () {
        GetFavouritesNamesTask namesTask = new GetFavouritesNamesTask();
        namesTask.execute();
    }

    public void fetchFavourites () {
        FetchFavouritesTask favouritesTask = new FetchFavouritesTask();
        favouritesTask.execute();
    }

    public void saveInDb (Movie movie) {
        SaveInDbTask saveTask = new SaveInDbTask();
        saveTask.execute(movie);
    }

    public void removeFromDb (Movie movie) {
        RemoveFromDbTask removeTask = new RemoveFromDbTask();
        removeTask.execute(movie);
    }

    public void fetchTrailers (String movieID) {
        FetchTrailersDbTask trailersTask = new FetchTrailersDbTask();
        trailersTask.execute(movieID);
    }

    public void fetchReviews (String movieID) {
        FetchReviewsDbTask reviewsTask = new FetchReviewsDbTask();
        reviewsTask.execute(movieID);
    }

    private class GetFavouritesNamesTask extends AsyncTask<Void, Void, List<String>> {
        @Override
        protected List<String> doInBackground(Void... params) {
            MovieDbHelper dbHelper = new MovieDbHelper(mReceiver);
            SQLiteDatabase db = dbHelper.getWritableDatabase();

            String[] columns = {MovieContract.MovieEntry.COLUMN_NAME};

            Cursor movieCursor = db.query(
                    MovieContract.MovieEntry.TABLE_NAME,  // Table to Query
                    columns, // leaving "columns" null just returns all the columns.
                    null, // cols for "where" clause
                    null, // values for "where" clause
                    null, // columns to group by
                    null, // columns to filter by row groups
                    null  // sort order
            );
            movieCursor.moveToFirst();

            List<String> favourites = new ArrayList<>();
            int nameIndex = movieCursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_NAME);
            while (!movieCursor.isAfterLast()) {
                favourites.add(movieCursor.getString(nameIndex));
                movieCursor.moveToNext();
            }
            movieCursor.close();
            db.close();
            dbHelper.close();
            return favourites;
        }

        @Override
        protected void onPostExecute(List<String> favourites) {
            FavouriteUtilities.setFavourites(favourites);
        }
    }

    private class FetchFavouritesTask extends AsyncTask<Void, Void, List<Movie>> {
        @Override
        protected List<Movie> doInBackground(Void... params) {
            MovieDbHelper dbHelper = new MovieDbHelper(receiver.getContext());
            SQLiteDatabase db = dbHelper.getWritableDatabase();

            Cursor movieCursor = db.query(
                    MovieContract.MovieEntry.TABLE_NAME,  // Table to Query
                    null, // leaving "columns" null just returns all the columns.
                    null, // cols for "where" clause
                    null, // values for "where" clause
                    null, // columns to group by
                    null, // columns to filter by row groups
                    null  // sort order
            );

            List<Movie> movieList = new ArrayList<>();

            movieCursor.moveToFirst();
            while (!movieCursor.isAfterLast()) {
                int idIndex = movieCursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_ID);
                int nameIndex = movieCursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_NAME);
                int posterIndex = movieCursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_POSTER_PATH);
                int overviewIndex = movieCursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_OVERVIEW);
                int rateIndex = movieCursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_RATE);
                int dateIndex = movieCursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_RELEASE_DATE);

                int id = movieCursor.getInt(idIndex);
                String name = movieCursor.getString(nameIndex);
                String date = movieCursor.getString(dateIndex);
                String overview = movieCursor.getString(overviewIndex);
                float rate = movieCursor.getFloat(rateIndex);
                String poster = movieCursor.getString(posterIndex);

                Movie movie = new Movie(id, poster, overview, name, date, rate);
                movieList.add(movie);
                movieCursor.moveToNext();
            }

            movieCursor.close();
            db.close();
            dbHelper.close();

            return movieList;
        }

        @Override
        protected void onPostExecute(List<Movie> movieList) {
            if (movieList != null) {
                ((GridCallback)receiver).handleFavourites(movieList);
            }
        }
    }

    private class SaveInDbTask extends AsyncTask<Movie, Void, Void> {
        @Override
        protected Void doInBackground(Movie... params) {
            MovieDbHelper dbHelper = new MovieDbHelper(receiver.getContext());
            SQLiteDatabase db = dbHelper.getWritableDatabase();

            Movie movie = params[0];

            ContentValues movieValues = new ContentValues();
            movieValues.put(MovieContract.MovieEntry.COLUMN_ID, movie.getId());
            movieValues.put(MovieContract.MovieEntry.COLUMN_NAME, movie.getTitle());
            movieValues.put(MovieContract.MovieEntry.COLUMN_OVERVIEW, movie.getOverview());
            movieValues.put(MovieContract.MovieEntry.COLUMN_POSTER_PATH, movie.getPosterPath());
            movieValues.put(MovieContract.MovieEntry.COLUMN_RATE, movie.getVote_average());
            movieValues.put(MovieContract.MovieEntry.COLUMN_RELEASE_DATE, movie.getRelease_date());

            db.insert(MovieContract.MovieEntry.TABLE_NAME, null, movieValues);

            // add trailers
            for (int i = 0; i < ((DetailCallback)receiver).getTrailerCount(); i++) {
                Trailer trailer = ((DetailCallback)receiver).getTrailerByIndex(i);

                ContentValues trailerValues = new ContentValues();
                trailerValues.put(MovieContract.TrailerEntry.COLUMN_MOVIE_KEY, movie.getId());
                trailerValues.put(MovieContract.TrailerEntry.COLUMN_NAME, trailer.getName());
                trailerValues.put(MovieContract.TrailerEntry.COLUMN_PATH, trailer.getPath());
                trailerValues.put(MovieContract.TrailerEntry.COLUMN_SITE, trailer.getType());

                db.insert(MovieContract.TrailerEntry.TABLE_NAME, null, trailerValues);
            }

            // add reviews
            for (int i = 0; i < ((DetailCallback)receiver).getReviewCount(); i++) {
                Review review = ((DetailCallback)receiver).getReviewByIndex(i);

                ContentValues reviewValues = new ContentValues();
                reviewValues.put(MovieContract.ReviewEntry.COLUMN_MOVIE_KEY, movie.getId());
                reviewValues.put(MovieContract.ReviewEntry.COLUMN_AUTHOR, review.getAuthor());
                reviewValues.put(MovieContract.ReviewEntry.COLUMN_CONTENT, review.getContent());

                db.insert(MovieContract.ReviewEntry.TABLE_NAME, null, reviewValues);
            }
            db.close();
            dbHelper.close();

            FavouriteUtilities.addToFavourite(movie.getTitle());
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            ((DetailCallback)receiver).makeToast(Constants.getDbAddingMessage());
        }
    }

    private class RemoveFromDbTask extends AsyncTask<Movie, Void, Void> {
        @Override
        protected Void doInBackground(Movie... params) {
            MovieDbHelper dbHelper = new MovieDbHelper(receiver.getContext());
            SQLiteDatabase db = dbHelper.getWritableDatabase();

            Movie movie = params[0];

            db.delete(MovieContract.MovieEntry.TABLE_NAME, MovieContract.MovieEntry.COLUMN_ID + "=" + movie.getId(), null);
            db.delete(MovieContract.TrailerEntry.TABLE_NAME, MovieContract.TrailerEntry.COLUMN_MOVIE_KEY + "=" + movie.getId(), null);
            db.delete(MovieContract.ReviewEntry.TABLE_NAME, MovieContract.ReviewEntry.COLUMN_MOVIE_KEY + "=" + movie.getId(), null);

            db.close();
            dbHelper.close();

            FavouriteUtilities.removeFromFavourite(movie.getTitle());
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            ((DetailCallback)receiver).makeToast(Constants.getDbRemovingMessage());
        }
    }

    private class FetchTrailersDbTask extends AsyncTask<String, Void, List<Trailer>> {
        @Override
        protected List<Trailer> doInBackground(String... params) {
            MovieDbHelper dbHelper = new MovieDbHelper(receiver.getContext());
            SQLiteDatabase db = dbHelper.getWritableDatabase();

            List<Trailer> trailerList = new ArrayList<>();

            Cursor trailerCursor = db.query(
                    MovieContract.TrailerEntry.TABLE_NAME,  // Table to Query
                    null, // leaving "columns" null just returns all the columns.
                    MovieContract.TrailerEntry.COLUMN_MOVIE_KEY + "=?", // cols for "where" clause
                    new String[]{String.valueOf(params[0])}, // values for "where" clause
                    null, // columns to group by
                    null, // columns to filter by row groups
                    null  // sort order
            );
            trailerCursor.moveToFirst();
            while (!trailerCursor.isAfterLast()) {
                int siteIndex = trailerCursor.getColumnIndex(MovieContract.TrailerEntry.COLUMN_SITE);
                int nameIndex = trailerCursor.getColumnIndex(MovieContract.TrailerEntry.COLUMN_NAME);
                int pathIndex = trailerCursor.getColumnIndex(MovieContract.TrailerEntry.COLUMN_PATH);
                String name = trailerCursor.getString(nameIndex);
                String path = trailerCursor.getString(pathIndex);
                String site = trailerCursor.getString(siteIndex);

                Trailer trailer = new Trailer(site, path, name);
                trailerList.add(trailer);
                trailerCursor.moveToNext();
            }
            trailerCursor.close();
            db.close();
            dbHelper.close();

            return trailerList;
        }

        @Override
        protected void onPostExecute(List<Trailer> trailersList) {
            if (trailersList != null) {
                ((DetailCallback)receiver).handleTrailers(trailersList);
            }
        }
    }

    private class FetchReviewsDbTask extends AsyncTask<String, Void, List<Review>> {
        @Override
        protected List<Review> doInBackground(String... params) {
            MovieDbHelper dbHelper = new MovieDbHelper(receiver.getContext());
            SQLiteDatabase db = dbHelper.getWritableDatabase();

            List<Review> reviewList = new ArrayList<>();

            Cursor reviewCursor = db.query(
                    MovieContract.ReviewEntry.TABLE_NAME,  // Table to Query
                    null, // leaving "columns" null just returns all the columns.
                    MovieContract.ReviewEntry.COLUMN_MOVIE_KEY + "=?", // cols for "where" clause
                    new String[]{String.valueOf(params[0])}, // values for "where" clause
                    null, // columns to group by
                    null, // columns to filter by row groups
                    null  // sort order
            );
            reviewCursor.moveToFirst();
            while (!reviewCursor.isAfterLast()) {
                int authorIndex = reviewCursor.getColumnIndex(MovieContract.ReviewEntry.COLUMN_AUTHOR);
                int contentIndex = reviewCursor.getColumnIndex(MovieContract.ReviewEntry.COLUMN_CONTENT);
                String author = reviewCursor.getString(authorIndex);
                String content = reviewCursor.getString(contentIndex);

                Review review = new Review(author, content);
                reviewList.add(review);
                reviewCursor.moveToNext();
            }
            db.close();
            dbHelper.close();

            return reviewList;
        }

        @Override
        protected void onPostExecute(List<Review> reviewsList) {
            if (reviewsList != null) {
                ((DetailCallback)receiver).handleReviews(reviewsList);
            }
        }
    }
}