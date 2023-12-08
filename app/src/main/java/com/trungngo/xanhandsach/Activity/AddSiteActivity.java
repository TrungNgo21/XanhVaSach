package com.trungngo.xanhandsach.Activity;

import static android.content.ContentValues.TAG;

import static java.util.Arrays.asList;

import android.app.Dialog;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewStub;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

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
import com.squareup.picasso.Picasso;
import com.trungngo.xanhandsach.Adapter.ImageAdapter;
import com.trungngo.xanhandsach.Callback.FirebaseCallback;
import com.trungngo.xanhandsach.Dto.UserDto;
import com.trungngo.xanhandsach.Model.Site;
import com.trungngo.xanhandsach.Model.User;
import com.trungngo.xanhandsach.R;
import com.trungngo.xanhandsach.Service.ImageService;
import com.trungngo.xanhandsach.Service.SiteService;
import com.trungngo.xanhandsach.Service.UserService;
import com.trungngo.xanhandsach.Shared.Constant;
import com.trungngo.xanhandsach.Shared.DialogBuilder;
import com.trungngo.xanhandsach.Shared.ImageHandler;
import com.trungngo.xanhandsach.Shared.InputValidator;
import com.trungngo.xanhandsach.Shared.Result;
import com.trungngo.xanhandsach.databinding.ActivityAddSiteBinding;

import org.checkerframework.checker.units.qual.C;

import java.util.ArrayList;
import java.util.List;

import kotlin.text.MatchNamedGroupCollection;

