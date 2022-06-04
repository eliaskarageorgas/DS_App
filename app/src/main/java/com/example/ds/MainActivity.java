package com.example.ds;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ListView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    static String topicString = "ds";
    private static ArrayList<String> userTopics;

    private ListView listView;
    private TopicAdapter topicAdapter;
    private static MyThread t;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_topics);
    }

    @Override
    protected void onStart(){
        super.onStart();
        t = new MyThread();
        t.start();

        listView = findViewById(R.id.topicsListView);
        ArrayList<Topic> topicList = new ArrayList<>();
        topicList.add(new Topic("ds", "#dc7bff"));
        topicList.add(new Topic("xontros", "#0925b3"));
        topicAdapter = new TopicAdapter(this, topicList);
        listView.setAdapter(topicAdapter);
    }

    private class MyThread extends Thread {
        public void run() {
            User.main(new String[0]);
        }
    }

    public static String getTopic() {
        return topicString;
   }

    public static void wakeUserThread() {
        t.notify();
    }

    public static void setUserT