package com.chickendinner.keep;

import android.app.Fragment;
import android.content.Intent;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.chickendinner.keep.recycler.CheckListBean;
import com.chickendinner.keep.recycler.RecyclerListFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ChecklistActivity extends NoteActivity implements View.OnFocusChangeListener  {
    RecyclerListFragment mRecyclerListFragment;
    private String noteId;

    EditText mNoteTitle;
    View mChecklistFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checklist);
        mRecyclerListFragment = (RecyclerListFragment) getFragmentManager().
                findFragmentById(R.id.checklistFragment);


        mAuth = FirebaseAuth.getInstance();
        uid = mAuth.getUid();
        mDatabase = FirebaseDatabase.getInstance();
        mReference = mDatabase.getReference("users").child(uid);

        mNoteTitle = (EditText) findViewById(R.id.textNoteTitle);
        mChecklistFragment = findViewById(R.id.checklistFragment);
        mNoteTitle.setOnFocusChangeListener(this);
        mChecklistFragment.setOnFocusChangeListener(this);

        Intent i = getIntent();
        String loadKey = i.getStringExtra(MainActivity.EXTRA_KEY);
        String loadTitle = i.getStringExtra(MainActivity.EXTRA_TITLE);

        if (!loadKey.equals("")) {
            EditText mNoteTitle = (EditText) findViewById(R.id.textNoteTitle);
            mNoteTitle.setText(loadTitle);
            noteId = loadKey;
            mReference.child(noteId).child("data").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    setSavedData((List<Object>) dataSnapshot.getValue());
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        } else {
            noteId = mNoteIdGenerator.generateNoteId();
            mRecyclerListFragment.addItem();
        }

        mEditTime = (TextView) findViewById(R.id.editTime);
        cal = Calendar.getInstance();
        updateTime();
    }

    protected void setSavedData(List<Object> dataSet) {
        for (Object tp : dataSet) {
            Map<String, Object> m = (Map<String, Object>) tp;
            mRecyclerListFragment.addItem((String)m.get("text"), (boolean)m.get("check"));
        }
    }

    public void save(){
        List<CheckListBean> data = mRecyclerListFragment.getmDataset();
        mReference.child(noteId).child("title").setValue(mNoteTitle.getText().toString());
        mReference.child(noteId).child("data").setValue(data);
        mReference.child(noteId).child("type").setValue("1");
    }

    @Override
    public void onFocusChange(View view, boolean hasFocus) {
        if (!hasFocus) {
            List<CheckListBean> data = mRecyclerListFragment.getmDataset();
            if (view == mNoteTitle) {
                mReference.child(noteId).child("title").setValue(mNoteTitle.getText().toString());
                mReference.child(noteId).child("data").setValue(data);
            }
            mReference.child(noteId).child("type").setValue("1");
        }
    }

    public void onClick(View view){
        clearAllFocus();
        switch (view.getId()){
            case R.id.textNoteTitle:
            case R.id.checklistFragment:
                updateTime();
                break;
            case R.id.backButton:
                //saveDataToDB();
                save();
                finish();
                break;
            case R.id.addItemButton:
                mRecyclerListFragment.addItem();
                break;
            case R.id.trashButton:
                mReference.child(noteId).removeValue();
                finish();
                break;
        }

    }

    private void clearAllFocus(){
        mNoteTitle.clearFocus();
    }
}