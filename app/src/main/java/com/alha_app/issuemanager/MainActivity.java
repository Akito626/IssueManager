package com.alha_app.issuemanager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;

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
    private ArrayList<Map<String, String>> openIssueSet;
    private ArrayList<Map<String, String>> closedIssueSet;
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

        openIssueSet = new ArrayList<>();
        closedIssueSet = new ArrayList<>();

        openIssueList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                issueManager.setIssueTitle(openIssueSet.get(position).get("title"));
                issueManager.setIssueBody(openIssueSet.get(position).get("body"));
                issueManager.setIssueNumber(openIssueSet.get(position).get("number"));
                issueManager.setIssueLabel(openIssueSet.get(position).get("label"));
                startActivity(new Intent(getApplication(), EditorActivity.class));
            }
        });
        closedIssueList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                issueManager.setIssueTitle(closedIssueSet.get(position).get("title"));
                issueManager.setIssueBody(closedIssueSet.get(position).get("body"));
                issueManager.setIssueNumber(closedIssueSet.get(position).get("number"));
                issueManager.setIssueLabel(closedIssueSet.get(position).get("label"));
                startActivity(new Intent(getApplication(), EditorActivity.class));
            }
        });

        openText = findViewById(R.id.open);
        openText.setOnClickListener(v -> {
            closedIssueList.setVisibility(View.INVISIBLE);
            openIssueList.setVisibility(View.VISIBLE);
            openText.setBackgroundColor(Color.parseColor("#afafb0"));
            closedText.setBackgroundColor(Color.parseColor("#00000000"));
        });
        closedText = findViewById(R.id.closed);
        closedText.setOnClickListener(v -> {
            openIssueList.setVisibility(View.INVISIBLE);
            closedIssueList.setVisibility(View.VISIBLE);
            closedText.setBackgroundColor(Color.parseColor("#afafb0"));
            openText.setBackgroundColor(Color.parseColor("#00000000"));
        });

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
        if (item.getItemId() == R.id.action_add) {
            issueManager.setIssueTitle(null);
            issueManager.setIssueBody(null);
            issueManager.setIssueNumber(null);
            issueManager.setIssueLabel(null);
            startActivity(new Intent(getApplication(), EditorActivity.class));
        } else if (item.getItemId() == R.id.action_update) {
            TextView textView = findViewById(R.id.nodata_text);
            textView.setVisibility(View.INVISIBLE);
            new Thread(() -> {
                int i = getIssues("closed");
                closedText.setText("Closed " + i);
                i = getIssues("open");
                openText.setText("Open " + i);
            }).start();
        } else if (item.getItemId() == R.id.action_logout) {
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
                Map<String, String> issueData = new HashMap<>();

                tmp = jsonResult.get(i).get("title").toString();
                tmp = tmp.substring(1, tmp.length() - 1);
                tmp = tmp.replaceAll("\\\\r", "");
                tmp = tmp.replaceAll("\\\\n", "\n");
                item.put("title", tmp);
                issueData.put("title", tmp);

                tmp = jsonResult.get(i).get("body").toString();
                tmp = tmp.substring(1, tmp.length() - 1);
                tmp = tmp.replaceAll("\\\\r", "");
                tmp = tmp.replaceAll("\\\\n", "\n");
                issueData.put("body", tmp);

                tmp = jsonResult.get(i).get("user").get("login").toString();
                tmp = tmp.substring(1, tmp.length() - 1);
                item.put("name", tmp);
                issueData.put("name", tmp);

                if (jsonResult.get(i).get("labels").size() == 0) {
                    item.put("label", "default");
                    issueData.put("label", "default");
                } else {
                    tmp = jsonResult.get(i).get("labels").get(0).get("name").toString();
                    tmp = tmp.substring(1, tmp.length() - 1);
                    item.put("label", tmp);
                    issueData.put("label", tmp);
                }

                tmp = jsonResult.get(i).get("created_at").toString();
                tmp = tmp.substring(1, tmp.length() - 1);
                tmp = tmp.replace("T", " ");
                tmp = tmp.replace("Z", "");
                item.put("date", tmp);
                issueData.put("date", tmp);

                tmp = jsonResult.get(i).get("number").toString();
                issueData.put("number", tmp);

                listData.add(item);

                if (s.equals("open")) {
                    openIssueSet.add(issueData);
                } else if (s.equals("closed")) {
                    closedIssueSet.add(issueData);
                }
            }

            if (s.equals("open")) {
                handler.post(() -> {
                    openIssueList.setAdapter(new SimpleAdapter(
                            this,
                            listData,
                            R.layout.issuelist_item,
                            new String[]{"title", "date", "label", "name"},
                            new int[]{R.id.title, R.id.date, R.id.label, R.id.name}
                    ));
                });
            } else if (s.equals("closed")) {
                handler.post(() -> {
                    closedIssueList.setAdapter(new SimpleAdapter(
                            this,
                            listData,
                            R.layout.issuelist_item,
                            new String[]{"title", "date", "label", "name"},
                            new int[]{R.id.title, R.id.date, R.id.label, R.id.name}
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