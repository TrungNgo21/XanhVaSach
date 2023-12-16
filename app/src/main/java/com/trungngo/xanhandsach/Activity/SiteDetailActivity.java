package com.trungngo.xanhandsach.Activity;

import static android.content.ContentValues.TAG;

import static java.util.Arrays.asList;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewStub;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.material.navigation.NavigationView;
import com.trungngo.xanhandsach.Adapter.SiteAdapter;
import com.trungngo.xanhandsach.Adapter.SliderAdapter;
import com.trungngo.xanhandsach.Adapter.VolunteerAdapter;
import com.trungngo.xanhandsach.Callback.FirebaseCallback;
import com.trungngo.xanhandsach.Dto.SiteDto;
import com.trungngo.xanhandsach.Dto.UserDto;
import com.trungngo.xanhandsach.Model.Notification;
import com.trungngo.xanhandsach.Model.Request;
import com.trungngo.xanhandsach.Model.User;
import com.trungngo.xanhandsach.R;
import com.trungngo.xanhandsach.Service.SiteService;
import com.trungngo.xanhandsach.Service.UserService;
import com.trungngo.xanhandsach.Shared.Constant;
import com.trungngo.xanhandsach.Shared.ImageHandler;
import com.trungngo.xanhandsach.Shared.Result;
import com.trungngo.xanhandsach.Shared.SeverityChipHandler;
import com.trungngo.xanhandsach.databinding.ActivitySignInBinding;
import com.trungngo.xanhandsach.databinding.ActivitySiteDetailBinding;

import java.util.ArrayList;
import java.util.List;

public class SiteDetailActivity extends AppCompatActivity implements OnMapReadyCallback {
  private ActivitySiteDetailBinding siteDetailBinding;

  private SiteService siteService;

  private SupportMapFragment mapFragment;
  private View mapPanel;

  private List<String> images;

  private SiteDto siteDetail;

  private UserService userService;

  private GoogleMap map;

  private Marker marker;

  private LatLng coordinates;

  private Handler handler;

  private Runnable runnable;

