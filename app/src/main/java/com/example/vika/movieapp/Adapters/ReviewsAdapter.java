package com.example.vika.movieapp.Adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.vika.movieapp.Objects.Review;
import com.example.vika.movieapp.R;

import java.util.List;

/**
 * Created by Eng.Ahmed on 10/19/2016.
 */

public class ReviewsAdapter extends ArrayAdapter<Review> {

    public ReviewsAdapter(Activity context, List<Review> reviews) {
        super(context, 0, reviews);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Review review = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(
                    R.layout.review_item, parent, false);
        }
        TextView author = (TextView) convertView.findViewById(R.id.author);
        TextView content = (TextView) convertView.findViewById(R.id.content);

        author.setText(review.getAuthor());
        content.setText(review.getContent());

        return convertView;
    }
}