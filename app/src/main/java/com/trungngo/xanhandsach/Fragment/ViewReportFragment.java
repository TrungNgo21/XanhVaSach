package com.trungngo.xanhandsach.Fragment;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.trungngo.xanhandsach.Activity.AddSiteActivity;
import com.trungngo.xanhandsach.Adapter.ReportAdapter;
import com.trungngo.xanhandsach.Callback.FirebaseCallback;
import com.trungngo.xanhandsach.Dto.SiteDto;
import com.trungngo.xanhandsach.Model.Report;
import com.trungngo.xanhandsach.Model.Site;
import com.trungngo.xanhandsach.R;
import com.trungngo.xanhandsach.Service.SiteService;
import com.trungngo.xanhandsach.Service.UserService;
import com.trungngo.xanhandsach.Shared.Result;
import com.trungngo.xanhandsach.databinding.FragmentViewReportBinding;

import java.util.List;

public class ViewReportFragment extends Fragment {

  private FragmentViewReportBinding fragmentViewReportBinding;
  private SiteService siteService;

  private UserService userService;

  private List<Report> reportList;

  private ReportAdapter reportAdapter;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    fragmentViewReportBinding = FragmentViewReportBinding.inflate(getLayoutInflater());
    siteService = new SiteService(requireContext());
    userService = new UserService(requireContext());
    reportAdapter = new ReportAdapter(requireContext());
    initialSetup();
    setUpButton();
  }

  private void setUpButton() {
    fragmentViewReportBinding.toCreateSite.setOnClickListener(
        view -> {
          if (getActivity() instanceof AddSiteActivity) {
            ((AddSiteActivity) getActivity())
                .switchToPage(0); // Replace 1 with the index of the desired page
          }
        });
    fragmentViewReportBinding.toCreateReport.setOnClickListener(
        view -> {
          if (getActivity() instanceof AddSiteActivity) {
            ((AddSiteActivity) getActivity())
                .switchToPage(1); // Replace 1 with the index of the desired page
          }
        });
  }

  @Override
  public void onResume() {
    super.onResume();
    initialSetup();
  }

  private void initialSetup() {
    String targetId = "";
    setIsLoading(true);
    fragmentViewReportBinding.reportList.setLayoutManager(new LinearLayoutManager(getContext()));

    if (userService.getCurrentUser().getSiteId() == null
        && !userService.getCurrentUser().getPermission().equals("super")) {
      setIsLoading(false);
      fragmentViewReportBinding.reportList.setVisibility(View.GONE);
      fragmentViewReportBinding.notHaveReport.setVisibility(View.GONE);
      fragmentViewReportBinding.notHaveSite.setVisibility(View.VISIBLE);
    } else {
      fragmentViewReportBinding.reportList.setVisibility(View.GONE);
      fragmentViewReportBinding.notHaveReport.setVisibility(View.GONE);
      fragmentViewReportBinding.notHaveSite.setVisibility(View.GONE);
    }
    if (userService.getCurrentUser().getPermission().equals("super")) {
      targetId = siteService.getCacheSiteId();
    } else {
      targetId = userService.getCurrentUser().getSiteId();
    }
    if (targetId != null) {
      siteService.getOneSite(
          targetId,
          new FirebaseCallback<Result<SiteDto>>() {
            @Override
            public void callbackListRes(List<Result<SiteDto>> listT) {}

            @Override
            public void callbackRes(Result<SiteDto> siteDtoResult) {
              if (siteDtoResult instanceof Result.Success) {
                setIsLoading(false);
                SiteDto siteDto = ((Result.Success<SiteDto>) siteDtoResult).getData();
                reportList = siteDto.getReports();
                if (reportList == null || reportList.isEmpty()) {
                  fragmentViewReportBinding.reportList.setVisibility(View.GONE);
                  fragmentViewReportBinding.notHaveReport.setVisibility(View.VISIBLE);
                  fragmentViewReportBinding.notHaveSite.setVisibility(View.GONE);
                } else {
                  fragmentViewReportBinding.reportList.setVisibility(View.VISIBLE);
                  fragmentViewReportBinding.notHaveReport.setVisibility(View.GONE);
                  fragmentViewReportBinding.notHaveSite.setVisibility(View.GONE);
                }
                reportAdapter.setData(reportList);
                fragmentViewReportBinding.reportList.setAdapter(reportAdapter);
              } else {
                Log.d("Report err", "cannot get report");
              }
            }
          });
    }
  }

  private void setIsLoading(boolean isOn) {
    if (isOn) {
      fragmentViewReportBinding.progressBarHolder.setVisibility(View.VISIBLE);
    } else {
      fragmentViewReportBinding.progressBarHolder.setVisibility(View.GONE);
    }
  }

  @Override
  public View onCreateView(
      LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    // Inflate the layout for this fragment
    return fragmentViewReportBinding.getRoot();
  }
}
