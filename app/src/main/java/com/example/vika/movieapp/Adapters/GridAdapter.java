package com.example.vika.movieapp.Adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.example.vika.movieapp.Objects.Movie;
import com.example.vika.movieapp.R;
import com.example.vika.movieapp.Utilities.Constants;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by Eng.Ahmed on 10/18/2016.
 */

public class GridAdapter extends ArrayAdapter<Movie> {

    public GridAdapter(Activity context, List<Movie> movies) {
        super(context, 0, movies);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Movie curMovie = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(
                    R.layout.grid_item, parent, false);
        }
        ImageView poster = (ImageView) convertView.findViewById(R.id.poster);
        String fullUrl = Constants.getBaseImageUrl() + curMovie.getPosterPath();
        Picasso.with(getContext()).load(fullUrl).into(poster);
        return convertView;
    }
}