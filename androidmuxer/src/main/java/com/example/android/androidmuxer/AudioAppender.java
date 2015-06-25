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
public class AudioAppender implements Appender {

    public Movie addAudio(Movie movie, ArrayList<String> audioPaths) throws IOException {

        ArrayList<Movie> audioList = new ArrayList<>();

        for (String audio : audioPaths) {
            audioList.add(MovieCreator.build(audio));
        }

        List<Track> audioTracks = new LinkedList<Track>();

        for (Movie m : audioList) {
            for (Track t : m.getTracks()) {
                if (t.getHandler().equals("soun")) {
                    audioTracks.add(t);
                }
            }
        }

        if (audioTracks.size() > 0) {
            movie.addTrack(new AppendTrack(audioTracks.toArray(new Track[audioTracks.size()])));
        }

        return getMovie(movie);
    }

    @Override
    public Movie getMovie(Movie movie) {
        return movie;
    }
}
