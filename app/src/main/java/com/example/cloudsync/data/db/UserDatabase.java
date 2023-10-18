package com.example.cloudsync.data.db;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {User.class}, version = 1)
public abstract class UserDatabase extends RoomDatabase {

  private static volatile UserDatabase INSTANCE;

  public static UserDatabase getInstance(final Context context) {
    if (INSTANCE == null) {
      synchronized (UserDatabase.class) {
        if (INSTANCE == null) {
          INSTANCE = Room.databaseBuilder(
                  context.getApplicationContext(),
                  UserDatabase.class,
                  "users.db"
              )
              .setJournalMode(JournalMode.TRUNCATE)
              .build();
        }
      }
    }
    return INSTANCE;
  }

  public abstract UserDao userDao();
}
