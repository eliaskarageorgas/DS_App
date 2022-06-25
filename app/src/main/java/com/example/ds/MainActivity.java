package com.example.ds;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

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
        @RequiresApi(api = Build.VERSION_CODES.O)
        public void run() {
            User.main(new String[0]);
        }
    }
}