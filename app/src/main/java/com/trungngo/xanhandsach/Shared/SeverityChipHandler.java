package com.trungngo.xanhandsach.Shared;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.trungngo.xanhandsach.R;

public class SeverityChipHandler {
  public static void chipDirective(String severity, View chipView, Context context) {
    if (severity.equals("Dirty")) {
      TextView severityText = chipView.findViewById(R.id.severityNameId);
      severityText.setText("Dirty");
      chipView.setBackgroundResource(R.drawable.warning_chip);
      severityText.setTextColor(context.getResources().getColor(R.color.warning));
    } else if (severity.equals("Quite Dirty")) {
      TextView severityText = chipView.findViewById(R.id.severityNameId);
      severityText.setText("Quite Dirty");
      chipView.setBackgroundResource(R.drawable.success_chip);
      severityText.setTextColor(context.getResources().getColor(R.color.primary_200));
    }
    if (severity.equals("Extremely Dirty")) {
      TextView severityText = chipView.findViewById(R.id.severityNameId);
      severityText.setText("Extremely Dirty");
      chipView.setBackgroundResource(R.drawable.error_chip);
      severityText.setTextColor(context.getResources().getColor(R.color.error));
    }
  }
}
