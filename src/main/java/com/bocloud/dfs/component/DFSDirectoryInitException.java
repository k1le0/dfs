package com.bocloud.dfs.component;

public class DFSDirectoryInitException extends RuntimeException {

    public DFSDirectoryInitException(String msg) {
        super(msg);
    }

    public DFSDirectoryInitException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
