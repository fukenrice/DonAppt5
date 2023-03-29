package com.example.donappt5.views.charitycreation.popups;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.donappt5.R;

import androidx.appcompat.app.AppCompatActivity;

public class ActivityConfirm extends AppCompatActivity {
    Context ctx;
    int layoutwidth;
    int layoutheight;
    TextView popupText;
    Button popupCancelButton;
    Button popupConfirmButton;
    public void onCreate(Bundle savedInstanceState) {
        ctx = this;
        Intent intent = getIntent();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.popup_yes_no);

        layoutheight = intent.getIntExtra("height", 100);
        layoutwidth = intent.getIntExtra("width", 100);

        LinearLayout layout = findViewById(R.id.layout_popup);
// Gets the layout params that will allow you to resize the layout
        ViewGroup.LayoutParams params = layout.getLayoutParams();
// Changes the height and width to the specified *pixels*
        params.height = layoutheight;
        params.width = layoutwidth;
        //layout.setLayoutParams(params);
        getWindow().setLayout(layoutwidth, layoutheight);

        popupCancelButton = findViewById(R.id.btnGeneralCancel);
        popupConfirmButton = findViewById(R.id.btnGeneralConfirm);
        popupText = findViewById(R.id.tvGeneralText);

        String btnName = intent.getStringExtra("CancelButtonTitle");
        popupCancelButton.setText(btnName);
        btnName= intent.getStringExtra("ConfirmButtonTitle");
        popupConfirmButton.setText(btnName);

        String givenText = intent.getStringExtra("PopupText");
        popupText.setText(givenText);

        popupCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCancel();
            }
        });
        popupConfirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onConfirm();
            }
        });
    }

    void onConfirm() {
        Intent intent = new Intent();
        intent.putExtra("resultingactivity", "ActivityConfirm");
        intent.putExtra("result", "confirmed");
        setResult(RESULT_OK, intent);
        finish();
    }
    void onCancel() {
        Intent intent = new Intent();
        intent.putExtra("resultingactivity", "ActivityConfirm");
        intent.putExtra("result", "cancelled");
        setResult(RESULT_OK, intent);
        finish();
    }
}
