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

import com.example.ds.ChatActivity;
import com.example.ds.MainActivity;
import com.example.ds.R;
import com.example.ds.Topic;

import java.util.ArrayList;

public class MessageAdapter extends ArrayAdapter<Message> {

    private Context context;
    private ArrayList<Message> messagesList;

    public MessageAdapter(@NonNull Context context, ArrayList<Message> list) {
        super(context, 0, list);
        this.context = context;
        messagesList = list;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItem = convertView;
        if(listItem == null)
            //Check who sends the message
            listItem = LayoutInflater.from(context).inflate(R.layout.topic,parent,false);

        Message currentMessage = messagesList.get(position);

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