package com.trungngo.xanhandsach.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.trungngo.xanhandsach.Model.Request;
import com.trungngo.xanhandsach.R;
import com.trungngo.xanhandsach.Shared.DateFormatter;
import com.trungngo.xanhandsach.Shared.ImageHandler;

import java.util.List;

public class RequestAdapter extends RecyclerView.Adapter<RequestAdapter.RequestViewHolder> {
  private List<Request> requests;
  private Context context;

  private OnAcceptClick onAcceptClick;
  private OnRejectClick onRejectClick;

  public RequestAdapter(Context context, OnRejectClick onRejectClick, OnAcceptClick onAcceptClick) {
    this.context = context;
    this.onAcceptClick = onAcceptClick;
    this.onRejectClick = onRejectClick;
  }

  public void setData(List<Request> requests) {
    this.requests = requests;
    notifyDataSetChanged();
  }

  @NonNull
  @Override
  public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View view =
        LayoutInflater.from(parent.getContext()).inflate(R.layout.request_item, parent, false);
    return new RequestViewHolder(view, onRejectClick, onAcceptClick);
  }

  @Override
  public void onBindViewHolder(@NonNull RequestViewHolder holder, int position) {
    Request request = requests.get(position);
    holder.userEmail.setText(request.getVolunteers().getEmail());
    holder.userName.setText(request.getVolunteers().getDisplayName());
    holder.createdDate.setText(DateFormatter.toDateString(request.getSentDate()));
    ImageHandler.setImage(
        ImageHandler.stringImageToBitMap(request.getVolunteers().getImage()), holder.userImage);
    holder.acceptBtn.setOnClickListener(
        view -> {
          onAcceptClick.clickAccept(position);
        });
    holder.rejectBtn.setOnClickListener(
        view -> {
          onRejectClick.clickReject(position);
        });
  }

  @Override
  public int getItemCount() {
    if (requests != null) {
      return requests.size();
    }
    return 0;
  }

  public static class RequestViewHolder extends RecyclerView.ViewHolder {
    private ImageView userImage;
    private TextView userName;
    private TextView userEmail;
    private TextView createdDate;

    private Button acceptBtn;
    private Button rejectBtn;
    private OnAcceptClick onAcceptClick;
    private OnRejectClick onRejectClick;

    public RequestViewHolder(
        @NonNull View itemView, OnRejectClick onRejectClick, OnAcceptClick onAcceptClick) {
      super(itemView);
      this.userImage = itemView.findViewById(R.id.userImage);
      this.userName = itemView.findViewById(R.id.userName);
      this.userEmail = itemView.findViewById(R.id.userEmail);
      this.createdDate = itemView.findViewById(R.id.createdDate);
      this.acceptBtn = itemView.findViewById(R.id.acceptBtn);
      this.rejectBtn = itemView.findViewById(R.id.rejectBtn);
      this.onAcceptClick = onAcceptClick;
      this.onRejectClick = onRejectClick;
    }
  }

  public interface OnAcceptClick {
    void clickAccept(int position);
  }

  public interface OnRejectClick {
    void clickReject(int position);
  }
}
