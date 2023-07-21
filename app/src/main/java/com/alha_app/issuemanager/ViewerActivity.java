package com.alha_app.issuemanager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.os.Handler;
import android.text.method.ScrollingMovementMethod;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ViewerActivity extends AppCompatActivity {
    private IssueManager issueManager;

    private TextView titleText;
    private TextView bodyText;
    private ArrayList<String> labelList;

    private String token;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewer);

        issueManager = (IssueManager)this.getApplication();
        token = issueManager.getToken();
        labelList = issueManager.getIssueLabel();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        handler = new Handler();

        titleText = findViewById(R.id.title_text);
        titleText.setText(issueManager.getIssueTitle());
        titleText.setMovementMethod(new ScrollingMovementMethod());

        bodyText = findViewById(R.id.body_text);
        bodyText.setText(issueManager.getIssueBody());
        bodyText.setMovementMethod(new ScrollingMovementMethod());

        boolean isDefault = true;
        for(int i = 0; i < labelList.size(); i++){
            switch (labelList.get(i)){
                case "bug":
                    TextView bugLabel = findViewById(R.id.label_bug);
                    bugLabel.setVisibility(View.VISIBLE);
                    isDefault = false;
                    break;
                case "duplicate":
                    TextView duplicateLabel = findViewById(R.id.label_duplicate);
                    duplicateLabel.setVisibility(View.VISIBLE);
                    isDefault = false;
                    break;
                case "enhancement":
                    TextView enhancementLabel = findViewById(R.id.label_enhancement);
                    enhancementLabel.setVisibility(View.VISIBLE);
                    isDefault = false;
                    break;
                case "invalid":
                    TextView invalidLabel = findViewById(R.id.label_invalid);
                    invalidLabel.setVisibility(View.VISIBLE);
                    isDefault = false;
                    break;
                case "question":
                    TextView questionLabel = findViewById(R.id.label_question);
                    questionLabel.setVisibility(View.VISIBLE);
                    isDefault = false;
                    break;
                case "wontfix":
                    TextView wontfixLabel = findViewById(R.id.label_wontfix);
                    wontfixLabel.setVisibility(View.VISIBLE);
                    isDefault = false;
                    break;
                default:
                    break;
            }
        }
        if(!isDefault){
            TextView defaultLabel = findViewById(R.id.label_default);
            defaultLabel.setVisibility(View.GONE);
        }

        new Thread(() -> getComments()).start();
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

    public void getComments(){
        String urlString = issueManager.getCommentUrl();

        String json = "";
        JsonNode jsonResult = null;
        ObjectMapper mapper = new ObjectMapper();

        ListView commentList = findViewById(R.id.comment_list);
        ArrayList<Map<String, Object>> listData = new ArrayList<>();

        int sampleImage = R.drawable.ic_sample_face;

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

            for(int i = 0; i < jsonResult.size(); i++){
                Map<String, Object> item = new HashMap<>();
                String tmp = jsonResult.get(i).get("body").toString();
                tmp = tmp.substring(1, tmp.length()-1);
                item.put("user_image", sampleImage);
                item.put("comment_text", tmp);
                listData.add(item);
            }

            handler.post(() -> {
                commentList.setAdapter(new SimpleAdapter(
                        this,
                        listData,
                        R.layout.comment_list_item,
                        new String[]{"user_image", "comment_text"},
                        new int[]{R.id.user_image, R.id.comment_text}
                ));
            });
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}