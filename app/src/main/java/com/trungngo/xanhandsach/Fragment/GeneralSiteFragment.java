package com.trungngo.xanhandsach.Fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
import com.trungngo.xanhandsach.databinding.FragmentGeneralSiteBinding;

import java.util.List;

public class GeneralSiteFragment extends Fragment implements SiteAdapter.OnSiteSelected {

  private FragmentGeneralSiteBinding fragmentGeneralSiteBinding;
  private UserService userService;
  private SiteService siteService;

  private SiteAdapter siteAdapter;

  private List<SiteDto> sites;

  @Override
  public View onCreateView(
      LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    //    userService.signOut();
    // Inflate the layout for this fragment
    View view = inflater.inflate(R.layout.fragment_general_site, container, false);
    UserDto currentUser = userService.getCurrentUser();
    fragmentGeneralSiteBinding.listSite.setLayoutManager(new LinearLayoutManager(getContext()));
    setUpShimmer(true);
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
    return fragmentGeneralSiteBinding.getRoot();
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

  @Override
  public void siteSelect(int position) {
    Intent intent =
        new Intent(fragmentGeneralSiteBinding.getRoot().getContext(), SiteDetailActivity.class);
    intent.putExtra(Constant.KEY_SITE_ID, sites.get(position).getId());
    startActivity(intent);
  }
}
