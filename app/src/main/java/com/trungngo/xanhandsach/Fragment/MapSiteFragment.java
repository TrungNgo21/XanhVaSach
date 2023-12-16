package com.trungngo.xanhandsach.Fragment;

import static android.content.ContentValues.TAG;

import android.Manifest;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.Priority;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.GeoPoint;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.PendingResult;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.internal.PolylineEncoding;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;
import com.trungngo.xanhandsach.Activity.SiteDetailActivity;
import com.trungngo.xanhandsach.Adapter.SliderAdapter;
import com.trungngo.xanhandsach.Callback.FirebaseCallback;
import com.trungngo.xanhandsach.Dto.SiteDto;
import com.trungngo.xanhandsach.Dto.UserDto;
import com.trungngo.xanhandsach.Model.CustomMarker;
import com.trungngo.xanhandsach.Model.PolylineInfo;
import com.trungngo.xanhandsach.Model.Request;
import com.trungngo.xanhandsach.Model.Site;
import com.trungngo.xanhandsach.R;
import com.trungngo.xanhandsach.Service.SiteService;
import com.trungngo.xanhandsach.Service.UserService;
import com.trungngo.xanhandsach.Shared.Constant;
import com.trungngo.xanhandsach.Shared.CustomerMarkerRenderer;
import com.trungngo.xanhandsach.Shared.Result;
import com.trungngo.xanhandsach.databinding.FragmentMapSiteBinding;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MapSiteFragment extends Fragment
    implements OnMapReadyCallback, GoogleMap.OnPolylineClickListener {

  private FragmentMapSiteBinding fragmentMapSiteBinding;

  private SiteService siteService;
  private UserService userService;
  private UserDto currentUser;

  private GoogleMap map;

  private FusedLocationProviderClient fusedLocationClient;
  private LocationRequest locationRequest;
  private LocationCallback locationCallback;

  private LatLngBounds latLngBounds;
  private ClusterManager<CustomMarker> customManager;
  private CustomerMarkerRenderer customerMarkerRenderer;

  private List<CustomMarker> customMarkers = new ArrayList<>();

  private List<Marker> locationMakers = new ArrayList<>();

  private List<SiteDto> sites = new ArrayList<>();

  private boolean isAllowedAccessLocation;
  private GeoApiContext mGeoApiContext;

  private Marker selectedMarker = null;

  private List<PolylineInfo> polylineInfoList = new ArrayList<>();
  private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
  private static final int LOCATION_INTERVAL = 3000;

  private static final int LOCATION_FASTEST_INTERVAL = 1000;
  private static final int LOCATION_MAX_WAIT_TIME = 100;

  private Handler handler;
  private Runnable runnable;

  @Override
  public View onCreateView(
      LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    SupportMapFragment mapFragment =
        (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
    mapFragment.getMapAsync(this);
    //    checkAndRequestLocationPermission();
    return fragmentMapSiteBinding.getRoot();
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    fragmentMapSiteBinding = FragmentMapSiteBinding.inflate(getLayoutInflater());
    siteService = new SiteService(requireContext());
    userService = new UserService(requireContext());
    currentUser = userService.getCurrentUser();
    //    startLocationUpdates();
    //    getLastLocation();
    siteService.getAllSite(
        currentUser.getId(),
        new FirebaseCallback<Result<List<SiteDto>>>() {
          @Override
          public void callbackListRes(List<Result<List<SiteDto>>> listT) {}

          @Override
          public void callbackRes(Result<List<SiteDto>> listResult) {
            if (listResult instanceof Result.Success) {
              sites = ((Result.Success<List<SiteDto>>) listResult).getData();
              setSiteMarkers(map, sites);
            } else {
              Log.d("Error!", "cannot get data");
            }
          }
        });

    fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext());
    locationRequest =
        new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY)
            .setIntervalMillis(LOCATION_INTERVAL)
            .setMinUpdateIntervalMillis(LOCATION_FASTEST_INTERVAL)
            .setMaxUpdateDelayMillis(LOCATION_MAX_WAIT_TIME)
            .setWaitForAccurateLocation(true)
            .build();

    //    LocationSettingsRequest.Builder builder =
    //        new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
    //    SettingsClient client = LocationServices.getSettingsClient(requireContext());
    //    Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

    // Create a location callback
    locationCallback =
        new LocationCallback() {
          @Override
          public void onLocationResult(@NonNull LocationResult locationResult) {
            for (Location location : locationResult.getLocations()) {
              Log.d("updated location:", String.valueOf(location.getLatitude()));
              Log.d("updated location:", String.valueOf(location.getLongitude()));
              //              if (currentUser.getLatitude() != location.getLatitude()) {
              //                setSiteMarkers(map, sites);
              //              }
            }
            super.onLocationResult(locationResult);
            //            getLastLocation();

            //            getLastLocation();
          }
        };

    //    checkAndRequestLocationPermission();
    getLastLocation();
  }

  @Override
  public void onStart() {
    super.onStart();
    checkAndRequestLocationPermission();
  }

  @Override
  public void onResume() {
    super.onResume();
    //    checkAndRequestLocationPermission();
    startLocationUpdates();
    getLastLocation();

    //    if (customManager != null) {
    //      setUserMarker(customManager);
    //    }
    if (handler != null) {
      handler.postDelayed(runnable, 3000);
    }
  }

  @Override
  public void onStop() {
    super.onStop();
    stopLocationUpdates();
    if (handler != null) {
      handler.removeCallbacks(runnable);
    }
  }

  private void checkAndRequestLocationPermission() {
    if (ActivityCompat.checkSelfPermission(
                requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        && ActivityCompat.checkSelfPermission(
                requireContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
      startLocationUpdates();
      getLastLocation();
    } else {
      requestLocationPermission();
    }
  }

  private final ActivityResultLauncher<String> requestPermissionLauncher =
      registerForActivityResult(
          new ActivityResultContracts.RequestPermission(),
          isGranted -> {
            if (isGranted) {
              // Permission granted; start location updates
              isAllowedAccessLocation = true;
              startLocationUpdates();
            } else {
              Toast.makeText(
                  requireContext(),
                  "Please turn on GPS permission otherwise you can not use map features",
                  Toast.LENGTH_SHORT);
              isAllowedAccessLocation = false;
            }
          });

  private void getLastLocation() {
    if (ActivityCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        && ActivityCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
      return;
    }
    fusedLocationClient
        .getLastLocation()
        .addOnCompleteListener(
            task -> {
              if (task.isSuccessful()) {
                Location location = task.getResult();
                GeoPoint geoPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
                Log.d("My location", "Latitude: " + geoPoint.getLatitude());
                Log.d("My location", "Longitude: " + geoPoint.getLongitude());
                try {
                  Geocoder geocoder = new Geocoder(requireContext(), Locale.getDefault());

                  List<Address> addresses =
                      geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                  if (addresses != null && addresses.size() > 0) {
                    Address address = addresses.get(0);

                    // Use the address details as needed
                    String addressLine = address.getAddressLine(0);
                    currentUser.setCurrentAddress(addressLine);
                  }
                } catch (IOException e) {
                  throw new RuntimeException(e);
                }
                currentUser.setLongitude(location.getLongitude());
                currentUser.setLatitude(location.getLatitude());
                userService.setCurrentUser(currentUser);
              } else {
                Log.d("Error!", task.getException().toString());
              }
            });
  }

  private void startLocationUpdates() {
    if (ActivityCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        && ActivityCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
      return;
    }
    fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
  }

  private void stopLocationUpdates() {
    fusedLocationClient.removeLocationUpdates(locationCallback);
  }

  private void requestLocationPermission() {
    requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
  }

  //  private void setUserMarker(ClusterManager<CustomMarker> customManager) {
  //    customManager.addItem(
  //        CustomMarker.builder()
  //            .site(null)
  //            .iconPic(R.drawable.location_icon)
  //            .title(currentUser.getDisplayName())
  //            .snippet(currentUser.getEmail())
  //            .position(new LatLng(currentUser.getLatitude(), currentUser.getLongitude()))
  //            .build());
  //    customManager.cluster();
  //  }

  private void setSiteMarkers(GoogleMap googleMap, List<SiteDto> sites) {
    if (customManager == null) {
      customManager = new ClusterManager<CustomMarker>(requireContext(), googleMap);
    }
    if (customerMarkerRenderer == null) {
      customerMarkerRenderer =
          new CustomerMarkerRenderer(requireActivity(), googleMap, customManager);
      customManager.setRenderer(customerMarkerRenderer);
    }
    for (SiteDto siteDto : sites) {
      CustomMarker customMarker =
          CustomMarker.builder()
              .site(siteDto)
              .iconPic(R.drawable.trash_icon)
              .title(siteDto.getDisplayName())
              .snippet(siteDto.getOwner().getEmail())
              .position(new LatLng(siteDto.getLatitude(), siteDto.getLongitude()))
              .build();
      customManager.addItem(customMarker);
      customMarkers.add(customMarker);
    }
    LatLng coordinates = new LatLng(currentUser.getLatitude(), currentUser.getLongitude());
    CustomMarker customMarker =
        CustomMarker.builder()
            .site(null)
            .iconPic(R.drawable.location_icon)
            .title(currentUser.getDisplayName())
            .snippet(currentUser.getEmail())
            .position(new LatLng(currentUser.getLatitude(), currentUser.getLongitude()))
            .build();
    customManager.addItem(customMarker);
    customMarkers.add(customMarker);
    customManager.cluster();
    customManager.setOnClusterItemClickListener(
        new ClusterManager.OnClusterItemClickListener<CustomMarker>() {
          @Override
          public boolean onClusterItemClick(CustomMarker item) {
            if (item.getSite() == null) {
              fragmentMapSiteBinding.siteName.setText("Your current location");
              fragmentMapSiteBinding.siteAddress.setText(currentUser.getCurrentAddress());
              setViewSiteInfo(false);

            } else {

              calculateDirections(item);
              fragmentMapSiteBinding.siteName.setText(item.getSite().getDisplayName());
              fragmentMapSiteBinding.siteAddress.setText(item.getSite().getAddress());
              setUpSlider(item.getSite().getImageUrl());
              setUpDetailButtonClicked(item.getSite());
              setViewSiteInfo(true);
            }
            return false;
          }
        });
    setCameraView(googleMap);
  }

  private void setViewSiteInfo(boolean isOn) {
    if (!isOn) {
      fragmentMapSiteBinding.toDetails.setVisibility(View.GONE);
      fragmentMapSiteBinding.imageSlider.setVisibility(View.GONE);
      fragmentMapSiteBinding.applyBtn.setVisibility(View.GONE);
      fragmentMapSiteBinding.sliderIndicator.setVisibility(View.GONE);
      fragmentMapSiteBinding.durationContainer.setVisibility(View.GONE);

    } else {
      fragmentMapSiteBinding.durationContainer.setVisibility(View.VISIBLE);
      fragmentMapSiteBinding.toDetails.setVisibility(View.VISIBLE);
      fragmentMapSiteBinding.imageSlider.setVisibility(View.VISIBLE);
      fragmentMapSiteBinding.applyBtn.setVisibility(View.VISIBLE);
      fragmentMapSiteBinding.sliderIndicator.setVisibility(View.VISIBLE);
    }
  }

  private void setUpDetailButtonClicked(SiteDto siteDto) {
    fragmentMapSiteBinding.toDetails.setOnClickListener(
        view -> {
          Intent intent = new Intent(requireContext(), SiteDetailActivity.class);
          intent.putExtra(Constant.KEY_SITE_ID, siteDto.getId());
          startActivity(intent);
        });
    if (userService.getCurrentUser().getPermission().equals("super")) {
      fragmentMapSiteBinding.applyBtn.setVisibility(View.GONE);
    } else {
      fragmentMapSiteBinding.applyBtn.setVisibility(View.VISIBLE);
      fragmentMapSiteBinding.applyBtn.setOnClickListener(
          view -> {
            userService.applyForVolunteer(
                siteDto,
                new FirebaseCallback<Result<Request>>() {
                  @Override
                  public void callbackListRes(List<Result<Request>> listT) {}

                  @Override
                  public void callbackRes(Result<Request> requestResult) {
                    if (requestResult instanceof Result.Success) {
                      fragmentMapSiteBinding.applyBtn.setEnabled(false);
                      Toast.makeText(
                              requireContext(),
                              "Your request sent successfully! Please wait for the owner",
                              Toast.LENGTH_SHORT)
                          .show();
                    }
                  }
                });
          });
    }
  }

  private void setCameraView(GoogleMap googleMap) {
    double bottomBoundary = currentUser.getLatitude() - .1;
    double leftBoundary = currentUser.getLongitude() - .1;
    double topBoundary = currentUser.getLatitude() + .1;
    double rightBoundary = currentUser.getLongitude() + .1;
    latLngBounds =
        new LatLngBounds(
            new LatLng(bottomBoundary, leftBoundary), new LatLng(topBoundary, rightBoundary));
    map.moveCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, 0));
  }

  @Override
  public void onMapReady(@NonNull GoogleMap googleMap) {
    map = googleMap;
    map.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
    try {
      boolean success =
          map.setMapStyle(MapStyleOptions.loadRawResourceStyle(requireContext(), R.raw.style_raw));
      if (!success) {
        Log.e(TAG, "Style parsing failed.");
      }
    } catch (Resources.NotFoundException e) {
      Log.e(TAG, "Can't find style. Error: ", e);
    }

    if (mGeoApiContext == null) {
      mGeoApiContext = new GeoApiContext.Builder().apiKey(Constant.KEY_GOOGLE_MAP_API).build();
    }
    //    if (customManager != null) setUserMarker(customManager);
    map.setOnPolylineClickListener(this);
  }

  private void calculateDirections(CustomMarker marker) {
    com.google.maps.model.LatLng destination =
        new com.google.maps.model.LatLng(
            marker.getPosition().latitude, marker.getPosition().longitude);
    DirectionsApiRequest directions = new DirectionsApiRequest(mGeoApiContext);

    directions.alternatives(true);
    directions.origin(
        new com.google.maps.model.LatLng(currentUser.getLatitude(), currentUser.getLongitude()));
    Log.d(TAG, "calculateDirections: destination: " + destination.toString());
    directions
        .destination(destination)
        .setCallback(
            new PendingResult.Callback<DirectionsResult>() {
              @Override
              public void onResult(DirectionsResult result) {

                Log.d(TAG, "onResult: successfully retrieved directions.");
                addPolylinesToMap(result);
              }

              @Override
              public void onFailure(Throwable e) {
                Log.e(TAG, "calculateDirections: Failed to get directions: " + e.getMessage());
              }
            });
  }

  private void addPolylinesToMap(final DirectionsResult result) {
    new Handler(Looper.getMainLooper())
        .post(
            new Runnable() {
              @Override
              public void run() {
                Log.d(TAG, "run: result routes: " + result.routes.length);
                if (polylineInfoList.size() > 0) {
                  for (PolylineInfo polylineData : polylineInfoList) {
                    polylineData.getPolyline().remove();
                  }
                  polylineInfoList.clear();
                  polylineInfoList = new ArrayList<>();
                }

                double duration = 999999999;
                for (DirectionsRoute route : result.routes) {
                  Log.d(TAG, "run: leg: " + route.legs[0].toString());
                  List<com.google.maps.model.LatLng> decodedPath =
                      PolylineEncoding.decode(route.overviewPolyline.getEncodedPath());

                  List<LatLng> newDecodedPath = new ArrayList<>();

                  // This loops through all the LatLng coordinates of ONE polyline.
                  for (com.google.maps.model.LatLng latLng : decodedPath) {

                    //                        Log.d(TAG, "run: latlng: " + latLng.toString());

                    newDecodedPath.add(new LatLng(latLng.lat, latLng.lng));
                  }
                  Polyline polyline = map.addPolyline(new PolylineOptions().addAll(newDecodedPath));
                  polyline.setColor(ContextCompat.getColor(requireActivity(), R.color.grey));
                  polyline.setClickable(true);
                  polylineInfoList.add(
                      PolylineInfo.builder().polyline(polyline).leg(route.legs[0]).build());
                  double tempDuration = route.legs[0].duration.inSeconds;
                  if (tempDuration < duration) {
                    duration = tempDuration;
                    onPolylineClick(polyline);
                    zoomRoute(polyline.getPoints());
                  }
                }
              }
            });
  }

  public void zoomRoute(List<LatLng> lstLatLngRoute) {

    if (map == null || lstLatLngRoute == null || lstLatLngRoute.isEmpty()) return;

    LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
    for (LatLng latLngPoint : lstLatLngRoute) boundsBuilder.include(latLngPoint);

    int routePadding = 50;
    LatLngBounds latLngBounds = boundsBuilder.build();

    map.animateCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, routePadding), 600, null);
  }

  private void setUpSlider(List<String> images) {
    SliderAdapter sliderAdapter = new SliderAdapter(requireContext(), images);
    fragmentMapSiteBinding.imageSlider.setAdapter(sliderAdapter);
    fragmentMapSiteBinding.sliderIndicator.setViewPager(fragmentMapSiteBinding.imageSlider);
    sliderAdapter.registerAdapterDataObserver(
        fragmentMapSiteBinding.sliderIndicator.getAdapterDataObserver());

    handler = new Handler(Looper.getMainLooper());
    runnable =
        new Runnable() {
          @Override
          public void run() {
            int position = fragmentMapSiteBinding.imageSlider.getCurrentItem();
            if (position == images.size() - 1) {
              fragmentMapSiteBinding.imageSlider.setCurrentItem(0);
            } else {
              fragmentMapSiteBinding.imageSlider.setCurrentItem(position + 1);
            }
          }
        };

    fragmentMapSiteBinding.imageSlider.registerOnPageChangeCallback(
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
  public void onPolylineClick(@NonNull Polyline polyline) {
    int index = 0;
    for (PolylineInfo polylineData : polylineInfoList) {
      index++;
      Log.d(TAG, "onPolylineClick: toString: " + polylineData.toString());
      if (polyline.getId().equals(polylineData.getPolyline().getId())) {
        polylineData
            .getPolyline()
            .setColor(ContextCompat.getColor(requireActivity(), R.color.primary_200));
        polylineData.getPolyline().setZIndex(1);

        LatLng endLocation =
            new LatLng(
                polylineData.getLeg().endLocation.lat, polylineData.getLeg().endLocation.lng);
        fragmentMapSiteBinding.duration.setText(polylineData.getLeg().duration.toString());
      } else {
        polylineData.getPolyline().setColor(ContextCompat.getColor(getActivity(), R.color.grey));
        polylineData.getPolyline().setZIndex(0);
      }
    }
  }
}
