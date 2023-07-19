package com.alha_app.issuemanager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class EditorActivity extends AppCompatActivity {
    private IssueManager issueManager;

    private EditText titleText;
    private EditText bodyText;

    private Handler handler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        handler = new Handler();
        issueManager = (IssueManager) this.getApplication();

        titleText = findViewById(R.id.title_text);
        titleText.setText(issueManager.getIssueTitle());

        bodyText = findViewById(R.id.body_text);
        bodyText.setText(issueManager.getIssueBody());

        Button saveButton = findViewById(R.id.save_button);
        saveButton.setOnClickListener(view -> {
            Toast.makeText(issueManager, "保存しました", Toast.LENGTH_SHORT).show();
            new Thread(() -> {
                addIssue();
            }).start();
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuButton){
        boolean result = true;
        int buttonId = menuButton.getItemId();
        switch(buttonId){
            //戻るボタンが押されたとき
            case android.R.id.home:
                //画面を終了させる
                finish();
                break;
            //それ以外の時
            default:
                result = super.onOptionsItemSelected(menuButton);
                break;
        }
        return result;
    }

    public void addIssue(){
        String urlString = BuildConfig.URL;
        String token = "";

        String json = "";
        JsonNode jsonResult = null;
        ObjectMapper mapper = new ObjectMapper();

        Map<String, String> formParamMap = new HashMap<>();
        formParamMap.put("title", titleText.getText().toString());
        formParamMap.put("body", bodyText.getText().toString());
        //formParamMap.put("labels", "");

        final FormBody.Builder formBuilder = new FormBody.Builder();
        formParamMap.forEach(formBuilder::add);
        RequestBody requestBody = formBuilder.build();

        try {
            Request request = new Request.Builder()
                    .url(urlString)
                    .addHeader("Accept", "application/vnd.github+json")
                    .addHeader("Authorization", token)
                    .post(requestBody)
                    .build();

            OkHttpClient okHttpClient = new OkHttpClient.Builder().build();
            Response response = okHttpClient.newCall(request).execute();

            json = response.body().string();
            //jsonResult = mapper.readTree(json);
            System.out.println(response.code());
            System.out.println(json);

            if(!response.isSuccessful()){
                handler.post(() -> {
                    Toast.makeText(issueManager, "追加できませんでした", Toast.LENGTH_SHORT).show();
                });
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public void updateIssue(){
        String urlString = BuildConfig.URL;

        String json = "";
        JsonNode jsonResult = null;
        ObjectMapper mapper = new ObjectMapper();

        Map<String, String> formParamMap = new HashMap<>();
        //formParamMap.put("title", );

        try {
            Request request = new Request.Builder()
                    .url(urlString)
                    .build();
            OkHttpClient okHttpClient = new OkHttpClient.Builder().build();
            Response response = okHttpClient.newCall(request).execute();
            System.out.println(response.code());
            json = response.body().string();
            jsonResult = mapper.readTree(json);
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}