package com.org.customvideoplayer.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;


import androidx.recyclerview.widget.RecyclerView;

import com.org.customvideoplayer.R;
import com.org.customvideoplayer.bean.LocalMedia;
import com.org.customvideoplayer.interfaces.OnPhotoClickListener;
import com.org.customvideoplayer.utils.ImageUtils;

import java.util.ArrayList;
import java.util.List;

public class GridGalleryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = "GridGalleryAdapter";

    public static final int TYPE_ADD = 0;
    public static final int TYPE_PIC_ITEM = 1;

    List<LocalMedia> data = new ArrayList<>();
    List<LocalMedia> selectedImages = new ArrayList<>();

    OnPhotoClickListener mClickListener;

    Context mContext;
    boolean isShowAdded = true;

    int maxSelectedSize = 2;

    public GridGalleryAdapter(Context context, OnPhotoClickListener listener){
        this.mContext = context;
        this.mClickListener = listener;
    }

    public GridGalleryAdapter(Context context, List<LocalMedia> data, OnPhotoClickListener listener){
        this.mContext = context;
        this.data = data;
        this.mClickListener = listener;
    }

    public void setShowAdded(boolean isShow){
        this.isShowAdded = isShow;
    }

    public boolean getIsShowAdded(){
        return isShowAdded;
    }

    public void bindData(List<LocalMedia> data){
        this.data = data == null ? new ArrayList<>() : data;
        notifyDataSetChanged();
    }

    public List<LocalMedia> getData(){
        return data;
    }

    public boolean isDataEmpty(){
        return data == null || data.isEmpty();
    }

    public int getMaxSelectedSize() {
        return maxSelectedSize;
    }

    public void setMaxSelectedSize(int maxSelectedSize) {
        this.maxSelectedSize = maxSelectedSize;
    }

    @Override
    public int getItemViewType(int position) {
        if (isShowAdded && position == 0){
            return TYPE_ADD;
        }else {
            return TYPE_PIC_ITEM;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        if (viewType == TYPE_ADD){
            View view = LayoutInflater.from(mContext).inflate(R.layout.picture_item_add, viewGroup, false);
            return new AddImageViewHolder(view);
        }else {
            View view = LayoutInflater.from(mContext).inflate(R.layout.picture_image_grid_item, viewGroup, false);
            return new ImageItemViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        Log.i(TAG, "onBindViewHolder, position = " + position);
        if (getItemViewType(position) == TYPE_ADD){
            AddImageViewHolder addImageViewHolder = (AddImageViewHolder) viewHolder;
            addImageViewHolder.rootView.setOnClickListener(onAddClick);
        }else {
            ImageItemViewHolder imageItemViewHolder = (ImageItemViewHolder) viewHolder;
            int index = isShowAdded ? position -1 : position;
            LocalMedia photo = data.get(index);
            imageItemViewHolder.rootView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mClickListener != null){
                        mClickListener.onPhotoClick(photo, index);
                    }
                }
            });
            ImageUtils.loadImageByGlide(mContext, photo.getPath(), imageItemViewHolder.picIv);

            imageItemViewHolder.checkBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    boolean isSelected = imageItemViewHolder.isSelect;
                    if (!isSelected){
                        if (maxSelectedSize > 0 && selectedImages.size() >= maxSelectedSize){
                            return;
                        }
                    }
                    imageItemViewHolder.isSelect = !imageItemViewHolder.isSelect;
                    changeSelectBtnStatus(imageItemViewHolder.checkBtn, imageItemViewHolder.isSelect);
                    updateSelectedImage(photo);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        int count = isShowAdded ? data.size() + 1 : data.size();
        return count;
    }

    View.OnClickListener onAddClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (mClickListener != null){
                mClickListener.onAddClick();
            }
        }
    };

    public void changeSelectBtnStatus(CheckBox checkBox, boolean isSelect){
        if (isSelect){
            checkBox.setBackgroundResource(R.drawable.picture_icon_sel);
        }else {
            checkBox.setBackgroundResource(R.drawable.picture_icon_def);
        }
    }

    /**
     * 更新已选中列表
     * @param image
     */
    public void updateSelectedImage(LocalMedia image){
        if (!isSelectedImage(image)){
            if (maxSelectedSize > 0 && selectedImages.size() >= maxSelectedSize){
                return;
            }
            selectedImages.add(image);
        }else {
            removeSelectedImage(image);
        }
    }

    /**
     * 判断LocalMedia是否已经被选中
     * @param image
     * @return
     */
    public boolean isSelectedImage(LocalMedia image){
        int index = selectedImages.size();
        if (index == 0){
            return false;
        }
        for (int i = 0; i < index; i++){
            LocalMedia media1 = selectedImages.get(i);
            if (media1 == null || TextUtils.isEmpty(media1.getRealPath())){
                continue;
            }
            if (media1.getRealPath().equals(image.getRealPath())){
                return true;
            }
        }
        return false;
    }

    /**
     * 从已选中列表中删除该LocalMedia
     * @param image
     * @return
     */
    public boolean removeSelectedImage(LocalMedia image){
        int index = selectedImages.size();
        if (index == 0){
            return false;
        }
        for (int i = 0; i < index; i++){
            LocalMedia media1 = selectedImages.get(i);
            if (media1 == null || TextUtils.isEmpty(media1.getRealPath())){
                continue;
            }
            if (media1.getRealPath().equals(image.getRealPath())){
                selectedImages.remove(media1);
                return true;
            }
        }
        return false;
    }

    public class AddImageViewHolder extends RecyclerView.ViewHolder{

        View rootView;
        public AddImageViewHolder(View itemView) {
            super(itemView);
            rootView = itemView;
        }
    }

    public class ImageItemViewHolder extends RecyclerView.ViewHolder{

        View rootView;

        ImageView picIv;
        CheckBox checkBtn;

        boolean isSelect = false;
        public ImageItemViewHolder(View itemView) {
            super(itemView);
            rootView = itemView;
            picIv = itemView.findViewById(R.id.iv_item_pic);
            checkBtn = itemView.findViewById(R.id.btn_check);
        }
    }
}
