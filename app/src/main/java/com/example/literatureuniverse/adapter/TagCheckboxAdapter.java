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

public class TagCheckboxAdapter extends BaseAdapter {
    private Context context;
    private List<Tag> tags;
    private boolean[] checkedStates;

    public TagCheckboxAdapter(Context context, List<Tag> tags) {
        this.context = context;
        this.tags = tags;
        this.checkedStates = new boolean[tags.size()];
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

    public List<Tag> getCheckedTags() {
        List<Tag> selected = new ArrayList<>();
        for (int i = 0; i < tags.size(); i++) {
            if (checkedStates[i]) selected.add(tags.get(i));
        }
        return selected;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        CheckBox checkBox;

        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.checkbox_item, viewGroup, false);
        }

        checkBox = view.findViewById(R.id.checkBox);
        checkBox.setText(tags.get(i).getLabel());
        checkBox.setChecked(checkedStates[i]);

        checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            checkedStates[i] = isChecked;
        });

        return view;
    }
}
