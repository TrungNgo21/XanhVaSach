package com.trungngo.xanhandsach.Dto;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.trungngo.xanhandsach.Model.Report;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@Data
@NoArgsConstructor
public class SiteDto {
  private UserDto owner;
  private String id;
  private int maxCapacity;
  private String displayName;
  private String severity;
  private Double latitude;
  private Double longitude;
  private String description;
  private List<String> imageUrl;
  private List<UserDto> volunteers;
  private List<Report> reports;
  private String createdDate;
  private String updatedDate;
  private String address;
}
