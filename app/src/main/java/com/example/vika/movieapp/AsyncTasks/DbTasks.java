package com.example.vika.movieapp.AsyncTasks;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import com.example.vika.movieapp.DataBase.MovieContract;
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

            String[] columns = {MovieContract.MovieEntry.COLUMN_NAME};

            Cursor movieCursor = mReceiver.getContentResolver()
                    .query(MovieContract.MovieEntry.CONTENT_URI,
                            columns,
                            null,
                            null,
                            null);

            movieCursor.moveToFirst();

            List<String> favourites = new ArrayList<>();
            int nameIndex = movieCursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_NAME);
            while (!movieCursor.isAfterLast()) {
                favourites.add(movieCursor.getString(nameIndex));
                movieCursor.moveToNext();
            }
            movieCursor.close();
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
            Cursor movieCursor = receiver.getContext().getContentResolver()
                    .query(MovieContract.MovieEntry.CONTENT_URI,
                    null,
                    null,
                    null,
                    null);

            List<Movie> movieList = new ArrayList<>();

            movieCursor.moveToFirst();
            while (!movieCursor.isAfterLast()) {
                int idIndex = movieCursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_ID);
                int nameIndex = movieCursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_NAME);
                int posterIndex = movieCursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_POSTER_PATH);
                int overviewIndex = movieCursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_OVERVIEW);
                int rateIndex = movieCursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_RATE);
                int dateIndex = movieCursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_RELEASE_DATE);
                int backgroundIndex = movieCursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_BACKGROUND);

                int id = movieCursor.getInt(idIndex);
                String name = movieCursor.getString(nameIndex);
                String date = movieCursor.getString(dateIndex);
                String overview = movieCursor.getString(overviewIndex);
                float rate = movieCursor.getFloat(rateIndex);
                String poster = movieCursor.getString(posterIndex);
                String background = movieCursor.getString(backgroundIndex);

                Movie movie = new Movie(id, poster, overview, name, date, rate, background);
                movieList.add(movie);
                movieCursor.moveToNext();
            }

            movieCursor.close();
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

            Movie movie = params[0];

            ContentValues movieValues = new ContentValues();
            movieValues.put(MovieContract.MovieEntry.COLUMN_ID, movie.getId());
            movieValues.put(MovieContract.MovieEntry.COLUMN_NAME, movie.getTitle());
            movieValues.put(MovieContract.MovieEntry.COLUMN_OVERVIEW, movie.getOverview());
            movieValues.put(MovieContract.MovieEntry.COLUMN_POSTER_PATH, movie.getPosterPath());
            movieValues.put(MovieContract.MovieEntry.COLUMN_RATE, movie.getVote_average());
            movieValues.put(MovieContract.MovieEntry.COLUMN_RELEASE_DATE, movie.getRelease_date());
            movieValues.put(MovieContract.MovieEntry.COLUMN_BACKGROUND, movie.getBackground());
            // Insert the content values via a ContentResolver
            Uri uri = receiver.getContext().getContentResolver().insert(MovieContract.MovieEntry.CONTENT_URI, movieValues);

            // add trailers
            int trailersCount = ((DetailCallback)receiver).getTrailerCount();
            ContentValues[] trailerValues = new ContentValues[trailersCount];
            for (int i = 0; i < trailersCount; i++) {
                Trailer trailer = ((DetailCallback)receiver).getTrailerByIndex(i);

                ContentValues value = new ContentValues();
                value.put(MovieContract.TrailerEntry.COLUMN_MOVIE_KEY, movie.getId());
                value.put(MovieContract.TrailerEntry.COLUMN_NAME, trailer.getName());
                value.put(MovieContract.TrailerEntry.COLUMN_PATH, trailer.getPath());
                value.put(MovieContract.TrailerEntry.COLUMN_SITE, trailer.getType());

                trailerValues[i] = value;
            }
            receiver.getContext().getContentResolver().bulkInsert(MovieContract.TrailerEntry.CONTENT_URI, trailerValues);

            // add reviews
            int reviewsCount = ((DetailCallback)receiver).getReviewCount();
            ContentValues[] reviewsValues = new ContentValues[reviewsCount];
            for (int i = 0; i < reviewsCount; i++) {
                Review review = ((DetailCallback)receiver).getReviewByIndex(i);

                ContentValues value = new ContentValues();
                value.put(MovieContract.ReviewEntry.COLUMN_MOVIE_KEY, movie.getId());
                value.put(MovieContract.ReviewEntry.COLUMN_AUTHOR, review.getAuthor());
                value.put(MovieContract.ReviewEntry.COLUMN_CONTENT, review.getContent());

                reviewsValues[i] = value;
            }
            receiver.getContext().getContentResolver().bulkInsert(MovieContract.ReviewEntry.CONTENT_URI, reviewsValues);

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
            Movie movie = params[0];

            Uri uri = MovieContract.MovieEntry.CONTENT_URI;
            String id = String.valueOf(movie.getId());
            uri = uri.buildUpon().appendPath(id).build();
            receiver.getContext().getContentResolver().delete(uri, null, null);
            receiver.getContext().getContentResolver()
                    .delete(MovieContract.TrailerEntry.CONTENT_URI, MovieContract.TrailerEntry.COLUMN_MOVIE_KEY + "=?", new String[]{id});
            receiver.getContext().getContentResolver()
                    .delete(MovieContract.ReviewEntry.CONTENT_URI, MovieContract.ReviewEntry.COLUMN_MOVIE_KEY + "=?", new String[]{id});
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
            List<Trailer> trailerList = new ArrayList<>();

            Cursor trailerCursor = receiver.getContext().getContentResolver()
                    .query(MovieContract.TrailerEntry.CONTENT_URI,
                            null,
                            MovieContract.TrailerEntry.COLUMN_MOVIE_KEY + "=?",
                            new String[]{String.valueOf(params[0])},
                            null);
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
            List<Review> reviewList = new ArrayList<>();

            Cursor reviewCursor = receiver.getContext().getContentResolver()
                    .query(MovieContract.ReviewEntry.CONTENT_URI,
                            null,
                            MovieContract.ReviewEntry.COLUMN_MOVIE_KEY + "=?",
                            new String[]{String.valueOf(params[0])},
                            null);
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