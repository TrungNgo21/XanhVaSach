package com.trungngo.xanhandsach.Activity;

import static android.content.ContentValues.TAG;

import static java.util.Arrays.asList;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.ViewStub;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.trungngo.xanhandsach.Adapter.ImageAdapter;
import com.trungngo.xanhandsach.R;
import com.trungngo.xanhandsach.Shared.Constant;
import com.trungngo.xanhandsach.Shared.ImageHandler;
import com.trungngo.xanhandsach.databinding.ActivityAddSiteBinding;

import org.checkerframework.checker.units.qual.C;

import java.util.ArrayList;
import java.util.List;

import kotlin.text.MatchNamedGroupCollection;

public class AddSiteActivity extends AppCompatActivity
    implements OnMapReadyCallback,
        AdapterView.OnItemSelectedListener,
        ImageAdapter.OnItemCountAfterDelete {
  private ActivityAddSiteBinding activityAddSiteBinding;
  private List<Uri> uriList = new ArrayList<>();

  private SupportMapFragment mapFragment;
  private View mapPanel;

  private Marker marker;
  private GoogleMap map;

  private LatLng coordinates;

  private ImageAdapter imageAdapter;

  private String severityCategory;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    activityAddSiteBinding = ActivityAddSiteBinding.inflate(getLayoutInflater());
    setContentView(activityAddSiteBinding.getRoot());

    imageAdapter = new ImageAdapter(getApplicationContext(), this);
    imageAdapter.setData(uriList);
    activityAddSiteBinding.imagesSelectedId.setAdapter(imageAdapter);
    setUpButtonPressed();
    setUpAutoCompleteAddress();
    setUpSpinner();
  }

  private void setUpButtonPressed() {
    activityAddSiteBinding.addImageButtonId.setOnClickListener(
        view -> {
          openFileChosen();
        });
  }

  private final ActivityResultLauncher<Intent> pickImages =
      registerForActivityResult(
          new ActivityResultContracts.StartActivityForResult(),
          new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult activityResult) {
              int result = activityResult.getResultCode();
              Intent data = activityResult.getData();
              if (result == RESULT_OK && data != null) {
                if (data.getClipData() != null) {
                  int numImages = data.getClipData().getItemCount();

                  for (int i = 0; i < numImages; i++) {
                    if (uriList.size() < 8) {
                      uriList.add(data.getClipData().getItemAt(i).getUri());

                    } else {
                      Toast.makeText(
                              AddSiteActivity.this,
                              Constant.KEY_NOT_ALLOWED_MORE_THAN_8,
                              Toast.LENGTH_SHORT)
                          .show();
                    }
                  }

                } else if (data.getData() != null) {
                  if (uriList.size() < 8) {
                    Uri imgUrl = data.getData();
                    uriList.add(imgUrl);

                  } else {
                    Toast.makeText(
                            AddSiteActivity.this,
                            Constant.KEY_NOT_ALLOWED_MORE_THAN_8,
                            Toast.LENGTH_SHORT)
                        .show();
                  }
                }
                imageAdapter.setData(uriList);
                activityAddSiteBinding.imageNumberId.setText(uriList.size() + "/" + "8");
              }
            }
          });

  @Override
  public void clickDelete(int leftNum) {
    activityAddSiteBinding.imageNumberId.setText(uriList.size() + "/" + "8");
  }

  private void openFileChosen() {
    Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
    intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
    intent.setType("image/*");
    intent.setAction(Intent.ACTION_GET_CONTENT);
    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
    pickImages.launch(intent);
  }

  public void setUpAutoCompleteAddress() {
    Places.initialize(getApplicationContext(), Constant.KEY_GOOGLE_MAP_API);
    Places.createClient(this);
    AutocompleteSupportFragment autocompleteSupportFragment =
        (AutocompleteSupportFragment)
            getSupportFragmentManager().findFragmentById(R.id.autoCompleteAddress);
    autocompleteSupportFragment.setActivityMode(AutocompleteActivityMode.FULLSCREEN);
    autocompleteSupportFragment.setPlaceFields(
        asList(Place.Field.LAT_LNG, Place.Field.ADDRESS, Place.Field.NAME));
    AutocompleteSessionToken.newInstance();

    autocompleteSupportFragment.setOnPlaceSelectedListener(
        new PlaceSelectionListener() {
          @Override
          public void onError(@NonNull Status status) {}

          @Override
          public void onPlaceSelected(@NonNull Place place) {
            activityAddSiteBinding.addressDisplay.setText(place.getAddress());
            coordinates = place.getLatLng();
            showMap(place);
          }
        });
  }

  public void setUpSpinner() {
    ArrayAdapter<CharSequence> adapter =
        ArrayAdapter.createFromResource(
            this, R.array.severity_category, android.R.layout.simple_spinner_item);
    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    activityAddSiteBinding.severityCate.setAdapter(adapter);
    activityAddSiteBinding.severityCate.setOnItemSelectedListener(this);
  }

  @Override
  public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
    severityCategory = parent.getItemAtPosition(position).toString();
  }

  @Override
  public void onNothingSelected(AdapterView<?> parent) {}

  private void showMap(Place place) {

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
