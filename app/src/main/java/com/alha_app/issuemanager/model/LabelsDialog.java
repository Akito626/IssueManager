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

public class LabelsDialog extends DialogFragment {
    private EditorActivity editorActivity;
    private boolean[] choicesChecked;
    public LabelsDialog(EditorActivity editorActivity, boolean[] choicesChecked){
        this.editorActivity = editorActivity;
        this.choicesChecked = choicesChecked;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        String [] choices = getResources().getStringArray(R.array.labels);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Labels")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        editorActivity.setLabels();
                    }
                })
                .setMultiChoiceItems(choices, choicesChecked, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        choicesChecked[which] = isChecked;
                    }
                });
        return builder.create();
    }
}
