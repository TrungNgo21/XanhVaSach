package com.trungngo.xanhandsach.Model;

import com.trungngo.xanhandsach.Shared.Constant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
  private String id;
  private String email;
  private String password;
  @Builder.Default private String fcmToken = null;
  @Builder.Default private String permission = Constant.KEY_PERMISSION_USER;
}
