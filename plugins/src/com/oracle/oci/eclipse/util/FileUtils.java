package com.oracle.oci.eclipse.util;

import java.io.File;

public class FileUtils {
    public static void cleanupDirectory(File file) throws Exception {
        if (!file.isDirectory()) {
            return;
        } else {
            deleteDirectory(file);
        }
    }

    private static void deleteDirectory(File dir) {
        assert dir.isDirectory();
        for (File file : dir.listFiles()) {
            if (file.isFile()) {
                file.delete();
            }
            else {
               deleteDirectory(dir);
            }
        }
    }

}
