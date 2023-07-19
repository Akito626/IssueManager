package com.alha_app.issuemanager;

import android.app.Application;

public class IssueManager extends Application {
    String token;
    String owner;
    String repo;
    String issueTitle;
    String issueBody;

    String issueNumber;

    @Override
    public void onCreate(){
        super.onCreate();
    }

    public String getToken() {
        return token;
    }
    public String getOwner() {
        return owner;
    }
    public String getRepo() {
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

    public void setToken(String token) {
        this.token = token;
    }
    public void setOwner(String owner) {
        this.owner = owner;
    }
    public void setRepo(String repo) {
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
}
