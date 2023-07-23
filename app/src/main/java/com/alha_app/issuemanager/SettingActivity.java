package com.alha_app.issuemanager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.alha_app.issuemanager.model.IssueData;
import com.alha_app.issuemanager.model.NotificationWorker;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class SettingActivity extends AppCompatActivity {
    private IssueManager issueManager;

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

        CheckBox notificationCheckbox = findViewById(R.id.notification_checkbox);
        notificationCheckbox.setChecked(isNotify);
        notificationCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                issueManager.setNotify(isChecked);
                if (isChecked) {
                    startWork();
                } else {
                    stopWork();
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
        Map<String, Object> issueMap = issueManager.getIssueMap();
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