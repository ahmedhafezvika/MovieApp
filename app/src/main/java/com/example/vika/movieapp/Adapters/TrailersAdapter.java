package com.example.vika.movieapp.Adapters;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.vika.movieapp.Objects.Movie;
import com.squareup.picasso.Picasso;

import com.example.vika.movieapp.Objects.Trailer;
import com.example.vika.movieapp.R;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

/**
 * Created by Eng.Ahmed on 10/19/2016.
 */

public class TrailersAdapter extends RecyclerView.Adapter<TrailersAdapter.TrailersAdapterViewHolder> {

    private List<Trailer> mData;
    private final TrailersAdapterOnClickHandler mClickHandler;
    private Context mContext;

    public interface TrailersAdapterOnClickHandler {
        void onClick(Trailer trailer);
    }

    public TrailersAdapter(Context context, TrailersAdapterOnClickHandler clickHandler, List<Trailer> trailers) {
        mClickHandler = clickHandler;
        mData = trailers;
        mContext = context;
    }

    @Override
    public TrailersAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        int layoutItemId = R.layout.trailer_item;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(layoutItemId, parent, shouldAttachToParentImmediately);
        return new TrailersAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(TrailersAdapterViewHolder holder, int position) {
        Trailer trailer = mData.get(position);
        String img_url="http://img.youtube.com/vi/"+ trailer.getPath() +"/0.jpg"; // this is link which will give u thumnail image of that video

        ImageView thumbnail = holder.thumbnail;

        Picasso.with(mContext)
                .load(img_url)
                .placeholder(R.drawable.ic_action_default_poster)
                .into(thumbnail);
    }

    @Override
    public int getItemCount() {
        if (null == mData) return 0;
        return mData.size();
    }

    public void setTrailersData(List<Trailer> trailers) {
        mData = trailers;
        notifyDataSetChanged();
    }

    public Trailer getTrailerByIndex (int index) {
        return mData.get(index);
    }

    public class TrailersAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public final ImageView thumbnail;

        public TrailersAdapterViewHolder(View view) {
            super(view);
            thumbnail = (ImageView) view.findViewById(R.id.img_thumnail);
            view.setOnClickListener(this);
        }


        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();
            Trailer trailer = mData.get(adapterPosition);
            mClickHandler.onClick(trailer);
        }
    }

}