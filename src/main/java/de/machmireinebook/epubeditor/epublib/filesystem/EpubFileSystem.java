package de.machmireinebook.epubeditor.epublib.filesystem;

import java.nio.file.FileSystem;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.google.common.jimfs.PathType;

/**
 * User: mjungierek
 * Date: 31.08.2014
 * Time: 18:00
 */
public class EpubFileSystem
{
    public static final FileSystem INSTANCE;
    static
    {
        Configuration config = Configuration.builder(PathType.unix())
                .setRoots("/")
                .setAttributeViews("basic")
                .setSupportedFeatures()
                .setWorkingDirectory("/")
                .build();
        INSTANCE = Jimfs.newFileSystem(config);
    }
}
