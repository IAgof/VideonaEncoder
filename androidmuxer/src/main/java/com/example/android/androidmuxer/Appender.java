package com.example.android.androidmuxer;

import com.googlecode.mp4parser.authoring.Movie;

/**
 * Created by Veronica Lago Fominaya on 25/06/2015.
 */
public interface Appender {
    Movie getMovie(Movie movie);
}
