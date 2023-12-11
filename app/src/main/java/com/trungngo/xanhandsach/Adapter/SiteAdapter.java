package com.trungngo.xanhandsach.Adapter;

import static java.util.Arrays.asList;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;
import com.trungngo.xanhandsach.Dto.SiteDto;
import com.trungngo.xanhandsach.R;
import com.trungngo.xanhandsach.Shared.Constant;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SiteAdapter extends RecyclerView.Adapter<SiteAdapter.SiteHolder> {
  private List<SiteDto> siteList;

  private Context context;

  private OnSiteSelected onSiteSelected;

  public SiteAdapter(Context context, OnSiteSelected onSiteSelected) {
    this.context = context;
    this.onSiteSelected = onSiteSelected;
  }

  public void setData(List<SiteDto> siteList) {
    this.siteList = siteList;
    notifyDataSetChanged();
  }

  @NonNull
  @Override
  public SiteHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View view =
        LayoutInflater.from(parent.getContext())
            .inflate(R.layout.site_detail_general, parent, false);

    return new SiteHolder(view, onSiteSelected);
  }

  @Override
  public void onBindViewHolder(@NonNull SiteHolder holder, int position) {
    SiteDto selectedSite = siteList.get(position);
    List<String> imageUrls = selectedSite.getImageUrl();
    if (selectedSite.getImageUrl().isEmpty()) {
      imageUrls = new ArrayList<>(Collections.singletonList(Constant.NO_IMG_DEFAULT));
    }
    Picasso.get()
        .load(Uri.parse(imageUrls.get(0)))
        .placeholder(R.drawable.bg_main)
        .into(holder.siteImage);
    holder.siteName.setText(selectedSite.getDisplayName());
    holder.siteAddress.setText(selectedSite.getAddress());
    holder.siteDate.setText(selectedSite.getUpdatedDate());
    holder.ownerName.setText(selectedSite.getOwner().getEmail());
    holder.maxVolun.setText(String.valueOf(selectedSite.getMaxCapacity()));
    holder.signUpVolun.setText(String.valueOf(selectedSite.getVolunteers().size()));
    if (selectedSite.getSeverity().equals("Dirty")) {
      holder.severityChip.setBackgroundResource(R.drawable.warning_chip);
      final TextView severityName =
          (TextView) holder.severityChip.findViewById(R.id.severityNameId);
      severityName.setText("Dirty");
    } else if (selectedSite.getSeverity().equals("Quite Dirty")) {
      holder.severityChip.setBackgroundResource(R.drawable.success_chip);
      final TextView severityName =
          (TextView) holder.severityChip.findViewById(R.id.severityNameId);
      severityName.setText("Quite Dirty");
      severityName.setTextColor(context.getResources().getColor(R.color.primary_200));
    } else {
      holder.severityChip.setBackgroundResource(R.drawable.error_chip);
      final TextView severityName =
          (TextView) holder.severityChip.findViewById(R.id.severityNameId);
      severityName.setText("Extremely Dirty");
      severityName.setTextColor(context.getResources().getColor(R.color.error));
    }
  }

  @Override
  public int getItemCount() {
    if (siteList != null) {
      return siteList.size();
    }
    return 0;
  }

  public static class SiteHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    private ImageView siteImage;
    private TextView siteName;
    private TextView siteAddress;
    private TextView ownerName;
    private TextView siteDate;
    private TextView maxVolun;
    private TextView signUpVolun;
    private View severityChip;

    private OnSiteSelected onSiteSelected;

    public SiteHolder(@NonNull View itemView, OnSiteSelected onSiteSelected) {
      super(itemView);
      this.siteImage = itemView.findViewById(R.id.siteImgId);
      this.siteName = itemView.findViewById(R.id.siteNameId);
      this.siteAddress = itemView.findViewById(R.id.siteAddressId);
      this.ownerName = itemView.findViewById(R.id.ownerId);
      this.siteDate = itemView.findViewById(R.id.createdDateId);
      this.maxVolun = itemView.findViewById(R.id.maximumCapacityId);
      this.signUpVolun = itemView.findViewById(R.id.signUpNumId);
      this.severityChip = itemView.findViewById(R.id.severityChip);
      this.onSiteSelected = onSiteSelected;
      itemView.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
      if (onSiteSelected != null) {
        onSiteSelected.siteSelect(getAdapterPosition());
      }
    }
  }

  public interface OnSiteSelected {
    void siteSelect(int position);
  }
}
