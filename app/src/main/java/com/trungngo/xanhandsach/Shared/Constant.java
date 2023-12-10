package com.trungngo.xanhandsach.Shared;

import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.List;

public class Constant {

  public static final String KEY_DEFAULT_USER_IMG =
      "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";
  public static final String KEY_COLLECTION_USERS = "users";

  public static final String KEY_GOOGLE_MAP_API = "AIzaSyDWzf27VCIQxu47qtXGw-1G3-M4cxR7LqQ";

  public static final String KEY_COLLECTION_SITES = "sites";
  public static final String KEY_SHARED_CONTEXT = "XanhVaSach";
  public static final String KEY_CURRENT_USER = "currentUser";

  public static final String KEY_CACHE_REGISTER = "cacheUser";

  public static final String KEY_SIGN_IN = "isSignIn";
  public static final String KEY_PERMISSION_ADMIN = "super";
  public static final String KEY_PERMISSION_USER = "user";
  public static final String KEY_PERMISSION_USER_ADMIN = "admin";

  public static final String KEY_SIGN_IN_TITLE = "Sign In";
  public static final List<String> WASTE_AMOUNT =
      new ArrayList<>(asList("10", "100", "1000", "10000", "100000", "1000000"));
  public static final String KEY_SIGN_UP_TITLE = "Sign Up";

  public static final String KEY_SITE_ID = "siteId";

  public static final String KEY_NOT_ALLOWED_MORE_THAN_8 =
      "You are not allow to pick more than 8 images!";
}
