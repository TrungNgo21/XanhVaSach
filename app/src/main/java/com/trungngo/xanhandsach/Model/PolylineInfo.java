package com.trungngo.xanhandsach.Model;

import com.google.android.gms.maps.model.Polyline;
import com.google.maps.model.DirectionsLeg;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class PolylineInfo {
  private Polyline polyline;
  private DirectionsLeg leg;
}
