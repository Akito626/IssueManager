package com.alha_app.issuemanager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private IssueManager issueManager;
    private ListView issueList;
    private ArrayList<String> issueTitle;
    private ArrayList<String> issueBodies;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        issueManager = (IssueManager) this.getApplication();

        issueList = findViewById(R.id.issue_list);
        handler = new Handler();

        issueTitle = new ArrayList<>();
        issueBodies = new ArrayList<>();

        issueList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                issueManager.setIssueTitle(issueTitle.get(position));
                issueManager.setIssueBody(issueBodies.get(position));
                startActivity(new Intent(getApplication(), EditorActivity.class));
            }
        });

        new Thread(() -> {
            getIssues();
        }).start();
    }

    public void getIssues(){
        String urlString = BuildConfig.URL;

        String json = "";
        StringBuilder sb = new StringBuilder();
        JsonNode jsonResult = null;
        ObjectMapper mapper = new ObjectMapper();

        try{
            Request request = new Request.Builder()
                    .url(urlString)
                    .build();
            OkHttpClient okHttpClient = new OkHttpClient.Builder().build();
            Response response = okHttpClient.newCall(request).execute();
            System.out.println(response.code());
            json = response.body().string();
            jsonResult = mapper.readTree(json);

            System.out.println(json);

            ArrayList<Map<String, String>> listData = new ArrayList<>();
            String tmp = "";
            for(int i = 0; i < jsonResult.size(); i++){
                Map<String, String> item = new HashMap<>();

                tmp = jsonResult.get(i).get("title").toString();
                tmp = tmp.substring(1, tmp.length()-1);
                tmp = tmp.replaceAll("\\\\r", "");
                tmp = tmp.replaceAll("\\\\n", "\n");
                issueTitle.add(tmp);
                item.put("title", tmp);
                tmp = jsonResult.get(i).get("body").toString();
                tmp = tmp.substring(1, tmp.length()-1);
                tmp = tmp.replaceAll("\\\\r", "");
                tmp = tmp.replaceAll("\\\\n", "\n");
                issueBodies.add(tmp);

                tmp = jsonResult.get(i).get("user").get("login").toString();
                tmp = tmp.substring(1, tmp.length()-1);
                item.put("name", tmp);

                System.out.println(jsonResult.get(i).get("labels").size());
                if(jsonResult.get(i).get("labels").size() == 0){
                    item.put("label", "default");
                } else {
                    tmp = jsonResult.get(i).get("labels").get(0).get("name").toString();
                    tmp = tmp.substring(1, tmp.length() - 1);
                    item.put("label", tmp);
                }

                tmp = jsonResult.get(i).get("created_at").toString();
                tmp = tmp.substring(1, tmp.length()-1);
                tmp = tmp.replace("T", " ");
                tmp = tmp.replace("Z", "");
                item.put("date", tmp);
                listData.add(item);
            }

            handler.post(() -> {
                issueList.setAdapter(new SimpleAdapter(
                        this,
                        listData,
                        R.layout.issuelist_item,
                        new String[] {"title", "date", "label", "name"},
                        new int[] {R.id.title, R.id.date, R.id.label, R.id.name}
                ));
            });
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}