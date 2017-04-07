package com.example.vika.movieapp.AsyncTasks;

import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;

import com.example.vika.movieapp.Objects.Movie;
import com.example.vika.movieapp.Objects.Review;
import com.example.vika.movieapp.Objects.Trailer;
import com.example.vika.movieapp.Utilities.Constants;
import com.example.vika.movieapp.Utilities.DetailCallback;
import com.example.vika.movieapp.Utilities.GridCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by Eng.Ahmed on 11/21/2016.
 */

public class ServerTasks {

    private Fragment receiver;

    public ServerTasks(Fragment receiver) {
        this.receiver = receiver;
    }

    public void fetchMovies (String sortType, int pageNum) {
        FetchMovieTask movieTask = new FetchMovieTask();
        movieTask.execute(sortType, String.valueOf(pageNum));
    }

    public void fetchTrailers (String movieID) {
        FetchTrailersOnlineTask trailersTask = new FetchTrailersOnlineTask();
        trailersTask.execute(movieID);
    }

    public void fetchReviews (String movieID) {
        FetchReviewsOnlineTask reviewsTask = new FetchReviewsOnlineTask();
        reviewsTask.execute(movieID);
    }

    private class FetchMovieTask extends AsyncTask <String, Void, List<Movie>> {

        private String localType;
        private int pageNum;

        List<Movie> parseJson(String jsonStr) throws JSONException {
            List<Movie> movies = new ArrayList<>();

            final String OWM_RESULTS = "results";
            final String OWM_ID = "id";
            final String OWM_POSTER_PATH = "poster_path";
            final String OWM_OVERVIEW = "overview";
            final String OWM_RELEASE_DATE = "release_date";
            final String OWM_TITLE = "title";
            final String OWM_VOTE_AVERAGE = "vote_average";

            JSONObject moviesJson = new JSONObject(jsonStr);
            JSONArray movieArray = moviesJson.getJSONArray(OWM_RESULTS);

            for(int i = 0; i < movieArray.length(); i++) {
                String poster_path;
                String overview;
                String release_date;
                String title;
                int id;
                float vote_average;

                JSONObject movieObject = movieArray.getJSONObject(i);

                poster_path = movieObject.getString(OWM_POSTER_PATH);
                overview = movieObject.getString(OWM_OVERVIEW);
                release_date = movieObject.getString(OWM_RELEASE_DATE);
                title = movieObject.getString(OWM_TITLE);
                id = movieObject.getInt(OWM_ID);
                vote_average = (float) movieObject.getDouble(OWM_VOTE_AVERAGE);

                Movie movie = new Movie(id, poster_path, overview, title, release_date, vote_average);

                movies.add(movie);
            }
            return movies;
        }

