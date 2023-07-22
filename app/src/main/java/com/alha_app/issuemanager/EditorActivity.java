package com.alha_app.issuemanager;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;

import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.alha_app.issuemanager.model.CreateIssueDialog;
import com.alha_app.issuemanager.model.IssueJson;
import com.alha_app.issuemanager.model.LabelsDialog;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.w3c.dom.Text;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import okhttp3.FormBody;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class EditorActivity extends AppCompatActivity {
    private final int NUMBER_OF_LABEL = 6;
    private Handler handler;
    private IssueManager issueManager;
    private String token;
    private String owner;
    private String repo;

    private EditText titleText;
    private EditText bodyText;
    private boolean[] choicesChecked;
    private AlertDialog assigneeDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        issueManager = (IssueManager) this.getApplication();
        token = issueManager.getToken();
        owner = issueManager.getOwner();
        repo = issueManager.getRepo();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        handler = new Handler();

        titleText = findViewById(R.id.title_text);
        titleText.setText(issueManager.getIssueTitle());

        bodyText = findViewById(R.id.body_text);
        bodyText.setText(issueManager.getIssueBody());

        // 登録ボタンを押したときのイベント
        Button createButton = findViewById(R.id.create_button);
        createButton.setOnClickListener(view -> {
            if(titleText.getText().toString().equals("")){
                Toast.makeText(issueManager, "タイトルを入力してください", Toast.LENGTH_SHORT).show();
                return;
            }
            DialogFragment dialog = new CreateIssueDialog(this);
            dialog.show(getSupportFragmentManager(), "createDialog");
        });

        // labelの編集ボタン
        choicesChecked = new boolean[NUMBER_OF_LABEL];
        Button labelButton = findViewById(R.id.label_button);
        labelButton.setOnClickListener(view -> {
            DialogFragment dialog = new LabelsDialog(this, choicesChecked);
            dialog.show(getSupportFragmentManager(), "labelsDialog");
        });

        // issueに人を割り当てるボタン
        Button assigneeButton = findViewById(R.id.assignee_button);
        assigneeButton.setOnClickListener(view -> {
            if(assigneeDialog == null){
                assigneeDialog = new AlertDialog.Builder(this)
                        .setTitle("割り当てる人の名前を記入")
                        .setMessage("開発中")
                        .setPositiveButton("OK", null)
                        .create();
            }
            assigneeDialog.show();
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
        ObjectMapper mapper = new ObjectMapper();

        String[] labels = getResources().getStringArray(R.array.labels);

        final okhttp3.MediaType mediaTypeJson = okhttp3.MediaType.parse("application/json; charset=UTF-8");

        try {
            // 送るJsonデータを作成
            IssueJson issue = new IssueJson();
            issue.setTitle(titleText.getText().toString());
            issue.setBody(bodyText.getText().toString());
            ArrayList<String> labelList = new ArrayList<>();
            for(int i = 0; i < choicesChecked.length; i++){
                if(choicesChecked[i])labelList.add(labels[i]);
            }
            issue.setLabels(labelList);
            ArrayList<String> assigneeList = new ArrayList<>();
            issue.setAssignees(assigneeList);
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

            OkHttpClient okHttpClient = new OkHttpClient.Builder().build();
            Response response = okHttpClient.newCall(request).execute();

            json = response.body().string();

            if(response.code() == 401){
                handler.post(() -> {
                    Toast.makeText(issueManager, "不正なtokenです", Toast.LENGTH_SHORT).show();
                });
                return;
            }

            if(!response.isSuccessful()){
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

    // 選択したラベルのみ見えるようにする
    public void setLabels(){
        TextView defaultLabel = findViewById(R.id.label_default);
        TextView[] labels = new TextView[6];
        labels[0] = findViewById(R.id.label_bug);
        labels[1] = findViewById(R.id.label_duplicate);
        labels[2] = findViewById(R.id.label_enhancement);
        labels[3] = findViewById(R.id.label_invalid);
        labels[4] = findViewById(R.id.label_question);
        labels[5] = findViewById(R.id.label_wontfix);

        boolean isDefault = true;
        for(int i = 0; i < choicesChecked.length; i++){
            if(choicesChecked[i]){
                isDefault = false;
                labels[i].setVisibility(View.VISIBLE);
            } else {
                labels[i].setVisibility(View.GONE);
            }
        }
        if(isDefault){
            defaultLabel.setVisibility(View.VISIBLE);
        } else {
            defaultLabel.setVisibility(View.GONE);
        }
    }

    public void createIssue(){
        new Thread(() -> addIssue()).start();
    }
}