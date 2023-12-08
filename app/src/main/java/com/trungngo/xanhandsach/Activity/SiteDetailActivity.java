package com.trungngo.xanhandsach.Activity;

import static android.content.ContentValues.TAG;

import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.ViewStub;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
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
import com.trungngo.xanhandsach.Adapter.SiteAdapter;
import com.trungngo.xanhandsach.Adapter.SliderAdapter;
import com.trungngo.xanhandsach.Callback.FirebaseCallback;
import com.trungngo.xanhandsach.Dto.SiteDto;
import com.trungngo.xanhandsach.R;
import com.trungngo.xanhandsach.Service.SiteService;
import com.trungngo.xanhandsach.Shared.Constant;
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

  private GoogleMap map;

  private Marker marker;

  private LatLng coordinates;

  private final Handler handler = new Handler(Looper.getMainLooper());
  private final Runnable runnable =
      new Runnable() {
        @Override
        public void run() {
          int position = siteDetailBinding.imageSlider.getCurrentItem();
          if (position == images.size() - 1) {
            siteDetailBinding.imageSlider.setCurrentItem(0);
          } else {
            siteDetailBinding.imageSlider.setCurrentItem(position + 1);
          }
        }
      };

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    siteDetailBinding = ActivitySiteDetailBinding.inflate(getLayoutInflater());
    setContentView(siteDetailBinding.getRoot());
    List<String> images = new ArrayList<>();
    siteService = new SiteService(this);

    images.add(
        "https://firebasestorage.googleapis.com/v0/b/xanhandsach.appspot.com/o/4hAsZETOX6Hl0uCwQbC4%2F419ff9c5-4d46-4909-9ebc-7d526fc4b03d?alt=media&token=e6588dc3-e97f-42a8-85fd-acdf0aa753bc");
    images.add(
        "https://firebasestorage.googleapis.com/v0/b/xanhandsach.appspot.com/o/4hAsZETOX6Hl0uCwQbC4%2F81d9b2a9-56b5-4831-98f0-29026f282672?alt=media&token=301f2301-8bca-4dee-927c-5319e12a7bbb");
    this.images = images;
    setUpGuestPerspective();
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
              setUpSlider(getSite.getImageUrl());
              siteDetailBinding.siteAddress.setText(getSite.getAddress());
              siteDetailBinding.createdDate.setText(getSite.getCreatedDate());
              siteDetailBinding.siteOwnerEmail.setText(getSite.getOwner().getEmail());
              siteDetailBinding.siteDes.setText(getSite.getDescription());
              SeverityChipHandler.chipDirective(
                  getSite.getSeverity(), chipView, SiteDetailActivity.this);
              coordinates = new LatLng(getSite.getLatitude(), getSite.getLongitude());
              siteDetailBinding.progressBarHolder.setVisibility(View.GONE);
              showMap();
            }
          }
        });
  }

  private void setUpSlider(List<String> images) {
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
    handler.removeCallbacks(runnable);
  }

  @Override
  protected void onResume() {
    super.onResume();
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
}
