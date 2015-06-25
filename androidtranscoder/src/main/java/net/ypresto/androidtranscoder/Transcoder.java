package net.ypresto.androidtranscoder;

import android.os.Environment;
import android.os.SystemClock;
import android.util.Log;

import net.ypresto.androidtranscoder.engine.MediaTranscoderEngine;
import net.ypresto.androidtranscoder.format.MediaFormatStrategy;
import net.ypresto.androidtranscoder.format.MediaFormatStrategyPresets;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Veronica Lago Fominaya on 17/06/2015.
 */
//public class AndroidTranscoder implements ThreadCompleteListener {
public class Transcoder {
    private static final String TAG = "FileTranscoder";
    private final Resolution outResolution;
    private static final int BITRATE_2MBPS = 2000 * 1000;
    private static final int BITRATE_5MBPS = 5000 * 1000;
    private static final int BITRATE_8MBPS = 8000 * 1000;
    private static final int BITRATE_10MBPS = 10000 * 1000;
    private static final int FRAMERATE_20FPS = 20;
    private static final int FRAMERATE_25FPS = 25;
    private static final int FRAMERATE_30FPS = 30;
    private static final int FRAMEINTERVAL_3SEC = 3;
    private static final int FRAMEINTERVAL_5SEC = 5;
    private static final int DEFAULT_BITRATE = BITRATE_5MBPS;
    private static final int DEFAULT_FRAMERATE = FRAMERATE_30FPS;
    private static final int DEFAULT_FRAMEINTERVAL = FRAMEINTERVAL_3SEC;
    private final int mBitRate;
    private final int mFrameRate;
    private final int mFrameInterval;
    private int numFilesToTranscoder = 1;
    private int numFilesTranscoded = 0;

    // TODO: implements the transcoder with threads
    /*
    @Override
    public void notifyOfThreadComplete(Thread thread) {
        Log.d(TAG,"ha acabadoooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo");
    }
    */

    public enum Resolution {
        HD720, HD1080, HD4K
    }

    /**
     * Constructor.
     */
    public Transcoder(Resolution resolution) {
        this.outResolution = resolution;
        this.mBitRate = DEFAULT_BITRATE;
        this.mFrameRate = DEFAULT_FRAMERATE;
        this.mFrameInterval = DEFAULT_FRAMEINTERVAL;
    }

    /**
     * Constructor.
     */
    public Transcoder(Resolution resolution, int bitRate) {
        this.outResolution = resolution;
        this.mBitRate = getBitRate(bitRate);
        this.mFrameRate = DEFAULT_FRAMERATE;
        this.mFrameInterval = DEFAULT_FRAMEINTERVAL;
    }

    /**
     * Constructor.
     */
    public Transcoder(Resolution resolution, int bitRate, int frameRate, int frameInterval) {
        this.outResolution = resolution;
        this.mBitRate = getBitRate(bitRate);
        this.mFrameRate = getFrameRate(frameRate);
        this.mFrameInterval = getFrameInterval(frameInterval);
    }

    private int getBitRate(int bitRate) {
        int outBitRate = bitRate;
        if (bitRate != BITRATE_2MBPS &&
                bitRate != BITRATE_5MBPS &&
                bitRate != BITRATE_8MBPS &&
                bitRate != BITRATE_10MBPS) {
            outBitRate = BITRATE_5MBPS;
        }
        return outBitRate;
    }

    private int getFrameRate(int frameRate) {
        int outFrameRate = frameRate;
        if (frameRate != FRAMERATE_20FPS &&
                frameRate != FRAMERATE_25FPS &&
                frameRate != FRAMERATE_30FPS) {
            outFrameRate = FRAMERATE_30FPS;
        }
        return outFrameRate;
    }

    private int getFrameInterval(int frameInterval) {
        int outFrameInterval = frameInterval;
        if (frameInterval != FRAMEINTERVAL_3SEC && frameInterval != FRAMEINTERVAL_5SEC) {
            outFrameInterval = FRAMEINTERVAL_5SEC;
        }
        return outFrameInterval;
    }

    public void transcodeFile(String filePath, final Listener transcoderListener) throws IOException {
        File file = new File(filePath);
        if(file.isDirectory()) {
            transcodeDirectory(file, transcoderListener);
        } else {
            try {
                transcode(file, transcoderListener);
            } catch (IOException e) {
                throw e;
            }
        }
    }

