package dev.forcecodes.pawmance;

import android.app.Application;
import com.devforcecodes.pawmance.BuildConfig;
import com.jakewharton.threetenabp.AndroidThreeTen;
import dagger.hilt.android.HiltAndroidApp;
import dev.forcecodes.pawmance.db.AppDatabase;
import timber.log.Timber;

@HiltAndroidApp
public class PawManceApplication extends Application {

  @Override
  public void onCreate() {
    super.onCreate();
    AndroidThreeTen.init(this);

    if (BuildConfig.DEBUG) {
      Timber.plant(new Timber.DebugTree());
    }

    AppDatabase.createInstance(this);
  }
}
