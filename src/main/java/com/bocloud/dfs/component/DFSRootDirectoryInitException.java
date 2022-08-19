package com.bocloud.dfs.component;

public class DFSRootDirectoryInitException  extends RuntimeException{
    public DFSRootDirectoryInitException(String msg) {
        super(msg);
    }
    public DFSRootDirectoryInitException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
