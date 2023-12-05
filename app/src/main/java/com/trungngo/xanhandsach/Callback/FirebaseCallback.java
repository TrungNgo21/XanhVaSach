package com.trungngo.xanhandsach.Callback;

import java.util.List;

public interface FirebaseCallback<T> {
  void callbackListRes(List<T> listT);

  void callbackRes(T t);
}
