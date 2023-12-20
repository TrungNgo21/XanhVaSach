package com.trungngo.xanhandsach.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.trungngo.xanhandsach.Model.Notification;
import com.trungngo.xanhandsach.R;
import com.trungngo.xanhandsach.Shared.DateFormatter;

import java.util.List;

public class NotiAdapter extends RecyclerView.Adapter<NotiAdapter.NotiViewHolder> {

  private List<Notification> notifications;

  private Context context;

  private OnDeleteButton onDeleteButton;

  private OnRequestButton onRequestButton;

  public NotiAdapter(
      Context context, OnDeleteButton onDeleteButton, OnRequestButton onRequestButton) {
    this.context = context;
    this.onDeleteButton = onDeleteButton;
    this.onRequestButton = onRequestButton;
  }

  public void setData(List<Notification> notifications) {
    this.notifications = notifications;
    notifyDataSetChanged();
  }

  @NonNull
  @Override
  public NotiViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.noti_item, parent, false);
    return new NotiViewHolder(view, onDeleteButton, onRequestButton);
  }

  @Override
  public void onBindViewHolder(@NonNull NotiViewHolder holder, int position) {
    Notification notification = notifications.get(position);
    holder.title.setText(notification.getTitle());
    holder.content.setText(notification.getBody());
    holder.date.setText(DateFormatter.toDateString(notification.getCreatedDate()));
    if (notification.isRequest()) {
      holder.toRequestButton.setVisibility(View.VISIBLE);
    }
    if (notification.isSiteUpdated()) {
      holder.toRequestButton.setVisibility(View.VISIBLE);

      holder.toRequestButton.setText("To Detail");
    }
    holder.deleteButton.setOnClickListener(
        view -> {
          onDeleteButton.deleteItem(position);
        });
    holder.toRequestButton.setOnClickListener(
        view -> {
          onRequestButton.toRequest(position);
        });
  }

  @Override
  public int getItemCount() {
    if (notifications != null) {
      return notifications.size();
    }
    return 0;
  }

  public static class NotiViewHolder extends RecyclerView.ViewHolder {
    private OnDeleteButton onDeleteButton;
    private OnRequestButton onRequestButton;
    private Button deleteButton;
    private Button toRequestButton;

    private TextView title;
    private TextView content;
    private TextView date;

    public NotiViewHolder(
        @NonNull View itemView, OnDeleteButton onDeleteButton, OnRequestButton onRequestButton) {
      super(itemView);
      this.deleteButton = itemView.findViewById(R.id.deleteBtn);
      this.toRequestButton = itemView.findViewById(R.id.toRequestBtn);
      this.title = itemView.findViewById(R.id.notiTitle);
      this.content = itemView.findViewById(R.id.notiContent);
      this.date = itemView.findViewById(R.id.createdDate);
      this.onDeleteButton = onDeleteButton;
      this.onRequestButton = onRequestButton;
    }
  }

  public interface OnRequestButton {
    void toRequest(int position);
  }

  public interface OnDeleteButton {
    void deleteItem(int position);
  }
}
