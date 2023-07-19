package com.alha_app.issuemanager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
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
import java.util.Map;
import java.util.Objects;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private IssueManager issueManager;
    private ListView openIssueList;
    private ListView closedIssueList;
    private ArrayList<String> openIssueTitle;
    private ArrayList<String> openIssueBodies;
    private ArrayList<String> closedIssueTitle;
    private ArrayList<String> closedIssueBodies;
    private Handler handler;

    TextView openText;
    TextView closedText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        issueManager = (IssueManager) this.getApplication();

        openIssueList = findViewById(R.id.open_issuelist);
        closedIssueList = findViewById(R.id.closed_issuelist);
        handler = new Handler();

        openIssueTitle = new ArrayList<>();
        openIssueBodies = new ArrayList<>();
        closedIssueTitle = new ArrayList<>();
        closedIssueBodies = new ArrayList<>();

        openIssueList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                issueManager.setIssueTitle(openIssueTitle.get(position));
                issueManager.setIssueBody(openIssueBodies.get(position));
                startActivity(new Intent(getApplication(), EditorActivity.class));
            }
        });
        closedIssueList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                issueManager.setIssueTitle(closedIssueTitle.get(position));
                issueManager.setIssueBody(closedIssueBodies.get(position));
                startActivity(new Intent(getApplication(), EditorActivity.class));
            }
        });

        openText = findViewById(R.id.open);
        openText.setOnClickListener(v -> {
            closedIssueList.setVisibility(View.INVISIBLE);
            openIssueList.setVisibility(View.VISIBLE);
        });
        closedText = findViewById(R.id.closed);
        closedText.setOnClickListener(v -> {
            openIssueList.setVisibility(View.INVISIBLE);
            closedIssueList.setVisibility(View.VISIBLE);
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
            Toast.makeText(issueManager, "追加", Toast.LENGTH_SHORT).show();
        } else if(item.getItemId() == R.id.action_update){
            new Thread(() -> {
                int i = getIssues("closed");
                closedText.setText("Closed " + i);
                i = getIssues("open");
                openText.setText("Open " + i);
            }).start();
        }
        return true;
    }

    public int getIssues(String s){
        String urlString = BuildConfig.URL + "?state=" + s;

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
                if(s.equals("open")){
                    openIssueTitle.add(tmp);
                } else if(s.equals("closed")){
                    closedIssueTitle.add(tmp);
                }

                item.put("title", tmp);
                tmp = jsonResult.get(i).get("body").toString();
                tmp = tmp.substring(1, tmp.length()-1);
                tmp = tmp.replaceAll("\\\\r", "");
                tmp = tmp.replaceAll("\\\\n", "\n");
                if(s.equals("open")){
                    openIssueBodies.add(tmp);
                } else if(s.equals("closed")){
                    closedIssueBodies.add(tmp);
                }

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

            if(s.equals("open")) {
                handler.post(() -> {
                    openIssueList.setAdapter(new SimpleAdapter(
                            this,
                            listData,
                            R.layout.issuelist_item,
                            new String[]{"title", "date", "label", "name"},
                            new int[]{R.id.title, R.id.date, R.id.label, R.id.name}
                    ));
                });
            } else if(s.equals("closed")){
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
        } catch (Exception e){
            e.printStackTrace();
        }
        return 0;
    }
}