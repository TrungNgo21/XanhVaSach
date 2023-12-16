package com.trungngo.xanhandsach.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.trungngo.xanhandsach.R;
import com.trungngo.xanhandsach.Service.UserService;
import com.trungngo.xanhandsach.Shared.ImageHandler;
import com.trungngo.xanhandsach.databinding.ActivityAboutUsBinding;

public class AboutUsActivity extends AppCompatActivity {
  private ActivityAboutUsBinding activityAboutUsBinding;
  private DrawerLayout drawerLayout;
  private NavigationView navigationView;
  private UserService userService;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    userService = new UserService(this);

    activityAboutUsBinding = ActivityAboutUsBinding.inflate(getLayoutInflater());
    setContentView(activityAboutUsBinding.getRoot());
    setUpDrawer();
  }

  private void setUpDrawer() {
    drawerLayout = activityAboutUsBinding.drawer;
    navigationView = activityAboutUsBinding.navView;
    activityAboutUsBinding.toolbarId.toolbarTitleId.setText("About us");
    activityAboutUsBinding.toolbarId.backIcon.setVisibility(View.VISIBLE);

    View headerView = navigationView.getHeaderView(0);
    TextView currentUserName = headerView.findViewById(R.id.displayName);
    TextView currentUserEmail = headerView.findViewById(R.id.email);
    ImageView currentUserImg = headerView.findViewById(R.id.userImage);
    drawerLayout.closeDrawer(GravityCompat.START);

    navigationView.bringToFront();
    activityAboutUsBinding.toolbarId.backIcon.setOnClickListener(
        view -> {
          finish();
        });
    currentUserName.setText(userService.getCurrentUser().getDisplayName());
    currentUserEmail.setText(userService.getCurrentUser().getEmail());
    ImageHandler.setImage(
        ImageHandler.stringImageToBitMap(userService.getCurrentUser().getImage()), currentUserImg);
    setSupportActionBar(activityAboutUsBinding.toolbarId.menuIconId);
    //    navigationView.bringToFront();
    ActionBarDrawerToggle drawerToggle =
        new ActionBarDrawerToggle(
            this, drawerLayout, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
    drawerLayout.addDrawerListener(drawerToggle);
    //    drawerToggle.syncState();
    navigationView.setCheckedItem(R.id.nav_site);
    activityAboutUsBinding.toolbarId.menuIconId.setOnClickListener(
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
              Intent intent = new Intent(AboutUsActivity.this, MainActivity.class);
              startActivity(intent);
            } else if (menuItem.getItemId() == R.id.nav_site) {
              Intent intent = new Intent(AboutUsActivity.this, AddSiteActivity.class);
              startActivity(intent);
            } else if (menuItem.getItemId() == R.id.nav_chat) {
              Intent intent = new Intent(AboutUsActivity.this, ViewRequestActivity.class);
              startActivity(intent);

            } else if (menuItem.getItemId() == R.id.nav_noti) {
              Intent intent = new Intent(AboutUsActivity.this, NotificationActivity.class);
              startActivity(intent);
            } else if (menuItem.getItemId() == R.id.nav_about_us) {
              return false;
            } else if (menuItem.getItemId() == R.id.nav_logout) {
              Intent intent = new Intent(AboutUsActivity.this, SignInActivity.class);
              userService.signOut();
              finish();
              startActivity(intent);
            }
            return false;
          }
        });
  }
}
