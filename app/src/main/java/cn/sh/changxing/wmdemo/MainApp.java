package cn.sh.changxing.wmdemo;

import android.app.Application;

/**
 * Created by yuanyi on 17-9-9.
 */

public class MainApp extends Application {
    private static MainApp mApp;

    @Override
    public void onCreate() {
        super.onCreate();
        mApp = this;
    }

    public static MainApp getApp() {
        return mApp;
    }
}
