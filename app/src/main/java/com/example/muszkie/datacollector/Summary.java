package com.example.muszkie.datacollector;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class Summary extends AppCompatActivity {

    private final int TEXT_SIZE = 24;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary);
        TextView textView = (TextView) findViewById(R.id.summ);
        textView.setTextSize(TEXT_SIZE);
        textView.setText(MainActivity.getSummaryReport());
    }
}
