package com.whiuk.philip.opensmime.remote;

import java.io.InputStream;

public abstract class AbstractContentLoaderTaskBuilder<T> {
    private InputStream inputStream;

    protected InputStream getInputStream() {
        return inputStream;
    }

    public AbstractContentLoaderTaskBuilder<T> setInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
        return this;
    }

    public abstract ContentLoaderTask<T> build();
}
