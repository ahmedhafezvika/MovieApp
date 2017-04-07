package com.example.vika.movieapp.Adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.vika.movieapp.Objects.Trailer;
import com.example.vika.movieapp.R;

import java.util.List;

/**
 * Created by Eng.Ahmed on 10/19/2016.
 */

public class TrailersAdapter extends ArrayAdapter<Trailer> {

    public TrailersAdapter(Activity context, List<Trailer> trailers) {
        super(context, 0, trailers);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Trailer trailer = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(
                    R.layout.trailer_item, parent, false);
        }

        TextView trailerName = (TextView) convertView.findViewById(R.id.trailer_name);
        trailerName.setText(trailer.getName());

        return convertView;
    }
}