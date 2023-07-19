package com.alha_app.issuemanager;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

public class EditorActivity extends AppCompatActivity {
    private IssueManager issueManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        issueManager = (IssueManager) this.getApplication();

        TextView textView = findViewById(R.id.title_text);
        textView.setText(issueManager.getIssueTitle());

        TextView bodyText = findViewById(R.id.body_text);
        bodyText.setText(issueManager.getIssueBody());
    }
}