    public void transcodeFile(ArrayList<String> videoList, final Listener transcoderListener) throws IOException {
        numFilesToTranscoder = videoList.size();
        for (String video : videoList) {
            try {
                transcode(new File(video), transcoderListener);
            } catch (IOException e) {
                throw e;
            }
        }
    }

    public void transcodeFile(File file, final Listener transcoderListener) throws IOException {
        if(file.isDirectory()) {
            transcodeDirectory(file, transcoderListener);
        } else {
            try {
                transcode(file, transcoderListener);
            } catch (IOException e) {
                throw e;
            }
        }
    }

    private void transcodeDirectory(File file, final Listener transcoderListener) throws IOException {
        File[] videoList = file.listFiles();
        numFilesToTranscoder = videoList.length;
        if(numFilesToTranscoder > 0) {
            for (File video : videoList) {
                try {
                    transcode(video, transcoderListener);
                } catch (IOException e) {
                    throw e;
                }
            }
        }
        /*
        if(videoList.length > 0) {
            for (final File video : videoList) {
                NotifyingThread thread1 = new NotifyingThread() {
                    @Override
                    public void doRun() {
                        try {
                            transcode(video, transcoderListener);
                        } catch (IOException e) {
                            //throw e;
                        }
                    }
                };
                thread1.addListener(this); // add ourselves as a listener
                thread1.start();
            }
        }
        */
    }

    private void transcode(final File file, final Listener transcoderListener) throws IOException {

        final File tempDir = new File (Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_MOVIES), "Transcoder_temp");
        if (!tempDir.exists())
            tempDir.mkdirs();
        final File tempFile = new File(tempDir, file.getName());
        final long startTime = SystemClock.uptimeMillis();
        MediaTranscoder.Listener listener = new MediaTranscoder.Listener() {
            @Override
            public void onTranscodeProgress(double progress) {
                transcoderListener.onTranscodeProgress(progress);
            }

            @Override
            public void onTranscodeCompleted() {
                Log.d(TAG, "transcoding finished");
                Log.d(TAG, "transcoding took " + (SystemClock.uptimeMillis() - startTime) + "ms");
                transcoderListener.onTranscodeCompleted(tempFile.getAbsolutePath());
                numFilesTranscoded++;
                if (numFilesTranscoded == numFilesToTranscoder) {
                    transcoderListener.onTranscodeFinished();
                }
            }

            @Override
            public void onTranscodeFailed(Exception exception) {
                transcoderListener.onTranscodeFailed(exception);
                numFilesTranscoded++;
                if (numFilesTranscoded == numFilesToTranscoder) {
                    transcoderListener.onTranscodeFinished();
                }
            }
        };
        Log.d(TAG, "transcoding into " + tempFile);
        try {
            MediaTranscoder.getInstance().transcodeVideo(file, tempFile.getAbsolutePath(),
                    getFormatStrategy(outResolution), listener);
        } catch (IOException e) {
            throw e;
        }
    }

    private MediaFormatStrategy getFormatStrategy(Resolution resolution) {
        switch (resolution) {
            case HD720:
                return MediaFormatStrategyPresets.createAndroid720pStrategy(mBitRate, mFrameRate, mFrameInterval);
            case HD1080:
                return MediaFormatStrategyPresets.createAndroid1080pStrategy(mBitRate, mFrameRate, mFrameInterval);
            case HD4K:
                return MediaFormatStrategyPresets.createAndroid2160pStrategy(mBitRate, mFrameRate, mFrameInterval);
            default:
                return MediaFormatStrategyPresets.createAndroid720pStrategy(mBitRate, mFrameRate, mFrameInterval);
        }
    }

    public interface Listener {
        /**
         * Called to notify progress.
         *
         * @param progress Progress in [0.0, 1.0] range, or negative value if progress is unknown.
         */
        void onTranscodeProgress(double progress);

        /**
         * Called when transcode completed.
         *
         * @param path
         */
        void onTranscodeCompleted(String path);

        /**
         * Called when transcode of all videos finished.
         */
        void onTranscodeFinished();

        /**
         * Called when transcode failed.
         *
         * @param exception Exception thrown from {@link MediaTranscoderEngine#transcodeVideo(String, MediaFormatStrategy)}.
         *                  Note that it IS NOT {@link Throwable}. This means {@link Error} won't be caught.
         */
        void onTranscodeFailed(Exception exception);
    }
}
