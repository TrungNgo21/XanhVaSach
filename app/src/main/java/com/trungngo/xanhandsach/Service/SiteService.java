package com.trungngo.xanhandsach.Service;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.trungngo.xanhandsach.Shared.Constant;
import com.trungngo.xanhandsach.Shared.PreferenceManager;

public class SiteService {
  private final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
  private final FirebaseFirestore fireStore = FirebaseFirestore.getInstance();
  private final CollectionReference siteReference =
      fireStore.collection(Constant.KEY_COLLECTION_SITES);
  private PreferenceManager preferenceManager;

  public SiteService(PreferenceManager preferenceManager) {
    this.preferenceManager = preferenceManager;
  }
}
