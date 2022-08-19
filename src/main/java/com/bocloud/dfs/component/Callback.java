package com.bocloud.dfs.component;

import com.bocloud.dfs.component.internals.DataChunk;

public interface Callback {

    void onCompletion(DataChunk dataChunk,Exception e);
}
