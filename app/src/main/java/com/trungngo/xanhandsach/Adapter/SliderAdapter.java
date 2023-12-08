package com.trungngo.xanhandsach.Adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.PagerAdapter;

import com.squareup.picasso.Picasso;
import com.trungngo.xanhandsach.R;

import java.util.List;

public class SliderAdapter extends RecyclerView.Adapter<SliderAdapter.SliderHolder> {

  private List<String> images;
  private Context context;

  public SliderAdapter(Context context, List<String> images) {
    this.images = images;
    this.context = context;
  }

  @NonNull
  @Override
  public SliderHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View view =
        LayoutInflater.from(parent.getContext()).inflate(R.layout.image_slider_item, parent, false);

    return new SliderHolder(view);
  }

  @Override
  public void onBindViewHolder(@NonNull SliderHolder holder, int position) {
    String uri = images.get(position);
    Picasso.get().load(Uri.parse(uri)).placeholder(R.drawable.bg_main).into(holder.imageView);
  }

  @Override
  public int getItemCount() {
    if (images != null) {
      return images.size();
    }
    return 0;
  }

  public static class SliderHolder extends RecyclerView.ViewHolder {
    private final ImageView imageView;

    public SliderHolder(@NonNull View itemView) {
      super(itemView);
      imageView = itemView.findViewById(R.id.imageSliderItem);
    }
  }
}
