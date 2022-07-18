package lll69.analyze;

import android.app.Application;

import java.util.ArrayList;

public class App extends Application {
    private static App app;
    public ArrayList<AnalyzeService.TaskThread> threadList = new ArrayList<>();

    @Override
    public void onCreate() {
        super.onCreate();
        app = this;
    }

    public static App getApp() {
        return app;
    }
}
