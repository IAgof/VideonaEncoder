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
import com.example.android.androidmuxer.utils.Constants;
import com.example.android.androidmuxer.utils.Utils;
import com.googlecode.mp4parser.authoring.Movie;

import net.ypresto.androidtranscoder.Transcoder;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Veronica Lago Fominaya on 09/06/2015.
 */
public class TranscoderActivity extends Activity {
    private static final String TAG = "TranscoderActivity";
    private static final int REQUEST_CODE_PICK = 1;
    private Transcoder transcoder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transcoder);
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

                    String directory = Environment.getExternalStoragePublicDirectory(
                            Environment.DIRECTORY_MOVIES) + File.separator + "Transcoder";
                    String output = directory + File.separator + "output.mp4";
                    //AppendVideos.MergeFiles(directory, output);
                    ArrayList<String> videos = new ArrayList<String>();
                    Appender appender = new Appender();
                    videos.add(Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_MOVIES) + File.separator + "transcode_Nexus5_original_One_plus_one_3.mp4");
                    videos.add(Environment.getExternalStoragePublicDirectory(
                            Environment.DIRECTORY_MOVIES) + File.separator + "transcode_Nexus5_original_Sony_SP_vlf_2.mp4");
                    Movie movie;
                    try {
                        movie = appender.appendVideos(videos, false);
                    } catch (IOException e) {
                        Log.d(TAG, String.valueOf(e));
                        //e.printStackTrace();
                    }
                    /*
                    Uri uri = Uri.parse(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES) +
                            File.separator + "output.mp4");
                    startActivity(new Intent(Intent.ACTION_VIEW).setDataAndType(uri, "video/mp4"));
                    */

            }
        });
        findViewById(R.id.trim_video_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                boolean success;
                String videoPath = Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_MOVIES) + File.separator + "transcode_Nexus5_original_One_plus_one_3.mp4";
                String outPath = Constants.TEMP_TRIM_DIRECTORY + File.separator +"merge_30sec.mp4";
                Trimmer trimmer;
                Movie movie;
                double movieDuration = 30000;

                try {
                    trimmer = new VideoTrimmer();
                    movie = trimmer.trim(videoPath, 0,movieDuration);
                    Utils.createFile(movie, outPath);
                    success = true;
                } catch (IOException e) {
                    //e.printStackTrace();
                    Log.d(TAG, String.valueOf(e));
                    success = false;
                }

                if(success) {
                    transcode(outPath);
                    Log.d(TAG, "ok");
                } else {
                    Log.d(TAG, "fail");
                }

            }
        });
    }

    private void transcode(String path) {
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
                Appender appender = new Appender();
                try {
                    ArrayList<String> audio = new ArrayList<>();
                    String audioPath = Environment.getExternalStoragePublicDirectory(
                            Environment.DIRECTORY_MOVIES) + File.separator + "audio_pop.m4a";
                    audio.add(audioPath);
                    double movieDuration = 30000;
                    String outPath = Environment.getExternalStoragePublicDirectory(
                            Environment.DIRECTORY_MOVIES) + File.separator + "resultGordo.mp4";;
                    Movie merge = appender.appendVideos(videoTranscoded, false);
                    Movie result = appender.addAudio(merge,audio,movieDuration);
                    Utils.createFile(result, outPath);
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
            transcoder.transcodeFile(new File(path), listener);
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
