/*
 * Copyright 2022 lll69
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

package lll69.analyze;

import java.io.File;
import java.nio.file.Files;

import static lll69.analyze.Utils.EMPTY_FILE;
import static lll69.analyze.Utils.tryGet;

public class FileItem {
    public static final String SEPARATOR = File.separator;
    public final AnalyzeThread analyzeThread;
    public final File file;
    public final DirItem parent;
    public final long fileSize;
    public final boolean isFile;
    public final boolean isLink;
    public final int level;
    public final String path, name;
    public final File[] fileList;

    public static FileItem get(File file, DirItem parent) {
        if (!tryGet(false, () -> Files.isSymbolicLink(file.toPath())) && !tryGet(true, file::isFile)) {
            return new DirItem(file, parent);
        }
        return new FileItem(file, parent);
    }

    public FileItem(File file, DirItem parent) {
        this(file, parent, parent.level + 1, parent.analyzeThread);
    }

    FileItem(File file, DirItem parent, int level, AnalyzeThread thread) {
        this.file = file;
        this.parent = parent;
        this.level = level;
        analyzeThread = thread;
        path = file.getPath();
        name = file.getName();
        isLink = tryGet(false, () -> Files.isSymbolicLink(file.toPath()));
        isFile = isLink || tryGet(false, file::isFile);
        analyzeThread.currentFile = path;
        if (isFile) {
            analyzeThread.currentType = "";
        } else {
            analyzeThread.currentType = " listing";
        }
        fileSize = tryGet(0L, file::length);
        fileList = tryGet(EMPTY_FILE, file::listFiles);
        analyzeThread.currentFile = path;
    }

    public double getProgress() {
        return 1;
    }

    public long calcSize() {
        analyzeThread.currentFile = path;
        analyzeThread.currentType = "";
        return fileSize < 0 ? 0 : fileSize;
    }

    public long getSize() {
        return fileSize < 0 ? 0 : fileSize;
    }
}
