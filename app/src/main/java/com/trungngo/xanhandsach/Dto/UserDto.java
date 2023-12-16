package com.trungngo.xanhandsach.Dto;

import com.trungngo.xanhandsach.Model.Notification;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
  private String id;
  private String email;
  private String fcmToken;
  private String permission;
  private String image;
  private String displayName;
  private double latitude;
  private double longitude;
  private String currentAddress;
  private String joinSiteId;
  private String siteId;
  private List<Notification> notifications;
}
