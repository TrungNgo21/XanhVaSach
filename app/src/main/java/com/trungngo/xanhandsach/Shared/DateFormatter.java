package com.trungngo.xanhandsach.Shared;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateFormatter {
  private static SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
  ;

  public static String toDateString(Date date) {
    return formatter.format(date);
  }

  public static Date toDate(String date) {
    try {
      return formatter.parse(date);
    } catch (ParseException e) {
      throw new RuntimeException(e);
    }
  }
}
