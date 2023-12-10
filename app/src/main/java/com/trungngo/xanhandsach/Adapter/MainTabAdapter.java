package com.trungngo.xanhandsach.Adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.trungngo.xanhandsach.Fragment.GeneralSiteFragment;
import com.trungngo.xanhandsach.Fragment.MapSiteFragment;

public class MainTabAdapter extends FragmentStateAdapter {

  public MainTabAdapter(@NonNull FragmentActivity fragmentActivity) {
    super(fragmentActivity);
  }

  @NonNull
  @Override
  public Fragment createFragment(int position) {
    switch (position) {
      case 0:
        return new GeneralSiteFragment();
      case 1:
        return new MapSiteFragment();
      default:
        return new GeneralSiteFragment();
    }
  }

  @Override
  public int getItemCount() {
    return 2;
  }
}
