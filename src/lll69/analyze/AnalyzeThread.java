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

public class AnalyzeThread extends Thread {
    public DirItem root;
    public String path;
    public long size = -1;
    public volatile String currentFile = "";
    public volatile String currentType = "";

    public AnalyzeThread() {
        super("AnalyzeThread");
    }

    @Override
    public void run() {
        size = (root = new DirItem(new File(path), this)).calcSize();
        System.out.println(size);
    }
}
