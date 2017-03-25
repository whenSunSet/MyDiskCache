package com.example.heshixiyang.mydiskcache.binaryResource;

/**
 * Created by heshixiyang on 2017/3/23.
 */

import com.example.heshixiyang.mydiskcache.util.Files;
import com.example.heshixiyang.mydiskcache.util.Preconditions;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Administrator on 2017/3/11 0011.
 *
 * 一个基于真正文件的BinaryResource。
 * Implementation of BinaryResource based on a real file. @see BinaryResource for more details.
 */
public class FileBinaryResource implements BinaryResource {
    private final File mFile;

    private FileBinaryResource(File file) {
        mFile = Preconditions.checkNotNull(file);
    }

    public File getFile() {
        return mFile;
    }

    @Override
    public InputStream openStream() throws IOException {
        return new FileInputStream(mFile);
    }

    @Override
    public long size() {
        return mFile.length();
    }

    @Override
    public byte[] read() throws IOException {
        return Files.toByteArray(mFile);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof FileBinaryResource)) {
            return false;
        }
        FileBinaryResource that = (FileBinaryResource)obj;
        return mFile.equals(that.mFile);
    }

    @Override
    public int hashCode() {
        return mFile.hashCode();
    }

    /*
     * 一个创建FileBinaryResource的工厂方法
     */
    public static FileBinaryResource createOrNull(File file) {
        return (file != null) ? new FileBinaryResource(file) : null;
    }
}