        @Override
        protected List<Movie> doInBackground(String... params) {
            if (params.length == 0) {
                return null;
            }

            localType = params[0];
            pageNum = Integer.parseInt(params[1]);

            HttpsURLConnection urlConnection = null;
            BufferedReader reader = null;
            String jsonStr = null;

            String api = Constants.getApiKey();

            try {
                final String BASE_URL = Constants.getBaseMovieUrl();
                final String PAGE_PARAM = "page";
                final String API_KEY_PARAM = "api_key";

                Uri builtUri = Uri.parse(BASE_URL).buildUpon()
                        .appendEncodedPath(params[0])
                        .appendQueryParameter(API_KEY_PARAM, api)
                        .appendQueryParameter(PAGE_PARAM, String.valueOf(pageNum))
                        .build();

                URL url = new URL(builtUri.toString());
                urlConnection = (HttpsURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }
                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                jsonStr = buffer.toString();
            } catch (IOException e) {
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            try {
                return parseJson(jsonStr);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            // This will only happen if there was an error getting or parsing the forecast.
            return null;
        }

        @Override
        protected void onPostExecute(List<Movie> movies) {
            ((GridCallback)receiver).handleMovies(movies, localType);
        }
    }

    private class FetchTrailersOnlineTask extends AsyncTask<String, Void, List<Trailer>> {

        List<Trailer> parseJson(String jsonStr) throws JSONException {
            List<Trailer> trailers = new ArrayList<>();

            final String OWM_RESULTS = "results";
            final String OWM_SITE = "site";
            final String OWM_KEY = "key";
            final String OWM_NAME = "name";

            JSONObject moviesJson = new JSONObject(jsonStr);
            JSONArray movieArray = moviesJson.getJSONArray(OWM_RESULTS);

            for(int i = 0; i < movieArray.length(); i++) {
                String site;
                String key;
                String name;

                JSONObject movieObject = movieArray.getJSONObject(i);
                site = movieObject.getString(OWM_SITE);
                key = movieObject.getString(OWM_KEY);
                name = movieObject.getString(OWM_NAME);

                Trailer trailer = new Trailer(site, key, name);
                trailers.add(trailer);
            }
            return trailers;
        }

        @Override
        protected List<Trailer> doInBackground(String... params) {
            if (params.length == 0) {
                return null;
            }

            HttpsURLConnection urlConnection = null;
            BufferedReader reader = null;

            String jsonStr = null;

            String api = Constants.getApiKey();
            int page = 1;

            try {
                final String BASE_URL = Constants.getBaseMovieUrl() + params[0] + "/videos?";
                final String PAGE_PARAM = "page";
                final String API_KEY_PARAM = "api_key";

                Uri builtUri = Uri.parse(BASE_URL).buildUpon()
                        .appendQueryParameter(API_KEY_PARAM, api)
                        .appendQueryParameter(PAGE_PARAM, Integer.toString(page))
                        .build();

                URL url = new URL(builtUri.toString());
                urlConnection = (HttpsURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                jsonStr = buffer.toString();
            } catch (IOException e) {
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            try {
                return parseJson(jsonStr);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            // This will only happen if there was an error getting or parsing the forecast.
            return null;
        }

        @Override
        protected void onPostExecute(List<Trailer> trailersList) {
            if (trailersList != null) {
                ((DetailCallback)receiver).handleTrailers(trailersList);
            }
        }
    }

    private class FetchReviewsOnlineTask extends AsyncTask<String, Void, List<Review>> {

        List<Review> parseJson(String jsonStr) throws JSONException{
            List<Review> reviews = new ArrayList<>();

            final String OWM_RESULTS = "results";
            final String OWM_AUTHOR = "author";
            final String OWM_CONTENT = "content";

            JSONObject moviesJson = new JSONObject(jsonStr);
            JSONArray movieArray = moviesJson.getJSONArray(OWM_RESULTS);

            for(int i = 0; i < movieArray.length(); i++) {
                String author;
                String content;

                JSONObject movieObject = movieArray.getJSONObject(i);
                author = movieObject.getString(OWM_AUTHOR);
                content = movieObject.getString(OWM_CONTENT);

                Review review = new Review(author, content);
                reviews.add(review);
            }
            return reviews;
        }

        @Override
        protected List<Review> doInBackground(String... params) {
            if (params.length == 0) {
                return null;
            }

            HttpsURLConnection urlConnection = null;
            BufferedReader reader = null;
            String jsonStr = null;

            String api = Constants.getApiKey();
            int page = 1;

            try {
                final String BASE_URL = Constants.getBaseMovieUrl() + params[0] + "/reviews?";
                final String PAGE_PARAM = "page";
                final String API_KEY_PARAM = "api_key";

                Uri builtUri = Uri.parse(BASE_URL).buildUpon()
                        .appendQueryParameter(API_KEY_PARAM, api)
                        .appendQueryParameter(PAGE_PARAM, Integer.toString(page))
                        .build();

                URL url = new URL(builtUri.toString());

                urlConnection = (HttpsURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                jsonStr = buffer.toString();
            } catch (IOException e) {
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            try {
                return parseJson(jsonStr);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            // This will only happen if there was an error getting or parsing the forecast.
            return null;
        }

        @Override
        protected void onPostExecute(List<Review> reviewsList) {
            if (reviewsList != null) {
                ((DetailCallback)receiver).handleReviews(reviewsList);
            }
        }
    }
}
