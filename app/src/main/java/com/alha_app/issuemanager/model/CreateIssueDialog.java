package com.alha_app.issuemanager.model;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.alha_app.issuemanager.EditorActivity;
import com.alha_app.issuemanager.R;

public class CreateIssueDialog extends DialogFragment {
    private EditorActivity editorActivity;

    public CreateIssueDialog(EditorActivity editorActivity){
        this.editorActivity = editorActivity;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        String [] choices = getResources().getStringArray(R.array.labels);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("警告")
                .setMessage("登録したissueはアプリ上では編集できません。確認の上登録してください")
                .setPositiveButton("登録", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        editorActivity.createIssue();
                    }
                })
                .setNeutralButton("キャンセル", null);
        return builder.create();
    }
}
