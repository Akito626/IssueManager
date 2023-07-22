package com.alha_app.issuemanager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Insets;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.text.method.ScrollingMovementMethod;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.view.WindowMetrics;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.alha_app.issuemanager.model.CommentJson;
import com.alha_app.issuemanager.model.IssueJson;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ViewerActivity extends AppCompatActivity {
    private Handler handler;
    private IssueManager issueManager;
    private TextView issueUser;
    private TextView titleText;
    private TextView bodyText;
    private ArrayList<String> labelList;
    private EditText commentText;

    private String token;
    private String owner;
    private String repo;
    private String issueNumber;

    private AlertDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewer);

        issueManager = (IssueManager) this.getApplication();
        token = issueManager.getToken();
        owner = issueManager.getOwner();
        repo = issueManager.getRepo();
        issueNumber = issueManager.getIssueNumber();
        labelList = issueManager.getIssueLabel();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        handler = new Handler();

        issueUser = findViewById(R.id.issue_user);
        issueUser.setText(issueManager.getIssueUser());

        titleText = findViewById(R.id.title_text);
        titleText.setText(issueManager.getIssueTitle());
        titleText.setMovementMethod(new ScrollingMovementMethod());

        bodyText = findViewById(R.id.body_text);
        bodyText.setText(issueManager.getIssueBody());
        bodyText.setMovementMethod(new ScrollingMovementMethod());

        // ダイアログ用のレイアウトを作成
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.layout_comment_dialog, null);
        commentText = dialogView.findViewById(R.id.comment_edittext);

        boolean isDefault = true;
        for (int i = 0; i < labelList.size(); i++) {
            switch (labelList.get(i)) {
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
        if (!isDefault) {
            TextView defaultLabel = findViewById(R.id.label_default);
            defaultLabel.setVisibility(View.GONE);
        }

        Button addCommentButton = findViewById(R.id.add_comment_button);
        addCommentButton.setOnClickListener(view -> {
            if(dialog == null) {
                dialog = new AlertDialog.Builder(this)
                        .setTitle("コメントを記入")
                        .setView(dialogView)
                        .setPositiveButton("投稿", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                new Thread(() -> {
                                    addComment();
                                    getComments();
                                }).start();
                            }
                        })
                        .setNeutralButton("キャンセル", null)
                        .create();
                dialog.setCanceledOnTouchOutside(false);
            }
            dialog.show();
        });

        new Thread(() -> getComments()).start();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuButton) {
        boolean result = true;
        int buttonId = menuButton.getItemId();
        switch (buttonId) {
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

    public void getComments() {
        String urlString = issueManager.getCommentUrl();

        String json = "";
        JsonNode jsonResult = null;
        ObjectMapper mapper = new ObjectMapper();

        ListView commentList = findViewById(R.id.comment_list);
        ArrayList<Map<String, Object>> listData = new ArrayList<>();

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

            for (int i = 0; i < jsonResult.size(); i++) {
                Map<String, Object> item = new HashMap<>();
                String tmp = jsonResult.get(i).get("user").get("login").toString();
                tmp = tmp.substring(1, tmp.length() - 1);
                item.put("comment_user", tmp);
                tmp = jsonResult.get(i).get("body").toString();
                tmp = tmp.substring(1, tmp.length() - 1);
                tmp = tmp.replaceAll("\\\\r", "");
                tmp = tmp.replaceAll("\\\\n", "\n");
                item.put("comment_text", tmp);
                listData.add(item);
            }

            handler.post(() -> {
                commentList.setAdapter(new SimpleAdapter(
                        this,
                        listData,
                        R.layout.comment_list_item,
                        new String[]{"comment_user", "comment_text"},
                        new int[]{R.id.comment_user, R.id.comment_text}
                ));
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addComment() {
        String urlString = BuildConfig.URL + owner + "/" + repo + "/issues/" + issueNumber + "/comments";

        String json = "";
        ObjectMapper mapper = new ObjectMapper();

        final okhttp3.MediaType mediaTypeJson = okhttp3.MediaType.parse("application/json; charset=UTF-8");

        try {
            CommentJson commentJson = new CommentJson();
            commentJson.setBody(commentText.getText().toString());
            json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(commentJson);
            final RequestBody requestBody = RequestBody.create(json, mediaTypeJson);
            System.out.println(json);

            Request request = new Request.Builder()
                    .url(urlString)
                    .addHeader("Accept", "application/vnd.github+json")
                    .addHeader("Authorization", "token " + token)
                    .addHeader("X-GitHub-Api-Version", "2022-11-28")
                    .post(requestBody)
                    .build();
            OkHttpClient okHttpClient = new OkHttpClient.Builder().build();
            Response response = okHttpClient.newCall(request).execute();

            if (!response.isSuccessful()) {
                Toast.makeText(issueManager, "通信エラーが発生しました", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}