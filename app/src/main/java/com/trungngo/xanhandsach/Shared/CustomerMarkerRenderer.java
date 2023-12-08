package com.trungngo.xanhandsach.Shared;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.ui.IconGenerator;
import com.trungngo.xanhandsach.Model.CustomMarker;
import com.trungngo.xanhandsach.R;

public class CustomerMarkerRenderer extends DefaultClusterRenderer<CustomMarker> {
  private final IconGenerator iconGenerator;
  private final ImageView imageView;
  private final int markerWidth;
  private final int markerHeight;

  public CustomerMarkerRenderer(
      Context context, GoogleMap map, ClusterManager<CustomMarker> clusterManager) {
    super(context, map, clusterManager);
    iconGenerator = new IconGenerator(context.getApplicationContext());
    imageView = new ImageView(context.getApplicationContext());
    markerWidth = (int) context.getResources().getDimension(R.dimen.custom_marker);
    markerHeight = (int) context.getResources().getDimension(R.dimen.custom_marker);
    imageView.setLayoutParams(new ViewGroup.LayoutParams(markerWidth, markerHeight));
    iconGenerator.setContentView(imageView);
  }

  @Override
  protected void onBeforeClusterItemRendered(
      @NonNull CustomMarker item, @NonNull MarkerOptions markerOptions) {
    imageView.setImageResource(item.getIconPic());
    Bitmap bitmapIcon = iconGenerator.makeIcon();
    markerOptions
        .icon(BitmapDescriptorFactory.fromBitmap(bitmapIcon))
        .title(item.getTitle())
        .snippet(item.getSnippet());
  }

  @Override
  protected boolean shouldRenderAsCluster(@NonNull Cluster<CustomMarker> cluster) {
    return false;
  }
}
