package com.alha_app.issuemanager;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.alha_app.issuemanager.model.NotificationWorker;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import android.Manifest;
import android.widget.Toast;

public class SettingActivity extends AppCompatActivity {
    private IssueManager issueManager;
    private CheckBox notificationCheckbox;

    private final ActivityResultLauncher<String>
            requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    issueManager.setNotify(true);
                    startWork();
                }
                else {
                    notificationCheckbox.setChecked(false);
                    Toast.makeText(issueManager, "通知を送るには許可が必要です", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("設定");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        issueManager = (IssueManager) this.getApplication();
        boolean isNotify = issueManager.isNotify();

        notificationCheckbox = findViewById(R.id.notification_checkbox);
        notificationCheckbox.setChecked(isNotify);
        notificationCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(Build.VERSION.SDK_INT >= 23){
                    if(ActivityCompat.checkSelfPermission(getApplicationContext(),
                            Manifest.permission.POST_NOTIFICATIONS)
                            != PackageManager.PERMISSION_GRANTED){
                        requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
                    } else {
                        issueManager.setNotify(isChecked);
                        if (isChecked) {
                            startWork();
                        } else {
                            stopWork();
                        }
                    }
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return true;
    }

    private void startWork() {
        String token = issueManager.getToken();
        String owner = issueManager.getOwner();
        String repo = issueManager.getRepo();

        Data data = new Data.Builder()
                .putString("token", token)
                .putString("owner", owner)
                .putString("repo", repo)
                .build();

        Constraints constraints = new Constraints.Builder()
                .setRequiresBatteryNotLow(true)
                .build();

        PeriodicWorkRequest request = new PeriodicWorkRequest.Builder(
                NotificationWorker.class, 15, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .setInputData(data)
                .addTag(NotificationWorker.WORK_TAG)
                .build();

        WorkManager manager = WorkManager.getInstance(this);
        manager.enqueue(request);

        Log.d("work", "Worker scheduled");
    }

    private void stopWork() {
        WorkManager manager = WorkManager.getInstance(this);
        manager.cancelAllWork();
        NotificationWorker.preData = null;
        Log.d("work", "Worker stopped");
    }
}