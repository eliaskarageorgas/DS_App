package com.example.ds;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;


import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class ChatActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
    }

    @Override
    protected void onStart() {
        super.onStart();

        EditText messageBox = (EditText) findViewById(R.id.message_box);

        ImageButton sendButton = findViewById(R.id.send_button);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                User.setSendButton(true);
                String text = messageBox.getText().toString();
                User.setInput(text);
            }
        });
    }

    @Override
    public void onBackPressed() {
        User.setBackButton(true);
    }
}
