package com.org.customvideoplayer.bean;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LocalMediaFolder implements Parcelable {

    //bucketId
    private long bucketId = -1;
    //文件夹名称
    private String name;
    //首图路径
    private String firstImagePath;
    //图片数量
    private int imageNum;
    private boolean isChecked;
    //图片列表
    private List<LocalMedia> data = new ArrayList<>();
    private boolean isHasMore;

    public long getBucketId() {
        return bucketId;
    }

    public void setBucketId(long bucketId) {
        this.bucketId = bucketId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFirstImagePath() {
        return firstImagePath;
    }

    public void setFirstImagePath(String firstImagePath) {
        this.firstImagePath = firstImagePath;
    }

    public int getImageNum() {
        return imageNum;
    }

    public void setImageNum(int imageNum) {
        this.imageNum = imageNum;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }

    public List<LocalMedia> getData() {
        return data;
    }

    public void setData(List<LocalMedia> data) {
        this.data = data;
    }

    public boolean isHasMore() {
        return isHasMore;
    }

    public void setHasMore(boolean hasMore) {
        isHasMore = hasMore;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeLong(this.bucketId);
        parcel.writeString(this.name);
        parcel.writeString(this.firstImagePath);
        parcel.writeInt(this.imageNum);
        parcel.writeByte(this.isChecked ? (byte) 1 : (byte) 0);
        parcel.writeTypedList(this.data);
        parcel.writeByte(this.isHasMore ? (byte) 1 : (byte) 0);
    }

    public LocalMediaFolder() {
    }

    public LocalMediaFolder(Parcel parcel){
        this.bucketId = parcel.readLong();
        this.name = parcel.readString();
        this.firstImagePath = parcel.readString();
        this.imageNum = parcel.readInt();
        this.isChecked = parcel.readByte() != 0;
        parcel.readTypedList(data, LocalMedia.CREATOR);
        this.isHasMore = parcel.readByte() != 0;
    }

    @Override
    public String toString() {
        return "LocalMediaFolder{" +
                "bucketId=" + bucketId +
                ", name='" + name + '\'' +
                ", firstImagePath='" + firstImagePath + '\'' +
                ", imageNum=" + imageNum +
                ", isChecked=" + isChecked +
                ", data=" + Arrays.toString(data.toArray()) +
                ", isHasMore=" + isHasMore +
                '}';
    }

    public static final Creator<LocalMediaFolder> CREATOR = new Creator<LocalMediaFolder>() {
        @Override
        public LocalMediaFolder createFromParcel(Parcel in) {
            return new LocalMediaFolder(in);
        }

        @Override
        public LocalMediaFolder[] newArray(int size) {
            return new LocalMediaFolder[size];
        }
    };
}
