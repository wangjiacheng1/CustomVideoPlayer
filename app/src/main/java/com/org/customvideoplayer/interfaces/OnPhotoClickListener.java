package com.org.customvideoplayer.interfaces;

public interface OnPhotoClickListener<T> {

    void onAddClick();
    void onPhotoClick(T data, int position);
}
