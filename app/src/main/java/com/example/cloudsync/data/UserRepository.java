package com.example.cloudsync.data;

import android.app.Application;
import com.example.cloudsync.data.db.User;
import com.example.cloudsync.data.db.UserDatabase;
import io.reactivex.rxjava3.core.Completable;

public class UserRepository {

  private final UserDatabase db;

  public UserRepository(Application application) {
    this.db = UserDatabase.getInstance(application);
  }

  public Completable insertUser(String firstName, String lastName, String phoneNumber) {
    User user = new User(firstName, lastName, phoneNumber);
    return db.userDao().insertUser(user);
  }
}
