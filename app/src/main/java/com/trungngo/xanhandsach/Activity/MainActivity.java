package com.trungngo.xanhandsach.Activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.squareup.picasso.Picasso;
import com.trungngo.xanhandsach.Adapter.MainTabAdapter;
import com.trungngo.xanhandsach.R;
import com.trungngo.xanhandsach.Service.UserService;
import com.trungngo.xanhandsach.Shared.ImageHandler;
import com.trungngo.xanhandsach.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {
  private ActivityMainBinding activityMainBinding;
  private DrawerLayout drawerLayout;
  private NavigationView navigationView;
  private UserService userService;

  private MainTabAdapter mainTabAdapter;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    activityMainBinding = ActivityMainBinding.inflate(getLayoutInflater());
    setContentView(activityMainBinding.getRoot());
    userService = new UserService(this);
    mainTabAdapter = new MainTabAdapter(getSupportFragmentManager());
    activityMainBinding.pageContent.setAdapter(mainTabAdapter);
    activityMainBinding.pageContent.addOnPageChangeListener(
        new ViewPager.OnPageChangeListener() {
          @Override
          public void onPageScrolled(
              int position, float positionOffset, int positionOffsetPixels) {}

          @Override
          public void onPageSelected(int position) {
            activityMainBinding.tabLayout.getTabAt(position).select();
          }

          @Override
          public void onPageScrollStateChanged(int state) {}
        });

    activityMainBinding.tabLayout.addOnTabSelectedListener(
        new TabLayout.OnTabSelectedListener() {
          @Override
          public void onTabSelected(TabLayout.Tab tab) {
            activityMainBinding.pageContent.setCurrentItem(tab.getPosition());
          }

          @Override
          public void onTabUnselected(TabLayout.Tab tab) {}

          @Override
          public void onTabReselected(TabLayout.Tab tab) {}
        });

    if (userService.getCurrentUser().getPermission().equals("super")) {
      setUpAdminDrawer();
    } else {
      setUpDrawer();
    }
  }

  private void generalDrawerSetUp() {
    drawerLayout = activityMainBinding.drawer;
    navigationView = activityMainBinding.navView;
    activityMainBinding.toolbarId.toolbarTitleId.setText("Home");
    activityMainBinding.toolbarId.backIcon.setVisibility(View.VISIBLE);

    View headerView = navigationView.getHeaderView(0);
    TextView currentUserName = headerView.findViewById(R.id.displayName);
    TextView currentUserEmail = headerView.findViewById(R.id.email);
    ImageView currentUserImg = headerView.findViewById(R.id.userImage);
    drawerLayout.closeDrawer(GravityCompat.START);

    navigationView.bringToFront();
    activityMainBinding.toolbarId.backIcon.setOnClickListener(
        view -> {
          finish();
        });
    currentUserName.setText(userService.getCurrentUser().getDisplayName());
    currentUserEmail.setText(userService.getCurrentUser().getEmail());
    ImageHandler.setImage(
        ImageHandler.stringImageToBitMap(userService.getCurrentUser().getImage()), currentUserImg);
    setSupportActionBar(activityMainBinding.toolbarId.menuIconId);
    ActionBarDrawerToggle drawerToggle =
        new ActionBarDrawerToggle(
            this, drawerLayout, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
    drawerLayout.addDrawerListener(drawerToggle);

    activityMainBinding.toolbarId.menuIconId.setOnClickListener(
        view -> {
          if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
          } else {
            drawerLayout.openDrawer(GravityCompat.START);
          }
        });
  }

  private void setUpAdminDrawer() {
    generalDrawerSetUp();
    navigationView.getMenu().clear();
    navigationView.inflateMenu(R.menu.admin_menu);
    navigationView.setCheckedItem(R.id.nav_home);

    navigationView.setNavigationItemSelectedListener(
        new NavigationView.OnNavigationItemSelectedListener() {
          @Override
          public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
            if (menuItem.getItemId() == R.id.nav_home) {
              return false;
            } else if (menuItem.getItemId() == R.id.nav_site) {
              Intent intent = new Intent(MainActivity.this, AddSiteActivity.class);
              startActivity(intent);
            } else if (menuItem.getItemId() == R.id.nav_chat) {
              Intent intent = new Intent(MainActivity.this, ViewRequestActivity.class);
              startActivity(intent);
            } else if (menuItem.getItemId() == R.id.nav_about_us) {
              Intent intent = new Intent(MainActivity.this, AboutUsActivity.class);
              startActivity(intent);
            } else if (menuItem.getItemId() == R.id.nav_logout) {
              Intent intent = new Intent(MainActivity.this, SignInActivity.class);
              userService.signOut();
              finish();
              startActivity(intent);
            }
            return false;
          }
        });
  }

  private void setUpDrawer() {
    generalDrawerSetUp();
    navigationView.getMenu().clear();
    navigationView.inflateMenu(R.menu.main_menu);
    navigationView.setCheckedItem(R.id.nav_home);

    navigationView.setNavigationItemSelectedListener(
        new NavigationView.OnNavigationItemSelectedListener() {
          @Override
          public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
            if (menuItem.getItemId() == R.id.nav_home) {
              return false;
            } else if (menuItem.getItemId() == R.id.nav_site) {
              Intent intent = new Intent(MainActivity.this, AddSiteActivity.class);
              startActivity(intent);
            } else if (menuItem.getItemId() == R.id.nav_chat) {
              Intent intent = new Intent(MainActivity.this, ViewRequestActivity.class);
              startActivity(intent);

            } else if (menuItem.getItemId() == R.id.nav_noti) {
              Intent intent = new Intent(MainActivity.this, NotificationActivity.class);
              startActivity(intent);
            } else if (menuItem.getItemId() == R.id.nav_about_us) {
              Intent intent = new Intent(MainActivity.this, AboutUsActivity.class);
              startActivity(intent);
            } else if (menuItem.getItemId() == R.id.nav_logout) {
              Intent intent = new Intent(MainActivity.this, SignInActivity.class);
              userService.signOut();
              finish();
              startActivity(intent);
            }
            return false;
          }
        });
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
