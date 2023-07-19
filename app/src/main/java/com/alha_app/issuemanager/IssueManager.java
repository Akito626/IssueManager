package com.alha_app.issuemanager;

import android.app.Application;

public class IssueManager extends Application {
    String issueTitle;
    String issueBody;

    @Override
    public void onCreate(){
        super.onCreate();
    }

    public String getIssueTitle() {
        return issueTitle;
    }

    public String getIssueBody() {
        return issueBody;
    }

    public void setIssueTitle(String issueTitle) {
        this.issueTitle = issueTitle;
    }

    public void setIssueBody(String issueBody) {
        this.issueBody = issueBody;
    }
}
