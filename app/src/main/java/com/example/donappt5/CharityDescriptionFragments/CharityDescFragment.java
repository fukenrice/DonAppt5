package com.example.donappt5.CharityDescriptionFragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.donappt5.R;
import com.example.donappt5.helpclasses.Charity;

import androidx.fragment.app.Fragment;

public class CharityDescFragment extends Fragment {

    final String LOG_TAG = "myLogs";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_charitydesc, container, false);

        TextView tvDesc = (TextView) v.findViewById(R.id.tvDesc);
        tvDesc.setText(getArguments().getString("fdesc"));
        Log.d(LOG_TAG, "Fragment1 onCreate");

        return v;
    }

    public static CharityDescFragment newInstance(Charity given) {

        CharityDescFragment f = new CharityDescFragment();
        Bundle b = new Bundle();
        b.putString("firestoreID", given.firestoreID);
        b.putString("name", given.name);
        b.putString("bdesc", given.briefDescription);
        b.putString("fdesc", given.fullDescription);
        b.putFloat("trust", given.trust);
        b.putInt("id", given.id);
        b.putInt("img", given.image);

        f.setArguments(b);

        return f;
    }
}