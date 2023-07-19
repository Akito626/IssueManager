package com.alha_app.issuemanager;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private IssueManager issueManager;
    private ListView issueList;
    private ArrayList<String> listData;
    private ArrayList<String> issueBodies;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        issueManager = (IssueManager) this.getApplication();

        issueList = findViewById(R.id.issue_list);
        handler = new Handler();

        listData = new ArrayList<>();
        issueBodies = new ArrayList<>();

        issueList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                issueManager.setIssueTitle(listData.get(position));
                issueManager.setIssueBody(issueBodies.get(position));
                startActivity(new Intent(getApplication(), EditorActivity.class));
            }
        });

        new Thread(() -> {
            test();
        }).start();
    }

    public void test(){
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

            String tmp = "";
            for(int i = 0; i < jsonResult.size(); i++){
                tmp = jsonResult.get(i).get("title").toString();
                //tmp.replaceAll("\\\\r\\\\n", "\n");
                listData.add(tmp);
                tmp = jsonResult.get(i).get("body").toString();
                //tmp.replaceAll("\\\\r\\\\n", "\n");
                issueBodies.add(tmp);
            }

            handler.post(() -> {
                issueList.setAdapter(new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_list_item_1,
                        listData
                ));
            });
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}