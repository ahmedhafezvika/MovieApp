package com.example.vika.movieapp.Fragments;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.vika.movieapp.Adapters.ReviewsAdapter;
import com.example.vika.movieapp.Adapters.TrailersAdapter;
import com.example.vika.movieapp.AsyncTasks.DbTasks;
import com.example.vika.movieapp.AsyncTasks.ServerTasks;
import com.example.vika.movieapp.Objects.Movie;
import com.example.vika.movieapp.Objects.Review;
import com.example.vika.movieapp.Objects.Trailer;
import com.example.vika.movieapp.R;
import com.example.vika.movieapp.Utilities.Constants;
import com.example.vika.movieapp.Utilities.DetailCallback;
import com.example.vika.movieapp.Utilities.ExpandableHeightListView;
import com.example.vika.movieapp.Utilities.FavouriteUtilities;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Eng.Ahmed on 10/19/2016.
 */

public class DetailFragment extends Fragment implements DetailCallback {

    private TrailersAdapter trailersAdapter;
    private ReviewsAdapter reviewsAdapter;
    private String movieID;
    private ExpandableHeightListView trailers;
    private ExpandableHeightListView reviews;
    private ProgressBar progressBar;
    private ScrollView scrollView;
    private boolean favourite = false;
    private DbTasks dbTasks;
    private ServerTasks serverTasks;
    private boolean gotData = false;
    private Context context;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView  = inflater.inflate(R.layout.fragment_detail, container, false);

        context = getActivity();
        dbTasks = new DbTasks(this);
        serverTasks = new ServerTasks(this);
        Gson gson = new Gson();
        Bundle bundle = getArguments();
        String movieStr = bundle.getString("movieDetail");
        final Movie movie = gson.fromJson(movieStr, Movie.class);

        scrollView = (ScrollView) rootView.findViewById(R.id.scroll);
        progressBar = (ProgressBar) rootView.findViewById(R.id.pbLoader);
        RatingBar ratingBar = (RatingBar) rootView.findViewById(R.id.ratingBar);
        TextView movieName = (TextView) rootView.findViewById(R.id.movie_name);
        ImageView moviePoster = (ImageView) rootView.findViewById(R.id.movie_poster);
        TextView movieDate = (TextView) rootView.findViewById(R.id.movie_date);
        TextView movieRate = (TextView) rootView.findViewById(R.id.movie_rate);
        TextView movieOverview = (TextView) rootView.findViewById(R.id.movie_overview);
        ImageButton favouriteButton = (ImageButton) rootView.findViewById(R.id.favourite);

        boolean favourite = FavouriteUtilities.foundInFavourites(movie.getTitle());

        if (favourite) {
            favouriteButton.setSelected(true);
        }

        favouriteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.isSelected()) {
                    v.setSelected(false);
                    FavouriteUtilities.setRemoved(movie);
                    dbTasks.removeFromDb(movie);
                } else {
                    v.setSelected(true);
                    dbTasks.saveInDb(movie);
                }
            }
        });

        trailers = (ExpandableHeightListView) rootView.findViewById(R.id.movie_trailers);
        reviews = (ExpandableHeightListView) rootView.findViewById(R.id.movie_reviews);

        trailers.setFocusable(false);
        reviews.setFocusable(false);

        if (trailersAdapter == null) {
            trailersAdapter = new TrailersAdapter(getActivity(), new ArrayList<Trailer>());
        }
        if (reviewsAdapter == null) {
            reviewsAdapter = new ReviewsAdapter(getActivity(), new ArrayList<Review>());
        }

        trailers.setAdapter(trailersAdapter);
        reviews.setAdapter(reviewsAdapter);

        if (!gotData && isOnline()) {
            progressBar.setVisibility(View.VISIBLE);
            scrollView.setVisibility(View.GONE);
        }
        else {
            progressBar.setVisibility(View.GONE);
            scrollView.setVisibility(View.VISIBLE);
        }

        trailers.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (isOnline()) {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    Uri trailerUri = Uri.parse("https://www.youtube.com/watch?").buildUpon()
                            .appendQueryParameter("v", ( (Trailer) parent.getItemAtPosition(position)).getPath())
                            .build();
                    intent.setData(trailerUri);
                    startActivity(intent);
                } else {
                    Toast.makeText(getActivity(), "you are offline please check your network connection", Toast.LENGTH_LONG).show();
                }
            }
        });

        if (bundle != null) {
            movieName.setText(movie.getTitle());
            movieDate.setText(movie.getRelease_date().substring(5, 7) + "/" + movie.getRelease_date().substring(0, 4));
            movieRate.setText(String.valueOf(movie.getVote_average()) + "/10");
            ratingBar.setIsIndicator(true);
            ratingBar.setRating(movie.getVote_average() / 2);
            movieOverview.setText(movie.getOverview());
            Picasso.with(getContext()).load("http://image.tmdb.org/t/p/w185" + movie.getPosterPath()).into(moviePoster);

            movieID = String.valueOf(movie.getId());
        }
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setRetainInstance(true);
    }

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    @Override
    public void onStart() {
        super.onStart();

        updateTrailers();
        updateReviews();
        gotData = true;
    }

    private void updateTrailers() {
        if (trailersAdapter.isEmpty()) {
            if (favourite) {
                dbTasks.fetchTrailers(movieID);
            } else if (isOnline()) {
                serverTasks.fetchTrailers(movieID);
            } else {
                Toast.makeText(getActivity(), Constants.getOfflineDetailMessage(), Toast.LENGTH_LONG).show();
            }
        } else {
            trailers.setExpanded(true);
        }
    }

    private void updateReviews() {
        if (reviewsAdapter.isEmpty()) {
            if (favourite) {
                dbTasks.fetchReviews(movieID);
            } else if (isOnline()) {
                serverTasks.fetchReviews(movieID);
            } else {
                Toast.makeText(getActivity(), Constants.getOfflineDetailMessage(), Toast.LENGTH_LONG).show();
            }
        } else {
            reviews.setExpanded(true);
        }
    }

    @Override
    public int getTrailerCount() {
        return trailersAdapter.getCount();
    }

    @Override
    public int getReviewCount() {
        return reviewsAdapter.getCount();
    }

    @Override
    public Trailer getTrailerByIndex(int index) {
        return trailersAdapter.getItem(index);
    }

    @Override
    public Review getReviewByIndex(int index) {
        return reviewsAdapter.getItem(index);
    }

    @Override
    public void makeToast(String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void handleTrailers(List<Trailer> trailersList) {
        trailersAdapter.clear();
        for(Trailer trailer : trailersList) {
            trailersAdapter.add(trailer);
        }
        trailers.setExpanded(true);
        scrollView.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
    }

    @Override
    public void handleReviews(List<Review> reviewsList) {
        reviewsAdapter.clear();
        for(Review review : reviewsList) {
            reviewsAdapter.add(review);
        }
        reviews.setExpanded(true);
        scrollView.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
    }
}
