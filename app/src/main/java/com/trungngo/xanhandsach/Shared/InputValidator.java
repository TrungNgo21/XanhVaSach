package com.trungngo.xanhandsach.Shared;

import android.text.TextUtils;
import android.util.Patterns;

public class InputValidator {
  public static boolean isValidEmail(String email) {
    return !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches();
  }

  public static boolean isValidPass(String password, int lowerBound, int upperBound) {
    return password.length() >= lowerBound && password.length() <= upperBound;
  }

  public static boolean isValidNumCharacters(String input, int lowerBound, int upperBound) {
    return input.length() >= lowerBound && input.length() <= upperBound;
  }
}
