package com.trungngo.xanhandsach.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
import com.trungngo.xanhandsach.Adapter.RequestAdapter;
import com.trungngo.xanhandsach.Callback.FirebaseCallback;
import com.trungngo.xanhandsach.Dto.SiteDto;
import com.trungngo.xanhandsach.Dto.UserDto;
import com.trungngo.xanhandsach.Model.Notification;
import com.trungngo.xanhandsach.Model.Request;
import com.trungngo.xanhandsach.R;
import com.trungngo.xanhandsach.Service.SiteService;
import com.trungngo.xanhandsach.Service.UserService;
import com.trungngo.xanhandsach.Shared.ImageHandler;
import com.trungngo.xanhandsach.Shared.Result;
import com.trungngo.xanhandsach.databinding.ActivityViewRequestBinding;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ViewRequestActivity extends AppCompatActivity
    implements RequestAdapter.OnAcceptClick, RequestAdapter.OnRejectClick {
  private ActivityViewRequestBinding activityViewRequestBinding;
  private DrawerLayout drawerLayout;
  private NavigationView navigationView;

  private RequestAdapter requestAdapter;
  private UserService userService;
  private SiteService siteService;

  private List<Request> requests;
  private SiteDto mSite;

  @Override
  protected void onCreate(Bundle savedInstanceState) {

    super.onCreate(savedInstanceState);
    activityViewRequestBinding = ActivityViewRequestBinding.inflate(getLayoutInflater());
    setContentView(activityViewRequestBinding.getRoot());
    siteService = new SiteService(this);
    userService = new UserService(this, siteService);
    requestAdapter = new RequestAdapter(ViewRequestActivity.this, this, this);
    activityViewRequestBinding.requestList.setLayoutManager(
        new LinearLayoutManager(getApplicationContext()));
    setUpView();
    setUpDrawer();
  }

  private void setUpView() {

    siteService.getOneSite(
        userService.getCurrentUser().getSiteId(),
        new FirebaseCallback<Result<SiteDto>>() {
          @Override
          public void callbackListRes(List<Result<SiteDto>> listT) {}

          @Override
          public void callbackRes(Result<SiteDto> siteDtoResult) {
            if (siteDtoResult instanceof Result.Success) {
              SiteDto siteDto = ((Result.Success<SiteDto>) siteDtoResult).getData();
              requests = siteDto.getRequests();
              if (requests == null || requests.isEmpty()) {
                activityViewRequestBinding.noVolunteer.setVisibility(View.VISIBLE);
              } else {
                activityViewRequestBinding.noVolunteer.setVisibility(View.GONE);
              }
              requestAdapter.setData(requests);
              activityViewRequestBinding.requestList.setAdapter(requestAdapter);
              mSite = siteDto;
            }
          }
        });
  }

  private void setUpDrawer() {
    drawerLayout = activityViewRequestBinding.drawer;
    navigationView = activityViewRequestBinding.navView;
    activityViewRequestBinding.toolbarId.toolbarTitleId.setText("Notifications");
    activityViewRequestBinding.toolbarId.backIcon.setVisibility(View.VISIBLE);
    activityViewRequestBinding.toolbarId.backIcon.setOnClickListener(
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
    setSupportActionBar(activityViewRequestBinding.toolbarId.menuIconId);
    //    navigationView.bringToFront();
    ActionBarDrawerToggle drawerToggle =
        new ActionBarDrawerToggle(
            this, drawerLayout, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
    drawerLayout.addDrawerListener(drawerToggle);
    //    drawerToggle.syncState();
    navigationView.setCheckedItem(R.id.nav_chat);
    activityViewRequestBinding.toolbarId.menuIconId.setOnClickListener(
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
              Intent intent = new Intent(ViewRequestActivity.this, MainActivity.class);
              startActivity(intent);
            } else if (menuItem.getItemId() == R.id.nav_site) {
              Intent intent = new Intent(ViewRequestActivity.this, AddSiteActivity.class);
              startActivity(intent);
            } else if (menuItem.getItemId() == R.id.nav_chat) {
              return false;
            } else if (menuItem.getItemId() == R.id.nav_noti) {
              Intent intent = new Intent(ViewRequestActivity.this, NotificationActivity.class);
              startActivity(intent);
            } else if (menuItem.getItemId() == R.id.nav_about_us) {
              Intent intent = new Intent(ViewRequestActivity.this, AboutUsActivity.class);
              startActivity(intent);
            } else if (menuItem.getItemId() == R.id.nav_logout) {
              Intent intent = new Intent(ViewRequestActivity.this, SignInActivity.class);
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

  @Override
  public void clickAccept(int position) {
    Request request = requests.get(position);
    mSite.getVolunteers().add(request.getVolunteers());
    userService.acceptRequest(
        request.getVolunteers(),
        mSite,
        new FirebaseCallback<Result<List<UserDto>>>() {
          @Override
          public void callbackListRes(List<Result<List<UserDto>>> listT) {}

          @Override
          public void callbackRes(Result<List<UserDto>> listResult) {
            if (listResult instanceof Result.Success) {
              Toast.makeText(ViewRequestActivity.this, "Accept successfully!", Toast.LENGTH_SHORT)
                  .show();
              Notification notification =
                  Notification.builder()
                      .title("Dear, " + request.getVolunteers().getDisplayName())
                      .body(
                          userService.getCurrentUser().getDisplayName()
                              + " has just accepted your joining request!\n"
                              + "Congratulations!!!")
                      .fcmToken(request.getVolunteers().getFcmToken())
                      .createdDate(new Date())
                      .build();

              userService.sendNotification(
                  notification,
                  request.getVolunteers().getId(),
                  new FirebaseCallback<Result<Notification>>() {
                    @Override
                    public void callbackListRes(List<Result<Notification>> listT) {}

                    @Override
                    public void callbackRes(Result<Notification> notificationResult) {
                      if (notificationResult instanceof Result.Success) {
                        Log.d("noti updates", "success");
                      } else {
                        Log.d("noti updates", "Error");
                      }
                    }
                  });
              requests.remove(position);
              requestAdapter.setData(requests);
              siteService.updateAllRequests(
                  userService.getCurrentUser().getSiteId(),
                  requests,
                  new FirebaseCallback<Result<List<Request>>>() {
                    @Override
                    public void callbackListRes(List<Result<List<Request>>> listT) {}

                    @Override
                    public void callbackRes(Result<List<Request>> listResult) {
                      if (listResult instanceof Result.Success) {
                        Log.d("request updates", "success");
                      } else {
                        Log.d("request updates", "Failed");
                      }
                    }
                  });
            } else {
              Toast.makeText(ViewRequestActivity.this, "Something wrong!", Toast.LENGTH_SHORT)
                  .show();
            }
          }
        });
  }

  @Override
  public void clickReject(int position) {
    Request request = requests.get(position);
    requests.remove(position);
    requestAdapter.setData(requests);
    siteService.updateAllRequests(
        userService.getCurrentUser().getSiteId(),
        requests,
        new FirebaseCallback<Result<List<Request>>>() {
          @Override
          public void callbackListRes(List<Result<List<Request>>> listT) {}

          @Override
          public void callbackRes(Result<List<Request>> listResult) {
            if (listResult instanceof Result.Success) {
              Toast.makeText(ViewRequestActivity.this, "Reject successfully!", Toast.LENGTH_SHORT)
                  .show();
              Notification notification =
                  Notification.builder()
                      .title("Dear, " + request.getVolunteers().getDisplayName())
                      .body(
                          userService.getCurrentUser().getDisplayName()
                              + " has just rejected your joining request!\n"
                              + "Don't be sad =<<")
                      .fcmToken(request.getVolunteers().getFcmToken())
                      .createdDate(new Date())
                      .build();

              userService.sendNotification(
                  notification,
                  request.getVolunteers().getId(),
                  new FirebaseCallback<Result<Notification>>() {
                    @Override
                    public void callbackListRes(List<Result<Notification>> listT) {}

                    @Override
                    public void callbackRes(Result<Notification> notificationResult) {
                      if (notificationResult instanceof Result.Success) {
                        Log.d("noti updates", "success");
                      } else {
                        Log.d("noti updates", "Error");
                      }
                    }
                  });
            } else {
              Toast.makeText(ViewRequestActivity.this, "Something wrong!", Toast.LENGTH_SHORT)
                  .show();
            }
          }
        });
  }
}
