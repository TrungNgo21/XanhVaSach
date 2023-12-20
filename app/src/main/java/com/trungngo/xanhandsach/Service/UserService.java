package com.trungngo.xanhandsach.Service;

import static java.util.Arrays.asList;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.material.dialog.InsetDialogOnTouchListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.trungngo.xanhandsach.Activity.MainActivity;
import com.trungngo.xanhandsach.Callback.FirebaseCallback;
import com.trungngo.xanhandsach.Dto.SiteDto;
import com.trungngo.xanhandsach.Dto.UserDto;
import com.trungngo.xanhandsach.Model.Notification;
import com.trungngo.xanhandsach.Model.User;
import com.trungngo.xanhandsach.Shared.Constant;
import com.trungngo.xanhandsach.Shared.Error;
import com.trungngo.xanhandsach.Shared.PreferenceManager;
import com.trungngo.xanhandsach.Shared.Result;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class UserService {
  private final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
  private final FirebaseFirestore fireStore = FirebaseFirestore.getInstance();
  private final CollectionReference userReference =
      fireStore.collection(Constant.KEY_COLLECTION_USERS);
  public static final MediaType JSON = MediaType.get("application/json");
  private static final String url = "https://fcm.googleapis.com/fcm/send";

  private SiteService siteService;

  private PreferenceManager preferenceManager;

  public UserService(Context context, SiteService siteService) {
    this.preferenceManager = new PreferenceManager(context.getApplicationContext());
    this.siteService = siteService;
  }

  public UserService(Context context) {
    this.preferenceManager = new PreferenceManager(context.getApplicationContext());
  }

  public void signIn(
      String email, String password, final FirebaseCallback<Result<UserDto>> callback) {
    firebaseAuth
        .signInWithEmailAndPassword(email, password)
        .addOnCompleteListener(
            task -> {
              if (task.isSuccessful()) {
                String firebaseUserId = firebaseAuth.getCurrentUser().getUid();
                userReference
                    .document(firebaseUserId)
                    .get()
                    .addOnCompleteListener(
                        getUserTask -> {
                          if (getUserTask.isSuccessful()) {
                            DocumentSnapshot userDocument = getUserTask.getResult();
                            UserDto currentUser = userDocument.toObject(UserDto.class);
                            currentUser.setId(firebaseUserId);
                            if (currentUser.getFcmToken() == null) {
                              getFCMToken(
                                  new FirebaseCallback<Result<String>>() {
                                    @Override
                                    public void callbackListRes(List<Result<String>> listT) {}

                                    @Override
                                    public void callbackRes(Result<String> stringResult) {
                                      if (stringResult instanceof Result.Success) {
                                        String fcmToken =
                                            ((Result.Success<String>) stringResult).getData();
                                        currentUser.setFcmToken(fcmToken);
                                        updateServerFcmToken(
                                            firebaseUserId,
                                            fcmToken,
                                            new FirebaseCallback<Result<String>>() {
                                              @Override
                                              public void callbackListRes(
                                                  List<Result<String>> listT) {}

                                              @Override
                                              public void callbackRes(Result<String> stringResult) {
                                                if (stringResult instanceof Result.Success) {
                                                  preferenceManager.putCurrentUser(currentUser);
                                                  preferenceManager.putBoolean(
                                                      Constant.KEY_SIGN_IN, true);
                                                  Result.Success<UserDto> signInRes =
                                                      new Result.Success<>(currentUser);
                                                  callback.callbackRes(signInRes);
                                                } else {
                                                  Log.d("FCM Token", "Fail to set token");
                                                }
                                              }
                                            });
                                      }
                                    }
                                  });
                            } else {
                              preferenceManager.putCurrentUser(currentUser);
                              preferenceManager.putBoolean(Constant.KEY_SIGN_IN, true);
                              Result.Success<UserDto> signInRes = new Result.Success<>(currentUser);
                              callback.callbackRes(signInRes);
                            }
                          } else {
                            Result.Error signInErr = new Result.Error(getUserTask.getException());
                            callback.callbackRes(signInErr);
                          }
                        });
              } else {
                Result.Error signInErr = new Result.Error(task.getException());
                callback.callbackRes(signInErr);
              }
            });
  }

  public void signUp(User user, final FirebaseCallback<Result<UserDto>> callback) {
    firebaseAuth
        .createUserWithEmailAndPassword(user.getEmail(), user.getPassword())
        .addOnCompleteListener(
            createUserTask -> {
              if (createUserTask.isSuccessful()) {
                FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                UserDto createdUser = user.toDto();
                createdUser.setId(firebaseUser.getUid());
                getFCMToken(
                    new FirebaseCallback<Result<String>>() {
                      @Override
                      public void callbackListRes(List<Result<String>> listT) {}

                      @Override
                      public void callbackRes(Result<String> stringResult) {
                        if (stringResult instanceof Result.Success) {
                          createdUser.setFcmToken(
                              ((Result.Success<String>) stringResult).getData());
                          userReference
                              .document(createdUser.getId())
                              .set(createdUser)
                              .addOnCompleteListener(
                                  addUserTask -> {
                                    if (addUserTask.isSuccessful()) {
                                      Result.Success<UserDto> userDtoRes =
                                          new Result.Success<>(createdUser);
                                      preferenceManager.putCurrentUser(createdUser);
                                      preferenceManager.putBoolean(Constant.KEY_SIGN_IN, true);
                                      callback.callbackRes(userDtoRes);
                                    } else {
                                      Result.Error userDtoErr =
                                          new Result.Error(addUserTask.getException());
                                      callback.callbackRes(userDtoErr);
                                    }
                                  });
                        }
                      }
                    });

              } else {
                Result.Error userDtoErr = new Result.Error(createUserTask.getException());
                callback.callbackRes(userDtoErr);
              }
            });
  }

  public void signOut() {
    preferenceManager.clear();
  }

  public boolean getLogInStatus() {
    return preferenceManager.getBoolean(Constant.KEY_SIGN_IN);
  }

  public UserDto getCurrentUser() {
    return preferenceManager.getCurrentUser();
  }

  public void setCurrentUser(UserDto currentUser) {
    preferenceManager.putCurrentUser(currentUser);
  }

  public void cacheRegister(User cacheUser) {
    if ((cacheUser.getImage().isEmpty()
            || cacheUser.getImage().equals(Constant.KEY_DEFAULT_USER_IMG))
        && cacheUser.getPassword().isEmpty()
        && cacheUser.getDisplayName().isEmpty()
        && cacheUser.getEmail().isEmpty()) {
      return;
    }
    preferenceManager.putCacheUser(cacheUser);
  }

  public User getCacheRegister() {
    return preferenceManager.getCacheUser();
  }

  public void clearRegisterCache() {
    preferenceManager.clearCacheUser();
  }

  public void updateUserSiteId(String siteId, final FirebaseCallback<Result<UserDto>> callback) {
    userReference
        .document(getCurrentUser().getId())
        .update("siteId", siteId)
        .addOnCompleteListener(
            updateTask -> {
              if (updateTask.isSuccessful()) {
                callback.callbackRes(new Result.Success<>(getCurrentUser()));
              } else {
                callback.callbackRes(new Result.Error(updateTask.getException()));
              }
            });
  }

  private void updateServerFcmToken(
      String id, String token, final FirebaseCallback<Result<String>> callback) {
    userReference
        .document(id)
        .update("fcmToken", token)
        .addOnCompleteListener(
            updateTask -> {
              if (updateTask.isSuccessful()) {
                callback.callbackRes(new Result.Success<>(token));
                Log.d("update token", token);
              } else {
                callback.callbackRes(new Result.Error(updateTask.getException()));
                Log.d("update token", updateTask.getException().toString());
              }
            });
  }

  private void getFCMToken(final FirebaseCallback<Result<String>> callback) {
    FirebaseMessaging.getInstance()
        .getToken()
        .addOnCompleteListener(
            getTokenTask -> {
              if (getTokenTask.isSuccessful()) {
                String fcmToken = getTokenTask.getResult();
                callback.callbackRes(new Result.Success<>(fcmToken));
              } else {
                callback.callbackRes(new Result.Error(getTokenTask.getException()));

                Log.d("Get Fcm token:", getTokenTask.getException().toString());
              }
            });
  }

  public void acceptRequest(
      UserDto volunteer, SiteDto site, final FirebaseCallback<Result<List<UserDto>>> callback) {
    site.setVolunteers(new ArrayList<>(asList(volunteer)));
    siteService.updateSiteVolunteers(
        site.getId(),
        volunteer,
        new FirebaseCallback<Result<UserDto>>() {
          @Override
          public void callbackListRes(List<Result<UserDto>> listT) {}

          @Override
          public void callbackRes(Result<UserDto> userDtoResult) {
            if (userDtoResult instanceof Result.Success) {
              callback.callbackRes(new Result.Success<>(site));
            } else {
              callback.callbackRes(new Result.Error(new Exception("cannot apply")));
            }
          }
        });
  }

  public void applyForVolunteer(
      SiteDto site,
      final FirebaseCallback<Result<com.trungngo.xanhandsach.Model.Request>> callback) {
    Notification request =
        Notification.builder()
            .fcmToken(site.getOwner().getFcmToken())
            .title("Request for volunteer")
            .body(
                "Dear, "
                    + site.getOwner().getDisplayName()
                    + "\n"
                    + "I am "
                    + getCurrentUser().getDisplayName()
                    + ". I want to apply for your site. Please approve =>>")
            .createdDate(new Date())
            .isRequest(true)
            .build();
    sendNotification(
        request,
        site.getOwner().getId(),
        new FirebaseCallback<Result<Notification>>() {
          @Override
          public void callbackListRes(List<Result<Notification>> listT) {}

          @Override
          public void callbackRes(Result<Notification> notificationResult) {
            if (notificationResult instanceof Result.Success) {
              Log.d("noti update", "Success");
            } else {
              Log.d("noti update", "Error");
            }
          }
        });
    com.trungngo.xanhandsach.Model.Request volunteerRequest =
        com.trungngo.xanhandsach.Model.Request.builder()
            .sentDate(new Date())
            .volunteers(getCurrentUser())
            .toSite(site)
            .build();
    siteService.updateRequest(
        site.getId(),
        volunteerRequest,
        new FirebaseCallback<Result<com.trungngo.xanhandsach.Model.Request>>() {
          @Override
          public void callbackListRes(List<Result<com.trungngo.xanhandsach.Model.Request>> listT) {}

          @Override
          public void callbackRes(Result<com.trungngo.xanhandsach.Model.Request> requestResult) {
            if (requestResult instanceof Result.Success) {
              com.trungngo.xanhandsach.Model.Request volunteerRequest =
                  ((Result.Success<com.trungngo.xanhandsach.Model.Request>) requestResult)
                      .getData();
              callback.callbackRes(new Result.Success<>(volunteerRequest));
            } else {
              callback.callbackRes(new Result.Error(new Exception(volunteerRequest.toString())));
            }
          }
        });
  }

  //  private void updateUserNotification(String userId, Notification){
  //      userReference.document(userId).update("notifications", FieldValue.arrayUnion())
  //  }

  public void sendNotification(
      Notification notification,
      String userId,
      final FirebaseCallback<Result<Notification>> callback) {
    userReference
        .document(userId)
        .update("notifications", FieldValue.arrayUnion(notification))
        .addOnCompleteListener(
            updateNotiTask -> {
              if (updateNotiTask.isSuccessful()) {
                callback.callbackRes(new Result.Success<>(notification));
              } else {
                callback.callbackRes(new Result.Error(updateNotiTask.getException()));
              }
            });
  }

  public void getNotifications(String userId, final FirebaseCallback<Result<UserDto>> callback) {
    userReference
        .document(userId)
        .get()
        .addOnCompleteListener(
            userTask -> {
              if (userTask.isSuccessful()) {
                DocumentSnapshot documentSnapshot = userTask.getResult();
                UserDto user = documentSnapshot.toObject(UserDto.class);
                user.setId(getCurrentUser().getId());
                callback.callbackRes(new Result.Success<>(user));
              } else {
                callback.callbackRes(new Result.Error(userTask.getException()));
              }
            });
  }

  public void updateNotifications(
      String userId,
      List<Notification> notifications,
      final FirebaseCallback<Result<List<Notification>>> callback) {
    userReference
        .document(userId)
        .update("notifications", notifications)
        .addOnCompleteListener(
            updateTask -> {
              if (updateTask.isSuccessful()) {
                callback.callbackRes(new Result.Success<>(notifications));
              } else {
                callback.callbackRes(new Result.Error(updateTask.getException()));
              }
            });
  }

  private void callApi(JSONObject jsonObject) {
    OkHttpClient client = new OkHttpClient();

    RequestBody body = RequestBody.create(jsonObject.toString(), JSON);
    Request request =
        new Request.Builder()
            .url(url)
            .post(body)
            .header("Authorization", "Bearer " + Constant.KEY_SERVER_MESSAGE)
            .build();
    client.newCall(request);
    //        .enqueue(
    //            new Callback() {
    //              @Override
    //              public void onFailure(@NonNull Call call, @NonNull IOException e) {}
    //
    //              @Override
    //              public void onResponse(@NonNull Call call, @NonNull Response response)
    //                  throws IOException {}
    //            });
  }
}
