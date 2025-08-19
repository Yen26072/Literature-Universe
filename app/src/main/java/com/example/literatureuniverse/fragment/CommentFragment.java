package com.example.literatureuniverse.fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.literatureuniverse.R;
import com.example.literatureuniverse.activity.HomeStory;
import com.example.literatureuniverse.adapter.CommentNotificationAdapter;
import com.example.literatureuniverse.model.CommentNotification;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CommentFragment extends Fragment implements CommentNotificationAdapter.OnNotificationClickListener {
    private RecyclerView recyclerView;
    private CommentNotificationAdapter adapter;
    private List<CommentNotification> notificationList;
    private DatabaseReference commentnotificationsRef;
    private String currentUserId;

    public CommentFragment() {
        // Required empty public constructor
    }

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_comment, container, false);

        recyclerView = view.findViewById(R.id.rcvCommentNotification);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        notificationList = new ArrayList<>();
        adapter = new CommentNotificationAdapter(getContext(), notificationList, this);
        recyclerView.setAdapter(adapter);

        // Lấy userId hiện tại
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Tham chiếu Firebase
        commentnotificationsRef = FirebaseDatabase.getInstance().getReference("commentnotifications").child(currentUserId);

        loadNotifications();

        return view;
    }

    private void loadNotifications() {
        commentnotificationsRef.orderByChild("createdAt").limitToLast(50) // lấy 50 thông báo gần nhất
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        notificationList.clear();
                        for (DataSnapshot data : snapshot.getChildren()) {
                            CommentNotification noti = data.getValue(CommentNotification.class);
                            if (noti != null) {
                                notificationList.add(noti);
                            }
                        }

                        // Sắp xếp theo thời gian giảm dần
                        Collections.sort(notificationList, (o1, o2) -> Long.compare(o2.getCreatedAt(), o1.getCreatedAt()));

                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(getContext(), "Lỗi tải thông báo", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onNotificationClick(String storyId, String commentId) {
        Intent intent = new Intent(getContext(), HomeStory.class);
        intent.putExtra("storyId", storyId);
        intent.putExtra("commentnotificationId", commentId);
        startActivity(intent);
    }
}