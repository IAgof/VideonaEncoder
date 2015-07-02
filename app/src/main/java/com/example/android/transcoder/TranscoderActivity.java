package com.example.android.transcoder;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.android.androidmuxer.Appender;
import com.example.android.androidmuxer.Trimmer;
import com.example.android.androidmuxer.VideoTrimmer;
import com.example.android.androidmuxer.utils.Utils;
import com.googlecode.mp4parser.authoring.Movie;

import net.ypresto.androidtranscoder.Transcoder;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Veronica Lago Fominaya on 09/06/2015.
 */
public class TranscoderActivity extends Activity {

    private static final String TAG = "TranscoderActivity";
    private static final int REQUEST_CODE_PICK = 1;
    private Transcoder transcoder;

    // Constants
    // Path folders
    final public static String PATH_APP = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_MOVIES) + File.separator + "VideonaTranscoder";
    final public static String PATH_APP_TRIM = PATH_APP + File.separator + "Trim";
    final public static String PATH_APP_APPEND = PATH_APP + File.separator + "Append";
    final public static String PATH_APP_MUSIC = PATH_APP + File.separator + "Music";
    final public static String PATH_APP_TRANSCODE = PATH_APP + File.separator + "Transcode";

    // Path videos, rename your files to these names.
    final public static String VIDEO1 = PATH_APP + File.separator + "video1.mp4";
    final public static String VIDEO2 = PATH_APP + File.separator + "video2.mp4";

    final public static String MUSIC1 = PATH_APP + File.separator + "music1.m4a";
    final public static String MUSIC2 = PATH_APP + File.separator + "music2.m4a";


    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
    final public static String VIDEO_EXTENSION = ".mp4";
    String fileName = "VID_" + timeStamp + ".mp4";

    // Output files, path videos
    private String VIDEO_TRIM = PATH_APP_TRIM + File.separator + "V_Trim_" + timeStamp + VIDEO_EXTENSION;
    private String VIDEO_APPEND = PATH_APP_APPEND + File.separator + "V_Append_" + timeStamp + VIDEO_EXTENSION;
    private String VIDEO_MUSIC = PATH_APP_MUSIC + File.separator + "V_Music_" + timeStamp + VIDEO_EXTENSION;
    private String VIDEO_TRANSCODE = PATH_APP_TRANSCODE + File.separator + "V_Transcode_" + timeStamp + VIDEO_EXTENSION;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transcoder);

        // Check folder paths
        checkPaths();

        transcoder = new Transcoder(Transcoder.Resolution.HD720);

        findViewById(R.id.select_video_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(Intent.ACTION_GET_CONTENT).setType("video/*"), REQUEST_CODE_PICK);
            }
        });
        findViewById(R.id.append_video_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                    boolean addOriginalAudio = false;
                    ArrayList<String> videos = new ArrayList<String>();
                    Appender appender = new Appender();
                    videos.add(VIDEO1);
                    videos.add(VIDEO2);
                    Movie movie;
                    try {
                        movie = appender.appendVideos(videos, addOriginalAudio);
                        Utils.createFile(movie, VIDEO_APPEND);
                    } catch (IOException e) {
                        Log.d(TAG, String.valueOf(e));
                    }
            }
        });

        findViewById(R.id.add_music_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                double duration = 60000;

                ArrayList<String> music = new ArrayList<String>();
                music.add(MUSIC1);
                ArrayList<String> videos = new ArrayList<String>();
                Appender appender = new Appender();
                videos.add(VIDEO1);
                Movie movie;
                Movie result;
                try {
                    movie = appender.appendVideos(videos, false);
                    result = appender.addAudio(movie, music, duration);
                    Utils.createFile(result, VIDEO_MUSIC);
                } catch (IOException e) {
                    Log.d(TAG, String.valueOf(e));
                }

            }
        });

        findViewById(R.id.trim_video_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                double movieDuration = 30000;

                Trimmer trimmer = new VideoTrimmer();
                Movie movie = null;
                try {
                    movie = trimmer.trim(VIDEO1, 0,movieDuration);
                    Utils.createFile(movie, VIDEO_TRIM);
                } catch (IOException e) {
                    Log.d(TAG, String.valueOf(e));
                }
            }
        });
        findViewById(R.id.transcode_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                boolean success = true;

                ArrayList<String> result = new ArrayList<String>();
                result.add(VIDEO1);

                transcode(result);

            }
        });
    }

    private void checkPaths() {

        File fApp = new File(PATH_APP);

        if (!fApp.exists()) {

            fApp.mkdir();
            //  Log.d(LOG_TAG, "Path Videona created");
        }

        File fAppend = new File(PATH_APP_APPEND);

        if (!fAppend.exists()) {

            fAppend.mkdir();
            //  Log.d(LOG_TAG, "Path Videona created");
        }

        File fMusic = new File(PATH_APP_MUSIC);

        if (!fMusic.exists()) {

            fMusic.mkdir();
            //  Log.d(LOG_TAG, "Path Videona created");
        }

        File fTranscode = new File(PATH_APP_TRANSCODE);

        if (!fTranscode.exists()) {

            fTranscode.mkdir();
            //  Log.d(LOG_TAG, "Path Videona created");
        }

        File fTrim = new File(PATH_APP_TRIM);

        if (!fTrim.exists()) {

            fTrim.mkdir();
            //  Log.d(LOG_TAG, "Path Videona created");
        }


    }

    private void transcode(ArrayList<String> videoPaths) {

        final ProgressBar progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        progressBar.setMax(1000);
        final long startTime = SystemClock.uptimeMillis();

        final ArrayList<String> videoTranscoded = new ArrayList<>();
        Transcoder.Listener listener = new Transcoder.Listener() {
            @Override
            public void onTranscodeProgress(double progress) {
                if (progress < 0) {
                    progressBar.setIndeterminate(true);
                } else {
                    progressBar.setIndeterminate(false);
                    progressBar.setProgress((int) Math.round(progress * 1000));
                }
            }

            @Override
            public void onTranscodeCompleted(String path) {
                Log.d(TAG, "transcoding finished listener");
                Log.d(TAG, "transcoding took " + (SystemClock.uptimeMillis() - startTime) + "ms");
                Toast.makeText(TranscoderActivity.this, "transcoded file placed on " + path, Toast.LENGTH_LONG).show();
                progressBar.setIndeterminate(false);
                progressBar.setProgress(1000);
                videoTranscoded.add(path);
            }

            @Override
            public void onTranscodeFinished() {
                //TODO ver cómo llamarlo cuando el último se codifique
                Appender appender = new Appender();
                try {

                    double movieDuration = 60000;

                    Movie merge = appender.appendVideos(videoTranscoded, true);
                    //Movie result = appender.addAudio(merge,audio,movieDuration);
                    //Utils.createFile(result, outPath);
                    Utils.createFile(merge, VIDEO_TRANSCODE);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Log.d(TAG,"ya ha acabadoooooooooooooooooo");
            }

            @Override
            public void onTranscodeFailed(Exception exception) {
                progressBar.setIndeterminate(false);
                progressBar.setProgress(0);
                Toast.makeText(TranscoderActivity.this, "Transcoder error occurred.", Toast.LENGTH_LONG).show();
            }
        };
        try {
            transcoder.transcodeFile(videoPaths, listener);
        } catch (IOException e) {
            Log.d(TAG, String.valueOf(e));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_PICK: {

                String videoPath = Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_MOVIES) + File.separator + "original_One_plus_one_3.mp4";
                String directory = Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_MOVIES) + File.separator + "prueba";
                final ProgressBar progressBar = (ProgressBar) findViewById(R.id.progress_bar);
                progressBar.setMax(1000);
                final long startTime = SystemClock.uptimeMillis();

                final ArrayList<String> videoTranscoded = new ArrayList<>();
                Transcoder.Listener listener = new Transcoder.Listener() {
                    @Override
                    public void onTranscodeProgress(double progress) {
                        if (progress < 0) {
                            progressBar.setIndeterminate(true);
                        } else {
                            progressBar.setIndeterminate(false);
                            progressBar.setProgress((int) Math.round(progress * 1000));
                        }
                    }

                    @Override
                    public void onTranscodeCompleted(String path) {
                        Log.d(TAG, "transcoding finished listener");
                        Log.d(TAG, "transcoding took " + (SystemClock.uptimeMillis() - startTime) + "ms");
                        Toast.makeText(TranscoderActivity.this, "transcoded file placed on " + path, Toast.LENGTH_LONG).show();
                        progressBar.setIndeterminate(false);
                        progressBar.setProgress(1000);
                        videoTranscoded.add(path);
                    }

                    @Override
                    public void onTranscodeFinished() {
                        //TODO ver cómo llamarlo cuando el último se codifique
                        Log.d(TAG,"ya ha acabadoooooooooooooooooo");
                    }

                    @Override
                    public void onTranscodeFailed(Exception exception) {
                        progressBar.setIndeterminate(false);
                        progressBar.setProgress(0);
                        Toast.makeText(TranscoderActivity.this, "Transcoder error occurred.", Toast.LENGTH_LONG).show();
                    }
                };
                try {
                    transcoder.transcodeFile(new File(videoPath), listener);
                } catch (IOException e) {
                    Log.d(TAG, String.valueOf(e));
                }


                /*
                final File file;
                if (resultCode == RESULT_OK) {
                    try {
                        file = File.createTempFile("transcode_test", ".mp4", getExternalFilesDir(null));
                    } catch (IOException e) {
                        Log.e(TAG, "Failed to create temporary file.", e);
                        Toast.makeText(this, "Failed to create temporary file.", Toast.LENGTH_LONG).show();
                        return;
                    }
                    ContentResolver resolver = getContentResolver();
                    final ParcelFileDescriptor parcelFileDescriptor;
                    try {
                        parcelFileDescriptor = resolver.openFileDescriptor(data.getData(), "r");
                    } catch (FileNotFoundException e) {
                        Log.w("Could not open '" + data.getDataString() + "'", e);
                        Toast.makeText(TranscoderActivity.this, "File not found.", Toast.LENGTH_LONG).show();
                        return;
                    }
                    final FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
                    final ProgressBar progressBar = (ProgressBar) findViewById(R.id.progress_bar);
                    progressBar.setMax(1000);
                    final long startTime = SystemClock.uptimeMillis();
                    MediaTranscoder.Listener listener = new MediaTranscoder.Listener() {
                        @Override
                        public void onTranscodeProgress(double progress) {
                            if (progress < 0) {
                                progressBar.setIndeterminate(true);
                            } else {
                                progressBar.setIndeterminate(false);
                                progressBar.setProgress((int) Math.round(progress * 1000));
                            }
                        }

                        @Override
                        public void onTranscodeCompleted() {
                            Log.d(TAG, "transcoding took " + (SystemClock.uptimeMillis() - startTime) + "ms");
                            Toast.makeText(TranscoderActivity.this, "transcoded file placed on " + file, Toast.LENGTH_LONG).show();
                            findViewById(R.id.select_video_button).setEnabled(true);
                            progressBar.setIndeterminate(false);
                            progressBar.setProgress(1000);
                            //startActivity(new Intent(Intent.ACTION_VIEW).setDataAndType(Uri.fromFile(file), "video/mp4"));
                            try {
                                parcelFileDescriptor.close();
                            } catch (IOException e) {
                                Log.w("Error while closing", e);
                            }
                        }

                        @Override
                        public void onTranscodeFailed(Exception exception) {
                            progressBar.setIndeterminate(false);
                            progressBar.setProgress(0);
                            findViewById(R.id.select_video_button).setEnabled(true);
                            Toast.makeText(TranscoderActivity.this, "Transcoder error occurred.", Toast.LENGTH_LONG).show();
                            try {
                                parcelFileDescriptor.close();
                            } catch (IOException e) {
                                Log.w("Error while closing", e);
                            }
                        }
                    };
                    Log.d(TAG, "transcoding into " + file);
                    MediaTranscoder.getInstance().transcodeVideo(fileDescriptor, file.getAbsolutePath(),
                            MediaFormatStrategyPresets.createAndroid1080pStrategy(), listener);
                    findViewById(R.id.select_video_button).setEnabled(false);
                }
                */
                break;
            }
            default:
                super.onActivityResult(requestCode, resultCode, data);

        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.transcoder, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
