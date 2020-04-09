package com.example.donappt5.CharityCreationFragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.example.donappt5.R;
import com.example.donappt5.helpclasses.Charity;

import androidx.fragment.app.Fragment;

public class CharityCreateDesc extends Fragment {

    private String LOG_TAG;
    private EditText etDesc;
    private LayoutInflater inflater;
    private ViewGroup container;
    private Bundle savedInstanceState;
    @Override
    public View onCreateView(LayoutInflater ginflater, ViewGroup gcontainer, Bundle gsavedInstanceState) {
        LOG_TAG = "myLogs";
        inflater = ginflater;
        container = gcontainer;
        savedInstanceState = gsavedInstanceState;
        View v = inflater.inflate(R.layout.fragment_charitydescedit, container, false);

        etDesc = v.findViewById(R.id.etDescriptionEdit);
        Log.d(LOG_TAG, etDesc.getText().toString());
        //etDesc.setText(getArguments().getString("fdesc"));
        Log.d(LOG_TAG, "Fragment1 onCreate");

        return v;
    }

    @Override
    public void onActivityCreated (Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        etDesc = (EditText) getView().findViewById(R.id.etDescriptionEdit);
    }

    public String getText(){
        return etDesc.getText().toString();
    }

    public static CharityCreateDesc newInstance(Charity given) {

        CharityCreateDesc f = new CharityCreateDesc();
        Bundle b = new Bundle();
        /*b.putString("name", given.name);
        b.putString("bdesc", given.briefDescription);
        b.putString("fdesc", given.fullDescription);
        b.putFloat("trust", given.trust);
        b.putInt("id", given.id);
        b.putInt("img", given.image);//*/

        f.setArguments(b);

        return f;
    }
}