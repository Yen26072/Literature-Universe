package com.example.literatureuniverse.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.literatureuniverse.fragment.CommentFragment;
import com.example.literatureuniverse.fragment.InboxFragment;
import com.example.literatureuniverse.fragment.NotificationFragment;

public class ViewPagerAdapter extends FragmentStateAdapter {
    public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position){
            case 0:
                return new InboxFragment();
            case 1:
                return new CommentFragment();
            case 2:
                return new NotificationFragment();
            default:
                return new InboxFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}
