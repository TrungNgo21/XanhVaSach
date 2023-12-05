package com.trungngo.xanhandsach.Service;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.trungngo.xanhandsach.Activity.MainActivity;
import com.trungngo.xanhandsach.Callback.FirebaseCallback;
import com.trungngo.xanhandsach.Dto.UserDto;
import com.trungngo.xanhandsach.Model.User;
import com.trungngo.xanhandsach.Shared.Constant;
import com.trungngo.xanhandsach.Shared.Error;
import com.trungngo.xanhandsach.Shared.PreferenceManager;
import com.trungngo.xanhandsach.Shared.Result;

public class UserService {
  private final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
  private final FirebaseFirestore fireStore = FirebaseFirestore.getInstance();
  private final CollectionReference userReference =
      fireStore.collection(Constant.KEY_COLLECTION_USERS);
  private PreferenceManager preferenceManager;

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
                            preferenceManager.putCurrentUser(currentUser);
                            preferenceManager.putBoolean(Constant.KEY_SIGN_IN, true);
                            Result.Success<UserDto> signInRes = new Result.Success<>(currentUser);
                            callback.callbackRes(signInRes);
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
                UserDto createdUser = user.toDto();
                userReference
                    .add(createdUser)
                    .addOnCompleteListener(
                        addUserTask -> {
                          if (addUserTask.isSuccessful()) {
                            Result.Success<UserDto> userDtoRes = new Result.Success<>(createdUser);
                            callback.callbackRes(userDtoRes);
                          } else {
                            Result.Error userDtoErr = new Result.Error(addUserTask.getException());
                            callback.callbackRes(userDtoErr);
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
}
