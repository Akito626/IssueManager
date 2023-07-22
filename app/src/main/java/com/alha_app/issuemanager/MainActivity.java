package com.alha_app.issuemanager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.alha_app.issuemanager.model.IssueData;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private IssueManager issueManager;
    private String token;
    private String owner;
    private String repo;
    private ListView openIssueList;
    private ListView closedIssueList;
    private ArrayList<IssueData> openIssueData;
    private ArrayList<IssueData> closedIssueData;
    private Handler handler;

    private TextView openText;
    private TextView closedText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        issueManager = (IssueManager) this.getApplication();
        token = issueManager.getToken();
        owner = issueManager.getOwner();
        repo = issueManager.getRepo();

        openIssueList = findViewById(R.id.open_issuelist);
        closedIssueList = findViewById(R.id.closed_issuelist);
        handler = new Handler();

        openIssueData = new ArrayList<>();
        closedIssueData = new ArrayList<>();

        // openのissueListをクリックした時のイベント
        openIssueList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                issueManager.setIssueUser(openIssueData.get(position).getUser());
                issueManager.setIssueTitle(openIssueData.get(position).getTitle());
                issueManager.setIssueBody(openIssueData.get(position).getBody());
                issueManager.setIssueNumber(openIssueData.get(position).getNumber());
                issueManager.setIssueLabel(openIssueData.get(position).getLabelList());
                issueManager.setCommentUrl(openIssueData.get(position).getCommentUrl());
                startActivity(new Intent(getApplication(), ViewerActivity.class));
            }
        });

        // closeのissueListをクリックした時のイベント
        closedIssueList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                issueManager.setIssueUser(closedIssueData.get(position).getUser());
                issueManager.setIssueTitle(closedIssueData.get(position).getTitle());
                issueManager.setIssueBody(closedIssueData.get(position).getBody());
                issueManager.setIssueNumber(closedIssueData.get(position).getNumber());
                issueManager.setIssueLabel(closedIssueData.get(position).getLabelList());
                issueManager.setCommentUrl(closedIssueData.get(position).getCommentUrl());
                startActivity(new Intent(getApplication(), ViewerActivity.class));
            }
        });

        // openを押すとopenのissueを表示
        openText = findViewById(R.id.open);
        openText.setOnClickListener(v -> {
            closedIssueList.setVisibility(View.INVISIBLE);
            openIssueList.setVisibility(View.VISIBLE);
            openText.setBackgroundColor(Color.parseColor("#afafb0"));
            closedText.setBackgroundColor(Color.parseColor("#00000000"));
        });
        // closedを押すとclosedのissueを表示
        closedText = findViewById(R.id.closed);
        closedText.setOnClickListener(v -> {
            openIssueList.setVisibility(View.INVISIBLE);
            closedIssueList.setVisibility(View.VISIBLE);
            closedText.setBackgroundColor(Color.parseColor("#afafb0"));
            openText.setBackgroundColor(Color.parseColor("#00000000"));
        });

        // 起動時に一度データを取得する
        new Thread(() -> {
            int i = getIssues("closed");
            closedText.setText("Closed " + i);
            i = getIssues("open");
            openText.setText("Open " + i);
        }).start();
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        //　追加ボタンを押すと閲覧画面に移動
        if (item.getItemId() == R.id.action_add) {
            startActivity(new Intent(getApplication(), EditorActivity.class));
        } else if (item.getItemId() == R.id.action_update) {    // updateボタンを押すとデータを再取得
            TextView textView = findViewById(R.id.nodata_text);
            textView.setVisibility(View.INVISIBLE);
            openIssueData.clear();
            closedIssueData.clear();
            new Thread(() -> {
                int i = getIssues("closed");
                closedText.setText("Closed " + i);
                i = getIssues("open");
                openText.setText("Open " + i);
            }).start();
        } else if (item.getItemId() == R.id.action_setting){
            startActivity(new Intent(getApplication(), SettingActivity.class));
        } else if (item.getItemId() == R.id.action_logout) {    // ログアウトボタンを押すとログイン画面に移動
            issueManager.setToken(null);
            issueManager.setOwner(null);
            issueManager.setRepo(null);
            finish();
        }
        return true;
    }

    public int getIssues(String s) {
        String urlString = BuildConfig.URL + owner + "/" + repo + "/issues" + "?state=" + s;

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

            // データがなければデータがないことを表示
            if (jsonResult.size() == 0) {
                handler.post(() -> {
                    TextView textView = findViewById(R.id.nodata_text);
                    textView.setVisibility(View.VISIBLE);
                });
                return 0;
            }

            ArrayList<Map<String, String>> listData = new ArrayList<>();
            String tmp = "";
            for (int i = 0; i < jsonResult.size(); i++) {
                Map<String, String> item = new HashMap<>();
                IssueData issueData = new IssueData();

                // タイトルを取得
                tmp = jsonResult.get(i).get("title").toString();
                tmp = tmp.substring(1, tmp.length() - 1);
                tmp = tmp.replaceAll("\\\\r", "");
                tmp = tmp.replaceAll("\\\\n", "\n");
                item.put("title", tmp);
                issueData.setTitle(tmp);

                // 内容を取得
                tmp = jsonResult.get(i).get("body").toString();
                tmp = tmp.substring(1, tmp.length() - 1);
                tmp = tmp.replaceAll("\\\\r", "");
                tmp = tmp.replaceAll("\\\\n", "\n");
                issueData.setBody(tmp);

                // issueを登録した人を取得
                tmp = jsonResult.get(i).get("user").get("login").toString();
                tmp = tmp.substring(1, tmp.length() - 1);
                item.put("name", tmp);
                issueData.setUser(tmp);

                // ラベルを取得する。設定されていなければdefaultをセット
                if (jsonResult.get(i).get("labels").size() == 0) {
                    item.put("labels", "default");
                    issueData.addLabel("default");
                } else {
                    tmp = jsonResult.get(i).get("labels").get(0).get("name").toString() + "\n";
                    issueData.addLabel(jsonResult.get(i).get("labels").get(0).get("name").toString().replaceAll("\\\"", ""));
                    for(int j = 1; j < jsonResult.get(i).get("labels").size(); j++) {
                        tmp += jsonResult.get(i).get("labels").get(j).get("name").toString() + "\n";
                        issueData.addLabel(jsonResult.get(i).get("labels").get(j).get("name").toString().replaceAll("\"", ""));
                    }
                    tmp = tmp.replaceAll("\\\"", "");
                    item.put("labels", tmp);
                }

                // issueが登録された日時を取得
                tmp = jsonResult.get(i).get("created_at").toString();
                tmp = tmp.substring(1, tmp.length() - 1);
                tmp = tmp.replace("T", " ");
                tmp = tmp.replace("Z", "");
                item.put("date", tmp);

                // issueNumberを取得
                tmp = jsonResult.get(i).get("number").toString();
                issueData.setNumber(tmp);

                // commentのurl
                tmp = jsonResult.get(i).get("comments_url").toString();
                tmp = tmp.substring(1, tmp.length() - 1);
                issueData.setCommentUrl(tmp);

                listData.add(item);

                if (s.equals("open")) {
                    openIssueData.add(issueData);
                } else if (s.equals("closed")) {
                    closedIssueData.add(issueData);
                }
            }

            Map<String, Object> issueMap = new HashMap<>();
            for (int i = 0; i < openIssueData.size(); i++){
                issueMap.put(openIssueData.get(i).getNumber(), openIssueData.get(i).getTitle());
            }
            issueManager.setIssueMap(issueMap);

            // openとclosedをそれぞれのリストに表示
            if (s.equals("open")) {
                handler.post(() -> {
                    openIssueList.setAdapter(new SimpleAdapter(
                            this,
                            listData,
                            R.layout.issuelist_item,
                            new String[]{"title", "date", "labels", "name"},
                            new int[]{R.id.title, R.id.date, R.id.labels, R.id.name}
                    ));
                });
            } else if (s.equals("closed")) {
                handler.post(() -> {
                    closedIssueList.setAdapter(new SimpleAdapter(
                            this,
                            listData,
                            R.layout.issuelist_item,
                            new String[]{"title", "date", "labels", "name"},
                            new int[]{R.id.title, R.id.date, R.id.labels, R.id.name}
                    ));
                });
            }
            return jsonResult.size();
        } catch (Exception e) {
            handler.post(() -> {
                TextView textView = findViewById(R.id.nodata_text);
                textView.setVisibility(View.VISIBLE);
            });
            e.printStackTrace();
        }
        return 0;
    }
}