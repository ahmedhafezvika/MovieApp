package com.example.vika.movieapp.Fragments;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Toast;

import com.example.vika.movieapp.Activities.MainActivity;
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
import com.example.vika.movieapp.databinding.DetailLayoutBinding;
import com.example.vika.movieapp.Adapters.TrailersAdapter.TrailersAdapterOnClickHandler;
import com.example.vika.movieapp.databinding.FragmentDetailBinding;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Eng.Ahmed on 10/19/2016.
 */

public class DetailFragment extends Fragment implements DetailCallback, TrailersAdapterOnClickHandler {

    private RecyclerView trailersRecyclerView;
    private TrailersAdapter trailersAdapter;
    private ReviewsAdapter reviewsAdapter;
    private String movieID;
    private ExpandableHeightListView reviews;
    private ProgressBar progressBar;
    private android.support.v4.widget.NestedScrollView scrollView;
    private boolean favourite = false;
    private DbTasks dbTasks;
    private ServerTasks serverTasks;
    private boolean gotData = false;
    private Context context;

    DetailLayoutBinding mBinding;
    FragmentDetailBinding fBinding;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        Log.d("hhhhhhhhhhh", "detail fragment on create view");

        fBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_detail, container, false);
        mBinding = fBinding.detailView;
        View rootView = fBinding.getRoot();

        context = getActivity();
        dbTasks = new DbTasks(this);
        serverTasks = new ServerTasks(this);
        Gson gson = new Gson();
        Bundle bundle = getArguments();
        String movieStr = bundle.getString("movieDetail");
        final Movie movie = gson.fromJson(movieStr, Movie.class);

        displayDataInfo(movie);
        movieID = String.valueOf(movie.getId());

        if (trailersAdapter == null) {
            Log.d("hhhhhhhhhhh", "trailer adapter is null");
            trailersAdapter = new TrailersAdapter(getActivity(), this, new ArrayList<Trailer>());
        }
        if (reviewsAdapter == null) {
            Log.d("hhhhhhhhhhh", "reviews adapter is null");
            reviewsAdapter = new ReviewsAdapter(getActivity(), new ArrayList<Review>());
        }

        reviews.setFocusable(false);
        trailersRecyclerView.setAdapter(trailersAdapter);
        reviews.setAdapter(reviewsAdapter);

        if (!gotData && isOnline()) {
            progressBar.setVisibility(View.VISIBLE);
            scrollView.setVisibility(View.INVISIBLE);
        }
        else {
            progressBar.setVisibility(View.GONE);
            scrollView.setVisibility(View.VISIBLE);
        }
        return rootView;
    }

    private void displayDataInfo (final Movie movie) {

        if (context instanceof MainActivity) {
            fBinding.collapsingToolbar.setTitle(movie.getTitle());
            Picasso.with(getContext()).load("http://image.tmdb.org/t/p/w185" + movie.getBackground()).into(fBinding.image);
        }
        mBinding.movieDate.setText(movie.getRelease_date().substring(5, 7) + "/" + movie.getRelease_date().substring(0, 4));
        mBinding.movieRate.setText(String.valueOf(movie.getVote_average()) + "/10");
        mBinding.ratingBar.setIsIndicator(true);
        mBinding.ratingBar.setRating(movie.getVote_average() / 2);
        mBinding.movieOverview.setText(movie.getOverview());
        Picasso.with(getContext()).load("http://image.tmdb.org/t/p/w185" + movie.getPosterPath())
                .placeholder(R.drawable.ic_action_default_poster)
                .error(R.drawable.ic_action_default_poster)
                .into(mBinding.moviePoster);

        boolean favourite = FavouriteUtilities.foundInFavourites(movie.getTitle());
        if (favourite) {
            mBinding.favourite.setSelected(true);
        }
        mBinding.favourite.setOnClickListener(new View.OnClickListener() {
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
        trailersRecyclerView = mBinding.movieTrailers;
        LinearLayoutManager layoutManager
                = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        trailersRecyclerView.setLayoutManager(layoutManager);
        reviews = mBinding.movieReviews;
        scrollView = mBinding.scroll;
        progressBar = mBinding.pbLoader;
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
        if (trailersAdapter.getItemCount() == 0) {
            if (favourite) {
                dbTasks.fetchTrailers(movieID);
            } else if (isOnline()) {
                serverTasks.fetchTrailers(movieID);
            } else {
                Toast.makeText(getActivity(), Constants.getOfflineDetailMessage(), Toast.LENGTH_LONG).show();
            }
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
        return trailersAdapter.getItemCount();
    }

    @Override
    public int getReviewCount() {
        return reviewsAdapter.getCount();
    }

    @Override
    public Trailer getTrailerByIndex(int index) {
        return trailersAdapter.getTrailerByIndex(index);
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
        trailersAdapter.setTrailersData(trailersList);
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

    @Override
    public void onClick(Trailer trailer) {
        if (isOnline()) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            Uri trailerUri = Uri.parse("https://www.youtube.com/watch?").buildUpon()
                    .appendQueryParameter("v", trailer.getPath())
                    .build();
            intent.setData(trailerUri);
            startActivity(intent);
        } else {
            Toast.makeText(getActivity(), "you are offline please check your network connection", Toast.LENGTH_LONG).show();
        }
    }
}
