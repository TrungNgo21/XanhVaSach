package com.trungngo.xanhandsach.Activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.trungngo.xanhandsach.Dto.UserDto;
import com.trungngo.xanhandsach.R;
import com.trungngo.xanhandsach.Service.UserService;
import com.trungngo.xanhandsach.Shared.Constant;
import com.trungngo.xanhandsach.Shared.ImageHandler;
import com.trungngo.xanhandsach.Shared.PreferenceManager;
import com.trungngo.xanhandsach.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

  private UserService userService;
  private ActivityMainBinding activityMainBinding;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    activityMainBinding = ActivityMainBinding.inflate(getLayoutInflater());
    setContentView(activityMainBinding.getRoot());
    userService = new UserService(this);
    UserDto currentUser = userService.getCurrentUser();
    activityMainBinding.testing.setText(currentUser.getEmail());
    ImageHandler.setImage(
        ImageHandler.stringImageToBitMap(currentUser.getImage()), activityMainBinding.testingImg);
    activityMainBinding.testing.setOnClickListener(
        view -> {
          userService.signOut();
          finish();
          startActivity(new Intent(this, SignInActivity.class));
        });
  }
}
