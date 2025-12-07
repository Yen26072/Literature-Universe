package com.example.literatureuniverse.base;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.example.literatureuniverse.R;
import com.example.literatureuniverse.activity.FilterStory;
import com.example.literatureuniverse.activity.FollowingStory;
import com.example.literatureuniverse.activity.FullAppRule;
import com.example.literatureuniverse.activity.FullReview;
import com.example.literatureuniverse.activity.HomeAdmin;
import com.example.literatureuniverse.activity.HomeAdminSuper;
import com.example.literatureuniverse.activity.Library;
import com.example.literatureuniverse.activity.Login;
import com.example.literatureuniverse.activity.MailBox;
import com.example.literatureuniverse.activity.MainActivity;
import com.example.literatureuniverse.activity.ManageAdmin;
import com.example.literatureuniverse.activity.ManageAppRules;
import com.example.literatureuniverse.activity.MyProfile;
import com.example.literatureuniverse.activity.MyStory;
import com.example.literatureuniverse.activity.Reading;
import com.example.literatureuniverse.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class BaseActivity extends AppCompatActivity {

    protected ImageView avatarImageView, imageView;
    protected TextView txtLogin;
    protected String currentRole = null;
    private PopupMenu popup;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Các Activity con sẽ gọi super.onCreate rồi setContentView riêng
    }

    @Override
    protected void onStart() {
        super.onStart();
    }
    @Override
    protected void onDestroy() {
        if (popup != null) {
            popup.dismiss(); // không bắt buộc nhưng an toàn nếu đang hiển thị
            popup = null;
        }
        super.onDestroy();
    }

    protected void setupHeader() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        Log.d("BaseActivity", "setupHeader called");
        avatarImageView = findViewById(R.id.imgAvatar);
        txtLogin = findViewById(R.id.txtLoginMain);
        imageView = findViewById(R.id.imageView);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            if(imageView != null){
                imageView.setOnClickListener( v -> showPopupMenuMenu(v));
            }
            // Chưa đăng nhập → ẩn avatar, hiện nút đăng nhập
            if (avatarImageView != null) avatarImageView.setVisibility(View.GONE);
            if (txtLogin != null) txtLogin.setVisibility(View.VISIBLE);
            txtLogin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(BaseActivity.this, Login.class);
                    startActivity(intent);
                }
            });
            return;
        }

        if (avatarImageView != null) {
            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            if(uid != null){
                avatarImageView.setVisibility(View.VISIBLE);
                txtLogin.setVisibility(View.GONE);
                DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(uid);

                userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            currentRole = snapshot.child("role").getValue(String.class);
                            String avatarUrl = snapshot.child("avatarUrl").getValue(String.class);
                            if (avatarUrl != null && !avatarUrl.isEmpty()) {
                                Glide.with(BaseActivity.this).load(avatarUrl).circleCrop().into(avatarImageView);
                            }
                        }
                        onRoleLoaded(currentRole);
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                    }
                });
            }
            else{
                avatarImageView.setVisibility(View.GONE);
                txtLogin.setVisibility(View.VISIBLE);
            }

        }

        if (avatarImageView != null) {
            avatarImageView.setOnClickListener(v -> showPopupMenuAvatar(v));
        }
        if(imageView != null){
            imageView.setOnClickListener( v -> showPopupMenuMenu(v));
        }
    }

    protected void onRoleLoaded(String currentRole) {
//        if ("admin_super".equals(currentRole) && !(this instanceof HomeAdminSuper)) {
//            Intent intent = new Intent(BaseActivity.this, HomeAdminSuper.class);
//            startActivity(intent);
//            finish();
//        } else {
//             Nếu là reader thì cho hiển thị giao diện bình thường
//             hoặc setup layout reader ở đây
//        }
    }

    protected void showPopupMenuMenu(View view){
        PopupMenu popup = new PopupMenu(this, view);
        MenuInflater inflater = popup.getMenuInflater();

        if ("admin_super".equals(currentRole)){
            inflater.inflate(R.menu.header_menu_admin_super, popup.getMenu());
        }
        if("admin".equals(currentRole)){
            inflater.inflate(R.menu.header_menu_admin, popup.getMenu());
        }
        if("reader".equals(currentRole) || "author".equals(currentRole) || FirebaseAuth.getInstance().getCurrentUser() == null){
            inflater.inflate(R.menu.menu_left, popup.getMenu());
        }

        popup.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if(id == R.id.menu_home2){
                Intent intent = new Intent(BaseActivity.this, MainActivity.class);
                startActivity(intent);
                return true;
            }
            if(id == R.id.menu_home1){
                Intent intent = new Intent(BaseActivity.this, HomeAdminSuper.class);
                startActivity(intent);
                return true;
            }
            if(id == R.id.menu_filter){
                Intent intent = new Intent(BaseActivity.this, FilterStory.class);
                startActivity(intent);
                return true;
            }
            if(id == R.id.menu_review){
                Intent intent = new Intent(BaseActivity.this, FullReview.class);
                startActivity(intent);
                return true;
            }
            if(id == R.id.menu_regulation){
                Intent intent = new Intent(BaseActivity.this, ManageAppRules.class);
                startActivity(intent);
                return true;
            }
            if(id == R.id.menu_regulation2){
                Intent intent = new Intent(BaseActivity.this, FullAppRule.class);
                startActivity(intent);
                return true;
            }
            if(id == R.id.menu_regulation3){
                Intent intent = new Intent(BaseActivity.this, FullAppRule.class);
                startActivity(intent);
                return true;
            }
            if(id == R.id.menu_home3){
                Intent intent = new Intent(BaseActivity.this, HomeAdmin.class);
                startActivity(intent);
                return true;
            }
            if(id == R.id.menu_admin_mana){
                Intent intent = new Intent(BaseActivity.this, ManageAdmin.class);
                startActivity(intent);
                return true;
            }
            return true;
        });

        popup.show();
    }

    protected void showPopupMenuAvatar(View view) {
        if (!isFinishing() && !isDestroyed()) {
            popup = new PopupMenu(this, view);
            popup.getMenuInflater().inflate(R.menu.header_menu_avatar, popup.getMenu());
        }

        // Nếu là admin_super thì ẩn các mục khác ngoài Logout
        if ("admin_super".equals(currentRole)) {
            popup.getMenu().findItem(R.id.menu_profile).setVisible(false);
            popup.getMenu().findItem(R.id.menu_mailbox).setVisible(false);
            popup.getMenu().findItem(R.id.menu_follow).setVisible(false);
            popup.getMenu().findItem(R.id.menu_library).setVisible(false);
            popup.getMenu().findItem(R.id.menu_reading).setVisible(false);
            popup.getMenu().findItem(R.id.menu_mystory).setVisible(false);
        }
        if ("admin".equals(currentRole)) {
            popup.getMenu().findItem(R.id.menu_mailbox).setVisible(false);
            popup.getMenu().findItem(R.id.menu_follow).setVisible(false);
            popup.getMenu().findItem(R.id.menu_library).setVisible(false);
            popup.getMenu().findItem(R.id.menu_reading).setVisible(false);
            popup.getMenu().findItem(R.id.menu_mystory).setVisible(false);
        }

        popup.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if(id == R.id.menu_logout){
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(BaseActivity.this, MainActivity.class));
                finish();
                return true;
            }
            if(id==R.id.menu_mailbox){
                Intent intent = new Intent(BaseActivity.this, MailBox.class);
                startActivity(intent);
                return true;
            }
            if(id==R.id.menu_mystory){
                Intent intent = new Intent(BaseActivity.this, MyStory.class);
                startActivity(intent);
                return true;
            }
            if(id==R.id.menu_follow){
                Intent intent = new Intent(BaseActivity.this, FollowingStory.class);
                startActivity(intent);
                return true;
            }
            if(id==R.id.menu_reading){
                Intent intent = new Intent(BaseActivity.this, Reading.class);
                startActivity(intent);
                return true;
            }
            if(id==R.id.menu_library){
                Intent intent = new Intent(BaseActivity.this, Library.class);
                startActivity(intent);
                return true;
            }
            if(id==R.id.menu_profile){
                Intent intent = new Intent(BaseActivity.this, MyProfile.class);
                startActivity(intent);
                return true;
            }
            return true;
        });

        popup.show();
    }
}
