/*
 * Copyright (C) 2015 Yuya Tanaka
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.androidmuxer.utils;

import com.coremedia.iso.boxes.Container;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;

public class FileUtils {
    public static void createFile(Movie movie, String outPath) throws IOException {
        Container out = new DefaultMp4Builder().build(movie);
        //String.format(FileConstants.TEMP_TRIM_DIRECTORY + File.separator + "output.mp4")
        FileChannel fc = new RandomAccessFile(outPath, "rw").getChannel();
        out.writeContainer(fc);
        fc.close();
    }
}
