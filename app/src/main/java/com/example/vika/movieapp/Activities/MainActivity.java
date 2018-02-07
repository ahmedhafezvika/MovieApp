package com.example.vika.movieapp.Activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.example.vika.movieapp.AsyncTasks.DbTasks;
import com.example.vika.movieapp.Fragments.DetailFragment;
import com.example.vika.movieapp.Fragments.GridFragment;
import com.example.vika.movieapp.Objects.Movie;
import com.example.vika.movieapp.R;
import com.example.vika.movieapp.Utilities.FavouriteUtilities;
import com.squareup.picasso.Picasso;

public class MainActivity extends AppCompatActivity implements GridFragment.Callback, SharedPreferences.OnSharedPreferenceChangeListener {

    private boolean mTwoPane;
    private FragmentManager fragmentManager;
    private DbTasks dbTasks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d("hhhhhhhhh", "this is new main activity");

        dbTasks = new DbTasks(this);
        fragmentManager = getSupportFragmentManager();
        Fragment gridFragment = fragmentManager.findFragmentByTag("GridFragment");
        if (gridFragment == null) {
            Log.d("hhhhhhhhhhh", "grid fragment is null");
            gridFragment = new GridFragment();
        }
        fragmentManager.beginTransaction().replace(R.id.fragment_grid, gridFragment, "GridFragment").commit();

        if (findViewById(R.id.content_detail) != null) {
            mTwoPane = true;
        } else {
            mTwoPane = false;
            getSupportActionBar().setElevation(0f);
        }

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (FavouriteUtilities.isNull()) {
            dbTasks.fetchFavouritesNames();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(String movieStr) {
        if (mTwoPane) {
            Fragment fragment = new DetailFragment();
            Log.d("hhhhhhhhhh", "create new detail fragment");
            Bundle args = new Bundle();
            args.putString("movieDetail", movieStr);
            fragment.setArguments(args);
            fragmentManager.beginTransaction()
                    .replace(R.id.content_detail, fragment, "DetailFragment")
                    .commit();
            Log.d("hhhhhhhhhh", "transact detail fragment");
        }else {
            Intent intent = new Intent(this, DetailActivity.class);
            intent.putExtra("movieDetail", movieStr);
            startActivity(intent);
        }
    }

    @Override
    public void onSortTypeChange() {
        if (mTwoPane) {
            Fragment fragment = fragmentManager.findFragmentByTag("DetailFragment");
            if (fragment != null) {
                fragmentManager.beginTransaction().detach(fragment).commit();
            }
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(R.string.pref_sorting_key))) {
            String  sortType = sharedPreferences.getString(getString(R.string.pref_sorting_key), "popular");
            ((GridFragment)fragmentManager.findFragmentByTag("GridFragment")).handleUpdate(sortType);
        }
    }
}
