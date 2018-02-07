package com.example.vika.movieapp.Fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.example.vika.movieapp.Adapters.MoviesAdapter;
import com.example.vika.movieapp.AsyncTasks.DbTasks;
import com.example.vika.movieapp.AsyncTasks.ServerTasks;
import com.example.vika.movieapp.Objects.Movie;
import com.example.vika.movieapp.R;
import com.example.vika.movieapp.Utilities.Constants;
import com.example.vika.movieapp.Utilities.FavouriteUtilities;
import com.example.vika.movieapp.Utilities.GridCallback;
import com.example.vika.movieapp.Adapters.MoviesAdapter.MoviesAdapterOnClickHandler;
import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Eng.Ahmed on 10/18/2016.
 */

public class GridFragment extends Fragment implements GridCallback, MoviesAdapterOnClickHandler {

    private ProgressBar progressBar;
    private RecyclerView mRecyclerView;
    private MoviesAdapter mMoviesAdapter;
    private int pageNum = 1;
    private String sortType;
    private boolean offlineWarning = false;
    private boolean firstTime = true;
    private DbTasks dbTasks;
    private ServerTasks serverTasks;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View rootView  = inflater.inflate(R.layout.fragment_grid, container, false);

        dbTasks = new DbTasks(this);
        serverTasks = new ServerTasks(this);

        if (mMoviesAdapter == null) {
            mMoviesAdapter = new MoviesAdapter(getActivity(), this, new ArrayList<Movie>());
        }

        if (firstTime) {
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
            sortType = pref.getString(getString(R.string.pref_sorting_key), getString(R.string.pref_sorting_popular));
            firstTime = false;
        }

        progressBar = (ProgressBar) rootView.findViewById(R.id.pbLoader);

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.posters_grid);

        int cols = getContext().getResources().getInteger(R.integer.grid_cols);;
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), cols);

        mRecyclerView.setLayoutManager(gridLayoutManager);
        mRecyclerView.setAdapter(mMoviesAdapter);

        if (mMoviesAdapter.getItemCount() == 0 && isOnline()) {
            progressBar.setVisibility(View.VISIBLE);
            mRecyclerView.setVisibility(View.GONE);
        }
        else {
            progressBar.setVisibility(View.GONE);
            mRecyclerView.setVisibility(View.VISIBLE);
        }
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void handleFavourites(List<Movie> movieList) {
        mMoviesAdapter.setMoviesData(movieList);

        progressBar.setVisibility(View.GONE);
        mRecyclerView.setVisibility(View.VISIBLE);
        if (mMoviesAdapter.getItemCount() == 0) {
            Toast.makeText(getActivity(), Constants.getNoFavouriteMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void handleMovies(List<Movie> movies, String localType) {
        if (movies != null && sortType == localType) {
            mMoviesAdapter.appendMoviesData(movies);
            progressBar.setVisibility(View.GONE);
            mRecyclerView.setVisibility(View.VISIBLE);

            pageNum ++;
            updateMovies();
        }
    }

    @Override
    public void onClick(Movie movie) {
        Gson gson = new Gson();
        String movieStr = gson.toJson(movie);

        ((Callback)getActivity()).onItemClick(movieStr);
    }

    public interface Callback {
        void onItemClick(String movieStr);

        void onSortTypeChange();
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


       if (isOnline()) {
           updateMovies();
       }
       else if (!offlineWarning && sortType != "favourite" && pageNum < 20) {
           Toast.makeText(getActivity(), Constants.getOfflineMessage(), Toast.LENGTH_LONG).show();
           progressBar.setVisibility(View.GONE);
           offlineWarning = true;
           mMoviesAdapter.setMoviesData(new ArrayList<Movie>());
           pageNum = 1;
           showFavourites();
       }

        if (sortType == "favourite" && mMoviesAdapter.getItemCount() != FavouriteUtilities.getFavouriteSize()) {
            mMoviesAdapter.setMoviesData(new ArrayList<Movie>());
            showFavourites();
        }
    }

    private void updateMovies() {
        if (getActivity() == null) {
            return;
        }
        if (pageNum <= 20 && sortType != "favourite") {
            serverTasks.fetchMovies(sortType, pageNum);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_grid_fragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            handleUpdate(sortType);
            return true;
        }
        else if (id == R.id.action_show_favourite) {
            handleUpdate("favourite");
            return true;
        }
        else if (id == R.id.action_sort_popular) {
            handleUpdate("popular");
            return true;
        }
        else if (id == R.id.action_sort_rating) {
            handleUpdate("top_rated");
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void handleUpdate(String type) {
        if (isOnline()) {
            sortType = type;
            prepareToUpdate();
            if (type == "favourite") {
                showFavourites();
            } else {
                updateMovies();
            }
        }
        else {
            Toast.makeText(getActivity(), Constants.getOfflineMessage(), Toast.LENGTH_LONG).show();
            progressBar.setVisibility(View.GONE);
        }
    }

    private void prepareToUpdate() {
        mRecyclerView.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        mMoviesAdapter.setMoviesData(new ArrayList<Movie>());
        pageNum = 1;
        ((Callback)getActivity()).onSortTypeChange();
    }

    private void showFavourites() {
        dbTasks.fetchFavourites();
    }

}