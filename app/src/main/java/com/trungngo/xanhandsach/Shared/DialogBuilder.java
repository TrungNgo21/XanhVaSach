package com.trungngo.xanhandsach.Shared;

import android.app.Dialog;
import android.content.Context;
import android.view.Window;

public class DialogBuilder {
  private Dialog dialog;
  private final Context context;

  private Window window;

  private DialogBuilder(Context context) {
    this.context = context;
  }

  public DialogBuilder builder() {
    dialog = new Dialog(context);
    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
    window = dialog.getWindow();
    return this;
  }

  public DialogBuilder withContentView(int resourceId) {
    dialog.setContentView(resourceId);
    return this;
  }

  public DialogBuilder withPosition(int position) {
    window.setGravity(position);
    return this;
  }

  public DialogBuilder withBg(int resourceId) {
    window.setBackgroundDrawableResource(resourceId);
    return this;
  }

  public DialogBuilder withLayout(int layoutW, int layoutH) {
    window.setLayout(layoutW, layoutH);
    return this;
  }

  public Dialog build() {
    return dialog;
  }
}
