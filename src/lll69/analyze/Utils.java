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
import java.util.concurrent.Callable;

public class Utils {
    public static final File[] EMPTY_FILE = new File[0];
    public static final long KB = 1024;
    public static final long MB = KB * KB;
    public static final long GB = MB * KB;
    public static final long TB = GB * KB;

    public static <T> T tryGet(T defaultValue, Callable<T> get) {
        try {
            T value = get.call();
            return value == null ? defaultValue : value;
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return defaultValue;
    }

    public static String formatSize(double size, int digit) {
        if (size >= TB)
            return String.format(String.format("%%.%dfTB", digit), size / TB);
        else if (size >= GB)
            return String.format(String.format("%%.%dfGB", digit), size / GB);
        else if (size >= MB)
            return String.format(String.format("%%.%dfMB", digit), size / MB);
        else if (size >= KB)
            return String.format(String.format("%%.%dfKB", digit), size / KB);
        return size + "B";
    }
}
