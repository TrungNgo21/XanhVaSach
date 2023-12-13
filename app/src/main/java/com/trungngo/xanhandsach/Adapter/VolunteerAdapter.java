package com.trungngo.xanhandsach.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.auth.User;
import com.trungngo.xanhandsach.Dto.UserDto;
import com.trungngo.xanhandsach.R;
import com.trungngo.xanhandsach.Shared.ImageHandler;

import java.util.List;

public class VolunteerAdapter extends RecyclerView.Adapter<VolunteerAdapter.VolunteerViewHolder> {
  List<UserDto> volunteers;
  Context context;

  public VolunteerAdapter(Context context) {
    this.context = context;
  }

  public void setData(List<UserDto> volunteers) {
    this.volunteers = volunteers;
    notifyDataSetChanged();
  }

  @NonNull
  @Override
  public VolunteerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View view =
        LayoutInflater.from(parent.getContext()).inflate(R.layout.volunteer_item, parent, false);
    return new VolunteerViewHolder(view);
  }

  @Override
  public void onBindViewHolder(@NonNull VolunteerViewHolder holder, int position) {
    UserDto userDto = volunteers.get(position);
    holder.userEmail.setText(userDto.getEmail());
    holder.userName.setText(userDto.getDisplayName());
    ImageHandler.setImage(ImageHandler.stringImageToBitMap(userDto.getImage()), holder.userImage);
  }

  @Override
  public int getItemCount() {
    if (volunteers != null) {
      return volunteers.size();
    }
    return 0;
  }

  public static class VolunteerViewHolder extends RecyclerView.ViewHolder {
    private ImageView userImage;
    private TextView userName;
    private TextView userEmail;

    public VolunteerViewHolder(@NonNull View itemView) {
      super(itemView);
      this.userImage = itemView.findViewById(R.id.userImage);
      this.userName = itemView.findViewById(R.id.userName);
      this.userEmail = itemView.findViewById(R.id.userEmail);
    }
  }
}
