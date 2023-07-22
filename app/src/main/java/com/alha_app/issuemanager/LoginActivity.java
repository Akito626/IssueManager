package com.alha_app.issuemanager;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class LoginActivity extends AppCompatActivity {
    private IssueManager issueManager;
    private String tokenStr;
    private String ownerStr;
    private String repoStr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // 保存しているデータを取得し、セットする。(ログアウトや戻るボタンを押したときに再入力の手間を省くため)
        issueManager = (IssueManager) this.getApplication();
        tokenStr = issueManager.getToken();
        ownerStr = issueManager.getOwner();
        repoStr = issueManager.getRepo();

        EditText token = findViewById(R.id.token);
        EditText owner = findViewById(R.id.owner);
        EditText repo = findViewById(R.id.repo);
        token.setText(tokenStr);
        owner.setText(ownerStr);
        repo.setText(repoStr);

        // 全て入力されていれば自動ログイン
        if(!tokenStr.equals("") && !ownerStr.equals("") && !repoStr.equals("")){
            startActivity(new Intent(getApplication(), MainActivity.class));
        }

        // ログインボタンを押したときのイベント
        Button loginButton = findViewById(R.id.login_button);
        loginButton.setOnClickListener(view -> {
            if(token.getText().toString().equals("") || owner.getText().toString().equals("") || repo.getText().toString().equals("")){
                Toast.makeText(issueManager, "全て入力してください", Toast.LENGTH_SHORT).show();
                return;
            }
            issueManager.setToken(token.getText().toString().trim());
            issueManager.setOwner(owner.getText().toString().trim());
            issueManager.setRepo(repo.getText().toString().trim());
            startActivity(new Intent(getApplication(), MainActivity.class));
        });
    }
}