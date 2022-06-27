package com.example.donappt5.paymentsstuff;

import android.os.Bundle;
import android.webkit.WebView;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.donappt5.R;


public class StripeConnectActivity extends AppCompatActivity {
    Button btnConnectStripe;
    WebView wvStipeConnect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        btnConnectStripe = findViewById(R.id.btnStripeConnect);
        wvStipeConnect = findViewById(R.id.wvStripeConnect);

        wvStipeConnect.loadUrl("https://www.example.com");
    }
}