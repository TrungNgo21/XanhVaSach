package com.trungngo.xanhandsach.Activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.trungngo.xanhandsach.Callback.FirebaseCallback;
import com.trungngo.xanhandsach.Dto.UserDto;
import com.trungngo.xanhandsach.Model.User;
import com.trungngo.xanhandsach.R;
import com.trungngo.xanhandsach.Service.UserService;
import com.trungngo.xanhandsach.Shared.Constant;
import com.trungngo.xanhandsach.Shared.Error;
import com.trungngo.xanhandsach.Shared.ImageHandler;
import com.trungngo.xanhandsach.Shared.InputValidator;
import com.trungngo.xanhandsach.Shared.Result;
import com.trungngo.xanhandsach.databinding.ActivitySignUpBinding;

import java.util.List;

public class SignUpActivity extends AppCompatActivity {

  private ActivitySignUpBinding activitySignUpBinding;

  private UserService userService;

  private final String defaultUserImg =
      "https://firebasestorage.googleapis.com/v0/b/interestingtaste-9ec6d.appspot.com/o/default.jpg?alt=media&token=c6b84bd9-f3b8-48fc-ad68-d271da090338";
  private Uri profileImg;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    activitySignUpBinding = ActivitySignUpBinding.inflate(getLayoutInflater());
    setContentView(activitySignUpBinding.getRoot());
    userService = new UserService(this);
    initialSetup();
    setUpToolbar();
    setOnButtonPressed();
  }

  private void initialSetup() {
    setErrorMessage(activitySignUpBinding.displayNameError, false);
    setErrorMessage(activitySignUpBinding.emailError, false);
    setErrorMessage(activitySignUpBinding.passwordError, false);
    setErrorMessage(activitySignUpBinding.registerFailedId, false);
    activitySignUpBinding.registerButtonId.setEnabled(false);

    if (userService.getCacheRegister() != null) {
      User cacheUser = userService.getCacheRegister();
      if (cacheUser.getImage() != null) {
        Bitmap imageBitmap = ImageHandler.stringImageToBitMap(cacheUser.getImage());
        ImageHandler.setImage(imageBitmap, activitySignUpBinding.userImgId);
      }
      activitySignUpBinding.emailSignUp.setText(cacheUser.getEmail());
      activitySignUpBinding.passwordSignUp.setText(cacheUser.getPassword());
      activitySignUpBinding.displayNameSignUp.setText(cacheUser.getDisplayName());
      initialCheckInput();
    }
    setLoading(false);
    setInputListener();
  }

  private void setErrorMessage(TextView errorMessage, boolean isOn) {
    if (isOn) {
      errorMessage.setVisibility(View.VISIBLE);
    } else {
      errorMessage.setVisibility(View.INVISIBLE);
    }
  }

  private void initialCheckInput() {
    if (InputValidator.isValidEmail(activitySignUpBinding.emailSignUp.getText().toString().trim())
        && InputValidator.isValidPass(
            activitySignUpBinding.passwordSignUp.getText().toString(), 7, 10)
        && InputValidator.isValidNumCharacters(
            activitySignUpBinding.displayNameSignUp.getText().toString().trim(), 10, 20)) {

      activitySignUpBinding.registerButtonId.setEnabled(true);
    }
    if (!InputValidator.isValidEmail(
        activitySignUpBinding.emailSignUp.getText().toString().trim())) {
      setErrorMessage(activitySignUpBinding.emailError, true);
    } else {
      setErrorMessage(activitySignUpBinding.emailError, false);
    }
    if (!InputValidator.isValidPass(
        activitySignUpBinding.passwordSignUp.getText().toString(), 7, 10)) {
      setErrorMessage(activitySignUpBinding.passwordError, true);
    } else {
      setErrorMessage(activitySignUpBinding.passwordError, false);
    }
    if (!InputValidator.isValidNumCharacters(
        activitySignUpBinding.displayNameSignUp.getText().toString().trim(), 10, 20)) {
      setErrorMessage(activitySignUpBinding.displayNameError, true);
    } else {
      setErrorMessage(activitySignUpBinding.displayNameError, false);
    }
  }

  private void setLoading(boolean isLoading) {
    if (isLoading) {
      activitySignUpBinding.progressBarId.setVisibility(View.VISIBLE);
    } else {
      activitySignUpBinding.progressBarId.setVisibility(View.INVISIBLE);
    }
  }

  private void setOnButtonPressed() {
    activitySignUpBinding.toSignInButtonId.setOnClickListener(
        view -> {
          finish();
          userService.cacheRegister(cacheUser());
        });
    activitySignUpBinding.toolbarId.backIcon.setOnClickListener(
        view -> {
          finish();
          userService.cacheRegister(cacheUser());
        });
    activitySignUpBinding.userImgId.setOnClickListener(
        view -> {
          openFileChosen();
        });
    activitySignUpBinding.registerButtonId.setOnClickListener(
        view -> {
          userService.cacheRegister(cacheUser());
          User previousCacheUser = userService.getCacheRegister();
          setEnableFormControl(false);
          setErrorMessage(activitySignUpBinding.registerFailedId, false);
          setLoading(true);
          userService.signUp(
              previousCacheUser,
              new FirebaseCallback<Result<UserDto>>() {
                @Override
                public void callbackListRes(List<Result<UserDto>> listT) {}

                @Override
                public void callbackRes(Result<UserDto> userDtoResult) {
                  if (userDtoResult instanceof Result.Success) {
                    finish();
                    startActivity(new Intent(SignUpActivity.this, MainActivity.class));
                    setLoading(false);
                  } else {
                    setErrorMessage(activitySignUpBinding.registerFailedId, true);
                    setLoading(false);
                    setEnableFormControl(true);
                    if (((Result.Error) userDtoResult)
                        .getError()
                        .toString()
                        .contains("The email address is already in use by another account")) {
                      activitySignUpBinding.registerFailedId.setText("Email already exist!");
                    }
                    Log.d(Error.AUTHENTICATE_ERROR, userDtoResult.toString());
                  }
                }
              });
        });
  }

  private void setEnableFormControl(boolean isEnabled) {
    activitySignUpBinding.displayNameSignUp.setEnabled(isEnabled);
    activitySignUpBinding.emailSignUp.setEnabled(isEnabled);
    activitySignUpBinding.passwordSignUp.setEnabled(isEnabled);
    activitySignUpBinding.userImgId.setEnabled(isEnabled);
  }

  private final ActivityResultLauncher<Intent> pickImage =
      registerForActivityResult(
          new ActivityResultContracts.StartActivityForResult(),
          new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult activityResult) {
              int result = activityResult.getResultCode();
              Intent data = activityResult.getData();
              if (result == RESULT_OK && data != null) {
                profileImg = data.getData();
                Bitmap toBitMapImg = ImageHandler.getBitmapImg(profileImg, SignUpActivity.this);
                ImageHandler.setImage(toBitMapImg, activitySignUpBinding.userImgId);
              }
            }
          });

  private void openFileChosen() {
    Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
    pickImage.launch(intent);
  }

  private void setUpToolbar() {
    activitySignUpBinding.toolbarId.menuIconId.setVisibility(View.INVISIBLE);
    activitySignUpBinding.toolbarId.backIcon.setVisibility(View.VISIBLE);
    activitySignUpBinding.toolbarId.toolbarTitleId.setText(Constant.KEY_SIGN_UP_TITLE);
  }

  private void setInputListener() {
    TextWatcher onInputChange =
        new TextWatcher() {
          @Override
          public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            if (InputValidator.isValidEmail(
                    activitySignUpBinding.emailSignUp.getText().toString().trim())
                && InputValidator.isValidPass(
                    activitySignUpBinding.passwordSignUp.getText().toString(), 7, 10)
                && InputValidator.isValidNumCharacters(
                    activitySignUpBinding.displayNameSignUp.getText().toString().trim(), 10, 20)) {

              activitySignUpBinding.registerButtonId.setEnabled(true);

            } else {
              activitySignUpBinding.registerButtonId.setEnabled(false);
            }
          }

          @Override
          public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (!InputValidator.isValidEmail(
                activitySignUpBinding.emailSignUp.getText().toString().trim())) {
              setErrorMessage(activitySignUpBinding.emailError, true);
            } else {
              setErrorMessage(activitySignUpBinding.emailError, false);
            }
            if (!InputValidator.isValidPass(
                activitySignUpBinding.passwordSignUp.getText().toString(), 7, 10)) {
              setErrorMessage(activitySignUpBinding.passwordError, true);
            } else {
              setErrorMessage(activitySignUpBinding.passwordError, false);
            }
            if (!InputValidator.isValidNumCharacters(
                activitySignUpBinding.displayNameSignUp.getText().toString().trim(), 10, 20)) {
              setErrorMessage(activitySignUpBinding.displayNameError, true);
            } else {
              setErrorMessage(activitySignUpBinding.displayNameError, false);
            }
          }

          @Override
          public void afterTextChanged(Editable s) {
            if (InputValidator.isValidEmail(
                    activitySignUpBinding.emailSignUp.getText().toString().trim())
                && InputValidator.isValidPass(
                    activitySignUpBinding.passwordSignUp.getText().toString(), 7, 10)
                && InputValidator.isValidNumCharacters(
                    activitySignUpBinding.displayNameSignUp.getText().toString().trim(), 10, 20)) {
              activitySignUpBinding.registerButtonId.setEnabled(true);
            } else {
              activitySignUpBinding.registerButtonId.setEnabled(false);
            }
          }
        };
    activitySignUpBinding.emailSignUp.addTextChangedListener(onInputChange);
    activitySignUpBinding.displayNameSignUp.addTextChangedListener(onInputChange);
    activitySignUpBinding.passwordSignUp.addTextChangedListener(onInputChange);
  }

  private User cacheUser() {
    User previousCache = userService.getCacheRegister();
    User cache =
        User.builder()
            .displayName(activitySignUpBinding.displayNameSignUp.getText().toString().trim())
            .email(activitySignUpBinding.emailSignUp.getText().toString().trim())
            .password(activitySignUpBinding.passwordSignUp.getText().toString())
            .build();
    if (profileImg != null) {
      cache.setImage(
          ImageHandler.toBitMapImg(
              ((BitmapDrawable) activitySignUpBinding.userImgId.getDrawable()).getBitmap(),
              activitySignUpBinding.userImgId.getWidth()));
      return cache;
    }
    if (previousCache == null) {
      cache.setImage(Constant.KEY_DEFAULT_USER_IMG);
    } else {
      if (previousCache.getImage() != null || !previousCache.getImage().isEmpty()) {
        cache.setImage(
            ImageHandler.toBitMapImg(
                ((BitmapDrawable) activitySignUpBinding.userImgId.getDrawable()).getBitmap(),
                activitySignUpBinding.userImgId.getWidth()));
        return cache;
      }
    }
    return cache;
  }
}
