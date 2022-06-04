package com.example.ds;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

public class TopicAdapter extends ArrayAdapter<Topic> {

    private Context context;
    private ArrayList<Topic> topicsList;

    public TopicAdapter(@NonNull Context context, ArrayList<Topic> list) {
        super(context, 0, list);
        this.context = context;
        topicsList = list;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItem = convertView;
        if(listItem == null)
            listItem = LayoutInflater.from(context).inflate(R.layout.topic,parent,false);

        Topic currentTopic = topicsList.get(position);

        View view = listItem.findViewById(R.id.profilePic);
        Drawable bubble = view.getBackground();
        bubble.mutate().setColorFilter(Color.parseColor(currentTopic.getTopicColour()), PorterDuff.Mode.MULTIPLY);

        TextView topicName = listItem.findViewById(R.id.topicName);
        topicName.setText(currentTopic.getTopicName());

        listItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.wakeUserThread();
                Intent chat = new Intent(view.getContext(), ChatActivity.class);
                context.startActivity(chat);
            }
        });

        return listItem;
    }
}
