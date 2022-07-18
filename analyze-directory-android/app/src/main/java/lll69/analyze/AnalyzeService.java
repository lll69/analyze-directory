package lll69.analyze;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.text.TextUtils;

import java.util.ArrayList;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import static lll69.analyze.MainActivity.NOTIFY;

public class AnalyzeService extends Service implements Runnable {
    public static final String EXTRA_PATH = Intent.EXTRA_TEXT;
    private Handler handler;

    @Override
    public void onCreate() {
        super.onCreate();
        handler = new Handler();
        handler.post(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        ArrayList<TaskThread> threadList = App.getApp().threadList;
        String path = intent.getStringExtra(EXTRA_PATH);
        if (TextUtils.isEmpty(path))
            path = "/storage/emulated/0";
        TaskThread thread = new TaskThread(startId);
        if (!threadList.contains(thread)) {
            thread.path = path;
            threadList.add(thread);
            thread.start();
        } else {
            stopSelf(startId);
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void run() {
        handler.postDelayed(this, 128);
        notify(true);
    }

    public void notify(boolean foreground) {
        for (TaskThread t : App.getApp().threadList) {
            if (t.root != null) {
                double progress = t.root.getProgress();
                if (progress < 1)
                    t.notify.setContentTitle(getString(R.string.analyze_details, t.path, progress*100));
                else
                    t.notify.setContentTitle(getString(R.string.analyze_finished, t.path));
                t.notify.setContentText(t.currentFile + t.currentType);
                t.notify.setProgress(10000, (int) (progress * 10000), false);
                if (foreground)
                    startForeground(t.startId, t.notify.build());
                else
                    ((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).notify(t.startId, t.notify.build());
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(this);
        notify(false);
    }

    public class TaskThread extends AnalyzeThread {
        final int startId;
        long startTime, endTime;
        NotificationCompat.Builder notify;

        public TaskThread(int startId) {
            this.startId = startId;
            notify = new NotificationCompat.Builder(AnalyzeService.this, NOTIFY).setSmallIcon(R.mipmap.ic_launcher_round);
        }

        @Override
        public boolean equals(@Nullable Object obj) {
            if (obj instanceof TaskThread && ((TaskThread) obj).path.equals(path))
                return true;
            return super.equals(obj);
        }

        @Override
        public void run() {
            startTime = System.currentTimeMillis();
            super.run();
            endTime = System.currentTimeMillis();
            stopSelf(startId);
        }
    }
}
