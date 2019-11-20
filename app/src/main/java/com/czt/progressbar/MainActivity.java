package com.czt.progressbar;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private ProgressBar progressBar;
    private Handler handler = new Handler();

    private Button buttonChangeColor;
    private Button buttonChangeTextColor;
    private Button buttonChangeNumberColor;
    private Button buttonChangeTextSize;
    private Button buttonChangeNumberSize;
    private Button buttonChangeStrokeWidth;
    private Button buttonChangeMaxValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        progressBar = findViewById(R.id.progress);

        final MyRunnable runnable = new MyRunnable();

        progressBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (progressBar.getProgressState() == ProgressBar.ProgressState.ON_INITIAL
                || progressBar.getProgressState() == ProgressBar.ProgressState.ON_PAUSE) {
                    progressBar.setProgressState(ProgressBar.ProgressState.ON_PROGRESS);
                    runnable.run();
                } else if (progressBar.getProgressState() == ProgressBar.ProgressState.ON_PROGRESS) {
                    progressBar.setProgressState(ProgressBar.ProgressState.ON_PAUSE);
                    handler.removeCallbacks(runnable);
                }
            }
        });

        buttonChangeColor = findViewById(R.id.btn_change_color);
        buttonChangeColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setProgressBarColor(Color.BLUE);
            }
        });

        buttonChangeNumberColor = findViewById(R.id.btn_change_number_color);
        buttonChangeNumberColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setNumberTextColor(Color.WHITE);
            }
        });

        buttonChangeTextColor = findViewById(R.id.btn_change_text_color);
        buttonChangeTextColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setTextColor(Color.BLUE);
            }
        });

        buttonChangeTextSize = findViewById(R.id.btn_change_text_size);
        buttonChangeTextSize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setTextSize(20);
            }
        });

        buttonChangeNumberSize = findViewById(R.id.btn_change_number_size);
        buttonChangeNumberSize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setNumberTextSize(20);
            }
        });

        buttonChangeStrokeWidth = findViewById(R.id.btn_change_stroke_width);
        buttonChangeStrokeWidth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setProgressStrokeWidth(10);
            }
        });

        buttonChangeMaxValue = findViewById(R.id.btn_change_max_val);
        buttonChangeMaxValue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setMaxValue(150);
            }
        });


    }

    private class MyRunnable implements Runnable {
        public int v = 0;
        @Override
        public void run() {
            progressBar.updateProgress(v);
            v++;
            if (v <= 100) {
                handler.postDelayed(this, 500);
            }
        }
    }

}
