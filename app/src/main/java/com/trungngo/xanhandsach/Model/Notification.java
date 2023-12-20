package com.trungngo.xanhandsach.Model;

import java.util.Date;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Notification {
  private String title;
  private String body;
  private String fcmToken;
  @Builder.Default private boolean isRequest = false;
  @Builder.Default private boolean isSiteUpdated = false;
  private String siteId;
  private Date createdDate;
}