  private DrawerLayout drawerLayout;
  private NavigationView navigationView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    siteDetailBinding = ActivitySiteDetailBinding.inflate(getLayoutInflater());
    setContentView(siteDetailBinding.getRoot());
    siteService = new SiteService(this);
    userService = new UserService(this, siteService);
    setUpGuestPerspective();
    setUpDrawer();
  }

  private void setUpGuestPerspective() {
    String siteId = getIntent().getStringExtra(Constant.KEY_SITE_ID);

    siteDetailBinding.progressBarHolder.setVisibility(View.VISIBLE);
    siteService.getOneSite(
        siteId,
        new FirebaseCallback<Result<SiteDto>>() {
          @Override
          public void callbackListRes(List<Result<SiteDto>> listT) {}

          @Override
          public void callbackRes(Result<SiteDto> siteDtoResult) {

            if (siteDtoResult instanceof Result.Success) {
              View chipView = siteDetailBinding.severityChip.getRoot();
              SiteDto getSite = ((Result.Success<SiteDto>) siteDtoResult).getData();
              siteDetail = getSite;
              setButtonListener();
              if (userService.getCurrentUser().getSiteId().equals(getSite.getId())
                  || userService.getCurrentUser().getPermission().equals("super")) {
                if (getSite.getVolunteers() == null || getSite.getVolunteers().isEmpty()) {
                  siteDetailBinding.volunteerSection.setVisibility(View.VISIBLE);
                  siteDetailBinding.noVolunteer.setVisibility(View.VISIBLE);
                } else {
                  siteDetailBinding.volunteerSection.setVisibility(View.VISIBLE);

                  siteDetailBinding.noVolunteer.setVisibility(View.GONE);
                  siteDetailBinding.listVolunteer.setVisibility(View.VISIBLE);
                  VolunteerAdapter volunteerAdapter = new VolunteerAdapter(SiteDetailActivity.this);
                  volunteerAdapter.setData(getSite.getVolunteers());
                  siteDetailBinding.listVolunteer.setLayoutManager(
                      new LinearLayoutManager(getApplicationContext()));
                  siteDetailBinding.listVolunteer.setAdapter(volunteerAdapter);
                }
              }
              siteDetailBinding.siteAddress.setText(getSite.getAddress());
              siteDetailBinding.createdDate.setText(getSite.getCreatedDate());
              siteDetailBinding.siteOwnerEmail.setText(getSite.getOwner().getEmail());
              siteDetailBinding.siteOwner.setText(getSite.getOwner().getDisplayName());
              siteDetailBinding.siteName.setText(getSite.getDisplayName());
              siteDetailBinding.siteDes.setText(getSite.getDescription());

              SeverityChipHandler.chipDirective(
                  getSite.getSeverity(), chipView, SiteDetailActivity.this);
              coordinates = new LatLng(getSite.getLatitude(), getSite.getLongitude());
              handler = new Handler(Looper.getMainLooper());
              runnable =
                  new Runnable() {
                    @Override
                    public void run() {
                      int position = siteDetailBinding.imageSlider.getCurrentItem();
                      if (position == getSite.getImageUrl().size() - 1) {
                        siteDetailBinding.imageSlider.setCurrentItem(0);
                      } else {
                        siteDetailBinding.imageSlider.setCurrentItem(position + 1);
                      }
                    }
                  };
              setUpSlider(getSite.getImageUrl());
              siteDetailBinding.progressBarHolder.setVisibility(View.GONE);
              showMap();
            }
          }
        });
  }

  //  @Override
  //  protected void onRestart() {
  //    super.onRestart();
  //    setUpGuestPerspective();
  //  }

  private void setButtonListener() {
    String siteId = getIntent().getStringExtra(Constant.KEY_SITE_ID);
    if (siteId.equals(userService.getCurrentUser().getSiteId())
        || userService.getCurrentUser().getPermission().equals("super")) {
      //      if (siteId == userService.getCurrentUser().getSiteId()) {
      //      }
      siteDetailBinding.pleaseHelpText.setVisibility(View.GONE);
      siteDetailBinding.submitButton.setText("Update site");
      siteDetailBinding.submitButton.setOnClickListener(
          view -> {
            finish();
            startActivity(new Intent(SiteDetailActivity.this, AddSiteActivity.class));
          });
    } else {
      siteDetailBinding.submitButton.setEnabled(
          !isApplied(
              siteDetail.getRequests(), siteDetail.getVolunteers(), userService.getCurrentUser()));
      siteDetailBinding.submitButton.setOnClickListener(
          view -> {
            userService.applyForVolunteer(
                siteDetail,
                new FirebaseCallback<Result<Request>>() {
                  @Override
                  public void callbackListRes(List<Result<Request>> listT) {}

                  @Override
                  public void callbackRes(Result<Request> requestResult) {
                    if (requestResult instanceof Result.Success) {
                      siteDetailBinding.submitButton.setEnabled(false);
                      Toast.makeText(
                              SiteDetailActivity.this,
                              "Your request sent successfully! Please wait for the owner",
                              Toast.LENGTH_SHORT)
                          .show();
                    }
                  }
                });
          });
    }
  }

  private boolean isApplied(List<Request> requests, List<UserDto> volunteers, UserDto currentUser) {
    if (requests == null) {
      return false;
    }
    for (Request requestEle : requests) {
      if (requestEle.getVolunteers().getId().equals(currentUser.getId())) {
        return true;
      }
    }
    for (UserDto volunteer : volunteers) {
      if (volunteer.getId().equals(currentUser.getId())) {
        return true;
      }
    }
    return false;
  }

  private void setUpSlider(List<String> images) {
    if (images.isEmpty()) {
      images = new ArrayList<>(asList(Constant.NO_IMG_DEFAULT));
    }
    SliderAdapter sliderAdapter = new SliderAdapter(this, images);
    siteDetailBinding.imageSlider.setAdapter(sliderAdapter);
    siteDetailBinding.sliderIndicator.setViewPager(siteDetailBinding.imageSlider);
    sliderAdapter.registerAdapterDataObserver(
        siteDetailBinding.sliderIndicator.getAdapterDataObserver());

    siteDetailBinding.imageSlider.registerOnPageChangeCallback(
        new ViewPager2.OnPageChangeCallback() {
          @Override
          public void onPageSelected(int position) {
            super.onPageSelected(position);
            handler.removeCallbacks(runnable);
            handler.postDelayed(runnable, 3000);
          }
        });
  }

  @Override
  protected void onPause() {
    super.onPause();
    if (handler == null) {
      return;
    }

    handler.removeCallbacks(runnable);
  }

  @Override
  protected void onResume() {
    super.onResume();
    if (handler == null) {
      return;
    }
    handler.postDelayed(runnable, 3000);
  }

  private void showMap() {

    mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentByTag("MAP");

    if (mapFragment == null) {
      mapPanel = ((ViewStub) findViewById(R.id.stub_map)).inflate();
      GoogleMapOptions mapOptions = new GoogleMapOptions();
      mapOptions.mapToolbarEnabled(false);
      mapFragment = SupportMapFragment.newInstance(mapOptions);
      getSupportFragmentManager()
          .beginTransaction()
          .add(R.id.confirmation_map, mapFragment, "MAP")
          .commit();
      mapFragment.getMapAsync(this);
    } else {
      updateMap(coordinates);
    }
  }

  private void updateMap(LatLng latLng) {
    marker.setPosition(latLng);
    map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18f));
    if (mapPanel.getVisibility() == View.GONE) {
      mapPanel.setVisibility(View.VISIBLE);
    }
  }

  @Override
  public void onMapReady(@NonNull GoogleMap googleMap) {
    map = googleMap;
    map.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
    try {
      boolean success =
          map.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.style_raw));

      if (!success) {
        Log.e(TAG, "Style parsing failed.");
      }
    } catch (Resources.NotFoundException e) {
      Log.e(TAG, "Can't find style. Error: ", e);
    }
    map.moveCamera(CameraUpdateFactory.newLatLngZoom(coordinates, 15f));
    marker = map.addMarker(new MarkerOptions().position(coordinates));
  }

  private void setUpDrawer() {
    drawerLayout = siteDetailBinding.drawer;
    navigationView = siteDetailBinding.navView;
    siteDetailBinding.toolbarId.toolbarTitleId.setText("Site detail");
    siteDetailBinding.toolbarId.backIcon.setOnClickListener(
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
    setSupportActionBar(siteDetailBinding.toolbarId.menuIconId);
    //    navigationView.bringToFront();
    ActionBarDrawerToggle drawerToggle =
        new ActionBarDrawerToggle(
            this, drawerLayout, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
    drawerLayout.addDrawerListener(drawerToggle);
    //    drawerToggle.syncState();
    siteDetailBinding.toolbarId.menuIconId.setOnClickListener(
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
              Intent intent = new Intent(SiteDetailActivity.this, MainActivity.class);
              finish();
              startActivity(intent);
            } else if (menuItem.getItemId() == R.id.nav_site) {
              Intent intent = new Intent(SiteDetailActivity.this, AddSiteActivity.class);
              finish();

              startActivity(intent);
            } else if (menuItem.getItemId() == R.id.nav_chat) {

              Intent intent = new Intent(SiteDetailActivity.this, ViewRequestActivity.class);

              finish();
              startActivity(intent);
            } else if (menuItem.getItemId() == R.id.nav_noti) {
              Intent intent = new Intent(SiteDetailActivity.this, NotificationActivity.class);
              finish();
              startActivity(intent);
            } else if (menuItem.getItemId() == R.id.nav_about_us) {
              Intent intent = new Intent(SiteDetailActivity.this, AboutUsActivity.class);
              finish();
              startActivity(intent);
            } else if (menuItem.getItemId() == R.id.nav_logout) {
              Intent intent = new Intent(SiteDetailActivity.this, SignInActivity.class);
              userService.signOut();
              finish();

              startActivity(intent);
            }
            return false;
          }
        });
  }
}
