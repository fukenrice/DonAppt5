package com.example.donappt5.helpclasses;
import android.app.Activity;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import com.example.donappt5.R;
import com.stripe.android.Stripe;
//import com.stripe.exception.AuthenticationException;
//import com.stripe.exception.CardException;
//import com.stripe.exception.InvalidRequestException;
//import com.stripe.model.Plan;
//import com.stripe.model.PlanCollection;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

//import io.fabric.sdk.android.Fabric;


public class CheckoutActivity extends AppCompatActivity {


    Stripe stripe;
    ArrayList<SimplePlan> planArrayList;
    RecyclerView recyclerView;
    ItemsAdapter adapter;
    Activity thisact;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.teststripe_main);
        stripe = new Stripe(this, "pk_test_GSMF14GK1NPKphtwTYRYl60W0083LGv2jw");

        planArrayList = new ArrayList<>();
        thisact = this;
        new Async().execute();
    }

    public void showRcv(ArrayList<SimplePlan> plans){
        adapter = new ItemsAdapter(thisact, plans);
        recyclerView = (RecyclerView)findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    public class Async extends AsyncTask<Void,String,ArrayList<SimplePlan>> {

        @Override
        protected ArrayList<SimplePlan> doInBackground(Void... params) {

            try {
                String line, newjson = "";
                URL url = new URL("https://donapp-d2378.firebaseapp.com/charge");
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream(), "UTF-8"))) {
                    while ((line = reader.readLine()) != null) {
                        newjson += line;
                        // System.out.println(line);
                    }
                    // System.out.println(newjson);
                    String json = newjson.toString();
                    JSONObject jObj = new JSONObject(json);
                    Log.e("Obj",jObj.toString());
                    JSONArray plans = jObj.getJSONArray("plans");
                    for (int i=0;i<plans.length();i++){
                        JSONObject plan = plans.getJSONObject(i);
                        plan.getString("amount");
                        Log.e("Amount",plan.getString("amount"));
                        planArrayList.add(new SimplePlan(plan.getInt("amount"),plan.getString("name"),plan.getString("statement_descriptor")));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return planArrayList;
        }

        @Override
        protected void onPostExecute(final ArrayList<SimplePlan> plan) {
            super.onPostExecute(plan);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    showRcv(plan);
                }
            },3000);
        }
    }
}