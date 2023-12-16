package com.trungngo.xanhandsach.Model;

import com.trungngo.xanhandsach.Dto.UserDto;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Request {
  private UserDto volunteers;
  private Date sentDate;
}
