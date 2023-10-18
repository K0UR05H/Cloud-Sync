package com.example.cloudsync.data.db;

import androidx.room.Dao;
import androidx.room.Insert;
import io.reactivex.rxjava3.core.Completable;

@Dao
public interface UserDao {

  @Insert
  Completable insertUser(User user);
}
