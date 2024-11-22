package com.org.customvideoplayer.interfaces;

import java.util.List;

public interface LoadMediaResultCallback<T> {

    void onLoadComplete(List<T> data, int currentPage, boolean isHasMore);

    void onLoadError(int errCode, String errMsg, Throwable e);
}
