package com.trungngo.xanhandsach.Model;

import com.trungngo.xanhandsach.Dto.SiteDto;
import com.trungngo.xanhandsach.Dto.UserDto;
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
  private String image;
  private String displayName;
  private String siteId;
  @Builder.Default private String fcmToken = null;
  @Builder.Default private String permission = Constant.KEY_PERMISSION_USER;

  public UserDto toDto() {
    return UserDto.builder()
        .id(id)
        .email(email)
        .fcmToken(fcmToken)
        .permission(permission)
        .image(image)
        .displayName(displayName)
        .siteId(siteId)
        .build();
  }
}
