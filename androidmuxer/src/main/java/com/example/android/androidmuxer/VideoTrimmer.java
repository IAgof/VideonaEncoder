package com.example.android.androidmuxer;

import com.googlecode.mp4parser.authoring.Movie;

/**
 * Created by Veronica Lago Fominaya on 25/06/2015.
 */
public class VideoTrimmer extends Trimmer{

    @Override
    public Movie trim(String videoPath, double startTime, double endTime) {
        return super.trim(videoPath, startTime, endTime);
    }
}
