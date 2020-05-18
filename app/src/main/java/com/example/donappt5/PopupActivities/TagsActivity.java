package com.example.donappt5.PopupActivities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;

import com.example.donappt5.R;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

public class TagsActivity extends AppCompatActivity {
    CheckBox kidscb;
    CheckBox povcb;
    CheckBox artcb;
    CheckBox srcb;
    CheckBox helcb;
    CheckBox educb;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.popup_checkboxes);

        ConstraintLayout kids = findViewById(R.id.kids);
        ConstraintLayout poverty = findViewById(R.id.poverty);
        ConstraintLayout science_research = findViewById(R.id.science_research);
        ConstraintLayout art = findViewById(R.id.art);
        ConstraintLayout healthcare = findViewById(R.id.healthcare);
        ConstraintLayout education = findViewById(R.id.education);
        kidscb = findViewById(R.id.kidscb);
        povcb = findViewById(R.id.povcb);
        artcb = findViewById(R.id.artcb);
        srcb = findViewById(R.id.rescb);
        helcb = findViewById(R.id.helcb);
        educb = findViewById(R.id.educb);

        kids.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                kidscb.setChecked(!kidscb.isChecked());
            }
        });
        poverty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                povcb.setChecked(!povcb.isChecked());
            }
        });
        art.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                artcb.setChecked(!artcb.isChecked());
            }
        });
        science_research.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                srcb.setChecked(!srcb.isChecked());
            }
        });
        healthcare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                helcb.setChecked(!helcb.isChecked());
            }
        });
        education.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                educb.setChecked(!educb.isChecked());
            }
        });

        Button btnNext = findViewById(R.id.btnSetTags);
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra("kids", kidscb.isChecked());
                intent.putExtra("art", artcb.isChecked());
                intent.putExtra("poverty", povcb.isChecked());
                intent.putExtra("science&research", srcb.isChecked());
                intent.putExtra("healthcare", helcb.isChecked());
                intent.putExtra("education", educb.isChecked());
                intent.putExtra("resultingactivity", "TagsActivity");
                Log.d("returningtags", kidscb.isChecked() + " " + povcb.isChecked() + " "
                        + artcb.isChecked() + " " + srcb.isChecked() + " " + helcb.isChecked() + " " +
                        educb.isChecked());
                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }
}
