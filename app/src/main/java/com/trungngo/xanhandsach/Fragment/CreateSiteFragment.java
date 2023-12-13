package com.trungngo.xanhandsach.Fragment;

import static android.app.Activity.RESULT_OK;

import static com.google.firebase.messaging.Constants.TAG;
import static java.util.Arrays.asList;

import android.app.Dialog;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Toast;

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
import com.trungngo.xanhandsach.Dto.SiteDto;
import com.trungngo.xanhandsach.Dto.UserDto;
import com.trungngo.xanhandsach.Model.Site;
import com.trungngo.xanhandsach.R;
import com.trungngo.xanhandsach.Service.ImageService;
import com.trungngo.xanhandsach.Service.SiteService;
import com.trungngo.xanhandsach.Service.UserService;
import com.trungngo.xanhandsach.Shared.Constant;
import com.trungngo.xanhandsach.Shared.DateFormatter;
import com.trungngo.xanhandsach.Shared.InputValidator;
import com.trungngo.xanhandsach.Shared.Result;
import com.trungngo.xanhandsach.databinding.FragmentCreateSiteBinding;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class CreateSiteFragment extends Fragment
    implements OnMapReadyCallback,
        AdapterView.OnItemSelectedListener,
        ImageAdapter.OnItemCountAfterDelete,
        ImageAdapter.OnItemZoom {
  private FragmentCreateSiteBinding fragmentCreateSiteBinding;
  private List<Uri> uriList = new ArrayList<>();

  private SupportMapFragment mapFragment;
  private View mapPanel;

  private Marker marker;
  private GoogleMap map;

  private LatLng coordinates;

  private ImageAdapter imageAdapter;

  private String severityCategory;
  private SiteDto currentSite;

  private ImageService imageService;

  private UserService userService;

  private SiteService siteService;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    fragmentCreateSiteBinding = fragmentCreateSiteBinding.inflate(getLayoutInflater());
    imageService = new ImageService();
    userService = new UserService(requireContext());
    siteService = new SiteService(requireContext());
    imageAdapter = new ImageAdapter(requireContext(), this, this);
    imageAdapter.setData(uriList);
    fragmentCreateSiteBinding.imagesSelectedId.setAdapter(imageAdapter);
    if (userService.getCurrentUser().getSiteId() != null) {
      ownerSetUp();
    } else {
      initialSetup();
    }
    setUpButtonPressed();
    setUpAutoCompleteAddress();
    setUpSpinner();
  }

  private void initialSetup() {
    //    fragmentCreateSiteBinding.noImageSelected.setVisibility(View.VISIBLE);
    setErrorMess(fragmentCreateSiteBinding.siteDesErr, false);
    setErrorMess(fragmentCreateSiteBinding.siteNameErr, false);
    setErrorMess(fragmentCreateSiteBinding.maxVolunErr, false);
    setErrorMess(fragmentCreateSiteBinding.addressDisplayErr, false);
    fragmentCreateSiteBinding.siteName.addTextChangedListener(onInputFieldListener());
    fragmentCreateSiteBinding.description.addTextChangedListener(onInputFieldListener());
    fragmentCreateSiteBinding.volunNumId.addTextChangedListener(onInputFieldListener());
    fragmentCreateSiteBinding.addressDisplay.addTextChangedListener(onInputFieldListener());
    setEnabledSubmitButton(false);
  }

  private void ownerSetUp() {
    setIsLoading(true);
    fragmentCreateSiteBinding.createSiteButton.setText("Update site");
    initialSetup();
    siteService.getOneSite(
        userService.getCurrentUser().getSiteId(),
        new FirebaseCallback<Result<SiteDto>>() {
          @Override
          public void callbackListRes(List<Result<SiteDto>> listT) {}

          @Override
          public void callbackRes(Result<SiteDto> siteDtoResult) {
            if (siteDtoResult instanceof Result.Success) {
              SiteDto siteDto = ((Result.Success<SiteDto>) siteDtoResult).getData();
              currentSite = siteDto;
              //              List<Uri> currentSiteImgs =
              //
              // siteDto.getImageUrl().stream().map(Uri::parse).collect(Collectors.toList());
              if (!siteDto.getImageUrl().isEmpty()) {
                uriList =
                    siteDto.getImageUrl().stream().map(Uri::parse).collect(Collectors.toList());
              }
              imageAdapter.setData(uriList);
              fragmentCreateSiteBinding.siteName.setText(siteDto.getDisplayName());
              fragmentCreateSiteBinding.description.setText(siteDto.getDescription());
              fragmentCreateSiteBinding.volunNumId.setText(
                  String.valueOf(siteDto.getMaxCapacity()));
              fragmentCreateSiteBinding.volunNumId.setEnabled(false);
              fragmentCreateSiteBinding.addressDisplay.setText(siteDto.getAddress());
              fragmentCreateSiteBinding.imageNumberId.setText(uriList.size() + "/" + "8");
              if (!uriList.isEmpty()) {
                fragmentCreateSiteBinding.noImageSelected.setVisibility(View.INVISIBLE);
              } else {
                fragmentCreateSiteBinding.noImageSelected.setVisibility(View.VISIBLE);
              }
              coordinates = new LatLng(siteDto.getLatitude(), siteDto.getLongitude());
              fragmentCreateSiteBinding.severityCate.setSelection(
                  Constant.SEVERITY_MAP.get(siteDto.getSeverity()));
              setIsLoading(false);
            }
          }
        });
  }

  @Override
  public void onResume() {
    super.onResume();
    if (userService.getCurrentUser().getSiteId() != null) {
      ownerSetUp();
    }
  }

  private void setUpButtonPressed() {
    fragmentCreateSiteBinding.addImageButtonId.setOnClickListener(
        view -> {
          openFileChosen();
        });
    fragmentCreateSiteBinding.createSiteButton.setOnClickListener(view -> uploadSites());
  }

  private Site createSite() {
    if (userService.getCurrentUser().getSiteId() != null) {
      return Site.builder()
          .id(userService.getCurrentUser().getSiteId())
          .displayName(fragmentCreateSiteBinding.siteName.getText().toString())
          .owner(userService.getCurrentUser())
          .maxCapacity(Integer.parseInt(fragmentCreateSiteBinding.volunNumId.getText().toString()))
          .severity(severityCategory)
          .description(fragmentCreateSiteBinding.description.getText().toString())
          .address(fragmentCreateSiteBinding.addressDisplay.getText().toString())
          .longitude(coordinates.longitude)
          .latitude(coordinates.latitude)
          .imageUrl(currentSite.getImageUrl())
          .reports(currentSite.getReports())
          .volunteers(currentSite.getVolunteers())
          .createdDate(DateFormatter.toDate(currentSite.getCreatedDate()))
          .updatedDate(new Date())
          .build();
    }
    return Site.builder()
        .displayName(fragmentCreateSiteBinding.siteName.getText().toString())
        .owner(userService.getCurrentUser())
        .maxCapacity(Integer.parseInt(fragmentCreateSiteBinding.volunNumId.getText().toString()))
        .severity(severityCategory)
        .description(fragmentCreateSiteBinding.description.getText().toString())
        .address(fragmentCreateSiteBinding.addressDisplay.getText().toString())
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
              siteService.updateSiteImgs(
                  site.getId(),
                  site,
                  new FirebaseCallback<Result<Site>>() {
                    @Override
                    public void callbackListRes(List<Result<Site>> listT) {}

                    @Override
                    public void callbackRes(Result<Site> siteResult) {
                      if (siteResult instanceof Result.Success) {
                        Toast.makeText(requireContext(), "Successful!", Toast.LENGTH_SHORT).show();

                      } else {
                        Toast.makeText(requireContext(), "Error!", Toast.LENGTH_SHORT).show();
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
    if (userService.getCurrentUser().getSiteId() != null) {
      siteService.updateSite(
          currentSite.getId(),
          createdSite,
          new FirebaseCallback<Result<Site>>() {
            @Override
            public void callbackListRes(List<Result<Site>> listT) {}

            @Override
            public void callbackRes(Result<Site> siteResult) {
              if (siteResult instanceof Result.Success) {
                List<Uri> currentSiteImg =
                    currentSite.getImageUrl().stream().map(Uri::parse).collect(Collectors.toList());
                if (!uriList.equals(currentSiteImg)) {
                  if (uriList.isEmpty()) {
                    Toast.makeText(requireContext(), "Successful!", Toast.LENGTH_SHORT).show();
                    setEnabledUi(true);
                    setIsLoading(false);
                  } else {
                    imageService.uploadImages(
                        uriList,
                        createdSite.getId(),
                        new FirebaseCallback<Result<List<String>>>() {
                          @Override
                          public void callbackListRes(List<Result<List<String>>> listT) {}

                          @Override
                          public void callbackRes(Result<List<String>> listResult) {
                            if (listResult instanceof Result.Success) {
                              createdSite.setImageUrl(
                                  ((Result.Success<List<String>>) listResult).getData());
                              siteService.updateSiteImgs(
                                  createdSite.getId(),
                                  createdSite,
                                  new FirebaseCallback<Result<Site>>() {
                                    @Override
                                    public void callbackListRes(List<Result<Site>> listT) {}

                                    @Override
                                    public void callbackRes(Result<Site> siteResult) {
                                      if (siteResult instanceof Result.Success) {
                                        Toast.makeText(
                                                requireContext(), "Successful!", Toast.LENGTH_SHORT)
                                            .show();

                                        setEnabledUi(true);
                                        setIsLoading(false);
                                      } else {
                                        Toast.makeText(
                                                requireContext(), "Error!", Toast.LENGTH_SHORT)
                                            .show();
                                        setEnabledUi(true);
                                        setIsLoading(false);
                                      }
                                    }
                                  });
                            }
                          }
                        });
                  }
                } else {
                  Toast.makeText(requireContext(), "Successful!", Toast.LENGTH_SHORT).show();
                  setEnabledUi(true);
                  setIsLoading(false);
                }
              } else {
                Toast.makeText(requireContext(), "Error!", Toast.LENGTH_SHORT).show();
                setEnabledUi(true);
                setIsLoading(false);
              }
            }
          });
    } else {
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
                  Toast.makeText(requireContext(), "Successful!", Toast.LENGTH_SHORT).show();
                  UserDto currentUser = userService.getCurrentUser();
                  currentUser.setSiteId(createdSite.getId());
                  userService.setCurrentUser(currentUser);
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
                          siteService.updateSiteImgs(
                              site.getId(),
                              site,
                              new FirebaseCallback<Result<Site>>() {
                                @Override
                                public void callbackListRes(List<Result<Site>> listT) {}

                                @Override
                                public void callbackRes(Result<Site> siteResult) {
                                  if (siteResult instanceof Result.Success) {
                                    Toast.makeText(
                                            requireContext(), "Successful!", Toast.LENGTH_SHORT)
                                        .show();
                                    setEnabledUi(true);
                                    setIsLoading(false);
                                  } else {
                                    Toast.makeText(requireContext(), "Error!", Toast.LENGTH_SHORT)
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
                Toast.makeText(requireContext(), "Error!", Toast.LENGTH_SHORT).show();
                setEnabledUi(true);
                setIsLoading(false);
              }
            }
          });
    }
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
                              requireContext(),
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
                            requireContext(),
                            Constant.KEY_NOT_ALLOWED_MORE_THAN_8,
                            Toast.LENGTH_SHORT)
                        .show();
                  }
                }
                imageAdapter.setData(uriList);
                if (!uriList.isEmpty()) {
                  fragmentCreateSiteBinding.noImageSelected.setVisibility(View.INVISIBLE);
                } else {
                  fragmentCreateSiteBinding.noImageSelected.setVisibility(View.VISIBLE);
                }
                fragmentCreateSiteBinding.imageNumberId.setText(uriList.size() + "/" + "8");
              }
            }
          });

  @Override
  public void clickDelete(int leftNum) {
    fragmentCreateSiteBinding.imageNumberId.setText(leftNum + "/" + "8");

    if (uriList.isEmpty()) {
      fragmentCreateSiteBinding.noImageSelected.setVisibility(View.VISIBLE);
    } else {
      fragmentCreateSiteBinding.noImageSelected.setVisibility(View.INVISIBLE);
    }
  }

  @Override
  public void itemZoomClick(int position) {
    Dialog dialog = new Dialog(requireContext());
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
    Places.initialize(requireContext(), Constant.KEY_GOOGLE_MAP_API);
    Places.createClient(requireContext());
    AutocompleteSupportFragment autocompleteSupportFragment =
        (AutocompleteSupportFragment)
            getChildFragmentManager().findFragmentById(R.id.autoCompleteAddress);
    //    AutocompleteSessionToken.newInstance();
    autocompleteSupportFragment.setActivityMode(AutocompleteActivityMode.FULLSCREEN);
    autocompleteSupportFragment.setPlaceFields(
        asList(Place.Field.LAT_LNG, Place.Field.ADDRESS, Place.Field.NAME));

    autocompleteSupportFragment.setOnPlaceSelectedListener(
        new PlaceSelectionListener() {
          @Override
          public void onError(@NonNull Status status) {}

          @Override
          public void onPlaceSelected(@NonNull Place place) {
            fragmentCreateSiteBinding.addressDisplay.setText(place.getAddress());
            coordinates = place.getLatLng();
            showMap(place);
          }
        });
  }

  public void setUpSpinner() {
    ArrayAdapter<CharSequence> adapter =
        ArrayAdapter.createFromResource(
            requireContext(), R.array.severity_category, android.R.layout.simple_spinner_item);
    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    fragmentCreateSiteBinding.severityCate.setAdapter(adapter);
    fragmentCreateSiteBinding.severityCate.setOnItemSelectedListener(this);
  }

  @Override
  public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
    severityCategory = parent.getItemAtPosition(position).toString();
  }

  @Override
  public void onNothingSelected(AdapterView<?> parent) {}

  private void showMap(Place place) {

    mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentByTag("MAP");

    if (mapFragment == null) {
      mapPanel = fragmentCreateSiteBinding.stubMap.inflate();
      GoogleMapOptions mapOptions = new GoogleMapOptions();
      mapOptions.mapToolbarEnabled(false);
      mapFragment = SupportMapFragment.newInstance(mapOptions);
      getChildFragmentManager()
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
          map.setMapStyle(MapStyleOptions.loadRawResourceStyle(requireContext(), R.raw.style_raw));

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
        String siteName = fragmentCreateSiteBinding.siteName.getText().toString();
        String maxVolunNum = fragmentCreateSiteBinding.volunNumId.getText().toString();
        String des = fragmentCreateSiteBinding.description.getText().toString();
        String address = fragmentCreateSiteBinding.addressDisplay.getText().toString();
        if (!InputValidator.isValidNumCharacters(siteName, 6, 15)) {
          setErrorMess(fragmentCreateSiteBinding.siteNameErr, true);
        } else {
          setErrorMess(fragmentCreateSiteBinding.siteNameErr, false);
        }
        if (!InputValidator.isValidNumCharacters(des, 12, 30)) {
          setErrorMess(fragmentCreateSiteBinding.siteDesErr, true);
        } else {
          setErrorMess(fragmentCreateSiteBinding.siteDesErr, false);
        }

        if (maxVolunNum.isEmpty()) {
          setErrorMess(fragmentCreateSiteBinding.maxVolunErr, true);
        } else {
          if (Integer.parseInt(maxVolunNum) == 0 || Integer.parseInt(maxVolunNum) > 20) {
            setErrorMess(fragmentCreateSiteBinding.maxVolunErr, true);
          } else {
            setErrorMess(fragmentCreateSiteBinding.maxVolunErr, false);
          }
        }
        if (address.isEmpty()) {
          setErrorMess(fragmentCreateSiteBinding.addressDisplayErr, true);
        } else {
          setErrorMess(fragmentCreateSiteBinding.addressDisplayErr, false);
        }
      }

      @Override
      public void afterTextChanged(Editable s) {
        String siteName = fragmentCreateSiteBinding.siteName.getText().toString();
        String maxVolunNum = fragmentCreateSiteBinding.volunNumId.getText().toString();
        String des = fragmentCreateSiteBinding.description.getText().toString();
        String address = fragmentCreateSiteBinding.addressDisplay.getText().toString();
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
    fragmentCreateSiteBinding.siteName.setEnabled(isEnabled);
    fragmentCreateSiteBinding.volunNumId.setEnabled(isEnabled);
    fragmentCreateSiteBinding.description.setEnabled(isEnabled);
    fragmentCreateSiteBinding.severityCate.setEnabled(isEnabled);
    fragmentCreateSiteBinding.addImageButtonId.setEnabled(isEnabled);
    fragmentCreateSiteBinding.autoCompleteContainer.setEnabled(isEnabled);
    fragmentCreateSiteBinding.createSiteButton.setEnabled(isEnabled);
  }

  private void setEnabledSubmitButton(boolean isEnabled) {
    fragmentCreateSiteBinding.createSiteButton.setEnabled(isEnabled);
  }

  private void setIsLoading(boolean isLoading) {
    if (isLoading) fragmentCreateSiteBinding.progressBarHolder.setVisibility(View.VISIBLE);
    else {
      fragmentCreateSiteBinding.progressBarHolder.setVisibility(View.INVISIBLE);
    }
  }

  @Override
  public View onCreateView(
      LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

    return fragmentCreateSiteBinding.getRoot();
  }
}
