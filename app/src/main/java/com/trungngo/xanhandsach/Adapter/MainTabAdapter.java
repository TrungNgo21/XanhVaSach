package com.trungngo.xanhandsach.Adapter;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.trungngo.xanhandsach.Fragment.GeneralSiteFragment;
import com.trungngo.xanhandsach.Fragment.MapSiteFragment;

public class MainTabAdapter extends FragmentPagerAdapter {

  public MainTabAdapter(@NonNull FragmentManager fm) {
    super(fm);
  }

  @NonNull
  @Override
  public Fragment getItem(int position) {
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
  public int getCount() {
    return 2;
  }
}
