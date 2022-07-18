package lll69.analyze;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission_group.STORAGE;
import static android.os.Build.VERSION_CODES.O;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {
    public static final String CHOOSE = "choose";
    public static final String NOTIFY = "notify";
    ListView listView;
    ArrayList<String> items;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        items = new ArrayList<>();
        items.add(Environment.getExternalStorageDirectory().getPath());
        items.add(CHOOSE);
        listView = findViewById(R.id.list);
        listView.setAdapter(new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1, items) {
            @Override
            public String getItem(int position) {
                String item = super.getItem(position);
                if (CHOOSE.equals(item)) {
                    item = getString(R.string.choose_directory);
                }
                return item;
            }
        });
        listView.setOnItemClickListener(this);
        if (Build.VERSION.SDK_INT >= O)
            ((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).createNotificationChannel(new NotificationChannel(NOTIFY, "Notification", NotificationManager.IMPORTANCE_DEFAULT));
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String path = items.get(position);
        try {
            if (ContextCompat.checkSelfPermission(this, READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, READ_EXTERNAL_STORAGE)) {
                    startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).setData(Uri.parse("package:" + BuildConfig.APPLICATION_ID)));
                } else {
                    ActivityCompat.requestPermissions(this, new String[]{READ_EXTERNAL_STORAGE}, 0);
                }
                Toast.makeText(this, getString(R.string.grant_permission,
                        getPackageManager().getPermissionGroupInfo(STORAGE, 0)
                                .loadLabel(getPackageManager())), Toast.LENGTH_SHORT).show();
            } else {
                open(path);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void open(String path) {
        if (CHOOSE.equals(path)) {

        } else {
            startActivity(new Intent(this, AnalyzeActivity.class)
                    .putExtra(AnalyzeService.EXTRA_PATH, path));
        }
    }
}
