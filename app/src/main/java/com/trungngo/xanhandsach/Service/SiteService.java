package com.trungngo.xanhandsach.Service;

import android.content.Context;
import android.util.Log;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.trungngo.xanhandsach.Callback.FirebaseCallback;
import com.trungngo.xanhandsach.Dto.SiteDto;
import com.trungngo.xanhandsach.Dto.UserDto;
import com.trungngo.xanhandsach.Model.Report;
import com.trungngo.xanhandsach.Model.Request;
import com.trungngo.xanhandsach.Model.Site;
import com.trungngo.xanhandsach.Model.User;
import com.trungngo.xanhandsach.Shared.Constant;
import com.trungngo.xanhandsach.Shared.PreferenceManager;
import com.trungngo.xanhandsach.Shared.Result;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.Nullable;

public class SiteService {
  private final FirebaseFirestore fireStore = FirebaseFirestore.getInstance();
  private final CollectionReference siteReference =
      fireStore.collection(Constant.KEY_COLLECTION_SITES);

  private UserService userService;

  private PreferenceManager preferenceManager;

  public SiteService(Context context) {
    this.preferenceManager = new PreferenceManager(context.getApplicationContext());
    this.userService = new UserService(context);
  }

  public List<SiteDto> getQuerySite(
      @Nullable String severity, String query, List<SiteDto> sites, @Nullable Date date) {
    List<SiteDto> results = new ArrayList<>();
    for (SiteDto site : sites) {
      if (severity != null) {
        if (query != null || !query.isEmpty()) {
          if (severity.equals("All")) {
            if (site.getDisplayName().toLowerCase().contains(query)
                || site.getAddress().toLowerCase().contains(query)) {
              results.add(site);
            }
          } else {
            if (site.getSeverity().equals(severity)
                && (site.getDisplayName().toLowerCase().contains(query)
                    || site.getAddress().toLowerCase().contains(query))) {
              results.add(site);
            }
          }
        } else {
          if (site.getSeverity().equals(severity)) {
            results.add(site);
          } else {
            if (severity.equals("All")) {
              results.add(site);
            }
          }
        }
      }
    }
    return results;
  }

  public void cacheSiteId(String siteId) {
    preferenceManager.putString(Constant.KEY_SITE_ID, siteId);
  }

  public String getCacheSiteId() {
    return preferenceManager.getString(Constant.KEY_SITE_ID);
  }

  public void getAllSiteAdmin(final FirebaseCallback<Result<List<SiteDto>>> callback) {
    siteReference
        .get()
        .addOnCompleteListener(
            getSiteTask -> {
              if (getSiteTask.isSuccessful()) {
                List<SiteDto> listSite = new ArrayList<>();
                for (QueryDocumentSnapshot document : getSiteTask.getResult()) {
                  Site site = document.toObject(Site.class);
                  SiteDto siteDto = site.toDto();
                  siteDto.setId(document.getId());
                  listSite.add(siteDto);
                }
                callback.callbackRes(new Result.Success<>(listSite));
              } else {
                callback.callbackRes(new Result.Error(getSiteTask.getException()));
              }
            });
  }

  public void getAllSite(String userId, final FirebaseCallback<Result<List<SiteDto>>> callback) {
    siteReference
        .whereNotEqualTo("owner.id", userId)
        .get()
        .addOnCompleteListener(
            getSiteTask -> {
              if (getSiteTask.isSuccessful()) {
                List<SiteDto> listSite = new ArrayList<>();
                for (QueryDocumentSnapshot document : getSiteTask.getResult()) {
                  Site site = document.toObject(Site.class);
                  SiteDto siteDto = site.toDto();
                  siteDto.setId(document.getId());
                  listSite.add(siteDto);
                }
                callback.callbackRes(new Result.Success<>(listSite));
              } else {
                callback.callbackRes(new Result.Error(getSiteTask.getException()));
              }
            });
  }

  public void getOneSite(String id, final FirebaseCallback<Result<SiteDto>> callback) {
    siteReference
        .document(id)
        .get()
        .addOnCompleteListener(
            getOneSiteTask -> {
              if (getOneSiteTask.isSuccessful()) {
                DocumentSnapshot documentSnapshot = getOneSiteTask.getResult();
                Site site = documentSnapshot.toObject(Site.class);
                SiteDto getSite = site.toDto();
                getSite.setId(documentSnapshot.getId());
                callback.callbackRes(new Result.Success<>(getSite));
              } else {
                callback.callbackRes(new Result.Error(getOneSiteTask.getException()));
              }
            });
  }

