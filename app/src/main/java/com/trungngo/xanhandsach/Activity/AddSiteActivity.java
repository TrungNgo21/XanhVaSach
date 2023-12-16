package com.trungngo.xanhandsach.Activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.trungngo.xanhandsach.Adapter.CreateSiteAdapter;
import com.trungngo.xanhandsach.Adapter.MainTabAdapter;
import com.trungngo.xanhandsach.R;
import com.trungngo.xanhandsach.Service.UserService;
import com.trungngo.xanhandsach.Shared.ImageHandler;
import com.trungngo.xanhandsach.databinding.ActivityAddSiteBinding;

public class AddSiteActivity extends AppCompatActivity {
  private ActivityAddSiteBinding activityAddSiteBinding;

  private CreateSiteAdapter createSiteAdapter;

  private DrawerLayout drawerLayout;
  private NavigationView navigationView;
  private UserService userService;

  @Override
  protected void onCreate(Bundle savedInstanceState) {

    super.onCreate(savedInstanceState);
    userService = new UserService(this);
    //    userService.signOut();
    activityAddSiteBinding = ActivityAddSiteBinding.inflate(getLayoutInflater());
    setContentView(activityAddSiteBinding.getRoot());
    createSiteAdapter = new CreateSiteAdapter(this);
    activityAddSiteBinding.pageContent.setAdapter(createSiteAdapter);

    setUpDrawer();

    activityAddSiteBinding.tabLayout.addOnTabSelectedListener(
        new TabLayout.OnTabSelectedListener() {
          @Override
          public void onTabSelected(TabLayout.Tab tab) {
            activityAddSiteBinding.pageContent.setCurrentItem(tab.getPosition());
          }

          @Override
          public void onTabUnselected(TabLayout.Tab tab) {}

          @Override
          public void onTabReselected(TabLayout.Tab tab) {}
        });
    activityAddSiteBinding.pageContent.registerOnPageChangeCallback(
        new ViewPager2.OnPageChangeCallback() {
          @Override
          public void onPageSelected(int position) {
            super.onPageSelected(position);
            activityAddSiteBinding.tabLayout.getTabAt(position).select();
          }
        });
  }

  private void setUpDrawer() {
    drawerLayout = activityAddSiteBinding.drawer;
    navigationView = activityAddSiteBinding.navView;
    activityAddSiteBinding.toolbarId.toolbarTitleId.setText("My Site");
    activityAddSiteBinding.toolbarId.backIcon.setVisibility(View.GONE);

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
    setSupportActionBar(activityAddSiteBinding.toolbarId.menuIconId);
    //    navigationView.bringToFront();
    ActionBarDrawerToggle drawerToggle =
        new ActionBarDrawerToggle(
            this, drawerLayout, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
    drawerLayout.addDrawerListener(drawerToggle);
    //    drawerToggle.syncState();
    navigationView.setCheckedItem(R.id.nav_site);
    activityAddSiteBinding.toolbarId.menuIconId.setOnClickListener(
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
              Intent intent = new Intent(AddSiteActivity.this, MainActivity.class);
              startActivity(intent);
            } else if (menuItem.getItemId() == R.id.nav_site) {
              return false;
            } else if (menuItem.getItemId() == R.id.nav_chat) {
              Intent intent = new Intent(AddSiteActivity.this, ViewRequestActivity.class);
              startActivity(intent);
            } else if (menuItem.getItemId() == R.id.nav_about_us) {
              Intent intent = new Intent(AddSiteActivity.this, AboutUsActivity.class);
              startActivity(intent);
            } else if (menuItem.getItemId() == R.id.nav_noti) {
              Intent intent = new Intent(AddSiteActivity.this, NotificationActivity.class);
              startActivity(intent);
            } else if (menuItem.getItemId() == R.id.nav_logout) {
              Intent intent = new Intent(AddSiteActivity.this, SignInActivity.class);
              userService.signOut();
              finish();

              startActivity(intent);
            }
            return false;
          }
        });
  }

  public void switchToPage(int pageIndex) {
    if (activityAddSiteBinding.pageContent != null
        && pageIndex >= 0
        && pageIndex < activityAddSiteBinding.pageContent.getAdapter().getItemCount()) {
      activityAddSiteBinding.pageContent.setCurrentItem(pageIndex);
    }
  }

  @Override
  public void onBackPressed() {
    if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
      drawerLayout.closeDrawer(GravityCompat.START);
    } else {
      super.onBackPressed();
    }
  }
}
