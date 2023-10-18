package com.example.cloudsync.data.cloud;

import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.http.InputStreamContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class CloudStorageRepository {

  private final Drive drive;

  public CloudStorageRepository(String accessToken) {
    Credential credential = new Credential(BearerToken.authorizationHeaderAccessMethod())
        .setAccessToken(accessToken);
    this.drive = new Drive(new NetHttpTransport(), GsonFactory.getDefaultInstance(), credential);
  }

  public Single<CloudFileInfo> upload(String filename, String mimeType, InputStream content) {
    return getFileId(filename)
        .flatMapSingle(fileId -> updateFile(fileId, filename, mimeType, content))
        .switchIfEmpty(Single.defer(() -> createFile(filename, mimeType, content)));
  }

  public Single<DownloadStatus> download(String filename, OutputStream outputStream) {
    return getFileId(filename)
        .flatMapSingle(fileId -> downloadFileWithFileId(fileId, outputStream)
            .toSingleDefault(DownloadStatus.DOWNLOADED)
        )
        .switchIfEmpty(Single.just(DownloadStatus.FILE_NOT_FOUND));
  }

  private Maybe<String> getFileId(String filename) {
    return Maybe.defer(() -> {
      FileList result = drive.files().list()
          .setQ("trashed=false and name = '" + filename + "'")
          .setSpaces("drive")
          .setFields("files(id)")
          .execute();

      Optional<String> firstFileId = result.getFiles().stream().map(File::getId).findFirst();
      return Maybe.fromOptional(firstFileId);
    });
  }

  private Single<CloudFileInfo> createFile(String name, String mimeType, InputStream inputStream) {
    return Single.fromCallable(() -> {
      File fileMetadata = new File();
      fileMetadata.setName(name);
      InputStreamContent content = new InputStreamContent(mimeType, inputStream);

      File file = drive.files().create(fileMetadata, content)
          .setFields("id, name, mimeType")
          .execute();

      return new CloudFileInfo(file.getId(), file.getName(), file.getMimeType());
    });
  }

  private Single<CloudFileInfo> updateFile(String fileId, String name, String mimeType,
      InputStream inputStream) {
    return Single.fromCallable(() -> {
      File fileMetadata = new File();
      fileMetadata.setName(name);
      InputStreamContent content = new InputStreamContent(mimeType, inputStream);

      File file = drive.files().update(fileId, fileMetadata, content)
          .setFields("id, name, mimeType")
          .execute();

      return new CloudFileInfo(file.getId(), file.getName(), file.getMimeType());
    });
  }

  private Completable downloadFileWithFileId(String fileId, OutputStream outputStream) {
    return Completable.fromAction(
        () -> drive.files().get(fileId).executeMediaAndDownloadTo(outputStream)
    );
  }

  public Single<List<CloudFileInfo>> listFiles() {
    return Single.fromCallable(() -> {
      List<CloudFileInfo> filesInfo = new ArrayList<>();
      String pageToken = null;

      do {
        FileList result = drive.files().list()
            // remove trashed files from the results
            .setQ("trashed=false")
            // don't include application data
            .setSpaces("drive")
            .setFields("nextPageToken, files(id, name, mimeType)")
            .setPageToken(pageToken)
            .execute();

        List<CloudFileInfo> newFilesInfo = result.getFiles()
            .stream()
            .map(file -> new CloudFileInfo(file.getId(), file.getName(), file.getMimeType()))
            .collect(Collectors.toList());

        filesInfo.addAll(newFilesInfo);

        pageToken = result.getNextPageToken();
      } while (pageToken != null);

      return filesInfo;
    });
  }
}
