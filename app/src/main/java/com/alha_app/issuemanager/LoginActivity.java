package com.alha_app.issuemanager;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class LoginActivity extends AppCompatActivity {
    private IssueManager issueManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        issueManager = (IssueManager) this.getApplication();

        EditText token = findViewById(R.id.token);
        EditText owner = findViewById(R.id.owner);
        EditText repo = findViewById(R.id.repo);

        Button loginButton = findViewById(R.id.login_button);
        loginButton.setOnClickListener(view -> {
            if(token.getText().toString().equals("") || owner.getText().toString().equals("") || repo.getText().toString().equals("")){
                Toast.makeText(issueManager, "全て入力してください", Toast.LENGTH_SHORT).show();
                return;
            }
            issueManager.setToken(token.getText().toString());
            issueManager.setOwner(owner.getText().toString());
            issueManager.setRepo(repo.getText().toString());
            startActivity(new Intent(getApplication(), MainActivity.class));
        });
    }
}