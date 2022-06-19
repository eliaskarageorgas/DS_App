package com.example.ds;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private ListView listView;
    private static MyThread t;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_topics);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onStart(){
        super.onStart();
        t = new MyThread();
        t.start();

        Intent topics = new Intent(MainActivity.this, TopicsActivity.class);
        MainActivity.this.startActivity(topics);
    }

    private class MyThread extends Thread {
        public void run() {
            User.main(new String[0]);
        }
    }
}