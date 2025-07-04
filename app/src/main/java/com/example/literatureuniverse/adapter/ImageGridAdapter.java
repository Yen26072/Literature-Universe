package com.example.literatureuniverse.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.literatureuniverse.R;

import java.util.List;

public class ImageGridAdapter extends BaseAdapter {
    private final Context context;
    private final List<String> imageUrls;
    private final OnImageClickListener listener;

    public ImageGridAdapter(Context context, List<String> imageUrls, OnImageClickListener listener) {
        this.context = context;
        this.imageUrls = imageUrls;
        this.listener = listener;
    }

    @Override
    public int getCount() {
        return imageUrls.size();
    }

    @Override
    public Object getItem(int position) {
        return imageUrls.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public interface OnImageClickListener {
        void onImageClick(String imageUrl);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_image_grid, parent, false);
            imageView = convertView.findViewById(R.id.imageViewGridItem);
            convertView.setTag(imageView);
        } else {
            imageView = (ImageView) convertView.getTag();
        }

        Glide.with(context)
                .load(imageUrls.get(position))
                .centerCrop()
                .into(imageView);

        imageView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onImageClick(imageUrls.get(position));
            }
        });

        return convertView;
    }
}
