package com.trungngo.xanhandsach.Service;

import static android.content.ContentValues.TAG;

import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.trungngo.xanhandsach.Callback.FirebaseCallback;
import com.trungngo.xanhandsach.Shared.Result;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ImageService {
  private final FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
  private StorageReference storageReference;

  private final List<String> finalUrls = new ArrayList<>();

  public void uploadImages(
      List<Uri> images, String parent, final FirebaseCallback<Result<List<String>>> callback) {
    if (images.isEmpty()) {
      return;
    }
    storageReference = firebaseStorage.getReference().child(parent);
    images.forEach(
        image -> {
          UUID uuid = UUID.randomUUID();
          final StorageReference imageReference = storageReference.child(String.valueOf(uuid));
          UploadTask uploadTask = imageReference.putFile(image);
          uploadTask
              .continueWithTask(
                  new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task)
                        throws Exception {
                      if (!task.isSuccessful()) {
                        throw task.getException();
                      }
                      return imageReference.getDownloadUrl();
                    }
                  })
              .addOnCompleteListener(
                  new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                      if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();
                        Log.d("link", String.valueOf(downloadUri));

                        finalUrls.add(String.valueOf(downloadUri));
                        if (finalUrls.size() == images.size()) {
                          callback.callbackRes(new Result.Success<>(finalUrls));
                        }
                      } else {
                        Log.d(TAG, "The bug is that " + task.getException().getMessage());
                        callback.callbackRes(new Result.Error(task.getException()));
                      }
                    }
                  });
        });
  }
}
