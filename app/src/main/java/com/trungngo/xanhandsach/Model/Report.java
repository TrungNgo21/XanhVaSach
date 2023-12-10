package com.trungngo.xanhandsach.Model;

import java.util.Date;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Report {
  @Builder.Default private String id = UUID.randomUUID().toString();
  private String content;
  private double amount;
  private Date createdDate;
}
