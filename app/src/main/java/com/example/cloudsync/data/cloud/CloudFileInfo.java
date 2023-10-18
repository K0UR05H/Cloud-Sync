package com.example.cloudsync.data.cloud;

public class CloudFileInfo {

  private final String id;
  private final String name;
  private final String mimeType;

  public CloudFileInfo(String id, String name, String mimeType) {
    this.id = id;
    this.name = name;
    this.mimeType = mimeType;
  }

  public String getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getMimeType() {
    return mimeType;
  }
}
