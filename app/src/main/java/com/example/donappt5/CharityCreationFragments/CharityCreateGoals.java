package com.example.donappt5.CharityCreationFragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.example.donappt5.R;
import com.example.donappt5.helpclasses.Charity;

import androidx.fragment.app.Fragment;

public class CharityCreateGoals extends Fragment {

    final String LOG_TAG = "myLogs";
    EditText goalname;
    EditText goaldesc;


    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(LOG_TAG, "Fragment1 onCreate");
        View v = inflater.inflate(R.layout.fragment_charitygoals, null);
        ProgressBar pb = (ProgressBar)v.findViewById(R.id.pbProgress);
        //TODO get information from the server about charity's goals getArguments()
        goalname = v.findViewById(R.id.etGoalName);
        goaldesc = v.findViewById(R.id.etGoalDesc);

        return v;
    }


    public static CharityCreateGoals newInstance(Charity given) {

        CharityCreateGoals f = new CharityCreateGoals();
        Bundle b = new Bundle();
        //b.putString("msg", text);
        //TODO get information from the server setArguments()
        f.setArguments(b);

        return f;
    }

    public void onStart() {
        super.onStart();
        Log.d(LOG_TAG, "Fragment2 onStart");
    }

    public void onResume() {
        super.onResume();
        Log.d(LOG_TAG, "Fragment2 onResume");
    }

    public void onPause() {
        super.onPause();
        Log.d(LOG_TAG, "Fragment2 onPause");
    }

    public void onStop() {
        super.onStop();
        Log.d(LOG_TAG, "Fragment2 onStop");
    }



    public void onDestroy() {
        super.onDestroy();
        Log.d(LOG_TAG, "Fragment2 onDestroy");
    }

}