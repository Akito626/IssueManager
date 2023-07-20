package com.alha_app.issuemanager;

import android.app.Application;
import android.content.SharedPreferences;

public class IssueManager extends Application {
    String token;
    String owner;
    String repo;
    String issueTitle;
    String issueBody;

    String issueNumber;
    String issueLabel;
    String issuePriority;

    @Override
    public void onCreate(){
        super.onCreate();
    }

    public String getToken() {
        if(token == null) {
            SharedPreferences preferences = getSharedPreferences("prefData", MODE_PRIVATE);
            token = preferences.getString("token", null);
        }
        return token;
    }
    public String getOwner() {
        if(owner == null) {
            SharedPreferences preferences = getSharedPreferences("prefData", MODE_PRIVATE);
            owner = preferences.getString("owner", null);
        }
        return owner;
    }
    public String getRepo() {
        if(repo == null) {
            SharedPreferences preferences = getSharedPreferences("prefData", MODE_PRIVATE);
            repo = preferences.getString("repo", null);
        }
        return repo;
    }
    public String getIssueTitle() {
        return issueTitle;
    }
    public String getIssueBody() {
        return issueBody;
    }
    public String getIssueNumber() {
        return issueNumber;
    }
    public String getIssueLabel() {
        return issueLabel;
    }
    public String getIssuePriority() {
        return issuePriority;
    }

    public void setToken(String token) {
        SharedPreferences preferences = getSharedPreferences("prefData", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("token", token);
        editor.commit();
        this.token = token;
    }
    public void setOwner(String owner) {
        SharedPreferences preferences = getSharedPreferences("prefData", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("owner", owner);
        editor.commit();
        this.owner = owner;
    }
    public void setRepo(String repo) {
        SharedPreferences preferences = getSharedPreferences("prefData", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("repo", repo);
        editor.commit();
        this.repo = repo;
    }
    public void setIssueTitle(String issueTitle) {
        this.issueTitle = issueTitle;
    }
    public void setIssueBody(String issueBody) {
        this.issueBody = issueBody;
    }
    public void setIssueNumber(String issueNumber) {
        this.issueNumber = issueNumber;
    }
    public void setIssueLabel(String issueLabel) {
        this.issueLabel = issueLabel;
    }
    public void setIssuePriority(String issuePriority) {
        this.issuePriority = issuePriority;
    }
}
