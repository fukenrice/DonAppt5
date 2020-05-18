package com.example.donappt5.helpclasses;

import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;

import com.example.donappt5.R;

import androidx.appcompat.app.AppCompatActivity;

public class SearchableActivity extends AppCompatActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.search);

        // Get the intent, verify the action and get the query
        Intent intent = getIntent();
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            //doMySearch(query);
        }
    }

}
