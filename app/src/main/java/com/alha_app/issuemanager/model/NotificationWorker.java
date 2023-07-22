package com.alha_app.issuemanager.model;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.alha_app.issuemanager.BuildConfig;
import com.alha_app.issuemanager.IssueManager;
import com.alha_app.issuemanager.R;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class NotificationWorker extends Worker {
    private Map<String, Object> preData = new HashMap<>();
    final public static String WORK_TAG = "NotificationWorkerTAG";
    public NotificationWorker(@NonNull Context context, @NonNull WorkerParameters params){
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork(){
        String CHANNEL_ID = "ISSUE CHANNEL";

        Data data = getInputData();
        Map<String, Object> issueMap = data.getKeyValueMap();
        String token = data.getString("token");
        String owner = data.getString("owner");
        String repo = data.getString("repo");

        String urlString = BuildConfig.URL + owner + "/" + repo + "/issues";

        String json = "";
        JsonNode jsonResult = null;
        ObjectMapper mapper = new ObjectMapper();

        try {
            Request request = new Request.Builder()
                    .addHeader("Accept", "application/vnd.github+json")
                    .addHeader("Authorization", "token " + token)
                    .addHeader("X-GitHub-Api-Version", "2022-11-28")
                    .url(urlString)
                    .build();
            OkHttpClient okHttpClient = new OkHttpClient.Builder().build();
            Response response = okHttpClient.newCall(request).execute();
            json = response.body().string();
            jsonResult = mapper.readTree(json);

            ArrayList<String> titleList = new ArrayList<>();
            for (int i = 0; i < jsonResult.size(); i++) {
                // タイトルを取得
                String title = jsonResult.get(i).get("title").toString();
                title = title.substring(1, title.length() - 1);
                title = title.replaceAll("\\\\r", "");
                title = title.replaceAll("\\\\n", "\n");

                // issueNumberを取得
                String number = jsonResult.get(i).get("number").toString();

                if(issueMap.get(number) == null){
                    titleList.add(title);
                }
            }

            if(titleList.size() > 0) {
                String groupKey = "GROUP_KEY";

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Issue通知", NotificationManager.IMPORTANCE_DEFAULT);
                    NotificationManager notificationManager = getApplicationContext().getSystemService(NotificationManager.class);
                    notificationManager.createNotificationChannel(channel);
                }

                NotificationCompat.Builder[] builders = new NotificationCompat.Builder[titleList.size() + 1];
                for (int i = 0; i <= titleList.size(); i++) {
                    if(i == titleList.size()){
                        builders[i] = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                                .setSmallIcon(R.drawable.issue)
                                .setGroup(groupKey)
                                .setGroupSummary(true);
                    } else {
                        builders[i] = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                                .setSmallIcon(R.drawable.issue)
                                .setContentTitle("新しいissueが登録されました")
                                .setContentText(titleList.get(i))
                                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                                .setGroup(groupKey);
                    }
                }
                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());
                for(int i = 0; i <= titleList.size(); i++){
                    notificationManager.notify(i, builders[i].build());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Result.success();
    }
}
