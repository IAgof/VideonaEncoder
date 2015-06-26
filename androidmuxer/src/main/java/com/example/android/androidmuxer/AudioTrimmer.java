package com.example.android.androidmuxer;

import android.util.Log;

import com.example.android.androidmuxer.utils.Utils;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import com.googlecode.mp4parser.authoring.tracks.CroppedTrack;

import java.io.IOException;
import java.util.List;

/**
 * Created by Veronica Lago Fominaya on 25/06/2015.
 */
public class AudioTrimmer implements Trimmer {

    @Override
    public Movie trim(String audioPath, double startTime, double endTime) {

        Movie originalMovie;
        Movie result;
        try {
            originalMovie = MovieCreator.build(audioPath);
            List<Track> tracks = originalMovie.getTracks();
            //MP4 parser usa segundos en vez de ms
            startTime = startTime / 1000;
            endTime = endTime / 1000;
            boolean timeCorrected = false;
            for (Track track : tracks) {
                if (track.getSyncSamples() != null && track.getSyncSamples().length > 0) {
                    if (timeCorrected) {
                        // This exception here could be a false positive in case we have multiple tracks
                        // with sync samples at exactly the same positions. E.g. a single movie containing
                        // multiple qualities of the same video (Microsoft Smooth Streaming file)
                        throw new RuntimeException("The startTime has already been corrected by another track with SyncSample. Not Supported.");
                    }
                    startTime = Utils.correctTimeToSyncSample(track, startTime, false);
                    endTime = Utils.correctTimeToSyncSample(track, endTime, true);
                    timeCorrected = true;
                }
            }
            result = new Movie();
            for (Track track : tracks) {
                long samples[] = Utils.getStartAndStopSamples(track, startTime, endTime);
                result.addTrack(new CroppedTrack(track, samples[0], samples[1]));
            }
        } catch (IOException e) {
            //e.printStackTrace();
            Log.d("VideoTrimmer", String.valueOf(e));
            result = null;
        }

        return result;
    }
}
