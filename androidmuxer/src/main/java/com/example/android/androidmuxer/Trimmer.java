package com.example.android.androidmuxer;

import com.googlecode.mp4parser.authoring.Movie;

import java.io.IOException;

/**
 * Created by Veronica Lago Fominaya on 25/06/2015.
 */
public interface Trimmer {
    Movie trim(String filePath, double startTime, double endTime) throws IOException;
}