public class AddSiteActivity extends AppCompatActivity
    implements OnMapReadyCallback,
        AdapterView.OnItemSelectedListener,
        ImageAdapter.OnItemCountAfterDelete,
        ImageAdapter.OnItemZoom {
  private ActivityAddSiteBinding activityAddSiteBinding;
  private List<Uri> uriList = new ArrayList<>();

  private SupportMapFragment mapFragment;
  private View mapPanel;

  private Marker marker;
  private GoogleMap map;

  private LatLng coordinates;

  private ImageAdapter imageAdapter;

  private String severityCategory;

  private ImageService imageService;

  private UserService userService;

  private SiteService siteService;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    activityAddSiteBinding = ActivityAddSiteBinding.inflate(getLayoutInflater());
    setContentView(activityAddSiteBinding.getRoot());
    imageService = new ImageService();
    userService = new UserService(this);
    siteService = new SiteService(this);
    imageAdapter = new ImageAdapter(getApplicationContext(), this, this);
    imageAdapter.setData(uriList);
    activityAddSiteBinding.imagesSelectedId.setAdapter(imageAdapter);
    initialSetup();
    setUpButtonPressed();
    setUpAutoCompleteAddress();
    setUpSpinner();
  }

  private void initialSetup() {
    activityAddSiteBinding.noImageSelected.setVisibility(View.VISIBLE);
    setErrorMess(activityAddSiteBinding.siteDesErr, false);
    setErrorMess(activityAddSiteBinding.siteNameErr, false);
    setErrorMess(activityAddSiteBinding.maxVolunErr, false);
    setErrorMess(activityAddSiteBinding.addressDisplayErr, false);
    activityAddSiteBinding.siteName.addTextChangedListener(onInputFieldListener());
    activityAddSiteBinding.description.addTextChangedListener(onInputFieldListener());
    activityAddSiteBinding.volunNumId.addTextChangedListener(onInputFieldListener());
    activityAddSiteBinding.addressDisplay.addTextChangedListener(onInputFieldListener());
    setEnabledSubmitButton(false);
  }

  private void setUpButtonPressed() {
    activityAddSiteBinding.addImageButtonId.setOnClickListener(
        view -> {
          openFileChosen();
        });
    activityAddSiteBinding.createSiteButton.setOnClickListener(view -> uploadSites());
  }

  private Site createSite() {
    return Site.builder()
        .displayName(activityAddSiteBinding.siteName.getText().toString())
        .owner(userService.getCurrentUser())
        .maxCapacity(Integer.parseInt(activityAddSiteBinding.volunNumId.getText().toString()))
        .severity(severityCategory)
        .description(activityAddSiteBinding.description.getText().toString())
        .address(activityAddSiteBinding.addressDisplay.getText().toString())
        .longitude(coordinates.longitude)
        .latitude(coordinates.latitude)
        .build();
  }

  private void uploadImage(String id, Site site) {
    imageService.uploadImages(
        uriList,
        id,
        new FirebaseCallback<Result<List<String>>>() {
          @Override
          public void callbackListRes(List<Result<List<String>>> listT) {}

          @Override
          public void callbackRes(Result<List<String>> listResult) {
            if (listResult instanceof Result.Success) {
              siteService.updateSite(
                  site.getId(),
                  site,
                  new FirebaseCallback<Result<Site>>() {
                    @Override
                    public void callbackListRes(List<Result<Site>> listT) {}

                    @Override
                    public void callbackRes(Result<Site> siteResult) {
                      if (siteResult instanceof Result.Success) {
                        Toast.makeText(AddSiteActivity.this, "Successful!", Toast.LENGTH_SHORT)
                            .show();
                      } else {
                        Toast.makeText(AddSiteActivity.this, "Error!", Toast.LENGTH_SHORT).show();
                      }
                    }
                  });
            }
          }
        });
  }

  private void uploadSites() {
    setEnabledUi(false);
    setIsLoading(true);

    Site createdSite = createSite();
    siteService.createSite(
        createdSite,
        new FirebaseCallback<Result<Site>>() {
          @Override
          public void callbackListRes(List<Result<Site>> listT) {}

          @Override
          public void callbackRes(Result<Site> siteResult) {
            if (siteResult instanceof Result.Success) {
              Site site = ((Result.Success<Site>) siteResult).getData();
              if (uriList.isEmpty()) {
                Toast.makeText(AddSiteActivity.this, "Successful!", Toast.LENGTH_SHORT).show();
                setEnabledUi(true);
                setIsLoading(false);
              }
              imageService.uploadImages(
                  uriList,
                  site.getId(),
                  new FirebaseCallback<Result<List<String>>>() {
                    @Override
                    public void callbackListRes(List<Result<List<String>>> listT) {}

                    @Override
                    public void callbackRes(Result<List<String>> listResult) {
                      if (listResult instanceof Result.Success) {
                        site.setImageUrl(((Result.Success<List<String>>) listResult).getData());
                        siteService.updateSite(
                            site.getId(),
                            site,
                            new FirebaseCallback<Result<Site>>() {
                              @Override
                              public void callbackListRes(List<Result<Site>> listT) {}

                              @Override
                              public void callbackRes(Result<Site> siteResult) {
                                if (siteResult instanceof Result.Success) {
                                  Toast.makeText(
                                          AddSiteActivity.this, "Successful!", Toast.LENGTH_SHORT)
                                      .show();
                                  setEnabledUi(true);
                                  setIsLoading(false);
                                } else {
                                  Toast.makeText(AddSiteActivity.this, "Error!", Toast.LENGTH_SHORT)
                                      .show();
                                  setEnabledUi(true);
                                  setIsLoading(false);
                                }
                              }
                            });
                      }
                    }
                  });
            } else {
              Toast.makeText(AddSiteActivity.this, "Error!", Toast.LENGTH_SHORT).show();
              setEnabledUi(true);
              setIsLoading(false);
            }
          }
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
                if (!uriList.isEmpty()) {
                  activityAddSiteBinding.noImageSelected.setVisibility(View.INVISIBLE);
                } else {
                  activityAddSiteBinding.noImageSelected.setVisibility(View.VISIBLE);
                }
                activityAddSiteBinding.imageNumberId.setText(uriList.size() + "/" + "8");
              }
            }
          });

  @Override
  public void clickDelete(int leftNum) {
    activityAddSiteBinding.imageNumberId.setText(uriList.size() + "/" + "8");
    if (uriList.isEmpty()) {
      activityAddSiteBinding.noImageSelected.setVisibility(View.VISIBLE);
    } else {
      activityAddSiteBinding.noImageSelected.setVisibility(View.INVISIBLE);
    }
  }

  @Override
  public void itemZoomClick(int position) {
    Dialog dialog = new Dialog(this);
    Window reviewWindow = dialog.getWindow();
    dialog.setContentView(R.layout.image_zoom_view);
    dialog.setCancelable(true);
    reviewWindow.setGravity(Gravity.CENTER);
    ImageView imageViewZoom = dialog.findViewById(R.id.imageZoomId);
    Picasso.get().load(uriList.get(position)).placeholder(R.drawable.bg_main).into(imageViewZoom);
    dialog.show();
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

  private TextWatcher onInputFieldListener() {
    return new TextWatcher() {

      @Override
      public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

      @Override
      public void onTextChanged(CharSequence s, int start, int before, int count) {
        String siteName = activityAddSiteBinding.siteName.getText().toString();
        String maxVolunNum = activityAddSiteBinding.volunNumId.getText().toString();
        String des = activityAddSiteBinding.description.getText().toString();
        String address = activityAddSiteBinding.addressDisplay.getText().toString();
        if (!InputValidator.isValidNumCharacters(siteName, 6, 15)) {
          setErrorMess(activityAddSiteBinding.siteNameErr, true);
        } else {
          setErrorMess(activityAddSiteBinding.siteNameErr, false);
        }
        if (!InputValidator.isValidNumCharacters(des, 12, 30)) {
          setErrorMess(activityAddSiteBinding.siteDesErr, true);
        } else {
          setErrorMess(activityAddSiteBinding.siteDesErr, false);
        }

        if (maxVolunNum.isEmpty()) {
          setErrorMess(activityAddSiteBinding.maxVolunErr, true);
        } else {
          if (Integer.parseInt(maxVolunNum) == 0 || Integer.parseInt(maxVolunNum) > 20) {
            setErrorMess(activityAddSiteBinding.maxVolunErr, true);
          } else {
            setErrorMess(activityAddSiteBinding.maxVolunErr, false);
          }
        }
        if (address.isEmpty()) {
          setErrorMess(activityAddSiteBinding.addressDisplayErr, true);
        } else {
          setErrorMess(activityAddSiteBinding.addressDisplayErr, false);
        }
      }

      @Override
      public void afterTextChanged(Editable s) {
        String siteName = activityAddSiteBinding.siteName.getText().toString();
        String maxVolunNum = activityAddSiteBinding.volunNumId.getText().toString();
        String des = activityAddSiteBinding.description.getText().toString();
        String address = activityAddSiteBinding.addressDisplay.getText().toString();
        if (InputValidator.isValidNumCharacters(siteName, 6, 15)
            && InputValidator.isValidNumCharacters(des, 12, 30)
            && !address.isEmpty()
            && !maxVolunNum.isEmpty()
            && Integer.parseInt(maxVolunNum) > 0
            && Integer.parseInt(maxVolunNum) <= 20) {
          setEnabledSubmitButton(true);
        } else {
          setEnabledSubmitButton(false);
        }
      }
    };
  }

  private void setErrorMess(View view, boolean isOn) {
    if (isOn) {
      view.setVisibility(View.VISIBLE);
    } else {
      view.setVisibility(View.INVISIBLE);
    }
  }

  private void setEnabledUi(boolean isEnabled) {
    activityAddSiteBinding.siteName.setEnabled(isEnabled);
    activityAddSiteBinding.volunNumId.setEnabled(isEnabled);
    activityAddSiteBinding.description.setEnabled(isEnabled);
    activityAddSiteBinding.severityCate.setEnabled(isEnabled);
    activityAddSiteBinding.addImageButtonId.setEnabled(isEnabled);
    activityAddSiteBinding.autoCompleteContainer.setEnabled(isEnabled);
    activityAddSiteBinding.createSiteButton.setEnabled(isEnabled);
  }

  private void setEnabledSubmitButton(boolean isEnabled) {
    activityAddSiteBinding.createSiteButton.setEnabled(isEnabled);
  }

  private void setIsLoading(boolean isLoading) {
    if (isLoading) activityAddSiteBinding.progressBarHolder.setVisibility(View.VISIBLE);
    else {
      activityAddSiteBinding.progressBarHolder.setVisibility(View.INVISIBLE);
    }
  }
}
