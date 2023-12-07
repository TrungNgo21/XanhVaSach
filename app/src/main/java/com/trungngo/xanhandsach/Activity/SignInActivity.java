package com.trungngo.xanhandsach.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.trungngo.xanhandsach.Callback.FirebaseCallback;
import com.trungngo.xanhandsach.Dto.UserDto;
import com.trungngo.xanhandsach.Service.UserService;
import com.trungngo.xanhandsach.Shared.Constant;
import com.trungngo.xanhandsach.Shared.Error;
import com.trungngo.xanhandsach.Shared.Result;
import com.trungngo.xanhandsach.databinding.ActivitySignInBinding;

import java.util.List;

public class SignInActivity extends AppCompatActivity {

  private ActivitySignInBinding activitySignInBinding;
  private UserService userService;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    activitySignInBinding = ActivitySignInBinding.inflate(getLayoutInflater());
    setContentView(activitySignInBinding.getRoot());
    userService = new UserService(this);
    if (userService.getLogInStatus()) {
      finish();
      startActivity(new Intent(this, MainActivity.class));
    }
    activitySignInBinding.loginButtonId.setEnabled(false);
    setLoading(false);
    setErrorMessage(activitySignInBinding.authenticatedFailedId, false);
    setButtonPressed();
    setUpToolbar();
    TextWatcher inputListener = setInputListener();
    activitySignInBinding.emailId.addTextChangedListener(inputListener);
    activitySignInBinding.passwordId.addTextChangedListener(inputListener);
  }

  private void setErrorMessage(TextView errorMessage, boolean isOn) {
    if (isOn) {
      errorMessage.setVisibility(View.VISIBLE);
    } else {
      errorMessage.setVisibility(View.INVISIBLE);
    }
  }

  private void setLoading(boolean isLoading) {
    if (isLoading) {
      activitySignInBinding.progressBarId.setVisibility(View.VISIBLE);
    } else {
      activitySignInBinding.progressBarId.setVisibility(View.INVISIBLE);
    }
  }

  private void login(String email, String password) {
    setLoading(true);
    setEnableFormControl(false);
    setErrorMessage(activitySignInBinding.authenticatedFailedId, false);
    userService.signIn(
        email,
        password,
        new FirebaseCallback<Result<UserDto>>() {
          @Override
          public void callbackListRes(List<Result<UserDto>> listT) {}

          @Override
          public void callbackRes(Result<UserDto> userDtoResult) {
            if (userDtoResult instanceof Result.Success) {
              finish();
              startActivity(new Intent(SignInActivity.this, MainActivity.class));
              setLoading(false);
            } else {
              setErrorMessage(activitySignInBinding.authenticatedFailedId, true);
              setLoading(false);
              setEnableFormControl(true);
              Log.d(Error.AUTHENTICATE_ERROR, userDtoResult.toString());
            }
          }
        });
  }

  private void setButtonPressed() {
    activitySignInBinding.loginButtonId.setOnClickListener(
        view -> {
          login(
              activitySignInBinding.emailId.getText().toString(),
              activitySignInBinding.passwordId.getText().toString());
        });
    activitySignInBinding.toSignUpButtonId.setOnClickListener(
        view -> {
          startActivity(new Intent(this, SignUpActivity.class));
        });
    activitySignInBinding.clearTemp.setOnClickListener(
        view -> {
          userService.signOut();
        });
  }

  private void setUpToolbar() {
    activitySignInBinding.toolbarId.toolbarTitleId.setText(Constant.KEY_SIGN_IN_TITLE);
    activitySignInBinding.toolbarId.backIcon.setVisibility(View.INVISIBLE);
    activitySignInBinding.toolbarId.menuIconId.setVisibility(View.INVISIBLE);
  }

  private void setEnableFormControl(boolean isEnabled) {
    activitySignInBinding.loginButtonId.setEnabled(isEnabled);
    activitySignInBinding.emailId.setEnabled(isEnabled);
    activitySignInBinding.passwordId.setEnabled(isEnabled);
  }

  private TextWatcher setInputListener() {
    return new TextWatcher() {
      @Override
      public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        activitySignInBinding.loginButtonId.setEnabled(
            !activitySignInBinding.emailId.getText().toString().isEmpty()
                && !activitySignInBinding.passwordId.getText().toString().isEmpty());
      }

      @Override
      public void onTextChanged(CharSequence s, int start, int before, int count) {}

      @Override
      public void afterTextChanged(Editable s) {
        activitySignInBinding.loginButtonId.setEnabled(
            !activitySignInBinding.emailId.getText().toString().isEmpty()
                && !activitySignInBinding.passwordId.getText().toString().isEmpty());
      }
    };
  }
}
