package com.alha_app.issuemanager.model;

import android.widget.ArrayAdapter;

import java.util.ArrayList;

public class IssueJson {
    String title;
    String body;
    //ArrayList<String> arrayList = new ArrayList<>();

    public String getTitle() {
        return title;
    }
    public String getBody() {
        return body;
    }

//    public ArrayList<String> getArrayList() {
//        return arrayList;
//    }

    public void setTitle(String title) {
        this.title = title;
    }
    public void setBody(String body) {
        this.body = body;
    }

//    public void setArrayList(ArrayList<String> arrayList) {
//        this.arrayList = arrayList;
//    }
}
