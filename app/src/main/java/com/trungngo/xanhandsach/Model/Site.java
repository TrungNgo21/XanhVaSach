package com.trungngo.xanhandsach.Model;

import com.trungngo.xanhandsach.Dto.SiteDto;
import com.trungngo.xanhandsach.Dto.UserDto;
import com.trungngo.xanhandsach.Shared.DateFormatter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Site {
  private UserDto owner;
  private String id;
  private int maxCapacity;
  private String displayName;
  private String severity;
  private Double latitude;
  private Double longitude;
  private String address;
  private String description;
  private boolean isDeleted;
  private List<Report> reports;
  @Builder.Default private List<String> imageUrl = new ArrayList<>();
  @Builder.Default private List<UserDto> volunteers = new ArrayList<>();
  @Builder.Default private Date createdDate = new Date();
  @Builder.Default private Date updatedDate = new Date();

  public SiteDto toDto() {
    return SiteDto.builder()
        .id(id)
        .owner(owner)
        .description(description)
        .displayName(displayName)
        .volunteers(volunteers)
        .severity(severity)
        .maxCapacity(maxCapacity)
        .address(address)
        .createdDate(DateFormatter.toDateString(createdDate))
        .updatedDate(DateFormatter.toDateString(createdDate))
        .imageUrl(imageUrl)
        .latitude(latitude)
        .longitude(longitude)
        .build();
  }
}
