package com.example.ds;

import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;

public class User implements Serializable {
    private static int brokerPort;
    private static String brokerIp;
    private static volatile boolean backButton = false;
    private static volatile boolean sendButtonPressed = false;
    private static String text;
    private String ip;
    private int port;
    private static int id;
    private Socket requestSocketUser;
    private static Socket requestSocketPublisher;
    private Socket requestSocketConsumer;
    private ObjectOutputStream outUser;
    private ObjectInputStream inUser;
    static private ObjectOutputStream outPublisher;
    static private ObjectInputStream inPublisher;
    private ObjectOutputStream outConsumer;
    private ObjectInputStream inConsumer;
    private ArrayList<String> userTopics;
    private static int topicCode;
    private String topicString;
    private boolean firstConnection = true;
    private Thread p;
    private Thread c;
    private Object lock;

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static void main(String[] args) {
        // TODO set IP
        brokerIp = "192.168.68.108";
        brokerPort = 1100;

        // TODO set port, IP, id manually
        int port = 2100;
        String ip = "192.168.68.108";
        int id = 1;
        User u = new User(ip, port, id);
        u.connect();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void connect() {
        try {
            Random rand = new Random();
            int random_int = rand.nextInt(0xffffff + 1);
            String userColor = String.format("#%06X", random_int);
//            Log.d("User", "Connect");
            requestSocketUser = new Socket(brokerIp, brokerPort);
            requestSocketPublisher = new Socket(brokerIp, brokerPort);
            requestSocketConsumer = new Socket(brokerIp, brokerPort);
            outUser = new ObjectOutputStream(requestSocketUser.getOutputStream());
            inUser = new ObjectInputStream(requestSocketUser.getInputStream());
            outPublisher = new ObjectOutputStream(requestSocketPublisher.getOutputStream());
            inPublisher = new ObjectInputStream(requestSocketPublisher.getInputStream());
            outConsumer = new ObjectOutputStream(requestSocketConsumer.getOutputStream());
            inConsumer = new ObjectInputStream(requestSocketConsumer.getInputStream());
//            System.out.println("\033[3mConnected to broker: " + brokerIp + " on port: " + brokerPort + "\033[0m");
            while (true) {
                while (true) {
                    lock = TopicsActivity.getLock();

                    outUser.writeInt(id); // 1U
                    outUser.flush();
                    System.out.println("Id " + id);

                    outUser.writeBoolean(firstConnection); // 2U
                    outUser.flush();
//                    System.out.println("First Connection " + firstConnection);

                    if (firstConnection) {
                        userTopics = (ArrayList<String>) inUser.readObject(); // 3U
                        // Send the topics to the TopicsActivity to show them on the screen
                        TopicsActivity.setUserTopics(userTopics);
//                        Log.d("User", "Topics received successfully");
                    }

                    topicCode = getTopic();
                    outUser.writeObject(topicString); // 4U
                    outUser.flush();
//                    Log.d("User", topicString);

                    outUser.writeInt(topicCode); // 5U
                    outUser.flush();
//                    System.out.println(topicCode);

                    // Get broker object which contains the requested topic
                    String matchedBrokerIp = (String) inUser.readObject(); // 6U
//                    Log.d("User", matchedBrokerIp);
                    int matchedBrokerPort = inUser.readInt(); // 6U
//                    Log.d("User", String.valueOf(matchedBrokerPort));

                    connectToMatchedBroker(matchedBrokerIp, matchedBrokerPort);
                    break;
                }

                c = new Consumer(brokerIp, brokerPort, topicCode, requestSocketConsumer, outConsumer, inConsumer, id, userColor);
                c.start();
                p = new Publisher(brokerIp, brokerPort, topicCode, requestSocketPublisher,
                        outPublisher, inPublisher, id);
                p.start();

//                Log.d("User", "Second while");
                while (true) {
                    boolean newTopic = false;
                    // Check if the user pressed back
                    if (sendButtonPressed) {
                        Publisher.messageSend(text);
                        sendButtonPressed = false;
//                        Log.d("User", "Send button on second while");
                    }

                    if (backButton) {
                        backButton = false;
                        firstConnection = true;
                        newTopic = true;
//                        Log.d("User", "Back button on second while");
                        outUser.writeBoolean(true); // 7U
                        outUser.flush();
                    } else {
                        outUser.writeBoolean(false); // 7U
                        outUser.flush();
                    }

                    if (newTopic) {
//                        Log.d("User", "Going back to main menu");
                        c.interrupt();
                        p.interrupt();
                        outPublisher.writeBoolean(false); //1P
                        outPublisher.flush();
                        break;
                    }
                }
                // If the consumer thread is still alive (waiting for an input in the receiveData function)
                // and we try to close it an error will be produced. In order to avoid the error
                // we check whether the thread is still alive and if it is the appropriate messages are send to it.
                // After the execution of the function the c.interrupted command is executed and the thread is interrupted.
                if (c != null && c.isAlive()) {
                    outUser.writeBoolean(true); // 8U
                    outUser.flush();
                } else {
                    outUser.writeBoolean(false); // 8U
                    outUser.flush();
                }
            }
        } catch (UnknownHostException unknownHost) {
//            System.err.println("\033[3mYou are trying to connect to an unknown host!\033[0m");
        } catch (ClassNotFoundException | IOException | InterruptedException e) {
//            System.out.println("\033[3mAn error occurred while trying to connect to host: " + brokerIp + " on port: " +
//                    brokerPort + ". Check the IP address and the port.\033[0m");
            e.printStackTrace();
        } finally {
            try {
                inUser.close();
                outUser.close();
                inPublisher.close();
                outPublisher.close();
                outConsumer.close();
                inConsumer.close();
                requestSocketPublisher.close();
                requestSocketConsumer.close();
//                System.out.println("Finally " + " \033[3mConnection to broker: " + brokerIp + " on port: " + brokerPort + " closed\033[0m");
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }

    // Create a hash code for the given topic
    private int getTopic() throws InterruptedException {
        synchronized(lock) {
            // Wait till the user clicks on a topic
//            Log.d("User", "lock");
            lock.wait();
        }
        topicString = TopicsActivity.getTopic();
        return topicString.hashCode();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static void SendButton(String text) throws InterruptedException, IOException {
        User.text = text;
        User.sendButtonPressed = true;
//        Log.d("User","Send button pressed");
    }

    public static void setBackButton(boolean backButton) {
        User.backButton = backButton;
//        Log.d("User","Back button pressed");
    }

    public static String getUserId() {
        return Integer.toString(id);
    }

    // Check if the current broker is the correct one
    // Otherwise close the current connection and connect to the right one
    private void connectToMatchedBroker(String matchedBrokerIp, int matchedBrokerPort) throws IOException {
        if (!Objects.equals(brokerIp, matchedBrokerIp) || !Objects.equals(brokerPort, matchedBrokerPort)) {
            inUser.close();
            outUser.close();
            inPublisher.close();
            outPublisher.close();
            outConsumer.close();
            inConsumer.close();
            requestSocketUser.close();
            requestSocketPublisher.close();
            requestSocketConsumer.close();
//            System.out.println("\033[3mConnection to broker: " + brokerIp + " on port: " + brokerPort + " closed\033[0m");
            brokerIp = matchedBrokerIp;
            brokerPort = matchedBrokerPort;
            requestSocketUser = new Socket(brokerIp, brokerPort);
            requestSocketPublisher = new Socket(brokerIp, brokerPort);
            requestSocketConsumer = new Socket(brokerIp, brokerPort);
            outUser = new ObjectOutputStream(requestSocketUser.getOutputStream());
            inUser = new ObjectInputStream(requestSocketUser.getInputStream());
            outPublisher = new ObjectOutputStream(requestSocketPublisher.getOutputStream());
            inPublisher = new ObjectInputStream(requestSocketPublisher.getInputStream());
            outConsumer = new ObjectOutputStream(requestSocketConsumer.getOutputStream());
            inConsumer = new ObjectInputStream(requestSocketConsumer.getInputStream());
//            System.out.println("\033[3mConnected to broker: " + brokerIp + " on port: " + brokerPort + "\033[0m");
            firstConnection = false;
            outUser.writeInt(id); // 1U
            outUser.flush();
            outUser.writeBoolean(firstConnection); // 2U
            outUser.flush();
            outUser.writeInt(topicCode); // 3U
            outUser.flush();
        }
    }

    public User(String ip, int port, int id) {
        this.ip = ip;
        this.port = port;
        User.id = id;
    }
}

// allagi topic 2h fora. crasharei o broker
// Messages appear only after keyboard is closed
// Keyboard doesn't close when a message is send
// Check if the user wants to exit the app
