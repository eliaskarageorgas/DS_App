package com.example.ds;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    static String topicString;
    private static ArrayList<String> userTopics;
    private ListView listView;
    private TopicAdapter topicAdapter;
    private static MyThread t;
    private static Object lock = new Object();


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

        listView = findViewById(R.id.topicsListView);
        ArrayList<Topic> topicList = new ArrayList<>();
        Random rand = new Random();
        for (String topic: userTopics) {
            float r = rand.nextFloat();
            float g = rand.nextFloat();
            float b = rand.nextFloat();
            int intColor = Color.rgb(r, g, b);
            String stringColor = String.format("#%06X", (0xFFFFFF & intColor));
            topicList.add(new Topic(topic, stringColor));
        }
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

    public static Object getLock() {
        return lock;
    }

    public static void wakeUserThread(String topicName) {
        synchronized (lock) {
            lock.notify();
            topicString = topicName;
        }
    }

    public static void setUserTopics(ArrayList<String> userTopics) {
        System.out.println("here");
        MainActivity.userTopics = userTopics;
    }
}