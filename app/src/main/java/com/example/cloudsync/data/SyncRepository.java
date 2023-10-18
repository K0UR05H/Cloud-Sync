package com.example.cloudsync.data;

import android.app.Application;
import com.example.cloudsync.data.cloud.CloudStorageRepository;
import com.example.cloudsync.data.cloud.DownloadStatus;
import com.example.cloudsync.data.db.UserDatabase;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class SyncRepository {

  private static final String SQLITE_MIMETYPE = "application/vnd.sqlite3";

  private final UserDatabase db;
  private final CloudStorageRepository cloudStorageRepository;

  public SyncRepository(Application application, CloudStorageRepository cloudStorageRepository) {
    this.db = UserDatabase.getInstance(application);
    this.cloudStorageRepository = cloudStorageRepository;
  }

  public Completable backup() {
    try {
      String dbPath = db.getOpenHelper().getWritableDatabase().getPath();
      File dbFile = new File(dbPath);
      InputStream dbFileInputStream = new FileInputStream(dbFile);

      return Completable.fromSingle(
          cloudStorageRepository.upload(dbFile.getName(), SQLITE_MIMETYPE, dbFileInputStream)
      );
    } catch (Exception e) {
      return Completable.error(e);
    }
  }

  public Single<DownloadStatus> restore() {
    try {
      String dbPath = db.getOpenHelper().getWritableDatabase().getPath();
      File dbFile = new File(dbPath);
      OutputStream dbFileOutputStream = new FileOutputStream(dbFile);

      return cloudStorageRepository.download(dbFile.getName(), dbFileOutputStream);
    } catch (Exception e) {
      return Single.error(e);
    }
  }
}
