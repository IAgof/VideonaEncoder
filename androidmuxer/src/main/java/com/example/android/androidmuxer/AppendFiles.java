package com.example.android.androidmuxer;

import android.os.Environment;

import com.coremedia.iso.boxes.Container;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import com.googlecode.mp4parser.authoring.tracks.AppendTrack;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
/**
 * Created by Veronica Lago Fominaya on 24/06/2015.
 */
public class AppendFiles {
    final static String LOG_TAG = "Merge videos";
    public static void merge(ArrayList<String> videoList, String audioSelected) throws IOException {

/*
        ArrayList<Movie> movieList = new ArrayList<>();

        for (String video : videoList) {
            movieList.add(MovieCreator.build(video));
        }

*/


        String f1 = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_MOVIES) + File.separator + "transcode_Nexus5_original_One_plus_one_3.mp4";
        String f2 = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_MOVIES) + File.separator + "transcode_Nexus5_original_Sony_SP_vlf_2.mp4";

        Movie[] movieList = new Movie[]{
                MovieCreator.build(f1),
                MovieCreator.build(f2)};


        List<Track> videoTracks = new LinkedList<Track>();
        List<Track> audioTracks = new LinkedList<Track>();

        for (Movie m : movieList) {
            for (Track t : m.getTracks()) {
            /*
            if (t.getHandler().equals("soun")) {
                audioTracks.add(t);
            }
            */
                if (t.getHandler().equals("vide")) {
                    videoTracks.add(t);
                }
            }
        }

        Movie result = new Movie();

    /*
    if (audioTracks.size() > 0) {
        result.addTrack(new AppendTrack(audioTracks.toArray(new Track[audioTracks.size()])));
    }
    */
        if (videoTracks.size() > 0) {
            result.addTrack(new AppendTrack(videoTracks.toArray(new Track[videoTracks.size()])));
        }

        Container out = new DefaultMp4Builder().build(result);

        FileChannel fc = new RandomAccessFile(String.format(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_MOVIES) + File.separator + "output.mp4"), "rw").getChannel();
        out.writeContainer(fc);
        fc.close();
    }
}
