package com.example.ds;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;


import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.util.ArrayList;

public class ChatActivity extends AppCompatActivity {

    private static ArrayList<Message> messageList = new ArrayList<>();
    private MessageAdapter messageAdapter;
    private ListView listView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
    }

    @Override
    protected void onStart() {
        super.onStart();

        EditText messageBox = findViewById(R.id.message_box);

        // Send button
        ImageButton sendButton = findViewById(R.id.send_button);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
                try {
                    String text = messageBox.getText().toString();
                    User.SendButton(text);
                } catch (InterruptedException | IOException e) {
                    e.printStackTrace();
                }
            }
        });
        listView = findViewById(R.id.messages_view);
        messageAdapter = new MessageAdapter(this, messageList);
        listView.setAdapter(messageAdapter);

        // Gallery button
        ImageButton galleryButton = findViewById(R.id.gallery_button);
        galleryButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
                Log.d("ChatActivity", "GalleryButton");
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_VIEW);
                galleryIntent.setType("image/*");
                galleryIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(galleryIntent);
            }
        });

        // Camera button
        ImageButton cameraButton = findViewById(R.id.camera_button);
        cameraButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
                Log.d("ChatActivity", "GalleryButton");
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivity(cameraIntent);
            }
        });
    }

    @Override
    public void onBackPressed() {
        User.setBackButton(true);
        MessageAdapter.backButtonPressed();
        super.onBackPressed();
    }

    public static void newMessage(Message message) {
        messageList.add(message);
//        Log.d("ChatActivity", message.getSender());
    }
}
