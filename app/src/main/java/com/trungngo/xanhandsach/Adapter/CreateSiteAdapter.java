package com.trungngo.xanhandsach.Adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.trungngo.xanhandsach.Fragment.CreateSiteFragment;
import com.trungngo.xanhandsach.Fragment.GeneralSiteFragment;
import com.trungngo.xanhandsach.Fragment.MapSiteFragment;
import com.trungngo.xanhandsach.Fragment.ReportFragment;
import com.trungngo.xanhandsach.Fragment.ViewReportFragment;

public class CreateSiteAdapter extends FragmentStateAdapter {

  public CreateSiteAdapter(@NonNull FragmentActivity fragmentActivity) {
    super(fragmentActivity);
  }

  @NonNull
  @Override
  public Fragment createFragment(int position) {
    switch (position) {
      case 0:
        return new CreateSiteFragment();

      case 1:
        return new ReportFragment();
      case 2:
        return new ViewReportFragment();
      default:
        return new CreateSiteFragment();
    }
  }

  @Override
  public int getItemCount() {
    return 3;
  }
}
