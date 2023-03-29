package com.example.donappt5.views.charitydescription;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.example.donappt5.R;
import com.example.donappt5.data.model.Charity;

import androidx.fragment.app.Fragment;

public class CharityGoalsFragment extends Fragment {

    final String LOG_TAG = "myLogs";



    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(LOG_TAG, "Fragment1 onCreate");
        View v = inflater.inflate(R.layout.fragment_charitygoals, null);
        ProgressBar pb = (ProgressBar)v.findViewById(R.id.pbProgress);
        //TODO get information from the server about charity's goals getArguments()
        return v;
    }


    public static CharityGoalsFragment newInstance(Charity given) {

        CharityGoalsFragment f = new CharityGoalsFragment();
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