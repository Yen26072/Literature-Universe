package com.example.literatureuniverse.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;

import com.example.literatureuniverse.R;
import com.example.literatureuniverse.model.Tag;

import java.util.ArrayList;
import java.util.List;

public class TagCheckboxAdapterForDetail extends BaseAdapter {
    private Context context;
    private List<Tag> tags;
    private List<String> selectedTagIds; // danh sách tagId của truyện
    boolean[] checkedStates;

    public TagCheckboxAdapterForDetail(Context context, List<Tag> tags, List<String> selectedTagIds) {
        this.context = context;
        this.tags = tags;
        this.selectedTagIds = selectedTagIds;
        checkedStates = new boolean[tags.size()];
        for (int i = 0; i < tags.size(); i++) {
            checkedStates[i] = selectedTagIds.contains(tags.get(i).getId());
        }
    }

    public List<Tag> getCheckedTags() {
        List<Tag> selected = new ArrayList<>();
        for (int i = 0; i < tags.size(); i++) {
            if (checkedStates[i]) selected.add(tags.get(i));
        }
        return selected;
    }

    public void setCheckedTags(List<String> selectedTagIds) {
        this.selectedTagIds = selectedTagIds;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return tags.size();
    }

    @Override
    public Object getItem(int position) {
        return tags.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public List<String> getSelectedTagIds() {
        return selectedTagIds;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder holder;

        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.checkbox_item, viewGroup, false);
            holder = new ViewHolder();
            holder.checkBox = view.findViewById(R.id.checkBox);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        Tag tag = tags.get(i);
        holder.checkBox.setText(tag.getLabel());

        // Bỏ listener cũ nếu có để tránh lỗi khi tái sử dụng view
        holder.checkBox.setOnCheckedChangeListener(null);

        holder.checkBox.setChecked(checkedStates[i]);

        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            checkedStates[i] = isChecked;
        });

        holder.checkBox.setChecked(selectedTagIds.contains(tag.getId()));

        return view;
    }

    static class ViewHolder {
        CheckBox checkBox;
    }

}

