package com.example.android.transcoder;

import android.os.Environment;
import android.util.Log;

import com.coremedia.iso.boxes.Container;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import com.googlecode.mp4parser.authoring.tracks.AppendTrack;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.BufferOverflowException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Veronica Lago Fominaya on 10/06/2015.
 */
public class AppendVideos {
    final static String LOG_TAG = "Merge videos";
    public static void merge() throws IOException {

        String f1 = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_MOVIES) + File.separator + "transcode_prueba_1.mp4";
        String f2 = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_MOVIES) + File.separator + "transcode_prueba_2.mp4";

        Movie[] inMovies = new Movie[]{
                MovieCreator.build(f1),
                MovieCreator.build(f2)};

        List<Track> videoTracks = new LinkedList<Track>();
        List<Track> audioTracks = new LinkedList<Track>();

        for (Movie m : inMovies) {
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

    public static boolean MergeFiles(String speratedDirPath,
                                     String targetFileName) {

        File videoSourceDirFile = new File(speratedDirPath);
        String[] videoList = videoSourceDirFile.list();
        List<Track> videoTracks = new LinkedList<Track>();
        List<Track> audioTracks = new LinkedList<Track>();
        for (String file : videoList) {
            Log.d(LOG_TAG, "source files" + speratedDirPath
                    + File.separator + file);
            try {

                FileChannel fc;
                fc = new FileInputStream(speratedDirPath
                        + File.separator + file).getChannel();

                Movie movie = null;

                String pathFile = speratedDirPath + File.separator + file;
                movie = MovieCreator.build(pathFile);
                for (Track t : movie.getTracks()) {
                    if (t.getHandler().equals("soun")) {
                        audioTracks.add(t);
                    }
                    if (t.getHandler().equals("vide")) {
                        videoTracks.add(t);
                    }
                }
            } catch (FileNotFoundException e) {
                //e.printStackTrace();
                return false;
            } catch (IOException e) {
                    //e.printStackTrace();
                    return false;
            }
        }

        Movie result = new Movie();

        try {

            if (audioTracks.size() > 0) {
                result.addTrack(new AppendTrack(audioTracks
                        .toArray(new Track[audioTracks.size()])));
            }
            if (videoTracks.size() > 0) {
                result.addTrack(new AppendTrack(videoTracks
                        .toArray(new Track[videoTracks.size()])));
            }

            Container out = new DefaultMp4Builder().build(result);

            FileOutputStream fos = null;

            try {
                fos = new FileOutputStream(targetFileName);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                //return false;
            }

            BufferedWritableFileByteChannel byteBufferByteChannel = new BufferedWritableFileByteChannel(fos);

            try {
                out.writeContainer(byteBufferByteChannel);
                byteBufferByteChannel.close();
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
                //return false;
            }

            for (String file : videoList) {
                File TBRFile = new File(speratedDirPath + File.separator + file);
                TBRFile.delete();
            }

            boolean a = videoSourceDirFile.delete();

            Log.d(LOG_TAG, "try to delete dir:" + a);

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return false;
        }

        return true;
    }
}
class BufferedWritableFileByteChannel implements WritableByteChannel {
    private static final int BUFFER_CAPACITY = 1000000;

    private boolean isOpen = true;
    private final OutputStream outputStream;
    private final ByteBuffer byteBuffer;
    private final byte[] rawBuffer = new byte[BUFFER_CAPACITY];

    BufferedWritableFileByteChannel(OutputStream outputStream) {
        this.outputStream = outputStream;
        this.byteBuffer = ByteBuffer.wrap(rawBuffer);
    }

    public int write(ByteBuffer inputBuffer) throws IOException {
        int inputBytes = inputBuffer.remaining();

        if (inputBytes > byteBuffer.remaining()) {
            dumpToFile();
            byteBuffer.clear();

            if (inputBytes > byteBuffer.remaining()) {
                throw new BufferOverflowException();
            }
        }

        byteBuffer.put(inputBuffer);

        return inputBytes;
    }

    public boolean isOpen() {
        return isOpen;
    }

    public void close() throws IOException {
        dumpToFile();
        isOpen = false;
    }

    private void dumpToFile() {
        try {
            outputStream.write(rawBuffer, 0, byteBuffer.position());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
