package com.alha_app.issuemanager;

import android.app.Application;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class IssueManager extends Application {
    private String token;
    private String owner;
    private String repo;
    private String issueUser;
    private String issueTitle;
    private String issueBody;

    private String issueNumber;
    private ArrayList<String> issueLabel = new ArrayList<>();
    private String commentUrl;
    private boolean isNotify;

    @Override
    public void onCreate(){
        super.onCreate();
    }

    public String getToken() {
        if(token == null) {
            SharedPreferences preferences = getSharedPreferences("prefData", MODE_PRIVATE);
            token = preferences.getString("token", "");
        }
        return token;
    }
    public String getOwner() {
        if(owner == null) {
            SharedPreferences preferences = getSharedPreferences("prefData", MODE_PRIVATE);
            owner = preferences.getString("owner", "");
        }
        return owner;
    }
    public String getRepo() {
        if(repo == null) {
            SharedPreferences preferences = getSharedPreferences("prefData", MODE_PRIVATE);
            repo = preferences.getString("repo", "");
        }
        return repo;
    }

    public String getIssueUser() {
        return issueUser;
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

    public ArrayList<String> getIssueLabel() {
        return issueLabel;
    }
    public String getCommentUrl() {
        return commentUrl;
    }
    public boolean isNotify() {
        if(!isNotify) {
            SharedPreferences preferences = getSharedPreferences("prefData", MODE_PRIVATE);
            isNotify = preferences.getBoolean("notify", false);
        }
        return isNotify;
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

    public void setIssueUser(String issueUser) {
        this.issueUser = issueUser;
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

    public void setIssueLabel(ArrayList<String> issueLabel) {
        this.issueLabel = issueLabel;
    }
    public void setCommentUrl(String commentUrl) {
        this.commentUrl = commentUrl;
    }
    public void setNotify(boolean notify) {
        SharedPreferences preferences = getSharedPreferences("prefData", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("notify", notify);
        editor.commit();
        isNotify = notify;
    }
}