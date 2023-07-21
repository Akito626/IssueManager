package com.alha_app.issuemanager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;

public class ViewerActivity extends AppCompatActivity {
    private IssueManager issueManager;

    private TextView titleText;
    private TextView bodyText;
    private ArrayList<String> labelList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewer);

        issueManager = (IssueManager)this.getApplication();
        labelList = issueManager.getIssueLabel();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        titleText = findViewById(R.id.title_text);
        titleText.setText(issueManager.getIssueTitle());

        bodyText = findViewById(R.id.body_text);
        bodyText.setText(issueManager.getIssueBody());

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
}