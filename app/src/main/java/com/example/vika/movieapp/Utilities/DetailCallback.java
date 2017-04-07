package com.example.vika.movieapp.Utilities;

import com.example.vika.movieapp.Objects.Review;
import com.example.vika.movieapp.Objects.Trailer;

import java.util.List;

/**
 * Created by Eng.Ahmed on 11/21/2016.
 */

public interface DetailCallback {

    int getTrailerCount();

    int getReviewCount();

    Trailer getTrailerByIndex(int index);

    Review getReviewByIndex(int index);

    void makeToast(String message);

    void handleTrailers(List<Trailer> trailersList);

    void handleReviews(List<Review> reviewsList);
}
