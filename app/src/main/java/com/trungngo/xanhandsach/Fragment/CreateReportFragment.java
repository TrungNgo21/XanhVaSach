package com.trungngo.xanhandsach.Fragment;

import android.app.DatePickerDialog;
import android.graphics.Typeface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.Toast;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.trungngo.xanhandsach.Callback.FirebaseCallback;
import com.trungngo.xanhandsach.Model.Report;
import com.trungngo.xanhandsach.R;
import com.trungngo.xanhandsach.Service.SiteService;
import com.trungngo.xanhandsach.Service.UserService;
import com.trungngo.xanhandsach.Shared.Constant;
import com.trungngo.xanhandsach.Shared.DateFormatter;
import com.trungngo.xanhandsach.Shared.Result;
import com.trungngo.xanhandsach.databinding.FragmentReportBinding;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class CreateReportFragment extends Fragment {

  private FragmentReportBinding fragmentReportBinding;

  private SiteService siteService;

  private UserService userService;

  private Report report = Report.builder().build();

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setUpChipGroup();
    fragmentReportBinding.wasteAmount.addTextChangedListener(onInputListener());
    siteService = new SiteService(requireContext());
    userService = new UserService(requireContext());
    initialSetUp();
    setUpDatePicker();
  }

  private void initialSetUp() {
    fragmentReportBinding.selectedDate.setText(DateFormatter.toDateString(new Date()));
    report.setCreatedDate(new Date());
  }

  private void setIsLoading(boolean isLoading) {
    if (isLoading) fragmentReportBinding.progressBarHolder.setVisibility(View.VISIBLE);
    else {
      fragmentReportBinding.progressBarHolder.setVisibility(View.INVISIBLE);
    }
  }

  private void setUpDatePicker() {
    fragmentReportBinding.datePicker.setOnClickListener(
        view -> {
          Calendar calendar = Calendar.getInstance();
          int day = calendar.get(Calendar.DAY_OF_MONTH);
          int month = calendar.get(Calendar.MONTH);
          int year = calendar.get(calendar.YEAR);
          DatePickerDialog datePickerDialog =
              new DatePickerDialog(
                  requireContext(),
                  new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                      Date date = new Date(year, month + 1, dayOfMonth);
                      report.setCreatedDate(date);
                      fragmentReportBinding.selectedDate.setText(DateFormatter.toDateString(date));
                    }
                  },
                  year,
                  month,
                  day);
          datePickerDialog.show();
        });

    fragmentReportBinding.createReportButton.setOnClickListener(
        view -> {
          setIsLoading(true);
          report.setContent(fragmentReportBinding.description.getText().toString());
          report.setAmount(
              Double.parseDouble(fragmentReportBinding.wasteAmount.getText().toString()));
          siteService.updateSiteReports(
              userService.getCurrentUser().getSiteId(),
              report,
              new FirebaseCallback<Result<Report>>() {
                @Override
                public void callbackListRes(List<Result<Report>> listT) {}

                @Override
                public void callbackRes(Result<Report> reportResult) {
                  if (reportResult instanceof Result.Success) {
                    Toast.makeText(
                            requireContext(), "Add report successfully !", Toast.LENGTH_SHORT)
                        .show();

                    setIsLoading(false);
                  } else {
                    Toast.makeText(requireContext(), reportResult.toString(), Toast.LENGTH_SHORT)
                        .show();
                    setIsLoading(false);
                  }
                }
              });
        });
  }

  private void setUpChipGroup() {
    fragmentReportBinding = FragmentReportBinding.inflate(getLayoutInflater());
    for (String wasteAmount : Constant.WASTE_AMOUNT) {
      Chip chipView =
          (Chip) LayoutInflater.from(requireContext()).inflate(R.layout.custom_chip, null);
      chipView.setText(wasteAmount);
      chipView.setId(new Random().nextInt());
      float width = getResources().getDimension(com.intuit.sdp.R.dimen._90sdp);
      chipView.setLayoutParams(
          new ViewGroup.LayoutParams((int) width, ViewGroup.LayoutParams.WRAP_CONTENT));
      chipView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
      chipView.setTypeface(null, Typeface.BOLD);
      fragmentReportBinding.wasteAmountGroup.addView(chipView);
      fragmentReportBinding.wasteAmountGroup.setOnCheckedStateChangeListener(
          new ChipGroup.OnCheckedStateChangeListener() {
            @Override
            public void onCheckedChanged(
                @NonNull ChipGroup chipGroup, @NonNull List<Integer> list) {
              if (list.isEmpty()) {
                for (int i = 0; i < fragmentReportBinding.wasteAmountGroup.getChildCount(); i++) {
                  View child = fragmentReportBinding.wasteAmountGroup.getChildAt(i);
                  if (child instanceof Chip) {
                    Chip chip = (Chip) child;
                    if (!chip.getText()
                        .toString()
                        .equals(fragmentReportBinding.wasteAmount.getText().toString())) {

                    } else {
                      chip.setChecked(true);
                    }
                  }
                }

                fragmentReportBinding.wasteAmount.setSelection(
                    fragmentReportBinding.wasteAmount.getText().length());
              } else {
                Chip chip = fragmentReportBinding.getRoot().findViewById(list.get(0));
                fragmentReportBinding.wasteAmount.setText(chip.getText().toString());
                fragmentReportBinding.wasteAmount.setSelection(
                    fragmentReportBinding.wasteAmount.getText().length());
              }
            }
          });
    }
  }

  private TextWatcher onInputListener() {
    return new TextWatcher() {
      @Override
      public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        for (int i = 0; i < fragmentReportBinding.wasteAmountGroup.getChildCount(); i++) {
          View child = fragmentReportBinding.wasteAmountGroup.getChildAt(i);
          if (child instanceof Chip) {
            Chip chip = (Chip) child;
            if (chip.getText()
                .toString()
                .equals(fragmentReportBinding.wasteAmount.getText().toString())) {
              chip.setChecked(true);
            } else {
              chip.setChecked(false);
            }
          }
        }
      }

      @Override
      public void onTextChanged(CharSequence s, int start, int before, int count) {
        for (int i = 0; i < fragmentReportBinding.wasteAmountGroup.getChildCount(); i++) {
          View child = fragmentReportBinding.wasteAmountGroup.getChildAt(i);
          if (child instanceof Chip) {
            Chip chip = (Chip) child;
            if (chip.getText()
                .toString()
                .equals(fragmentReportBinding.wasteAmount.getText().toString())) {
              chip.setChecked(true);
            } else {
              chip.setChecked(false);
            }
          }
        }
      }

      @Override
      public void afterTextChanged(Editable s) {
        for (int i = 0; i < fragmentReportBinding.wasteAmountGroup.getChildCount(); i++) {
          View child = fragmentReportBinding.wasteAmountGroup.getChildAt(i);
          if (child instanceof Chip) {
            Chip chip = (Chip) child;
            if (chip.getText()
                .toString()
                .equals(fragmentReportBinding.wasteAmount.getText().toString())) {
              chip.setChecked(true);
            }
          }
        }
      }
    };
  }

  @Override
  public View onCreateView(
      LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    return fragmentReportBinding.getRoot();
  }
}
