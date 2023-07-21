package com.alha_app.issuemanager.model;

import android.widget.ArrayAdapter;

import java.util.ArrayList;

public class IssueJson {
    String title;
    String body;
    ArrayList<String> labels = new ArrayList<>();
    ArrayList<String> assignees = new ArrayList<>();

    public String getTitle() {
        return title;
    }
    public String getBody() {
        return body;
    }

    public ArrayList<String> getLabels() {
        return labels;
    }

    public ArrayList<String> getAssignees() {
        return assignees;
    }

    public void setTitle(String title) {
        this.title = title;
    }
    public void setBody(String body) {
        this.body = body;
    }
    public void setLabels(ArrayList<String> labels) {
        this.labels = labels;
    }
    public void setAssignees(ArrayList<String> assignees) {
        this.assignees = assignees;
    }
}
