package com.trungngo.xanhandsach.Adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;
import com.trungngo.xanhandsach.R;

import java.util.List;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {
  private Context context;
  private List<Uri> imageUris;
  private OnItemCountAfterDelete onItemCountAfterDelete;

  public ImageAdapter(Context context, OnItemCountAfterDelete onItemCountAfterDelete) {
    this.context = context;
    this.onItemCountAfterDelete = onItemCountAfterDelete;
  }

  public void setData(List<Uri> uriList) {
    this.imageUris = uriList;
    notifyDataSetChanged();
  }

  @NonNull
  @Override
  public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View view =
        LayoutInflater.from(parent.getContext()).inflate(R.layout.added_image, parent, false);
    return new ImageViewHolder(view, onItemCountAfterDelete);
  }

  @Override
  public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
    Uri imageUri = imageUris.get(position);
    Picasso.get().load(imageUri).placeholder(R.drawable.bg_main).into(holder.imageView);
    holder.deleteIcon.setOnClickListener(
        view -> {
          imageUris.remove(position);
          notifyItemRemoved(position);
          notifyItemRangeChanged(position, getItemCount());
          onItemCountAfterDelete.clickDelete(imageUris.size());
        });
  }

  @Override
  public int getItemCount() {
    if (imageUris != null) {
      return imageUris.size();
    }
    return 0;
  }

  public static class ImageViewHolder extends RecyclerView.ViewHolder {
    private final ImageView imageView;
    private final ImageView deleteIcon;

    private OnItemCountAfterDelete onItemCountAfterDelete;

    public ImageViewHolder(@NonNull View itemView, OnItemCountAfterDelete onItemCountAfterDelete) {
      super(itemView);
      this.onItemCountAfterDelete = onItemCountAfterDelete;
      imageView = itemView.findViewById(R.id.addedImageId);
      deleteIcon = itemView.findViewById(R.id.deleteImageIcon);
    }
  }

  public interface OnItemCountAfterDelete {
    void clickDelete(int leftNum);
  }
}
