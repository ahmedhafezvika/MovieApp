package com.example.vika.movieapp.Activities;

import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageView;

import com.example.vika.movieapp.Fragments.DetailFragment;
import com.example.vika.movieapp.Objects.Movie;
import com.example.vika.movieapp.R;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

public class DetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Log.d("hhhhhhhhh", "this is new detail activity");

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        ActionBar actionBar = this.getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        String movieStr = getIntent().getStringExtra("movieDetail");

        Gson gson = new Gson();
        final Movie movie = gson.fromJson(movieStr, Movie.class);
        CollapsingToolbarLayout collapsingToolbarLayout =
                (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        collapsingToolbarLayout.setTitle(movie.getTitle());
        final ImageView image = (ImageView) findViewById(R.id.image);
        Picasso.with(this).load("http://image.tmdb.org/t/p/w185" + movie.getBackground()).into(image);

        if (savedInstanceState == null) {
            Log.d("hhhhhhhhhhh", "save instance state is null");
            FragmentManager fragmentManager = getSupportFragmentManager();
            Fragment fragment = fragmentManager.findFragmentByTag("DetailFragment");
            if (fragment == null) {
                Log.d("hhhhhhhhhhh", "detail fragment is null");
                fragment = new DetailFragment();
            }
            Bundle arguments = new Bundle();
            arguments.putString("movieDetail", movieStr);
            fragment.setArguments(arguments);
            fragmentManager.beginTransaction()
                    .replace(R.id.content_detail, fragment, "DetailFragment")
                    .commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(menuItem);
    }
}
