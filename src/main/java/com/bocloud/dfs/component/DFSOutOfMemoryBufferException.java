package com.bocloud.dfs.component;

public class DFSOutOfMemoryBufferException extends RuntimeException {


    public DFSOutOfMemoryBufferException(String msg) {
        super(msg);
    }

    public DFSOutOfMemoryBufferException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
