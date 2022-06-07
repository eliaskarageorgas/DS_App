package com.example.ds;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Objects;

public class User implements Serializable {
    private static int brokerPort;
    private static String brokerIp;
    private static boolean backButton = false;
    private static boolean sendButton = false;
    private static String text;
    private String ip;
    private int port;
    private int id;
    private Socket requestSocketUser;
    private Socket requestSocketPublisher;
    private Socket requestSocketConsumer;
    private ObjectOutputStream outUser;
    private ObjectInputStream inUser;
    private ObjectOutputStream outPublisher;
    private ObjectInputStream inPublisher;
    private ObjectOutputStream outConsumer;
    private ObjectInputStream inConsumer;
    private ArrayList<String> userTopics;
    private int topicCode;
    private String topicString;
    private boolean firstConnection = true;
    boolean publisherMode = false;
    private Thread p;
    private Thread c;
    private Object lock;

    public static void main(String[] args) {
        // TODO set IP
        brokerIp = "192.168.68.108";
        brokerPort = 1100;

        // TODO set port, IP, id manually
        int port = 2100;
        String ip = "192.168.68.108";
        int id = 0;
        User u = new User(ip, port, id);
        u.connect();
    }

    private void connect() {
        try {
            requestSocketUser = new Socket(brokerIp, brokerPort);
            requestSocketPublisher = new Socket(brokerIp, brokerPort);
            requestSocketConsumer = new Socket(brokerIp, brokerPort);
            outUser = new ObjectOutputStream(requestSocketUser.getOutputStream());
            inUser = new ObjectInputStream(requestSocketUser.getInputStream());
            outPublisher = new ObjectOutputStream(requestSocketPublisher.getOutputStream());
            inPublisher = new ObjectInputStream(requestSocketPublisher.getInputStream());
            outConsumer = new ObjectOutputStream(requestSocketConsumer.getOutputStream());
            inConsumer = new ObjectInputStream(requestSocketConsumer.getInputStream());
            System.out.println("\033[3mConnected to broker: " + brokerIp + " on port: " + brokerPort + "\033[0m");
            boolean disconnect = false;
            while (!disconnect) {
                while (true) {
                    lock = MainActivity.getLock();

                    outUser.writeInt(id); // 1U
                    outUser.flush();

                    userTopics = (ArrayList<String>) inUser.readObject();
                    MainActivity.setUserTopics(userTopics);

                    outUser.writeBoolean(firstConnection); // 2U
                    outUser.flush();

                    topicCode = getTopic();
                    outUser.writeObject(topicString); // 3U
                    outUser.flush();

                    outUser.writeInt(topicCode); // 4U
                    outUser.flush();

                    boolean registeredUser = inUser.readBoolean(); // 5U

                    if (!registeredUser) {
                        System.out.println("\033[3mYou are unable to access the requested topic." +
                                "(not registered user)\033[0m");
                        continue;
                    }

                    // Get broker object which contains the requested topic
                    String matchedBrokerIp = (String) inUser.readObject(); // 6U
                    int matchedBrokerPort = inUser.readInt();

                    // If the user pressed "Q" when asked to enter the topic disconnect
                    if (topicCode == 81) {
                        if (c != null)
                            c.interrupt();
                        disconnect = true;
                        break;
                    }

                    if (matchedBrokerPort == 0)
                        System.out.println("\033[3mThe topic \"" + topicString + "\" doesn't exist.\033[0m");
                    else {
                        connectToMatchedBroker(matchedBrokerIp, matchedBrokerPort);
                        break;
                    }
                }

                c = new Consumer(brokerIp, brokerPort, topicCode, requestSocketConsumer, outConsumer, inConsumer);
                c.start();

                while (true) {
                    // Check whether the button was pressed
                    if (sendButton) {
                        sendButton = false;
                        // make publisherMode true
                        publisherMode = true;
                    }
                    if (publisherMode) {
                        p = new Publisher(brokerIp, brokerPort, topicCode, requestSocketPublisher,
                                outPublisher, inPublisher, id, text);
                        p.start();
                        p.join();
                        publisherMode = false;
                    }
                    boolean newTopic = false;
                    // Check if the user pressed back
                    if (backButton) {
                        backButton = false;
                        firstConnection = true;
                        newTopic = true;
                        outUser.writeObject(true); // 7U
                        outUser.flush();
                    } else {
                        outUser.writeObject(false); // 7U
                        outUser.flush();
                    }

                    if (newTopic) {
                        c.interrupt();
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
            System.err.println("\033[3mYou are trying to connect to an unknown host!\033[0m");
        } catch (ClassNotFoundException | IOException e) {
            System.out.println("\033[3mAn error occurred while trying to connect to host: " + brokerIp + " on port: " +
                    brokerPort + ". Check the IP address and the port.\033[0m");
            e.printStackTrace();
        } catch (InterruptedException e) {
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
                System.out.println("\033[3mConnection to broker: " + brokerIp + " on port: " + brokerPort + " closed\033[0m");
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }

    // Create a hash code for the given topic
    private int getTopic() throws InterruptedException {
        synchronized (lock) {
            lock.wait();
        }
        topicString = MainActivity.getTopic();
        return topicString.hashCode();
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
            System.out.println("\033[3mConnection to broker: " + brokerIp + " on port: " + brokerPort + " closed\033[0m");
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
            System.out.println("\033[3mConnected to broker: " + brokerIp + " on port: " + brokerPort + "\033[0m");
            firstConnection = false;
            outUser.writeInt(id); // 1U
            outUser.flush();
            outUser.writeBoolean(firstConnection); // 2U
            outUser.flush();
            outUser.writeInt(topicCode); // 3U
            outUser.flush();
        }
    }

    public static void setSendButton(boolean sendButton) {
        User.sendButton = sendButton;
    }

    public static void setBackButton(boolean backButton) {
        User.backButton = backButton;
    }

    public static void setInput(String text) {
        User.text = text;
    }

    public User(String ip, int port, int id) {
        this.ip = ip;
        this.port = port;
        this.id = id;
    }
}


// Check if the user wants to exit the app