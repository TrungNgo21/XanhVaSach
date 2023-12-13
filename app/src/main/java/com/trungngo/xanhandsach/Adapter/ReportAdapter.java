package com.trungngo.xanhandsach.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.trungngo.xanhandsach.Model.Report;
import com.trungngo.xanhandsach.R;
import com.trungngo.xanhandsach.Shared.DateFormatter;

import java.util.List;

public class ReportAdapter extends RecyclerView.Adapter<ReportAdapter.ReportViewHolder> {
  private List<Report> reports;
  private Context context;

  public ReportAdapter(Context context) {
    this.context = context;
  }

  public void setData(List<Report> reports) {
    this.reports = reports;
    notifyDataSetChanged();
  }

  @NonNull
  @Override
  public ReportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View view =
        LayoutInflater.from(parent.getContext()).inflate(R.layout.report_item, parent, false);
    return new ReportViewHolder(view);
  }

  @Override
  public void onBindViewHolder(@NonNull ReportViewHolder holder, int position) {
    Report report = reports.get(position);
    holder.description.setText(report.getContent());
    holder.amountOfWaste.setText(String.valueOf(report.getAmount()));
    holder.createdDate.setText(DateFormatter.toDateString(report.getCreatedDate()));
  }

  @Override
  public int getItemCount() {
    if (reports != null) {
      return reports.size();
    }
    return 0;
  }

  public static class ReportViewHolder extends RecyclerView.ViewHolder {
    private TextView description;
    private TextView createdDate;
    private TextView amountOfWaste;

    public ReportViewHolder(@NonNull View itemView) {
      super(itemView);
      this.description = itemView.findViewById(R.id.description);
      this.createdDate = itemView.findViewById(R.id.createdDate);
      this.amountOfWaste = itemView.findViewById(R.id.amountWaste);
    }
  }
}
