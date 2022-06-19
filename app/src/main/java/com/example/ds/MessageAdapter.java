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
import java.util.Objects;

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
        Message currentMessage = messagesList.get(position);
        if(listItem == null)
            //Check who sends the message
            if (currentMessage.getSender() == 1)
                listItem = LayoutInflater.from(context).inflate(R.layout.left_messages, parent,false);
            else
                listItem = LayoutInflater.from(context).inflate(R.layout.right_messages, parent,false);

        if (currentMessage.getSender() == 1) {
            View view = listItem.findViewById(R.id.profilePic);
            Drawable bubble = view.getBackground();
            bubble.mutate().setColorFilter(Color.parseColor(currentMessage.getSenderColour()), PorterDuff.Mode.MULTIPLY);

            TextView userId = listItem.findViewById(R.id.userName);
            userId.setText(currentMessage.getSender());
        }

        TextView text = listItem.findViewById(R.id.message_body);
        text.setText(currentMessage.getText());

        return listItem;
    }
}