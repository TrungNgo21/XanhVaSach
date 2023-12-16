package com.trungngo.xanhandsach.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.navigation.NavigationView;
import com.trungngo.xanhandsach.Adapter.NotiAdapter;
import com.trungngo.xanhandsach.Callback.FirebaseCallback;
import com.trungngo.xanhandsach.Dto.UserDto;
import com.trungngo.xanhandsach.Model.Notification;
import com.trungngo.xanhandsach.R;
import com.trungngo.xanhandsach.Service.UserService;
import com.trungngo.xanhandsach.Shared.ImageHandler;
import com.trungngo.xanhandsach.Shared.Result;
import com.trungngo.xanhandsach.databinding.ActivityNotificationBinding;

import java.util.List;

public class NotificationActivity extends AppCompatActivity
    implements NotiAdapter.OnDeleteButton, NotiAdapter.OnRequestButton {
  private ActivityNotificationBinding activityNotificationBinding;
  private DrawerLayout drawerLayout;
  private NavigationView navigationView;
  private NotiAdapter notiAdapter;
  private UserService userService;

  private List<Notification> notifications;
  private UserDto targetUser;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    activityNotificationBinding = ActivityNotificationBinding.inflate(getLayoutInflater());
    setContentView(activityNotificationBinding.getRoot());
    userService = new UserService(this);
    notiAdapter = new NotiAdapter(NotificationActivity.this, this, this);
    activityNotificationBinding.notiList.setLayoutManager(
        new LinearLayoutManager(getApplicationContext()));
    setUpDrawer();
    setUpView();
  }

  private void setUpView() {
    userService.getNotifications(
        userService.getCurrentUser().getId(),
        new FirebaseCallback<Result<UserDto>>() {
          @Override
          public void callbackListRes(List<Result<UserDto>> listT) {}

          @Override
          public void callbackRes(Result<UserDto> userDtoResult) {
            if (userDtoResult instanceof Result.Success) {
              UserDto userDto = ((Result.Success<UserDto>) userDtoResult).getData();
              targetUser = userDto;
              notifications = userDto.getNotifications();
              notiAdapter.setData(notifications);
              activityNotificationBinding.notiList.setAdapter(notiAdapter);
            }
          }
        });
  }

  private void setUpDrawer() {
    drawerLayout = activityNotificationBinding.drawer;
    navigationView = activityNotificationBinding.navView;
    activityNotificationBinding.toolbarId.toolbarTitleId.setText("Notifications");
    activityNotificationBinding.toolbarId.backIcon.setVisibility(View.VISIBLE);
    activityNotificationBinding.toolbarId.backIcon.setOnClickListener(
        view -> {
          finish();
        });

    View headerView = navigationView.getHeaderView(0);
    TextView currentUserName = headerView.findViewById(R.id.displayName);
    TextView currentUserEmail = headerView.findViewById(R.id.email);
    ImageView currentUserImg = headerView.findViewById(R.id.userImage);
    drawerLayout.closeDrawer(GravityCompat.START);

    navigationView.bringToFront();

    currentUserName.setText(userService.getCurrentUser().getDisplayName());
    currentUserEmail.setText(userService.getCurrentUser().getEmail());
    ImageHandler.setImage(
        ImageHandler.stringImageToBitMap(userService.getCurrentUser().getImage()), currentUserImg);
    setSupportActionBar(activityNotificationBinding.toolbarId.menuIconId);
    //    navigationView.bringToFront();
    ActionBarDrawerToggle drawerToggle =
        new ActionBarDrawerToggle(
            this, drawerLayout, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
    drawerLayout.addDrawerListener(drawerToggle);
    //    drawerToggle.syncState();
    navigationView.setCheckedItem(R.id.nav_noti);
    activityNotificationBinding.toolbarId.menuIconId.setOnClickListener(
        view -> {
          if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
          } else {
            drawerLayout.openDrawer(GravityCompat.START);
          }
        });

    navigationView.setNavigationItemSelectedListener(
        new NavigationView.OnNavigationItemSelectedListener() {
          @Override
          public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
            if (menuItem.getItemId() == R.id.nav_home) {
              Intent intent = new Intent(NotificationActivity.this, MainActivity.class);
              startActivity(intent);
            } else if (menuItem.getItemId() == R.id.nav_site) {
              Intent intent = new Intent(NotificationActivity.this, AddSiteActivity.class);
              startActivity(intent);
            } else if (menuItem.getItemId() == R.id.nav_chat) {
              Intent intent = new Intent(NotificationActivity.this, ViewRequestActivity.class);
              startActivity(intent);
            } else if (menuItem.getItemId() == R.id.nav_about_us) {
              Intent intent = new Intent(NotificationActivity.this, AboutUsActivity.class);
              startActivity(intent);
            } else if (menuItem.getItemId() == R.id.nav_noti) {
              return false;
            } else if (menuItem.getItemId() == R.id.nav_logout) {
              Intent intent = new Intent(NotificationActivity.this, SignInActivity.class);
              userService.signOut();
              finish();

              startActivity(intent);
            }
            return false;
          }
        });
  }

  @Override
  public void toRequest() {}

  @Override
  public void deleteItem(int position) {
    notifications.remove(position);
    notiAdapter.setData(notifications);
    userService.updateNotifications(
        userService.getCurrentUser().getId(),
        notifications,
        new FirebaseCallback<Result<List<Notification>>>() {
          @Override
          public void callbackListRes(List<Result<List<Notification>>> listT) {}

          @Override
          public void callbackRes(Result<List<Notification>> listResult) {
            if (listResult instanceof Result.Success) {
              Toast.makeText(NotificationActivity.this, "Delete successfully", Toast.LENGTH_SHORT)
                  .show();
            } else {
              Toast.makeText(NotificationActivity.this, "Something wrong!", Toast.LENGTH_SHORT)
                  .show();
            }
          }
        });
  }
}
