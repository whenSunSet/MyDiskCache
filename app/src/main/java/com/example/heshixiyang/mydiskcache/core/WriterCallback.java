package com.example.heshixiyang.mydiskcache.core;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by heshixiyang on 2017/3/23.
 */
public interface WriterCallback {
    void write(OutputStream os) throws IOException;
}
