package com.example.cloudsync.authorization;

import android.app.Activity;
import android.content.Context;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.ActivityResultRegistry;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.google.android.gms.auth.api.identity.AuthorizationClient;
import com.google.android.gms.auth.api.identity.AuthorizationRequest;
import com.google.android.gms.auth.api.identity.AuthorizationResult;
import com.google.android.gms.auth.api.identity.Identity;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Scope;
import com.google.api.services.drive.DriveScopes;
import java.util.Collections;

public class GoogleAuthorizationClient implements DefaultLifecycleObserver {

  private final AuthorizationClient authClient;
  private final MutableLiveData<AuthState> authState = new MutableLiveData<>(
      AuthState.UNAUTHORIZED);
  private final ActivityResultRegistry registry;
  private AuthorizationResult result;
  private ActivityResultLauncher<IntentSenderRequest> launcher;

  public GoogleAuthorizationClient(Context context, ActivityResultRegistry registry) {
    this.authClient = Identity.getAuthorizationClient(context);
    this.registry = registry;
  }

  public LiveData<AuthState> getAuthState() {
    return authState;
  }

  public AuthorizationResult getResult() {
    return result;
  }

  @Override
  public void onCreate(@NonNull LifecycleOwner owner) {
    launcher = registry.register("key", owner,
        new ActivityResultContracts.StartIntentSenderForResult(), result -> {
          switch (result.getResultCode()) {
            case Activity.RESULT_OK:
              try {
                AuthorizationResult authResult = authClient.getAuthorizationResultFromIntent(
                    result.getData()
                );
                onSuccess(authResult);
              } catch (ApiException e) {
                onFailure(e);
              }
              break;
            case Activity.RESULT_CANCELED:
              onFailure(new Exception("Canceled"));
              break;
          }
        });
  }

  public void authorize() {
    authState.setValue(AuthState.AUTHORIZING);

    AuthorizationRequest authRequest = AuthorizationRequest.builder()
        .setRequestedScopes(Collections.singletonList(new Scope(DriveScopes.DRIVE_FILE)))
        .build();

    authClient.authorize(authRequest)
        .addOnSuccessListener(this::onSuccess)
        .addOnFailureListener(this::onFailure);
  }

  private void onSuccess(AuthorizationResult result) {
    if (result.hasResolution()) {
      final IntentSenderRequest request = new IntentSenderRequest.Builder(result.getPendingIntent())
          .build();
      launcher.launch(request);
    } else {
      this.result = result;
      authState.setValue(AuthState.AUTHORIZED);
    }
  }

  private void onFailure(Exception e) {
    authState.setValue(AuthState.UNAUTHORIZED);
  }
}
