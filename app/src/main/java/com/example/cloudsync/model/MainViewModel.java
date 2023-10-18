package com.example.cloudsync.model;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.cloudsync.data.SyncRepository;
import com.example.cloudsync.data.UserRepository;
import com.example.cloudsync.data.cloud.CloudStorageRepository;
import com.google.android.gms.auth.api.identity.AuthorizationResult;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class MainViewModel extends AndroidViewModel {

  private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
  private final MutableLiveData<String> message = new MutableLiveData<>();
  private final CompositeDisposable disposables = new CompositeDisposable();
  private final UserRepository userRepository;
  private SyncRepository syncRepository;

  public MainViewModel(Application application) {
    super(application);
    userRepository = new UserRepository(application);
  }

  @Override
  protected void onCleared() {
    super.onCleared();
    disposables.clear();
  }

  public LiveData<String> getMessage() {
    return this.message;
  }

  public LiveData<Boolean> getIsLoading() {
    return this.isLoading;
  }

  public void onAuthResult(AuthorizationResult result) {
    String accessToken = result.getAccessToken();
    CloudStorageRepository cloudStorageRepository = new CloudStorageRepository(accessToken);
    this.syncRepository = new SyncRepository(getApplication(), cloudStorageRepository);
  }

  public void onMessageDisplayed() {
    this.message.setValue(null);
  }

  public void backup() {
    disposables.add(syncRepository.backup()
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .doOnSubscribe(disposable -> isLoading.setValue(true))
        .doOnTerminate(() -> isLoading.setValue(false))
        .subscribe(
            () -> message.setValue("Backup complete!"),
            throwable -> message.setValue("Backup error: " + throwable.getMessage())
        )
    );
  }

  public void restore() {
    disposables.add(syncRepository.restore()
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .doOnSubscribe(disposable -> isLoading.setValue(true))
        .doOnTerminate(() -> isLoading.setValue(false))
        .subscribe(
            downloadStatus -> {
              switch (downloadStatus) {
                case DOWNLOADED:
                  message.setValue("Restore complete!");
                  break;
                case FILE_NOT_FOUND:
                  message.setValue("No backups found");
                  break;
              }
            },
            throwable -> message.setValue("Restore error: " + throwable.getMessage())
        )
    );
  }

  public void addUser(String firstName, String lastName, String phoneNumber) {
    disposables.add(userRepository.insertUser(firstName, lastName, phoneNumber)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .doOnSubscribe(disposable -> isLoading.setValue(true))
        .doOnTerminate(() -> isLoading.setValue(false))
        .subscribe(
            () -> message.setValue("User added"),
            throwable -> message.setValue("Failed to add user: " + throwable.getMessage())
        )
    );
  }
}
