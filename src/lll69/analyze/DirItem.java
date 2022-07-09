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
import java.util.ArrayList;

public class DirItem extends FileItem {
    public ArrayList<FileItem> fileItems = new ArrayList<>();
    public long calcSize = -1;
    public long tempSize = 0;

    public DirItem(File file, DirItem parent) {
        super(file, parent);
    }

    public DirItem(File file, AnalyzeThread thread) {
        super(file, null, 0, thread);
    }

    @Override
    public long calcSize() {
        if (fileList.length == 0) return 0;
        if (calcSize >= 0) return calcSize;
        analyzeThread.currentFile = path;
        analyzeThread.currentType = "";
        tempSize = 0;
        fileItems = new ArrayList<>(fileList.length);
        for (int i = 0; i < fileList.length; i++) {
            fileItems.add(get(fileList[i], this));
        }
        for (int i = 0; i < fileItems.size(); i++) {
            FileItem fileItem = fileItems.get(i);
            if (fileItem != null)
                tempSize += fileItem.calcSize();
        }
        return calcSize = tempSize;
    }

    @Override
    public long getSize() {
        return tempSize;
    }

    @Override
    public double getProgress() {
        if (fileList.length == 0) return 1;
        if (fileItems == null) return 0;
        double progress = 0;
        for (int i = 0; i < fileItems.size(); i++) {
            FileItem fileItem = fileItems.get(i);
            progress += (fileItem == null ? 1 : fileItem.getProgress()) / fileList.length;
        }
        return progress;
    }
}
