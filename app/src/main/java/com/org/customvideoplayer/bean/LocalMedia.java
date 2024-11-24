package com.org.customvideoplayer.bean;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.Gson;

public class LocalMedia implements Parcelable {

    private long id;

    private String path;

    private String realPath;

    private String parentFolderName;

    private String title;

    private String displayName;

    private String mimeType;

    private int width;

    private int height;

    private long size;

    private long bucketId;

    public LocalMedia(){

    }

    public LocalMedia(long id, String path, String realPath, String parentFolderName, String title,
                      String displayName, String mimeType, int width, int height, long size, long bucketId) {
        this.id = id;
        this.path = path;
        this.realPath = realPath;
        this.parentFolderName = parentFolderName;
        this.title = title;
        this.displayName = displayName;
        this.mimeType = mimeType;
        this.width = width;
        this.height = height;
        this.size = size;
        this.bucketId = bucketId;
    }

    protected LocalMedia(Parcel in) {
        id = in.readLong();
        path = in.readString();
        realPath = in.readString();
        parentFolderName = in.readString();
        title = in.readString();
        displayName = in.readString();
        mimeType = in.readString();
        width = in.readInt();
        height = in.readInt();
        size = in.readLong();
        bucketId = in.readLong();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getRealPath() {
        return realPath;
    }

    public void setRealPath(String realPath) {
        this.realPath = realPath;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getParentFolderName() {
        return parentFolderName;
    }

    public void setParentFolderName(String parentFolderName) {
        this.parentFolderName = parentFolderName;
    }

    public long getBucketId() {
        return bucketId;
    }

    public void setBucketId(long bucketId) {
        this.bucketId = bucketId;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeLong(this.id);
        parcel.writeString(this.path);
        parcel.writeString(this.realPath);
        parcel.writeString(this.parentFolderName);
        parcel.writeString(this.title);
        parcel.writeString(this.displayName);
        parcel.writeString(this.mimeType);
        parcel.writeInt(this.width);
        parcel.writeInt(this.height);
        parcel.writeLong(this.size);
        parcel.writeLong(this.bucketId);
    }

    @Override
    public String toString() {
        return "LocalMedia{" +
                "id=" + id +
                ", path='" + path + '\'' +
                ", realPath='" + realPath + '\'' +
                ", parentFolderName='" + parentFolderName + '\'' +
                ", title='" + title + '\'' +
                ", displayName='" + displayName + '\'' +
                ", mimeType='" + mimeType + '\'' +
                ", width=" + width +
                ", height=" + height +
                ", size=" + size +
                ", bucketId=" + bucketId +
                '}';
    }

    public String toJson() {
        Gson gson = new Gson();
        String json = gson.toJson(this);
        return json;
    }

    public static final Creator<LocalMedia> CREATOR = new Creator<LocalMedia>() {
        @Override
        public LocalMedia createFromParcel(Parcel in) {
            return new LocalMedia(in);
        }

        @Override
        public LocalMedia[] newArray(int size) {
            return new LocalMedia[size];
        }
    };
}
