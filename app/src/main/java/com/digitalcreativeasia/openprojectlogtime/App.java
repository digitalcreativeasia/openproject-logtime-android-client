package com.digitalcreativeasia.openprojectlogtime;

import android.app.Application;

import com.androidnetworking.AndroidNetworking;
import com.digitalcreativeasia.openprojectlogtime.storage.TinyDB;
import com.digitalcreativeasia.openprojectlogtime.storage.logger.CrashReportingTree;

import okhttp3.OkHttpClient;
import timber.log.Timber;

public class App extends Application {

    private static Application sApplication;
    private static TinyDB tinyDB;

    public static Application getApplication() {
        return sApplication;
    }

    public static TinyDB getTinyDB() {
        return tinyDB;
    }

    public interface PATH {
        String AUTH = "users/";
    }


    // debug api key
    public static String getDebugKey(){
        return getApplication().getString(R.string.debug_api_key);
    }


    @Override
    public void onCreate() {
        super.onCreate();
        sApplication = this;
        tinyDB = new TinyDB(sApplication);

        OkHttpClient okHttpClient = new OkHttpClient().newBuilder()
                .build();
        AndroidNetworking.initialize(getApplicationContext(), okHttpClient);

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        } else {
            Timber.plant(new CrashReportingTree());
        }

    }

}