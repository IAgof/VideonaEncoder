package com.example.android.androidmuxer;

import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import com.googlecode.mp4parser.authoring.tracks.AppendTrack;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Veronica Lago Fominaya on 25/06/2015.
 */
public class VideoAppender implements Appender {

    public Movie append(ArrayList<String> videoPaths, boolean addOriginalAudio) throws IOException {

        ArrayList<Movie> movieList = new ArrayList<>();

        for (String video : videoPaths) {
            movieList.add(MovieCreator.build(video));
        }

        List<Track> videoTracks = new LinkedList<Track>();
        List<Track> audioTracks = new LinkedList<Track>();

        for (Movie m : movieList) {
            for (Track t : m.getTracks()) {
                if(addOriginalAudio) {
                    if (t.getHandler().equals("soun")) {
                        audioTracks.add(t);
                    }
                }
                if (t.getHandler().equals("vide")) {
                    videoTracks.add(t);
                }
            }
        }

        Movie result = new Movie();

        if (audioTracks.size() > 0) {
            result.addTrack(new AppendTrack(audioTracks.toArray(new Track[audioTracks.size()])));
        }

        if (videoTracks.size() > 0) {
            result.addTrack(new AppendTrack(videoTracks.toArray(new Track[videoTracks.size()])));
        }

        return getMovie(result);
    }

    @Override
    public Movie getMovie(Movie movie) {
        return movie;
    }
}
