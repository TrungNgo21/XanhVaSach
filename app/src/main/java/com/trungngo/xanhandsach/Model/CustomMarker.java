package com.trungngo.xanhandsach.Model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;
import com.trungngo.xanhandsach.Dto.SiteDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class CustomMarker implements ClusterItem {
  private LatLng position;
  private String title;
  private String snippet;
  private int iconPic;
  private SiteDto site;
}
