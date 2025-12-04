package com.example.literatureuniverse.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.literatureuniverse.R;
import com.example.literatureuniverse.adapter.NotificationAdapter;
import com.example.literatureuniverse.model.Notification;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NotificationFragment extends Fragment {
    private RecyclerView rcvNotification;
    private NotificationAdapter adapter;
    private List<Notification> notificationList;

    private DatabaseReference notifRef;
    private String currentUserId;

    public NotificationFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_notification, container, false);

        rcvNotification = view.findViewById(R.id.rcvNotification);

        notificationList = new ArrayList<>();
        rcvNotification.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new NotificationAdapter(getContext(), new ArrayList<>());
        rcvNotification.setAdapter(adapter);

        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        notifRef = FirebaseDatabase.getInstance().getReference("notifications").child(currentUserId);

//        loadNotifications();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadNotifications(); // reload danh sách từ Firebase
    }

    private void loadNotifications() {
        notifRef.orderByChild("timestamp").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                notificationList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Notification notification = ds.getValue(Notification.class);
                    if (notification != null) notificationList.add(notification);
                }

                // Sắp xếp mới → cũ
                Collections.sort(notificationList, (n1, n2) -> Long.compare(n2.getTimestamp(), n1.getTimestamp()));

                adapter.setData(notificationList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("NotificationFragment", "loadNotifications: " + error.getMessage());
            }
        });
    }
}