  public void createSite(Site site, final FirebaseCallback<Result<Site>> callback) {
    siteReference
        .add(site)
        .addOnSuccessListener(
            documentReference -> {
              String id = documentReference.getId();
              site.setId(id);
              userService.updateUserSiteId(
                  id,
                  new FirebaseCallback<Result<UserDto>>() {
                    @Override
                    public void callbackListRes(List<Result<UserDto>> listT) {}

                    @Override
                    public void callbackRes(Result<UserDto> userDtoResult) {
                      if (userDtoResult instanceof Result.Success) {
                        UserDto currentUser = ((Result.Success<UserDto>) userDtoResult).getData();
                        Log.d("Update site", "Successfully");
                      } else {
                        Log.d("Update site", "Error!");
                      }
                    }
                  });
            })
        .addOnCompleteListener(
            createSiteTask -> {
              if (createSiteTask.isSuccessful()) {
                callback.callbackRes(new Result.Success<Site>(site));
              } else {
                callback.callbackRes(new Result.Error(createSiteTask.getException()));
              }
            });
  }

  public void updateSite(
      String siteId, Site updatedSite, final FirebaseCallback<Result<Site>> callback) {
    siteReference
        .document(siteId)
        .set(updatedSite)
        .addOnCompleteListener(
            task -> {
              if (task.isSuccessful()) {
                callback.callbackRes(new Result.Success<>(updatedSite));
              } else {
                callback.callbackRes(new Result.Error(task.getException()));
              }
            });
  }

  public void updateSiteVolunteers(
      String siteId, UserDto volunteers, final FirebaseCallback<Result<UserDto>> callback) {
    siteReference
        .document(siteId)
        .update("volunteers", FieldValue.arrayUnion(volunteers))
        .addOnCompleteListener(
            updateTask -> {
              if (updateTask.isSuccessful()) {
                callback.callbackRes(new Result.Success<UserDto>(volunteers));
              } else {
                callback.callbackRes(new Result.Error(updateTask.getException()));
              }
            });
  }

  public void updateSiteImgs(
      String id, Site updatedSite, final FirebaseCallback<Result<Site>> callback) {
    siteReference
        .document(id)
        .update("imageUrl", updatedSite.getImageUrl())
        .addOnFailureListener(
            task -> {
              Log.d("Update failed", task.getMessage());
            })
        .addOnCompleteListener(
            task -> {
              if (task.isSuccessful()) {
                callback.callbackRes(new Result.Success<>(updatedSite));
              } else {
                callback.callbackRes(new Result.Error(task.getException()));
              }
            });
  }

  public void updateRequest(
      String siteId, Request request, final FirebaseCallback<Result<Request>> callback) {
    siteReference
        .document(siteId)
        .update("requests", FieldValue.arrayUnion(request))
        .addOnFailureListener(
            task -> {
              Log.d("Update failed", task.getMessage());
            })
        .addOnCompleteListener(
            task -> {
              if (task.isSuccessful()) {
                callback.callbackRes(new Result.Success<>(request));
              } else {
                callback.callbackRes(new Result.Error(task.getException()));
              }
            });
  }

  public void updateAllRequests(
      String siteId,
      List<Request> requests,
      final FirebaseCallback<Result<List<Request>>> callback) {
    siteReference
        .document(siteId)
        .update("requests", requests)
        .addOnCompleteListener(
            updateTask -> {
              if (updateTask.isSuccessful()) {
                callback.callbackRes(new Result.Success<>(requests));
              } else {
                callback.callbackRes(new Result.Error(updateTask.getException()));
              }
            });
  }

  public void updateSiteReports(
      String id, Report report, final FirebaseCallback<Result<Report>> callback) {
    siteReference
        .document(id)
        .update("reports", FieldValue.arrayUnion(report))
        .addOnFailureListener(
            task -> {
              Log.d("Update failed", task.getMessage());
            })
        .addOnCompleteListener(
            task -> {
              if (task.isSuccessful()) {
                callback.callbackRes(new Result.Success<>(report));
              } else {
                callback.callbackRes(new Result.Error(task.getException()));
              }
            });
  }
}
