package com.alha_app.issuemanager.model;

import java.util.ArrayList;

public class IssueData {
    String title;
    String body;
    String number;
    ArrayList<String> labelList = new ArrayList<>();

    public String getTitle() {
        return title;
    }
    public String getBody() {
        return body;
    }
    public String getNumber() {
        return number;
    }
    public ArrayList<String> getLabelList() {
        return labelList;
    }

    public void setTitle(String title) {
        this.title = title;
    }
    public void setBody(String body) {
        this.body = body;
    }
    public void setNumber(String number) {
        this.number = number;
    }
    public void setLabelList(ArrayList<String> labelList) {
        this.labelList = labelList;
    }

    public void addLabel(String label){
        labelList.add(label);
    }
}
