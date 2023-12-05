package com.trungngo.xanhandsach.Shared;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.view.View;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Base64;

public class ImageHandler {
  public static String toBitMapImg(Bitmap bitmap, int previewWidth) {
    int previewHeight = bitmap.getHeight() * previewWidth / bitmap.getWidth();
    Bitmap previewBitmap = Bitmap.createScaledBitmap(bitmap, previewWidth, previewHeight, false);
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    previewBitmap.compress(Bitmap.CompressFormat.PNG, 50, byteArrayOutputStream);
    byte[] imageBytes = byteArrayOutputStream.toByteArray();
    return Base64.getEncoder().encodeToString(imageBytes);
  }

  public static Bitmap stringImageToBitMap(String image) {
    byte[] imageBytes = Base64.getDecoder().decode(image);
    return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
  }

  public static Bitmap getBitmapImg(Uri imageUri, Context context) {
    InputStream inputImg = null;
    try {
      inputImg = context.getContentResolver().openInputStream(imageUri);
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
    return BitmapFactory.decodeStream(inputImg);
  }

  public static void setImage(Bitmap bitmapImage, ImageView imageView) {
    imageView.setImageBitmap(bitmapImage);
  }
}
