package com.example.ds;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Random;

public class TopicsActivity extends AppCompatActivity {

    static String topicString;
    private static ArrayList<String> userTopics;
    private ListView listView;
    private TopicAdapter topicAdapter;
    private static Object lock = new Object();
    private static boolean topicsReady = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_topics);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onStart(){
        super.onStart();

        while (!topicsReady) {}
        generateTopics();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void generateTopics() {
        listView = findViewById(R.id.topicsListView);
        ArrayList<Topic> topicList = new ArrayList<>();
        Random rand = new Random();
        for (String topic: userTopics) {
            float r = rand.nextFloat();
            float g = rand.nextFloat();
            float b = rand.nextFloat();
//            int intColor = Color.rgb(r, g, b);
//            String stringColor = String.format("#%06X", (0xFFFFFF & intColor));
            topicList.add(new Topic(topic, "#c615e2"));
        }
        topicAdapter = new TopicAdapter(this, topicList);
        listView.setAdapter(topicAdapter);
    }

    public static String getTopic() {
        return topicString;
    }

    public static Object getLock() {
        return lock;
    }

    public static void wakeUserThread(String topicName) {
        synchronized (lock) {
            Log.d("TopicsActivity", "lock");
            lock.notify();
            topicString = topicName;
        }
    }

    public static void setUserTopics(ArrayList<String> userTopics) {
        TopicsActivity.userTopics = userTopics;
        topicsReady = true;
    }
}
