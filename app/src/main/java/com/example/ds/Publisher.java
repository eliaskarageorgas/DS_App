package com.example.ds;

import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.io.*;
import java.net.*;

public class Publisher extends Thread {
    private static String text;
    private String brokerIp;
    private int brokerPort;
    private Socket requestSocketPublisher;
    static private ObjectOutputStream outPublisher;
    private ObjectInputStream inPublisher;
    private static int topicCode;
    private static int id;

    @RequiresApi(api = Build.VERSION_CODES.O)
    private static void push() throws IOException {
//        Log.d("Publisher", "Push started successfully");
        outPublisher.writeInt(topicCode); // 2P
        outPublisher.flush();
        byte[] data = text.getBytes(StandardCharsets.UTF_8);
        ArrayList<byte[]> chunks = createChunks(data);
        createInfoChunks(chunks.size());
        for (byte[] chunk: chunks) {
            outPublisher.writeObject(chunk); // 6P
            outPublisher.flush();
        }
    }

    private static void createInfoChunks(int blockCount) throws IOException {
        byte[] blockCountChunk = ByteBuffer.allocate(Integer.BYTES).putInt(blockCount).array();
        outPublisher.writeObject(blockCountChunk); // 4P
        outPublisher.flush();
        byte[] publisherId = ByteBuffer.allocate(Integer.BYTES).putInt(id).array();
        outPublisher.writeObject(publisherId); // 5P
        outPublisher.flush();
    }

    // From the byte array create the chunks to be sent to the broker
    private static ArrayList<byte[]> createChunks(byte[] data) {
        int blockSize = 512 * 1024;
        ArrayList<byte[]> listOfChunks = new ArrayList<>();
        int blockCount = (data.length + blockSize - 1) / blockSize;
        byte[] chunk;
        int start;
        for (int i = 1; i < blockCount; i++) {
            start = (i - 1) * blockSize;
            chunk = Arrays.copyOfRange(data, start, start + blockSize);
            listOfChunks.add(chunk);
        }

        int end;
        if (data.length % blockSize == 0) {
            end = data.length;
        } else {
            end = data.length % blockSize + blockSize * (blockCount - 1);
        }
        chunk = Arrays.copyOfRange(data, (blockCount - 1) * blockSize, end);
        listOfChunks.add(chunk);
        return listOfChunks;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static void messageSend(String text) throws IOException {
        Publisher.text = text;
//        Log.d("Publisher", "Sending message to start actions for publishers.");
        outPublisher.writeBoolean(true); //1P
        outPublisher.flush();
        push();
    }

    Publisher(String ip, int port, int topicCode, Socket requestSocket,
              ObjectOutputStream out, ObjectInputStream in, int id) {
        this.brokerIp = ip;
        this.brokerPort = port;
        Publisher.topicCode = topicCode;
        this.requestSocketPublisher = requestSocket;
        Publisher.outPublisher = out;
        this.inPublisher = in;
        Publisher.id = id;
    }
}