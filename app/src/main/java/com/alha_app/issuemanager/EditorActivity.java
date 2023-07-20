package com.alha_app.issuemanager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.alha_app.issuemanager.model.IssueJson;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.FormBody;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class EditorActivity extends AppCompatActivity {
    private IssueManager issueManager;
    private String token;
    private String owner;
    private String repo;

    private EditText titleText;
    private EditText bodyText;
    private String issueNumber;
    private String issueLabel;

    private Handler handler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        issueManager = (IssueManager) this.getApplication();
        token = issueManager.getToken();
        owner = issueManager.getOwner();
        repo = issueManager.getRepo();
        issueNumber = issueManager.getIssueNumber();
        issueLabel = issueManager.getIssueLabel();

        if(issueLabel == null){
            issueLabel = "default";
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Spinner labelsSpinner = findViewById(R.id.labels_spinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                R.layout.spinner_item,
                getResources().getStringArray(R.array.labels)
        );
        adapter.setDropDownViewResource(R.layout.spinner_item);
        labelsSpinner.setAdapter(adapter);
        switch (issueLabel){
            case "default":
                labelsSpinner.setSelection(0);
                break;
            case "bug":
                labelsSpinner.setSelection(1);
                break;
            case "duplicate":
                labelsSpinner.setSelection(2);
                break;
            case "enhancement":
                labelsSpinner.setSelection(3);
                break;
            case "invalid":
                labelsSpinner.setSelection(4);
                break;
            case "question":
                labelsSpinner.setSelection(5);
                break;
            case "wontfix":
                labelsSpinner.setSelection(6);
                break;
        }

        handler = new Handler();

        titleText = findViewById(R.id.title_text);
        titleText.setText(issueManager.getIssueTitle());

        bodyText = findViewById(R.id.body_text);
        bodyText.setText(issueManager.getIssueBody());

        Button saveButton = findViewById(R.id.save_button);
        saveButton.setOnClickListener(view -> {
            new Thread(() -> {
                if(issueNumber == null){
                    addIssue();
                } else {
                    //updateIssue();
                }
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
        String urlString = BuildConfig.URL + owner + "/" + repo + "/issues";
        String json = "";
        JsonNode jsonResult = null;
        ObjectMapper mapper = new ObjectMapper();

        if(titleText.getText().toString().equals("")){
            Toast.makeText(issueManager, "タイトルを入力してください", Toast.LENGTH_SHORT).show();
            return;
        }

        final okhttp3.MediaType mediaTypeJson = okhttp3.MediaType.parse("application/json; charset=UTF-8");

        try {
            IssueJson issue = new IssueJson();
            issue.setTitle(titleText.getText().toString());
            issue.setBody(bodyText.getText().toString());
            json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(issue);
            final RequestBody requestBody = RequestBody.create(json, mediaTypeJson);
            System.out.println(json);

            Request request = new Request.Builder()
                    .url(urlString)
                    .addHeader("Accept", "application/vnd.github+json")
                    .addHeader("Authorization", "token " + token)
                    .addHeader("X-GitHub-Api-Version", "2022-11-28")
                    .post(requestBody)
                    .build();

            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .build();
            Response response = okHttpClient.newCall(request).execute();

            json = response.body().string();
            //jsonResult = mapper.readTree(json);
            System.out.println(response.code());
            System.out.println(json);

            if(response.code() == 401){
                handler.post(() -> {
                    Toast.makeText(issueManager, "不正なtokenです", Toast.LENGTH_SHORT).show();
                });
                return;
            }

            if(!response.isSuccessful()){
                String s = json;
                handler.post(() -> {
                    Toast.makeText(issueManager, "通信に失敗しました", Toast.LENGTH_LONG).show();
                });
                return;
            }

            handler.post(() -> {
                Toast.makeText(issueManager, "保存しました", Toast.LENGTH_SHORT).show();
            });
        } catch (Exception e){
            e.printStackTrace();
        }
    }

//    // v4.35.0以降のみ対応
//    public void updateIssue(){
//        String urlString = BuildConfig.URL + owner + "/" + repo + "/issues/" + issueNumber;
//
//        System.out.println(urlString);
//
//        String json = "";
//        JsonNode jsonResult = null;
//        ObjectMapper mapper = new ObjectMapper();
//
//        final okhttp3.MediaType mediaTypeJson = okhttp3.MediaType.parse("application/json; charset=UTF-8");
//
//        try {
//            IssueJson issue = new IssueJson();
//            issue.setTitle(titleText.getText().toString());
//            issue.setBody(bodyText.getText().toString());
//            json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(issue);
//            final RequestBody requestBody = RequestBody.create(json, mediaTypeJson);
//            System.out.println(json);
//
//            Request request = new Request.Builder()
//                    .url(urlString)
//                    .addHeader("Accept", "application/vnd.github+json")
//                    .addHeader("Authorization", "token " + token)
//                    .addHeader("X-GitHub-Api-Version", "2022-11-28")
//                    .patch(requestBody)
//                    .build();
//
//            OkHttpClient okHttpClient = new OkHttpClient.Builder().build();
//            Response response = okHttpClient.newCall(request).execute();
//
//            json = response.body().string();
//            //jsonResult = mapper.readTree(json);
//            System.out.println(response.code());
//            System.out.println(json);
//
//            if(response.code() == 401){
//                handler.post(() -> {
//                    Toast.makeText(issueManager, "不正なtokenです", Toast.LENGTH_SHORT).show();
//                });
//                return;
//            }
//
//            if(!response.isSuccessful()){
//                String s = json;
//                handler.post(() -> {
//                    Toast.makeText(issueManager, "GitBucketのバージョンが古い可能性があります(v4.30.0未満)", Toast.LENGTH_LONG).show();
//                });
//                return;
//            }
//
//            handler.post(() -> {
//                Toast.makeText(issueManager, "保存しました", Toast.LENGTH_SHORT).show();
//            });
//        } catch (Exception e){
//            e.printStackTrace();
//        }
//    }
}