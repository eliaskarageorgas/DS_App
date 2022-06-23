package com.example.ds;

import android.graphics.Color;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Random;

public class Consumer extends Thread implements Serializable {
    private String brokerIp;
    private int brokerPort;
    private Socket requestSocketConsumer;
    private ObjectOutputStream outConsumer;
    private ObjectInputStream inConsumer;
    private int topicCode;
    private ArrayList<byte[]> history = new ArrayList<>();
    private int pointerChunk;
    private int currentId;

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void run() {
        try {
            receiveAllData();
            while (!Thread.currentThread().isInterrupted()) {
                receiveData();
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    // Collecting the chunks for a specific file and adding them to the correct ArrayList
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void receiveData() throws IOException, ClassNotFoundException {
        pointerChunk = history.size();
        System.out.println("\033[3mWaiting new message!\033[0m");

        byte[] blockCountChunk = (byte[]) inConsumer.readObject(); // 4C
        if (blockCountChunk != null)
            history.add(blockCountChunk);
        byte[] publisherId = (byte[]) inConsumer.readObject(); // 4C
        if (publisherId != null)
            history.add(publisherId);

        int blockCount = 0;
        // Converting blockCount to integer
        if (blockCountChunk != null) {
            for (byte b : blockCountChunk)
                blockCount += b;
            // Saving chunks in the corresponding ArrayList
            for (int i = 1; i <= blockCount; i++) {
                byte[] chunk = (byte[]) inConsumer.readObject(); // 4C
                history.add(chunk);
            }
            recreateMessage(blockCount);
            System.out.println("\033[3mNew message fetched successfully!\033[0m");
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void recreateMessage(int blockCount) {
        byte[] completeMessage = new byte[512 * 1024 * blockCount];
        int i;
        int j = 0;
        Random rand = new Random();
        pointerChunk++;

        // Converting publisherId to integer
        int publisherId = 0;
        for (byte b : history.get(pointerChunk))
            publisherId += b;
        pointerChunk++;

        for (i = pointerChunk; i < pointerChunk + blockCount; i++) {
            for (byte b: history.get(i)) {
                completeMessage[j] = b;
                j++;
            }
        }
        pointerChunk = i;

        String messageText = new String(completeMessage);

        Message m;
        if (currentId != publisherId) {
            float r = rand.nextFloat();
            float g = rand.nextFloat();
            float b = rand.nextFloat();
            int intColor = Color.rgb(r, g, b);
            String stringColor = String.format("#%06X", (0xFFFFFF & intColor));
            m = new Message(messageText, publisherId, stringColor);
        } else {
            m = new Message(messageText);
        }
        Log.d("Consumer", m.getText());
        ChatActivity.newMessage(m);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void receiveAllData() throws IOException, ClassNotFoundException {
        boolean isEmpty = inConsumer.readBoolean(); // 1C
        if (!isEmpty) {
            System.out.println("\033[3mFetching history!\033[0m");
            int totalChunks = inConsumer.readInt();  // 2C
            for (int i = 1; i <= totalChunks; i++) {
                byte[] chunk = (byte[]) inConsumer.readObject(); // 3C
                history.add(chunk);
            }
            recreateAllData();
            System.out.println("\033[3mHistory fetched successfully!\033[0m");
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void recreateAllData() throws IOException {
        int currentChunk = 0;
        Random rand = new Random();
        while (currentChunk < history.size()) {
            // Converting blockCount to integer
            int blockCount = 0;
            for (byte b : history.get(currentChunk))
                blockCount += b;
            currentChunk++;

            // Converting publisherId to integer
            int publisherId = 0;
            for (byte b : history.get(currentChunk))
                publisherId += b;
            currentChunk++;

            int j = 0;
            byte[] completeMessage = new byte[512 * 1024 * blockCount];
            for (int i = currentChunk; i < currentChunk + blockCount; i++) {
                for (byte b: history.get(i)) {
                    completeMessage[j] = b;
                    j++;
                }
            }
            String messageText = new String(completeMessage);

            Message m;
            if (currentId != publisherId) {
                float r = rand.nextFloat();
                float g = rand.nextFloat();
                float b = rand.nextFloat();
                int intColor = Color.rgb(r, g, b);
                String stringColor = String.format("#%06X", (0xFFFFFF & intColor));
                m = new Message(messageText, publisherId, stringColor);
            } else {
                m = new Message(messageText);
            }
            currentChunk += blockCount;
            ChatActivity.newMessage(m);
        }
    }

    Consumer(String ip, int port, int topicCode, Socket requestSocket, ObjectOutputStream out, ObjectInputStream in, int currentId) {
        this.brokerIp = ip;
        this.brokerPort = port;
        this.topicCode = topicCode;
        this.requestSocketConsumer = requestSocket;
        this.outConsumer = out;
        this.inConsumer = in;
        this.currentId = currentId;
    }
}