package com.trungngo.xanhandsach.Fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.trungngo.xanhandsach.Activity.AddSiteActivity;
import com.trungngo.xanhandsach.Activity.SiteDetailActivity;
import com.trungngo.xanhandsach.Adapter.SiteAdapter;
import com.trungngo.xanhandsach.Callback.FirebaseCallback;
import com.trungngo.xanhandsach.Dto.SiteDto;
import com.trungngo.xanhandsach.Dto.UserDto;
import com.trungngo.xanhandsach.R;
import com.trungngo.xanhandsach.Service.SiteService;
import com.trungngo.xanhandsach.Service.UserService;
import com.trungngo.xanhandsach.Shared.Constant;
import com.trungngo.xanhandsach.Shared.Result;
import com.trungngo.xanhandsach.Shared.SeverityChipHandler;
import com.trungngo.xanhandsach.databinding.FragmentGeneralSiteBinding;
import com.trungngo.xanhandsach.databinding.SiteDetailGeneralBinding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;

public class GeneralSiteFragment extends Fragment
    implements SiteAdapter.OnSiteSelected, AdapterView.OnItemSelectedListener {

  private FragmentGeneralSiteBinding fragmentGeneralSiteBinding;
  private UserService userService;
  private SiteService siteService;

  private SiteAdapter siteAdapter;

  private List<SiteDto> sites;

  private String severityCategory;

  @Override
  public View onCreateView(
      LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    //    userService.signOut();
    // Inflate the layout for this fragment
    View view = inflater.inflate(R.layout.fragment_general_site, container, false);
    fragmentGeneralSiteBinding.listSite.setLayoutManager(new LinearLayoutManager(getContext()));
    UserDto currentUser = userService.getCurrentUser();
    if (currentUser.getPermission().equals("super")) {
      setUpAdminView();
    } else {
      setUpNormalView(currentUser);
    }
    setUpShimmer(true);
    setUpSpinner();
    setUpSearchQuery();
    return fragmentGeneralSiteBinding.getRoot();
  }

  private void setUpNormalView(UserDto currentUser) {
    if (userService.getCurrentUser().getSiteId() == null) {
      fragmentGeneralSiteBinding.mySite.setVisibility(View.VISIBLE);
      getYourOwnSite(null, null);
    } else {
      siteService.getOneSite(
          userService.getCurrentUser().getSiteId(),
          new FirebaseCallback<Result<SiteDto>>() {
            @Override
            public void callbackListRes(List<Result<SiteDto>> listT) {}

            @Override
            public void callbackRes(Result<SiteDto> siteDtoResult) {
              if (siteDtoResult instanceof Result.Success) {
                fragmentGeneralSiteBinding.mySite.setVisibility(View.VISIBLE);
                fragmentGeneralSiteBinding.notHaveSite.setVisibility(View.GONE);
                SiteDto siteDto = ((Result.Success<SiteDto>) siteDtoResult).getData();
                getYourOwnSite(userService.getCurrentUser().getSiteId(), siteDto);
              }
            }
          });
    }

    siteService.getAllSite(
        currentUser.getId(),
        new FirebaseCallback<Result<List<SiteDto>>>() {
          @Override
          public void callbackListRes(List<Result<List<SiteDto>>> listT) {}

          @Override
          public void callbackRes(Result<List<SiteDto>> listResult) {
            if (listResult instanceof Result.Success) {
              sites = ((Result.Success<List<SiteDto>>) listResult).getData();
              siteAdapter.setData(sites);
              fragmentGeneralSiteBinding.listSite.setAdapter(siteAdapter);
              setUpShimmer(false);

            } else {
              Log.d("Cannot get any thing", listResult.toString());
            }
          }
        });
  }

  private void setUpAdminView() {
    siteService.getAllSiteAdmin(
        new FirebaseCallback<Result<List<SiteDto>>>() {
          @Override
          public void callbackListRes(List<Result<List<SiteDto>>> listT) {}

          @Override
          public void callbackRes(Result<List<SiteDto>> listResult) {
            if (listResult instanceof Result.Success) {
              sites = ((Result.Success<List<SiteDto>>) listResult).getData();
              siteAdapter.setData(sites);
              fragmentGeneralSiteBinding.listSite.setAdapter(siteAdapter);
              fragmentGeneralSiteBinding.mySite.setVisibility(View.GONE);

              setUpShimmer(false);

            } else {
              Log.d("Cannot get any thing", listResult.toString());
            }
          }
        });
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    fragmentGeneralSiteBinding = FragmentGeneralSiteBinding.inflate(getLayoutInflater());
    userService = new UserService(getContext());
    siteService = new SiteService(getContext());
    siteAdapter = new SiteAdapter(getContext(), this);
  }

  private void setUpShimmer(boolean isOn) {
    if (isOn) {
      fragmentGeneralSiteBinding.shimmerView.startShimmer();
      fragmentGeneralSiteBinding.shimmerView.setVisibility(View.VISIBLE);
    } else {
      fragmentGeneralSiteBinding.shimmerView.stopShimmer();
      fragmentGeneralSiteBinding.shimmerView.setVisibility(View.INVISIBLE);
    }
  }

  private void getYourOwnSite(String siteId, SiteDto siteDto) {
    fragmentGeneralSiteBinding.notHaveSite.setVisibility(View.GONE);

    fragmentGeneralSiteBinding.toCreateSite.setOnClickListener(
        view -> {
          startActivity(new Intent(requireContext(), AddSiteActivity.class));
        });

    if (siteId != null) {
      ImageView siteImage;
      TextView siteName;
      TextView siteAddress;
      TextView ownerName;
      TextView siteDate;
      TextView maxVolun;
      TextView signUpVolun;
      View severityChip;
      LinearLayout layout =
          (LinearLayout)
              LayoutInflater.from(requireContext()).inflate(R.layout.site_detail_general, null);
      layout.setLayoutParams(
          new ViewGroup.LayoutParams(
              ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
      fragmentGeneralSiteBinding.mySiteDetail.addView(layout);
      siteImage = layout.findViewById(R.id.siteImgId);
      siteName = layout.findViewById(R.id.siteNameId);
      siteAddress = layout.findViewById(R.id.siteAddressId);
      ownerName = layout.findViewById(R.id.ownerId);
      siteDate = layout.findViewById(R.id.createdDateId);
      maxVolun = layout.findViewById(R.id.maximumCapacityId);
      signUpVolun = layout.findViewById(R.id.signUpNumId);
      severityChip = layout.findViewById(R.id.severityChip);
      List<String> imageUrls = siteDto.getImageUrl();
      if (siteDto.getImageUrl().isEmpty()) {
        imageUrls = new ArrayList<>(Collections.singletonList(Constant.NO_IMG_DEFAULT));
      }
      Picasso.get()
          .load(Uri.parse(imageUrls.get(0)))
          .placeholder(R.drawable.bg_main)
          .into(siteImage);
      siteName.setText(siteDto.getDisplayName());
      siteAddress.setText(siteDto.getAddress());
      siteDate.setText(siteDto.getUpdatedDate());
      ownerName.setText(siteDto.getOwner().getEmail());
      maxVolun.setText(String.valueOf(siteDto.getMaxCapacity()));
      signUpVolun.setText(String.valueOf(siteDto.getVolunteers().size()));
      SeverityChipHandler.chipDirective(siteDto.getSeverity(), severityChip, layout.getContext());
      fragmentGeneralSiteBinding.mySiteDetail.setOnClickListener(
          view -> {
            Intent intent = new Intent(requireContext(), SiteDetailActivity.class);
            intent.putExtra(Constant.KEY_SITE_ID, siteId);
            startActivity(intent);
          });
    } else {
      fragmentGeneralSiteBinding.notHaveSite.setVisibility(View.VISIBLE);
    }
  }

  @Override
  public void siteSelect(int position) {
    Intent intent =
        new Intent(fragmentGeneralSiteBinding.getRoot().getContext(), SiteDetailActivity.class);
    intent.putExtra(Constant.KEY_SITE_ID, sites.get(position).getId());
    siteService.cacheSiteId(sites.get(position).getId());
    startActivity(intent);
  }

  private void setUpSearchQuery() {
    fragmentGeneralSiteBinding.searchBar.query.addTextChangedListener(onQueryChange());
  }

  private void setUpSpinner() {
    ArrayAdapter<CharSequence> adapter =
        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.severity_category_search,
            android.R.layout.simple_spinner_item);
    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    fragmentGeneralSiteBinding.searchBar.severityCate.setAdapter(adapter);
    fragmentGeneralSiteBinding.searchBar.severityCate.setOnItemSelectedListener(this);
  }

  private TextWatcher onQueryChange() {
    return new TextWatcher() {
      @Override
      public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

      @Override
      public void onTextChanged(CharSequence s, int start, int before, int count) {
        List<SiteDto> res = new ArrayList<>();
        res =
            siteService.getQuerySite(
                severityCategory,
                fragmentGeneralSiteBinding.searchBar.query.getText().toString(),
                sites,
                null);
        siteAdapter.setData(res);
      }

      @Override
      public void afterTextChanged(Editable s) {}
    };
  }

  @Override
  public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
    severityCategory = parent.getItemAtPosition(position).toString();
    List<SiteDto> res = new ArrayList<>();
    if (sites != null) {
      res =
          siteService.getQuerySite(
              severityCategory,
              fragmentGeneralSiteBinding.searchBar.query.getText().toString(),
              sites,
              null);
      siteAdapter.setData(res);
    }
  }

  @Override
  public void onNothingSelected(AdapterView<?> parent) {}
}
